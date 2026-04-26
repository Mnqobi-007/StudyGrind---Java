package com.studygrind.repository;

import com.studygrind.model.Course;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    // ==================== TRIAL MANAGEMENT QUERIES ====================

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.trialUsed = true AND ce.trialEndDate BETWEEN :startDate AND :endDate AND ce.isEnrolled = true")
    List<CourseEnrollment> findByTrialEndDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.trialUsed = true AND ce.trialEndDate < CURRENT_TIMESTAMP AND ce.isEnrolled = true AND (ce.subscriptionActive = false OR ce.subscriptionActive IS NULL)")
    List<CourseEnrollment> findExpiredTrials();

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.student.id = :studentId AND ce.trialUsed = true AND ce.trialEndDate > CURRENT_TIMESTAMP AND ce.isEnrolled = true")
    List<CourseEnrollment> findActiveTrialsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.trialUsed = true AND ce.trialEndDate BETWEEN :start AND :end AND ce.isEnrolled = true")
    List<CourseEnrollment> findTrialsEndingBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT CASE WHEN COUNT(ce) > 0 THEN true ELSE false END FROM CourseEnrollment ce WHERE ce.student.id = :studentId AND ce.course.id = :courseId AND ce.isEnrolled = true AND (ce.trialEndDate > CURRENT_TIMESTAMP OR ce.subscriptionActive = true)")
    boolean canAccessCourseContent(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT ce FROM CourseEnrollment ce WHERE ce.student.id = :studentId AND ce.isEnrolled = true ORDER BY ce.enrollmentDate DESC")
    List<CourseEnrollment> findRecentEnrollmentsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(ce) FROM CourseEnrollment ce WHERE ce.course.id = :courseId AND ce.isEnrolled = true AND ce.trialUsed = true AND ce.trialEndDate > CURRENT_TIMESTAMP")
    long countActiveTrialsByCourseId(@Param("courseId") Long courseId);
}