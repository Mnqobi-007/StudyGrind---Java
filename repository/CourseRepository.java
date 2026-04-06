package com.studygrind.repository;

import com.studygrind.model.Course;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByTeacher(User teacher);
    
    @Query("SELECT c FROM Course c WHERE c.id NOT IN (SELECT ce.course.id FROM CourseEnrollment ce WHERE ce.student.id = :studentId AND ce.isEnrolled = true)")
    List<Course> findAvailableCoursesForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT DISTINCT c FROM Course c JOIN FETCH c.teacher WHERE c.id IN (SELECT ce.course.id FROM CourseEnrollment ce WHERE ce.student.id = :studentId)")
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT c FROM Course c JOIN FETCH c.teacher")
    List<Course> findAllWithTeachers();
    
    @Query("SELECT c FROM Course c JOIN FETCH c.teacher WHERE c.teacher.id = :teacherId")
    List<Course> findCoursesByTeacherId(@Param("teacherId") Long teacherId);
}