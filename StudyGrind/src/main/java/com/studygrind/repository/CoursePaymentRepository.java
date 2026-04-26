package com.studygrind.repository;

import com.studygrind.model.CoursePayment;
import com.studygrind.model.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursePaymentRepository extends JpaRepository<CoursePayment, Long> {
    List<CoursePayment> findByEnrollment(CourseEnrollment enrollment);
    List<CoursePayment> findByEnrollmentOrderByPaymentDateDesc(CourseEnrollment enrollment);
    
    @Query("SELECT cp FROM CoursePayment cp WHERE cp.enrollment.student.id = :studentId ORDER BY cp.paymentDate DESC")
    List<CoursePayment> findByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT SUM(cp.amount) FROM CoursePayment cp WHERE cp.enrollment.course.id = :courseId")
    Double getTotalPaymentsForCourse(@Param("courseId") Long courseId);
}