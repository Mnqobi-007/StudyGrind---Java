package com.studygrind.util;

import com.studygrind.model.User;
import com.studygrind.model.Course;
import com.studygrind.model.CourseEnrollment;
import java.time.LocalDateTime;

public class TestUtils {

    public static User createTestStudent(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFullName("Test Student " + id);
        user.setRole("student");
        user.setVerificationStatus("approved");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public static User createTestTeacher(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFullName("Test Teacher " + id);
        user.setRole("teacher");
        user.setVerificationStatus("approved");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public static Course createTestCourse(Long id, String name, User teacher) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setDescription("Test Description for " + name);
        course.setTeacher(teacher);
        course.setPrice(29.99);
        course.setCreatedAt(LocalDateTime.now());
        return course;
    }

    public static CourseEnrollment createTestEnrollment(User student, Course course) {
        CourseEnrollment enrollment = new CourseEnrollment(student, course);
        enrollment.startTrial();
        return enrollment;
    }
}