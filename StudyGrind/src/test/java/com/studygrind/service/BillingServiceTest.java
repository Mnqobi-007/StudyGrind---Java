package com.studygrind.service;

import com.studygrind.dto.response.BillingSummaryResponse;
import com.studygrind.model.Course;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.model.User;
import com.studygrind.repository.CourseEnrollmentRepository;
import com.studygrind.repository.CoursePaymentRepository;
import com.studygrind.util.PayFastUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @Mock
    private CoursePaymentRepository paymentRepository;

    @Mock
    private UserService userService;

    @Mock
    private PayFastUtil payFastUtil;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private BillingService billingService;

    private User testStudent;
    private Course testCourse;
    private CourseEnrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testStudent = new User();
        testStudent.setId(1L);
        testStudent.setFullName("Test Student");
        testStudent.setEmail("student@test.com");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("Test Course");
        testCourse.setPrice(29.99);

        testEnrollment = new CourseEnrollment(testStudent, testCourse);
        testEnrollment.setId(1L);
        testEnrollment.setTrialUsed(true);
        testEnrollment.setTrialStartDate(LocalDateTime.now().minusDays(10));
        testEnrollment.setTrialEndDate(LocalDateTime.now().minusDays(1)); // expired
        testEnrollment.setOutstandingBalance(29.99);
        testEnrollment.setIsEnrolled(true);
    }

    @Test
    void getBillingSummary_ShouldReturnCorrectSummary() {
        List<CourseEnrollment> enrollments = new ArrayList<>();
        enrollments.add(testEnrollment);
        
        when(userService.findById(1L)).thenReturn(testStudent);
        when(enrollmentRepository.findByStudent(testStudent)).thenReturn(enrollments);

        BillingSummaryResponse result = billingService.getBillingSummary(1L);

        assertNotNull(result);
        assertEquals(29.99, result.getTotalOutstandingBalance());
        assertEquals(0, result.getActiveCourses());
        assertEquals(1, result.getExpiredTrials());
    }

    @Test
    void getBillingSummary_ShouldShowActiveCourse_WhenTrialActive() {
        testEnrollment.setTrialEndDate(LocalDateTime.now().plusDays(5));
        
        List<CourseEnrollment> enrollments = new ArrayList<>();
        enrollments.add(testEnrollment);
        
        when(userService.findById(1L)).thenReturn(testStudent);
        when(enrollmentRepository.findByStudent(testStudent)).thenReturn(enrollments);

        BillingSummaryResponse result = billingService.getBillingSummary(1L);

        assertNotNull(result);
        assertEquals(1, result.getActiveCourses());
        assertEquals(0, result.getExpiredTrials());
    }

    @Test
    void createPaymentForEnrollment_ShouldReturnPaymentData_WhenOutstandingBalance() {
        when(enrollmentRepository.findById(1L)).thenReturn(java.util.Optional.of(testEnrollment));
        when(userService.findById(1L)).thenReturn(testStudent);

        var result = billingService.createPaymentForEnrollment(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.get("enrollmentId"));
        assertEquals(29.99, result.get("amount"));
        assertEquals("Test Course", result.get("courseName"));
    }

    @Test
    void createPaymentForEnrollment_ShouldThrowException_WhenNoOutstandingBalance() {
        testEnrollment.setOutstandingBalance(0.0);
        testEnrollment.setSubscriptionActive(true);
        testEnrollment.setSubscriptionEndDate(LocalDateTime.now().plusDays(30));
        
        when(enrollmentRepository.findById(1L)).thenReturn(java.util.Optional.of(testEnrollment));
        when(userService.findById(1L)).thenReturn(testStudent);

        assertThrows(RuntimeException.class, () -> billingService.createPaymentForEnrollment(1L, 1L));
    }
}