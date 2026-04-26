package com.studygrind.repository;

import com.studygrind.model.Textbook;
import com.studygrind.model.Course;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TextbookRepository extends JpaRepository<Textbook, Long> {
    List<Textbook> findByUploadedBy(User teacher);
    List<Textbook> findByCourse(Course course);
    List<Textbook> findByCourseId(Long courseId);

    @Query("SELECT t FROM Textbook t WHERE t.uploadedBy.id = :teacherId AND t.course.id = :courseId")
    List<Textbook> findByTeacherIdAndCourseId(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);
}