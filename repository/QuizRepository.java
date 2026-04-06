package com.studygrind.repository;

import com.studygrind.model.Quiz;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByTeacher(User teacher);
    
    @Query("SELECT q FROM Quiz q WHERE q.id NOT IN (SELECT qa.quiz.id FROM QuizAttempt qa WHERE qa.student.id = :studentId)")
    List<Quiz> findPendingQuizzesForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT q FROM Quiz q WHERE q.teacher.id = :teacherId ORDER BY q.createdAt DESC")
    List<Quiz> findQuizzesByTeacherId(@Param("teacherId") Long teacherId);
    
    @Query("SELECT q FROM Quiz q ORDER BY q.createdAt DESC")
    List<Quiz> findAllQuizzesOrderByDate();
}