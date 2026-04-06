package com.studygrind.repository;

import com.studygrind.model.Course;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    Optional<CourseEnrollment> findByStudentAndCourse(User student, Course course);
    List<CourseEnrollment> findByStudent(User student);
    List<CourseEnrollment> findByCourse(Course course);
    
    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.student.id = :studentId AND ce.isEnrolled = true")
    List<CourseEnrollment> findActiveEnrollmentsByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.student.id = :studentId AND ce.trialUsed = true AND ce.trialEndDate < CURRENT_TIMESTAMP AND ce.subscriptionActive = false")
    List<CourseEnrollment> findExpiredTrialsNeedingPayment(@Param("studentId") Long studentId);
    
    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.student.id = :studentId AND ce.subscriptionActive = true AND ce.subscriptionEndDate < CURRENT_TIMESTAMP")
    List<CourseEnrollment> findExpiredSubscriptions(@Param("studentId") Long studentId);
    
    @Query("SELECT SUM(ce.outstandingBalance) FROM CourseEnrollment ce WHERE ce.student.id = :studentId AND ce.isEnrolled = true")
    Double getTotalOutstandingBalance(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.course.id = :courseId AND ce.isEnrolled = true")
    long countActiveEnrollmentsByCourse(@Param("courseId") Long courseId);
    
    boolean existsByStudentAndCourseAndTrialUsedTrue(User student, Course course);
}