package com.studygrind.repository;

import com.studygrind.model.Question;
import com.studygrind.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuiz(Quiz quiz);
    long countByQuiz(Quiz quiz);
    
    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId ORDER BY q.id ASC")
    List<Question> findQuestionsByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT SUM(q.points) FROM Question q WHERE q.quiz.id = :quizId")
    Integer getTotalPointsByQuizId(@Param("quizId") Long quizId);
}