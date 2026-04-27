package com.studygrind.service;

import com.studygrind.dto.request.CourseRequest;
import com.studygrind.dto.response.CourseResponse;
import com.studygrind.model.Course;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.model.User;
import com.studygrind.repository.CourseEnrollmentRepository;
import com.studygrind.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CourseService courseService;

    private User testTeacher;
    private User testStudent;
    private Course testCourse;
    private CourseRequest courseRequest;

    @BeforeEach
    void setUp() {
        testTeacher = new User();
        testTeacher.setId(1L);
        testTeacher.setFullName("Test Teacher");
        testTeacher.setRole("teacher");

        testStudent = new User();
        testStudent.setId(2L);
        testStudent.setFullName("Test Student");
        testStudent.setRole("student");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("Test Course");
        testCourse.setDescription("Test Description");
        testCourse.setTeacher(testTeacher);
        testCourse.setPrice(29.99);
        testCourse.setCreatedAt(LocalDateTime.now());

        courseRequest = new CourseRequest();
        courseRequest.setName("New Course");
        courseRequest.setDescription("New Description");
        courseRequest.setPrice(49.99);
    }

    @Test
    void createCourse_ShouldCreateCourse_WhenValidRequest() {
        when(userService.findById(1L)).thenReturn(testTeacher);
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);

        CourseResponse result = courseService.createCourse(courseRequest, 1L);

        assertNotNull(result);
        assertEquals("Test Course", result.getName());
    }

    @Test
    void enrollStudent_ShouldCreateEnrollment_WhenValid() {
        when(userService.findById(2L)).thenReturn(testStudent);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.findByStudentAndCourse(testStudent, testCourse)).thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(CourseEnrollment.class))).thenAnswer(i -> i.getArgument(0));

        CourseEnrollment result = courseService.enrollStudent(2L, 1L);

        assertNotNull(result);
        assertTrue(result.getTrialUsed());
        
        verify(notificationService).sendNotification(eq(2L), anyString(), eq("enrollment"));
    }

    @Test
    void enrollStudent_ShouldThrowException_WhenAlreadyEnrolled() {
        when(userService.findById(2L)).thenReturn(testStudent);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.findByStudentAndCourse(testStudent, testCourse))
            .thenReturn(Optional.of(new CourseEnrollment()));

        assertThrows(RuntimeException.class, () -> courseService.enrollStudent(2L, 1L));
    }

    @Test
    void canAccessContent_ShouldReturnTrue_WhenTrialActive() {
        CourseEnrollment enrollment = new CourseEnrollment(testStudent, testCourse);
        enrollment.startTrial();
        
        when(userService.findById(2L)).thenReturn(testStudent);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.findByStudentAndCourse(testStudent, testCourse))
            .thenReturn(Optional.of(enrollment));

        boolean result = courseService.canAccessContent(2L, 1L);

        assertTrue(result);
    }
}