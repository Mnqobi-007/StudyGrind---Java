package com.studygrind.repository;

import com.studygrind.model.Assignment;
import com.studygrind.model.Submission;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByStudent(User student);
    List<Submission> findByAssignment(Assignment assignment);
    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, User student);
    
    @Query("SELECT s FROM Submission s WHERE s.assignment.teacher.id = :teacherId AND s.score IS NULL")
    List<Submission> findPendingSubmissionsForTeacher(@Param("teacherId") Long teacherId);
    
    @Query("SELECT s FROM Submission s WHERE s.assignment.teacher.id = :teacherId AND s.assignment.course.id = :courseId AND s.score IS NULL")
    List<Submission> findPendingSubmissionsForTeacherAndCourse(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);
    
    @Query("SELECT AVG(s.score) FROM Submission s WHERE s.student.id = :studentId AND s.score IS NOT NULL")
    Double getAverageScoreForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT s FROM Submission s WHERE s.student.id = :studentId ORDER BY s.submittedAt DESC")
    List<Submission> findRecentSubmissionsByStudentId(@Param("studentId") Long studentId);
}