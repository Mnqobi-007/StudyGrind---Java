package com.studygrind.repository;

import com.studygrind.model.Note;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByAuthor(User author);
    List<Note> findByCourseId(Long courseId);
    List<Note> findBySubjectContainingIgnoreCase(String subject);
    
    @Query("SELECT n FROM Note n WHERE n.author.id = :teacherId AND n.course.id = :courseId")
    List<Note> findByTeacherIdAndCourseId(@Param("teacherId") Long teacherId, @Param("courseId") Long courseId);
}