package com.studygrind.service;

import com.studygrind.model.CourseEnrollment;
import com.studygrind.repository.CourseEnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrialSchedulerService {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    // Run every hour to check for trials ending soon
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkTrialsEndingSoon() {
        System.out.println("=== Running trial expiration check ===");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysFromNow = now.plusDays(2);

        // Find trials ending in the next 2 days
        List<CourseEnrollment> trialsEndingSoon = enrollmentRepository.findByTrialEndDateBetween(now, twoDaysFromNow);

        for (CourseEnrollment enrollment : trialsEndingSoon) {
            long daysRemaining = enrollment.getTrialDaysRemaining();

            // Send notification for 2-day warning
            if (daysRemaining == 2) {
                notificationService.sendNotification(enrollment.getStudent().getId(),
                        "⚠️ Your free trial for " + enrollment.getCourse().getName() + " ends in 2 days! Subscribe now to continue access.",
                        "trial_warning");

                emailService.sendTrialExpiringEmail(
                        enrollment.getStudent().getEmail(),
                        enrollment.getStudent().getFullName(),
                        enrollment.getCourse().getName(),
                        daysRemaining
                );
                System.out.println("Sent 2-day trial warning for student " + enrollment.getStudent().getId() +
                        " for course " + enrollment.getCourse().getId());
            }

            // Send notification for 1-day warning
            if (daysRemaining == 1) {
                notificationService.sendNotification(enrollment.getStudent().getId(),
                        "⚠️ Your free trial for " + enrollment.getCourse().getName() + " ends TOMORROW! Subscribe now to avoid interruption.",
                        "trial_warning");

                emailService.sendTrialExpiringEmail(
                        enrollment.getStudent().getEmail(),
                        enrollment.getStudent().getFullName(),
                        enrollment.getCourse().getName(),
                        daysRemaining
                );
                System.out.println("Sent 1-day trial warning for student " + enrollment.getStudent().getId() +
                        " for course " + enrollment.getCourse().getId());
            }
        }

        // Check for expired trials
        courseService.checkAndUpdateExpiredTrials();

        System.out.println("=== Trial check completed ===");
    }

    // Run every day at 9 AM to send trial reminders (3-day notice)
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendTrialReminders() {
        System.out.println("=== Sending 3-day trial reminders ===");

        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        LocalDateTime fourDaysFromNow = LocalDateTime.now().plusDays(4);

        List<CourseEnrollment> trialsEndingIn3Days = enrollmentRepository.findByTrialEndDateBetween(threeDaysFromNow, fourDaysFromNow);

        for (CourseEnrollment enrollment : trialsEndingIn3Days) {
            long daysRemaining = enrollment.getTrialDaysRemaining();
            if (daysRemaining == 3) {
                notificationService.sendNotification(enrollment.getStudent().getId(),
                        "📚 Reminder: Your free trial for " + enrollment.getCourse().getName() + " ends in 3 days! Subscribe to continue access.",
                        "trial_reminder");

                emailService.sendTrialExpiringEmail(
                        enrollment.getStudent().getEmail(),
                        enrollment.getStudent().getFullName(),
                        enrollment.getCourse().getName(),
                        daysRemaining
                );
                System.out.println("Sent 3-day trial reminder for student " + enrollment.getStudent().getId());
            }
        }

        System.out.println("=== Trial reminders completed ===");
    }
}