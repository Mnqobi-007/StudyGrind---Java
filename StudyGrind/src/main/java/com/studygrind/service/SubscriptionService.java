package com.studygrind.service;

import com.studygrind.dto.response.SubscriptionStatusResponse;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.model.Subscription;
import com.studygrind.model.TrialPeriod;
import com.studygrind.model.User;
import com.studygrind.repository.CourseEnrollmentRepository;
import com.studygrind.repository.SubscriptionRepository;
import com.studygrind.repository.TrialPeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    @Autowired
    private TrialPeriodRepository trialPeriodRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Value("${app.trial.days:14}")
    private int trialDays;

    @Transactional
    public void startTrialForStudent(Long studentId) {
        User student = userService.findById(studentId);

        if (trialPeriodRepository.existsByStudentAndIsActiveTrue(student)) {
            return;
        }

        TrialPeriod trial = new TrialPeriod(student, trialDays);
        trialPeriodRepository.save(trial);
    }

    @Transactional(readOnly = true)  // ADD THIS ANNOTATION
    public SubscriptionStatusResponse getSubscriptionStatus(Long studentId) {
        User student = userService.findById(studentId);
        SubscriptionStatusResponse response = new SubscriptionStatusResponse();

        // First check for active course trials
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(student);
        for (CourseEnrollment enrollment : enrollments) {
            if (enrollment.isTrialActive()) {
                response.setHasActiveTrial(true);
                response.setTrialRemainingDays(enrollment.getTrialDaysRemaining());
                response.setCanAccessContent(true);
                // FIXED: Access course name safely within transaction
                String courseName = enrollment.getCourse() != null ? enrollment.getCourse().getName() : "Course";
                response.setStatusMessage("Trial period: " + enrollment.getTrialDaysRemaining() + " days remaining for " + courseName);
                return response;
            }
        }

        // Check for global trial period (legacy)
        Optional<TrialPeriod> activeTrial = trialPeriodRepository.findByStudentAndIsActiveTrue(student);
        if (activeTrial.isPresent()) {
            TrialPeriod trial = activeTrial.get();
            if (!trial.isExpired()) {
                response.setHasActiveTrial(true);
                response.setTrialRemainingDays(trial.getRemainingDays());
                response.setCanAccessContent(true);
                response.setStatusMessage("Trial period: " + trial.getRemainingDays() + " days remaining");
                return response;
            } else {
                trial.setIsActive(false);
                trialPeriodRepository.save(trial);
            }
        }

        // Check for active subscription
        Optional<Subscription> activeSubscription = subscriptionRepository.findTopByStudentAndStatusOrderByEndDateDesc(student, "active");
        if (activeSubscription.isPresent()) {
            Subscription subscription = activeSubscription.get();
            if (!subscription.isExpired()) {
                response.setHasActiveSubscription(true);
                response.setSubscriptionRemainingDays(subscription.getRemainingDays());
                response.setCanAccessContent(true);
                response.setStatusMessage("Subscription active: " + subscription.getRemainingDays() + " days remaining");
                return response;
            } else {
                subscription.setStatus("expired");
                subscriptionRepository.save(subscription);
            }
        }

        response.setCanAccessContent(false);
        response.setStatusMessage("Your trial has expired. Please subscribe to continue accessing content.");
        return response;
    }

    @Transactional(readOnly = true)
    public boolean canAccessContent(Long studentId) {
        User student = userService.findById(studentId);

        // First check if user has ANY active trial in any course
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(student);
        for (CourseEnrollment enrollment : enrollments) {
            if (enrollment.isTrialActive()) {
                return true;  // Has active trial somewhere
            }
        }

        // Check for global trial period (legacy)
        Optional<TrialPeriod> activeTrial = trialPeriodRepository.findByStudentAndIsActiveTrue(student);
        if (activeTrial.isPresent() && !activeTrial.get().isExpired()) {
            return true;
        }

        // Then check regular subscription
        Optional<Subscription> activeSubscription = subscriptionRepository.findTopByStudentAndStatusOrderByEndDateDesc(student, "active");
        if (activeSubscription.isPresent() && !activeSubscription.get().isExpired()) {
            return true;
        }

        return false;
    }

    @Transactional(readOnly = true)
    public boolean canAccessCourseContent(Long studentId, Long courseId) {
        User student = userService.findById(studentId);

        // Check specific course enrollment for trial or subscription
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(student);
        for (CourseEnrollment enrollment : enrollments) {
            if (enrollment.getCourse().getId().equals(courseId)) {
                if (enrollment.isTrialActive()) {
                    return true;
                }
                if (enrollment.getSubscriptionActive() != null && enrollment.getSubscriptionActive() &&
                        enrollment.getSubscriptionEndDate() != null && enrollment.getSubscriptionEndDate().isAfter(LocalDateTime.now())) {
                    return true;
                }
            }
        }

        // Fall back to global access check
        return canAccessContent(studentId);
    }
}