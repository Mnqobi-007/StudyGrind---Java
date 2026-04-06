package com.studygrind.repository;

import com.studygrind.model.Assignment;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByTeacher(User teacher);
    List<Assignment> findByCourseId(Long courseId);
    List<Assignment> findByDueDateBefore(LocalDateTime date);
    List<Assignment> findByDueDateAfter(LocalDateTime date);
    
    @Query("SELECT a FROM Assignment a WHERE a.id NOT IN (SELECT s.assignment.id FROM Submission s WHERE s.student.id = :studentId)")
    List<Assignment> findPendingAssignmentsForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT a FROM Assignment a WHERE a.teacher.id = :teacherId AND a.course.id = :courseId")
    List<Assignment> findByTeacherIdAndCourseId(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);
}