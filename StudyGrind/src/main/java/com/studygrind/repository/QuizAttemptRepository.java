package com.studygrind.repository;

import com.studygrind.model.Quiz;
import com.studygrind.model.QuizAttempt;
import com.studygrind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByStudent(User student);
    List<QuizAttempt> findByQuiz(Quiz quiz);
    Optional<QuizAttempt> findByQuizAndStudent(Quiz quiz, User student);
    
    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.student.id = :studentId")
    Double getAverageScoreForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.student.id = :studentId")
    long countCompletedQuizzesForStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId ORDER BY qa.score DESC")
    List<QuizAttempt> findTopScoresByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.quiz.id = :quizId")
    Double getAverageScoreForQuiz(@Param("quizId") Long quizId);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.student.id = :studentId AND qa.quiz.id = :quizId")
    Optional<QuizAttempt> findByStudentIdAndQuizId(@Param("studentId") Long studentId, @Param("quizId") Long quizId);
    
    boolean existsByStudentAndQuiz(User student, Quiz quiz);
}