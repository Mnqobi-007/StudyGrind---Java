package com.studygrind.repository;

import com.studygrind.model.QuizAnswer;
import com.studygrind.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findByAttempt(QuizAttempt attempt);
    
    @Query("SELECT qa FROM QuizAnswer qa WHERE qa.attempt.id = :attemptId")
    List<QuizAnswer> findAnswersByAttemptId(@Param("attemptId") Long attemptId);
    
    @Query("SELECT COUNT(qa) FROM QuizAnswer qa WHERE qa.attempt.id = :attemptId AND qa.isCorrect = true")
    long countCorrectAnswersByAttemptId(@Param("attemptId") Long attemptId);
}