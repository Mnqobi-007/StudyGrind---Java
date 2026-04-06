package com.studygrind.repository;

import com.studygrind.model.StudentCourse;
import com.studygrind.model.User;
import com.studygrind.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * @deprecated Use CourseEnrollmentRepository instead. This repository is kept for backward compatibility.
 */
@Deprecated
@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {
    List<StudentCourse> findByStudent(User student);
    List<StudentCourse> findByCourse(Course course);
    Optional<StudentCourse> findByStudentAndCourse(User student, Course course);
    boolean existsByStudentAndCourse(User student, Course course);
    long countByCourse(Course course);
}