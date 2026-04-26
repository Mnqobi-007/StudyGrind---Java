package com.studygrind.service;

import com.studygrind.dto.request.CourseRequest;
import com.studygrind.dto.response.CourseDetailResponse;
import com.studygrind.dto.response.CourseResponse;
import com.studygrind.dto.response.EnrollmentResponse;
import com.studygrind.model.Course;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.model.User;
import com.studygrind.repository.CourseEnrollmentRepository;
import com.studygrind.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Transactional
    public CourseResponse createCourse(CourseRequest request, Long teacherId) {
        User teacher = userService.findById(teacherId);
        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setTeacher(teacher);
        course.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now());
        course.setEndDate(request.getEndDate() != null ? request.getEndDate() : LocalDateTime.now().plusMonths(3));
        course.setPrice(request.getPrice() != null ? request.getPrice() : 29.99);
        course.setCreatedAt(LocalDateTime.now());
        return convertToResponse(courseRepository.save(course), false);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
        enrollmentRepository.deleteAll(enrollments);
        courseRepository.delete(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAllWithTeachers().stream()
                .map(c -> convertToResponse(c, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public CourseEnrollment enrollStudent(Long studentId, Long courseId) {
        User student = userService.findById(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if already enrolled
        if (enrollmentRepository.findByStudentAndCourse(student, course).isPresent()) {
            throw new RuntimeException("Already enrolled in this course");
        }

        // Check if teacher trying to enroll in own course
        if (course.getTeacher().getId().equals(studentId)) {
            throw new RuntimeException("Teachers cannot enroll in their own courses");
        }

        // Check if student has already used trial for this course
        if (hasUsedTrial(studentId, courseId)) {
            throw new RuntimeException("You have already used your free trial for this course");
        }

        CourseEnrollment enrollment = new CourseEnrollment(student, course);
        enrollment.startTrial();

        CourseEnrollment saved = enrollmentRepository.save(enrollment);

        // Send notification about trial start
        notificationService.sendNotification(studentId,
                "You have been enrolled in " + course.getName() + "! Your 14-day free trial has started.",
                "enrollment");

        // Send email about trial start
        emailService.sendCourseEnrollmentEmail(student.getEmail(), student.getFullName(), course.getName(), 14);

        return saved;
    }

    @Transactional
    public void unenrollStudent(Long studentId, Long courseId, String reason) {
        User student = userService.findById(studentId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        CourseEnrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new RuntimeException("Not enrolled in this course"));

        double balance = enrollment.getOutstandingBalance() != null ? enrollment.getOutstandingBalance() : 0.0;

        if (enrollment.isTrialActive()) {
            enrollment.unenroll(reason);
            notificationService.sendNotification(studentId,
                    "You have been unenrolled from " + course.getName() + " during your trial period.",
                    "unenrollment");
        } else if (Boolean.TRUE.equals(enrollment.getSubscriptionActive()) &&
                enrollment.getSubscriptionEndDate() != null &&
                enrollment.getSubscriptionEndDate().isAfter(LocalDateTime.now())) {
            enrollment.unenroll(reason);
            notificationService.sendNotification(studentId,
                    "You have been unenrolled from " + course.getName() + ". Your subscription has been cancelled.",
                    "unenrollment");
        } else if (balance > 0) {
            throw new RuntimeException("Cannot unenroll: You have an outstanding balance of $" + balance);
        } else {
            enrollment.unenroll(reason);
        }

        enrollmentRepository.save(enrollment);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesForStudent(Long studentId) {
        return courseRepository.findCoursesByStudentId(studentId).stream()
                .map(c -> convertToResponse(c, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAvailableCoursesForStudent(Long studentId) {
        // Only show courses that the student is NOT enrolled in
        return courseRepository.findAvailableCoursesForStudent(studentId).stream()
                .map(c -> convertToResponse(c, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesForTeacher(Long teacherId) {
        return courseRepository.findCoursesByTeacherId(teacherId).stream()
                .map(c -> convertToResponse(c, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseForTeacher(Long teacherId) {
        List<Course> courses = courseRepository.findByTeacher(userService.findById(teacherId));
        if (courses.isEmpty()) return null;
        return convertToResponse(courses.get(0), false);
    }

    @Transactional(readOnly = true)
    public boolean canAccessContent(Long studentId, Long courseId) {
        User student = userService.findById(studentId);
        return courseRepository.findById(courseId)
                .flatMap(c -> enrollmentRepository.findByStudentAndCourse(student, c))
                .map(enrollment -> {
                    boolean canAccess = enrollment.canAccessContent();
                    System.out.println("Access check - Student: " + studentId +
                            ", Course: " + courseId +
                            ", CanAccess: " + canAccess +
                            ", TrialActive: " + enrollment.isTrialActive() +
                            ", SubActive: " + enrollment.getSubscriptionActive());
                    return canAccess;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean hasUsedTrial(Long studentId, Long courseId) {
        User student = userService.findById(studentId);
        return courseRepository.findById(courseId)
                .flatMap(c -> enrollmentRepository.findByStudentAndCourse(student, c))
                .map(CourseEnrollment::getTrialUsed)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetails(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        boolean isEnrolled = false;
        boolean canAccess = false;

        if (studentId != null) {
            User student = userService.findById(studentId);
            var enrollment = enrollmentRepository.findByStudentAndCourse(student, course);
            isEnrolled = enrollment.isPresent();
            canAccess = enrollment.map(CourseEnrollment::canAccessContent).orElse(false);
        }

        CourseDetailResponse response = new CourseDetailResponse();
        response.setCourse(convertToResponse(course, isEnrolled));
        response.setTotalEnrollments(course.getEnrollments() != null ? course.getEnrollments().size() : 0);
        response.setCanAccessContent(canAccess);

        return response;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrolledStudents(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourse(course);
        List<EnrollmentResponse> responses = new ArrayList<>();

        for (CourseEnrollment enrollment : enrollments) {
            if (Boolean.TRUE.equals(enrollment.getIsEnrolled())) {
                EnrollmentResponse response = new EnrollmentResponse();
                response.setStudentId(enrollment.getStudent().getId());
                response.setStudentName(enrollment.getStudent().getFullName());
                response.setStudentEmail(enrollment.getStudent().getEmail());
                response.setEnrolledAt(enrollment.getEnrollmentDate());
                response.setTrialActive(enrollment.isTrialActive());
                response.setSubscriptionActive(enrollment.getSubscriptionActive() != null && enrollment.getSubscriptionActive());
                responses.add(response);
            }
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<CourseEnrollment> getEnrollmentsNeedingTrialReminder() {
        LocalDateTime twoDaysFromNow = LocalDateTime.now().plusDays(2);
        LocalDateTime oneDayFromNow = LocalDateTime.now().plusDays(1);
        LocalDateTime today = LocalDateTime.now();

        return enrollmentRepository.findByTrialEndDateBetween(today, twoDaysFromNow);
    }

    @Transactional
    public void checkAndUpdateExpiredTrials() {
        List<CourseEnrollment> expiredTrials = enrollmentRepository.findExpiredTrials();

        for (CourseEnrollment enrollment : expiredTrials) {
            if (enrollment.isTrialExpired() && !enrollment.hasActiveSubscription()) {
                // Send notification that trial expired
                notificationService.sendNotification(enrollment.getStudent().getId(),
                        "Your free trial for " + enrollment.getCourse().getName() + " has expired. Please subscribe to continue accessing content.",
                        "trial_expired");

                // Send email
                emailService.sendTrialExpiredEmail(
                        enrollment.getStudent().getEmail(),
                        enrollment.getStudent().getFullName(),
                        enrollment.getCourse().getName(),
                        enrollment.getCourse().getPrice()
                );

                System.out.println("Trial expired for student " + enrollment.getStudent().getId() +
                        " in course " + enrollment.getCourse().getId());
            }
        }
    }

    private CourseResponse convertToResponse(Course course, boolean isEnrolled) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setName(course.getName());
        response.setDescription(course.getDescription());
        response.setTeacherName(course.getTeacher() != null ? course.getTeacher().getFullName() : "Unknown");
        response.setTeacherId(course.getTeacher() != null ? course.getTeacher().getId() : null);
        response.setStartDate(course.getStartDate());
        response.setEndDate(course.getEndDate());
        response.setEnrolledCount(course.getEnrollments() != null ? course.getEnrollments().size() : 0);
        response.setCreatedAt(course.getCreatedAt());
        response.setIsEnrolled(isEnrolled);
        response.setPrice(course.getPrice());
        return response;
    }
}