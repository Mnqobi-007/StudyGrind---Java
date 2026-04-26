package com.studygrind.service;

import com.studygrind.dto.request.QuestionRequest;
import com.studygrind.dto.request.QuizRequest;
import com.studygrind.dto.request.QuizSubmitRequest;
import com.studygrind.dto.response.QuestionResponse;
import com.studygrind.dto.response.QuizResponse;
import com.studygrind.model.*;
import com.studygrind.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Quiz createQuiz(QuizRequest request, Long teacherId) {
        User teacher = userService.findById(teacherId);

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setSubject(request.getSubject());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setTeacher(teacher);
        quiz.setCreatedAt(LocalDateTime.now());

        return quizRepository.save(quiz);
    }

    @Transactional
    public Question addQuestion(Long quizId, QuestionRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        Question question = new Question();
        question.setQuiz(quiz);
        question.setText(request.getText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setPoints(request.getPoints());

        return questionRepository.save(question);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesForStudent(Long studentId) {
        User student = userService.findById(studentId);
        // FIXED: Use a query that fetches questions count without loading the collection
        List<Quiz> quizzes = quizRepository.findAll();
        List<QuizResponse> responses = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            responses.add(convertToQuizResponse(quiz, student));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesForTeacher(Long teacherId) {
        User teacher = userService.findById(teacherId);
        // FIXED: Use a query that fetches questions count without loading the collection
        List<Quiz> quizzes = quizRepository.findByTeacher(teacher);
        List<QuizResponse> responses = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            responses.add(convertToQuizResponse(quiz, null));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizForTaking(Long quizId, Long studentId) {
        // FIXED: Use join fetch to load questions within the transaction
        Quiz quiz = quizRepository.findByIdWithQuestions(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));

        User student = userService.findById(studentId);
        Optional<QuizAttempt> existingAttempt = quizAttemptRepository.findByQuizAndStudent(quiz, student);
        if (existingAttempt.isPresent()) {
            throw new RuntimeException("You have already attempted this quiz");
        }

        QuizResponse response = new QuizResponse();
        response.setId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setDescription(quiz.getDescription());
        response.setTimeLimit(quiz.getTimeLimit());
        response.setSubject(quiz.getSubject());
        // FIXED: Get question count safely
        response.setQuestionCount(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0);

        List<QuestionResponse> questionResponses = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            for (Question question : quiz.getQuestions()) {
                QuestionResponse qr = new QuestionResponse();
                qr.setId(question.getId());
                qr.setText(question.getText());

                List<String> options = new ArrayList<>();
                if (question.getOptionA() != null && !question.getOptionA().isEmpty()) options.add(question.getOptionA());
                if (question.getOptionB() != null && !question.getOptionB().isEmpty()) options.add(question.getOptionB());
                if (question.getOptionC() != null && !question.getOptionC().isEmpty()) options.add(question.getOptionC());
                if (question.getOptionD() != null && !question.getOptionD().isEmpty()) options.add(question.getOptionD());
                qr.setOptions(options);

                questionResponses.add(qr);
            }
        }
        response.setQuestions(questionResponses);

        return response;
    }

    @Transactional
    public Double submitQuiz(Long quizId, Long studentId, QuizSubmitRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));
        User student = userService.findById(studentId);

        Optional<QuizAttempt> existingAttempt = quizAttemptRepository.findByQuizAndStudent(quiz, student);
        if (existingAttempt.isPresent()) {
            throw new RuntimeException("You have already attempted this quiz");
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt = quizAttemptRepository.save(attempt);

        Map<String, String> answers = request.getAnswers();
        int totalPoints = 0;
        int earnedPoints = 0;

        // FIXED: Load questions within transaction
        List<Question> questions = questionRepository.findQuestionsByQuizId(quizId);
        for (Question question : questions) {
            String selected = answers.get(String.valueOf(question.getId()));
            boolean isCorrect = selected != null && selected.equals(question.getCorrectAnswer());

            QuizAnswer answer = new QuizAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedAnswer(selected != null ? selected : "");
            answer.setIsCorrect(isCorrect);
            quizAnswerRepository.save(answer);

            totalPoints += question.getPoints();
            if (isCorrect) {
                earnedPoints += question.getPoints();
            }
        }

        double score = totalPoints > 0 ? (earnedPoints * 100.0 / totalPoints) : 0;
        attempt.setScore(score);
        quizAttemptRepository.save(attempt);

        return score;
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuizResults(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        User student = userService.findById(studentId);

        QuizAttempt attempt = quizAttemptRepository.findByQuizAndStudent(quiz, student)
                .orElseThrow(() -> new RuntimeException("You haven't attempted this quiz yet"));

        QuizResponse response = new QuizResponse();
        response.setId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setDescription(quiz.getDescription());
        response.setSubject(quiz.getSubject());
        response.setAttempted(true);
        response.setScore(attempt.getScore());

        return response;
    }

    // FIXED: Convert without accessing lazy collections
    private QuizResponse convertToQuizResponse(Quiz quiz, User student) {
        boolean attempted = false;
        Double score = null;
        if (student != null) {
            Optional<QuizAttempt> attempt = quizAttemptRepository.findByQuizAndStudent(quiz, student);
            attempted = attempt.isPresent();
            if (attempted) {
                score = attempt.get().getScore();
            }
        }

        // FIXED: Get question count from repository instead of lazy collection
        long questionCount = questionRepository.countByQuiz(quiz);

        QuizResponse response = new QuizResponse();
        response.setId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setDescription(quiz.getDescription());
        response.setSubject(quiz.getSubject());
        response.setTimeLimit(quiz.getTimeLimit());
        response.setQuestionCount((int) questionCount);
        response.setAttempted(attempted);
        response.setScore(score);

        return response;
    }
}