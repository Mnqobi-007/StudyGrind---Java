package com.studygrind.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordLoginSuccess() {
        meterRegistry.counter("auth.login.success").increment();
    }

    public void recordLoginFailure() {
        meterRegistry.counter("auth.login.failure").increment();
    }

    public void recordRegistration() {
        meterRegistry.counter("user.registration").increment();
    }

    public void recordRegistrationWithCard() {
        meterRegistry.counter("user.registration.with_card").increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String name, String... tags) {
        Timer.Builder builder = Timer.builder(name)
                .description("Request timing")
                .publishPercentileHistogram();
        
        for (int i = 0; i < tags.length; i += 2) {
            if (i + 1 < tags.length) {
                builder.tag(tags[i], tags[i + 1]);
            }
        }
        
        sample.stop(builder.register(meterRegistry));
    }

    public void recordCourseEnrollment() {
        meterRegistry.counter("course.enrollment").increment();
    }

    public void recordCourseUnenrollment() {
        meterRegistry.counter("course.unenrollment").increment();
    }

    public void recordPaymentComplete(Double amount) {
        meterRegistry.counter("payment.complete").increment();
        meterRegistry.summary("payment.amount").record(amount);
    }

    public void recordAssignmentSubmitted() {
        meterRegistry.counter("assignment.submitted").increment();
    }

    public void recordAssignmentGraded() {
        meterRegistry.counter("assignment.graded").increment();
    }

    public void recordQuizCompleted(Double score) {
        meterRegistry.counter("quiz.completed").increment();
        meterRegistry.summary("quiz.score").record(score);
    }

    public void recordActiveUsers(long count) {
        meterRegistry.gauge("users.active", count);
    }

    public void recordApiCall(String endpoint, String method, int status) {
        meterRegistry.counter("api.calls",
                "endpoint", endpoint,
                "method", method,
                "status", String.valueOf(status)).increment();
    }

    public void recordError(String errorType, String endpoint) {
        meterRegistry.counter("errors.total",
                "type", errorType,
                "endpoint", endpoint).increment();
    }
}