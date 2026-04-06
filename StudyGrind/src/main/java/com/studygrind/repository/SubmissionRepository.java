package com.studygrind.repository;

import com.studygrind.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByStudentId(Long studentId);

    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    @Query("SELECT s FROM Submission s JOIN s.assignment a WHERE a.teacherId = :teacherId")
    List<Submission> findByTeacherId(@Param("teacherId") Long teacherId);
}
