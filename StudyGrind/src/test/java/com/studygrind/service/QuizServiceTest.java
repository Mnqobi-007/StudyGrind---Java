package com.studygrind.service;

import com.studygrind.dto.request.QuestionRequest;
import com.studygrind.dto.request.QuizRequest;
import com.studygrind.dto.request.QuizSubmitRequest;
import com.studygrind.model.Question;
import com.studygrind.model.Quiz;
import com.studygrind.model.User;
import com.studygrind.repository.QuestionRepository;
import com.studygrind.repository.QuizAttemptRepository;
import com.studygrind.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private QuizAnswerRepository quizAnswerRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private QuizService quizService;

    private User testTeacher;
    private Quiz testQuiz;
    private Question testQuestion;
    private QuizRequest quizRequest;

    @BeforeEach
    void setUp() {
        testTeacher = new User();
        testTeacher.setId(1L);
        testTeacher.setFullName("Test Teacher");

        testQuiz = new Quiz();
        testQuiz.setId(1L);
        testQuiz.setTitle("Test Quiz");
        testQuiz.setDescription("Test Description");
        testQuiz.setSubject("Testing");
        testQuiz.setTeacher(testTeacher);

        testQuestion = new Question();
        testQuestion.setId(1L);
        testQuestion.setText("What is 2+2?");
        testQuestion.setOptionA("3");
        testQuestion.setOptionB("4");
        testQuestion.setOptionC("5");
        testQuestion.setOptionD("6");
        testQuestion.setCorrectAnswer("B");
        testQuestion.setPoints(1);
        testQuestion.setQuiz(testQuiz);

        quizRequest = new QuizRequest();
        quizRequest.setTitle("New Quiz");
        quizRequest.setDescription("New Description");
        quizRequest.setSubject("Mathematics");
        quizRequest.setTimeLimit(30);
    }

    @Test
    void createQuiz_ShouldCreateQuiz_WhenValidRequest() {
        when(userService.findById(1L)).thenReturn(testTeacher);
        when(quizRepository.save(any(Quiz.class))).thenReturn(testQuiz);

        Quiz result = quizService.createQuiz(quizRequest, 1L);

        assertNotNull(result);
        assertEquals("Test Quiz", result.getTitle());
        assertEquals("Testing", result.getSubject());
    }

    @Test
    void addQuestion_ShouldAddQuestion_WhenValid() {
        when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);

        QuestionRequest request = new QuestionRequest();
        request.setText("What is 2+2?");
        request.setOptionA("3");
        request.setOptionB("4");
        request.setOptionC("5");
        request.setOptionD("6");
        request.setCorrectAnswer("B");
        request.setPoints(1);

        Question result = quizService.addQuestion(1L, request);

        assertNotNull(result);
        assertEquals("What is 2+2?", result.getText());
        assertEquals("B", result.getCorrectAnswer());
    }

    @Test
    void submitQuiz_ShouldCalculateScoreCorrectly() {
        List<Question> questions = new ArrayList<>();
        questions.add(testQuestion);
        
        QuizSubmitRequest submitRequest = new QuizSubmitRequest();
        Map<String, String> answers = new HashMap<>();
        answers.put("1", "B"); // Correct answer
        submitRequest.setAnswers(answers);
        
        when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));
        when(userService.findById(1L)).thenReturn(testTeacher);
        when(quizAttemptRepository.findByQuizAndStudent(testQuiz, testTeacher)).thenReturn(Optional.empty());
        when(questionRepository.findQuestionsByQuizId(1L)).thenReturn(questions);
        when(quizAttemptRepository.save(any())).thenReturn(null);
        when(quizAnswerRepository.save(any())).thenReturn(null);

        Double score = quizService.submitQuiz(1L, 1L, submitRequest);

        assertEquals(100.0, score);
    }

    @Test
    void submitQuiz_ShouldCalculateZeroScore_WhenAllWrong() {
        List<Question> questions = new ArrayList<>();
        questions.add(testQuestion);
        
        QuizSubmitRequest submitRequest = new QuizSubmitRequest();
        Map<String, String> answers = new HashMap<>();
        answers.put("1", "A"); // Wrong answer
        submitRequest.setAnswers(answers);
        
        when(quizRepository.findById(1L)).thenReturn(Optional.of(testQuiz));
        when(userService.findById(1L)).thenReturn(testTeacher);
        when(quizAttemptRepository.findByQuizAndStudent(testQuiz, testTeacher)).thenReturn(Optional.empty());
        when(questionRepository.findQuestionsByQuizId(1L)).thenReturn(questions);
        when(quizAttemptRepository.save(any())).thenReturn(null);
        when(quizAnswerRepository.save(any())).thenReturn(null);

        Double score = quizService.submitQuiz(1L, 1L, submitRequest);

        assertEquals(0.0, score);
    }
}