package com.studygrind.service;

import com.studygrind.dto.CreateQuestionRequest;
import com.studygrind.dto.CreateQuizRequest;
import com.studygrind.dto.QuizSubmitRequest;
import com.studygrind.model.Question;
import com.studygrind.model.Quiz;
import com.studygrind.model.QuizAnswer;
import com.studygrind.model.QuizAttempt;
import com.studygrind.model.User;
import com.studygrind.repository.QuestionRepository;
import com.studygrind.repository.QuizAnswerRepository;
import com.studygrind.repository.QuizAttemptRepository;
import com.studygrind.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizService {

    private QuizRepository quizRepository;
    private QuestionRepository questionRepository;
    private QuizAttemptRepository quizAttemptRepository;
    private QuizAnswerRepository quizAnswerRepository;

    public QuizService(QuizRepository quizRepository,
                       QuestionRepository questionRepository,
                       QuizAttemptRepository quizAttemptRepository,
                       QuizAnswerRepository quizAnswerRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAnswerRepository = quizAnswerRepository;
    }

    public List<Map<String, Object>> getAllQuizzes(User user) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        List<Quiz> quizzes;

        if (user.isAdmin()) {
            quizzes = quizRepository.findAll();
        } else if (user.isTeacher()) {
            quizzes = quizRepository.findByTeacherId(user.getId());
        } else {
            quizzes = quizRepository.findAll();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Quiz quiz : quizzes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", quiz.getId());
            map.put("title", quiz.getTitle());
            map.put("description", quiz.getDescription());
            map.put("subject", quiz.getSubject());
            map.put("time_limit", quiz.getTimeLimit());

            List<Question> questions = questionRepository.findByQuizId(quiz.getId());
            map.put("question_count", questions.size());

            if (!user.isTeacher() && !user.isAdmin()) {
                boolean attempted = hasStudentAttemptedQuiz(quiz.getId(), user.getId());
                map.put("attempted", attempted);
            }

            result.add(map);
        }

        return result;
    }

    private boolean hasStudentAttemptedQuiz(Long quizId, Long studentId) {
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        for (QuizAttempt attempt : attempts) {
            if (attempt.getStudentId().equals(studentId)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> createQuiz(CreateQuizRequest request, User user) {
        if (request == null) {
            throw new RuntimeException("Request body is required");
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.isTeacher() && !user.isAdmin()) {
            throw new RuntimeException("Only teachers can create quizzes");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("Quiz title is required");
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setSubject(request.getSubject());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setTeacherId(user.getId());

        quiz = quizRepository.save(quiz);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", quiz.getId());
        return result;
    }

    public Map<String, Object> addQuestion(Long quizId, CreateQuestionRequest request) {
        if (quizId == null) {
            throw new RuntimeException("Quiz ID is required");
        }

        if (request == null) {
            throw new RuntimeException("Request body is required");
        }

        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (!quizOpt.isPresent()) {
            throw new RuntimeException("Quiz not found with id: " + quizId);
        }

        if (request.getText() == null || request.getText().isBlank()) {
            throw new RuntimeException("Question text is required");
        }

        Question question = new Question();
        question.setQuizId(quizId);
        question.setQuiz(quizOpt.get());
        question.setText(request.getText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setPoints(request.getPoints() > 0 ? request.getPoints() : 1);

        question = questionRepository.save(question);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", question.getId());
        return result;
    }

    public Map<String, Object> takeQuiz(Long quizId, User user) {
        if (quizId == null) {
            throw new RuntimeException("Quiz ID is required");
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (!quizOpt.isPresent()) {
            return null;
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = questionRepository.findByQuizId(quizId);

        Map<String, Object> quizData = new HashMap<>();
        quizData.put("id", quiz.getId());
        quizData.put("title", quiz.getTitle());
        quizData.put("description", quiz.getDescription());
        quizData.put("time_limit", quiz.getTimeLimit());

        List<Map<String, Object>> questionList = new ArrayList<>();

        for (Question question : questions) {
            Map<String, Object> questionMap = new HashMap<>();
            questionMap.put("id", question.getId());
            questionMap.put("text", question.getText());
            questionMap.put("options", java.util.List.of(
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD()
            ));
            // Do NOT include correct answer when taking the quiz
            questionList.add(questionMap);
        }

        quizData.put("questions", questionList);

        return quizData;
    }

    public Map<String, Object> submitQuiz(Long quizId, QuizSubmitRequest request, User user) {
        if (quizId == null) {
            throw new RuntimeException("Quiz ID is required");
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.isTeacher() || user.isAdmin()) {
            throw new RuntimeException("Only students can take quizzes");
        }

        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (!quizOpt.isPresent()) {
            throw new RuntimeException("Quiz not found with id: " + quizId);
        }

        if (request == null || request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new RuntimeException("Quiz answers are required");
        }

        Quiz quiz = quizOpt.get();
        List<Question> questions = questionRepository.findByQuizId(quizId);

        if (questions.isEmpty()) {
            throw new RuntimeException("This quiz has no questions");
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId(quizId);
        attempt.setQuiz(quiz);
        attempt.setStudentId(user.getId());
        attempt.setStudent(user);
        attempt.setStartedAt(LocalDateTime.now());

        attempt = quizAttemptRepository.save(attempt);

        int totalPoints = 0;
        int earnedPoints = 0;

        for (Question question : questions) {
            String questionKey = String.valueOf(question.getId());
            String selectedAnswer = request.getAnswers().get(questionKey);
            if (selectedAnswer == null) {
                selectedAnswer = "";
            }

            boolean isCorrect = selectedAnswer.equals(question.getCorrectAnswer());

            QuizAnswer answer = new QuizAnswer();
            answer.setAttemptId(attempt.getId());
            answer.setAttempt(attempt);
            answer.setQuestionId(question.getId());
            answer.setQuestion(question);
            answer.setSelectedAnswer(selectedAnswer);
            answer.setCorrect(isCorrect);

            quizAnswerRepository.save(answer);

            totalPoints += question.getPoints();
            if (isCorrect) {
                earnedPoints += question.getPoints();
            }
        }

        double scorePercentage = 0.0;
        if (totalPoints > 0) {
            scorePercentage = ((double) earnedPoints / totalPoints) * 100;
        }

        attempt.setScore(scorePercentage);
        attempt.setCompletedAt(LocalDateTime.now());
        quizAttemptRepository.save(attempt);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("score", scorePercentage);
        return result;
    }
}
