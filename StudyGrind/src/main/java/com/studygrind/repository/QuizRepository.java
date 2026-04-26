package com.studygrind.repository;

import com.studygrind.model.Quiz;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByTeacher(User teacher);
    
    @Query("SELECT q FROM Quiz q WHERE q.id NOT IN (SELECT qa.quiz.id FROM QuizAttempt qa WHERE qa.student.id = :studentId)")
    List<Quiz> findPendingQuizzesForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT q FROM Quiz q WHERE q.teacher.id = :teacherId ORDER BY q.createdAt DESC")
    List<Quiz> findQuizzesByTeacherId(@Param("teacherId") Long teacherId);
    
    @Query("SELECT q FROM Quiz q ORDER BY q.createdAt DESC")
    List<Quiz> findAllQuizzesOrderByDate();
    
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.questions WHERE q.id = :quizId")
    Optional<Quiz> findByIdWithQuestions(@Param("quizId") Long quizId);
}