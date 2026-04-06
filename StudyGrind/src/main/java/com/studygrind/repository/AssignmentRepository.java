package com.studygrind.repository;

import com.studygrind.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByTeacherId(Long teacherId);

    @Query("SELECT a FROM Assignment a WHERE NOT EXISTS (SELECT s FROM Submission s WHERE s.assignment.id = a.id AND s.student.id = :studentId)")
    List<Assignment> findPendingByStudentId(@Param("studentId") Long studentId);
}
