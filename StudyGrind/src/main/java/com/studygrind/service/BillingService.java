package com.studygrind.service;

import com.studygrind.dto.response.BillingSummaryResponse;
import com.studygrind.dto.response.CourseEnrollmentResponse;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.model.CoursePayment;
import com.studygrind.model.User;
import com.studygrind.repository.CourseEnrollmentRepository;
import com.studygrind.repository.CoursePaymentRepository;
import com.studygrind.util.PayFastUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BillingService {

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private CoursePaymentRepository paymentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PayFastUtil payFastUtil;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Value("${payfast.enabled:false}")
    private boolean payFastEnabled;

    @Value("${payfast.merchant.id:}")
    private String merchantId;

    @Value("${payfast.merchant.key:}")
    private String merchantKey;

    @Value("${payfast.passphrase:}")
    private String passphrase;

    @Value("${payfast.testing:true}")
    private boolean testing;

    @Transactional(readOnly = true)
    public BillingSummaryResponse getBillingSummary(Long studentId) {
        User student = userService.findById(studentId);
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(student);

        double totalOutstanding = 0.0;
        int activeCourses = 0;
        int expiredTrials = 0;
        int activeSubscriptions = 0;

        List<CourseEnrollmentResponse> enrollmentResponses = new ArrayList<>();

        for (CourseEnrollment enrollment : enrollments) {
            if (enrollment.getIsEnrolled() == null || !enrollment.getIsEnrolled()) {
                continue;
            }

            CourseEnrollmentResponse er = new CourseEnrollmentResponse();
            er.setEnrollmentId(enrollment.getId());
            er.setCourseId(enrollment.getCourse().getId());
            er.setCourseName(enrollment.getCourse().getName());
            er.setCoursePrice(enrollment.getCourse().getPrice());

            // Calculate actual outstanding balance based on access status
            double outstanding = enrollment.getEffectiveOutstandingBalance();
            er.setOutstandingBalance(outstanding);

            boolean trialActive = enrollment.isTrialActive();
            boolean trialUsed = enrollment.getTrialUsed() != null && enrollment.getTrialUsed();
            long trialDaysRemaining = enrollment.getTrialDaysRemaining();

            er.setTrialActive(trialActive);
            er.setTrialUsed(trialUsed);
            er.setTrialDaysRemaining(trialDaysRemaining);

            boolean subscriptionActive = enrollment.hasActiveSubscription();
            long subscriptionDaysRemaining = enrollment.getSubscriptionDaysRemaining();

            er.setSubscriptionActive(subscriptionActive);
            er.setSubscriptionDaysRemaining(subscriptionDaysRemaining);
            er.setCanAccessContent(enrollment.canAccessContent());
            er.setPaymentStatus(enrollment.getPaymentStatus() != null ? enrollment.getPaymentStatus() : "pending");

            enrollmentResponses.add(er);

            totalOutstanding += outstanding;

            if (enrollment.canAccessContent()) {
                activeCourses++;
            }
            if (trialUsed && !trialActive && outstanding > 0 && !subscriptionActive) {
                expiredTrials++;
            }
            if (subscriptionActive) {
                activeSubscriptions++;
            }
        }

        BillingSummaryResponse response = new BillingSummaryResponse();
        response.setTotalOutstandingBalance(totalOutstanding);
        response.setActiveCourses(activeCourses);
        response.setExpiredTrials(expiredTrials);
        response.setActiveSubscriptions(activeSubscriptions);
        response.setEnrollments(enrollmentResponses);

        return response;
    }

    public List<CourseEnrollmentResponse> getEnrollmentsWithBillingStatus(Long studentId) {
        User student = userService.findById(studentId);
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(student);
        List<CourseEnrollmentResponse> responses = new ArrayList<>();

        for (CourseEnrollment enrollment : enrollments) {
            if (enrollment.getIsEnrolled() == null || !enrollment.getIsEnrolled()) {
                continue;
            }

            CourseEnrollmentResponse er = new CourseEnrollmentResponse();
            er.setEnrollmentId(enrollment.getId());
            er.setCourseId(enrollment.getCourse().getId());
            er.setCourseName(enrollment.getCourse().getName());
            er.setCoursePrice(enrollment.getCourse().getPrice());
            er.setOutstandingBalance(enrollment.getEffectiveOutstandingBalance());
            er.setTrialActive(enrollment.isTrialActive());
            er.setTrialUsed(enrollment.getTrialUsed() != null && enrollment.getTrialUsed());
            er.setTrialDaysRemaining(enrollment.getTrialDaysRemaining());
            er.setSubscriptionActive(enrollment.hasActiveSubscription());
            er.setSubscriptionDaysRemaining(enrollment.getSubscriptionDaysRemaining());
            er.setCanAccessContent(enrollment.canAccessContent());
            er.setPaymentStatus(enrollment.getPaymentStatus() != null ? enrollment.getPaymentStatus() : "pending");
            responses.add(er);
        }

        return responses;
    }

    public Map<String, Object> createPaymentForEnrollment(Long enrollmentId, Long studentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (!enrollment.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Check if already has active subscription
        if (enrollment.hasActiveSubscription()) {
            throw new RuntimeException("You already have an active subscription for this course");
        }

        double outstanding = enrollment.getEffectiveOutstandingBalance();

        if (outstanding <= 0) {
            throw new RuntimeException("No outstanding balance for this course");
        }

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("enrollmentId", enrollmentId);
        paymentData.put("amount", outstanding);
        paymentData.put("courseName", enrollment.getCourse().getName());
        paymentData.put("description", "Payment for course: " + enrollment.getCourse().getName());

        Map<String, String> payFastRequest = createPayFastRequest(
                studentId,
                outstanding,
                "Course Payment: " + enrollment.getCourse().getName(),
                "enrollment_" + enrollmentId
        );

        paymentData.put("payfast", payFastRequest);
        paymentData.put("mode", payFastEnabled ? "live" : "mock");

        return paymentData;
    }

    public Map<String, Object> createPaymentForAllOutstanding(Long studentId) {
        User student = userService.findById(studentId);
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(student);

        double totalAmount = 0.0;
        List<Long> enrollmentIds = new ArrayList<>();
        List<String> courseNames = new ArrayList<>();

        for (CourseEnrollment enrollment : enrollments) {
            if ((enrollment.getIsEnrolled() == null || !enrollment.getIsEnrolled())) {
                continue;
            }

            // Skip if already has active subscription
            if (enrollment.hasActiveSubscription()) {
                continue;
            }

            double outstanding = enrollment.getEffectiveOutstandingBalance();
            if (outstanding > 0) {
                totalAmount += outstanding;
                enrollmentIds.add(enrollment.getId());
                courseNames.add(enrollment.getCourse().getName());
            }
        }

        if (totalAmount <= 0) {
            throw new RuntimeException("No outstanding payments");
        }

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("enrollmentIds", enrollmentIds);
        paymentData.put("totalAmount", totalAmount);
        paymentData.put("courseNames", courseNames);
        paymentData.put("description", "Payment for " + enrollmentIds.size() + " courses");

        Map<String, String> payFastRequest = createPayFastRequest(
                studentId,
                totalAmount,
                "Payment for " + enrollmentIds.size() + " courses",
                "bulk_" + System.currentTimeMillis()
        );

        paymentData.put("payfast", payFastRequest);
        paymentData.put("mode", payFastEnabled ? "live" : "mock");

        return paymentData;
    }

    private Map<String, String> createPayFastRequest(Long studentId, Double amount, String itemName, String itemId) {
        User student = userService.findById(studentId);

        Map<String, String> data = new HashMap<>();

        if (payFastEnabled && merchantId != null && !merchantId.isEmpty() && merchantKey != null && !merchantKey.isEmpty()) {
            data.put("merchant_id", merchantId);
            data.put("merchant_key", merchantKey);
            data.put("return_url", "http://localhost:8080/payment/success");
            data.put("cancel_url", "http://localhost:8080/payment/cancel");
            data.put("notify_url", "http://localhost:8080/api/payfast/notify");

            data.put("m_payment_id", itemId + "_" + System.currentTimeMillis());
            data.put("amount", String.format("%.2f", amount));
            data.put("item_name", itemName);
            data.put("item_description", "Course payment");

            data.put("custom_str1", String.valueOf(studentId));
            data.put("custom_str2", itemId);

            String[] nameParts = student.getFullName().split(" ", 2);
            data.put("name_first", nameParts[0]);
            data.put("name_last", nameParts.length > 1 ? nameParts[1] : "");
            data.put("email_address", student.getEmail());

            if (testing) {
                data.put("testing", "1");
            }

            String signature = payFastUtil.generateSignature(data, passphrase);
            data.put("signature", signature);
        } else {
            data.put("mock", "true");
            data.put("message", "PayFast not configured. Mock payment mode.");
        }

        return data;
    }

    @Transactional
    public boolean processPaymentNotification(Map<String, String> params) {
        String paymentId = params.get("pf_payment_id");
        String amount = params.get("amount_gross");
        String customStr1 = params.get("custom_str1");
        String customStr2 = params.get("custom_str2");
        String paymentStatus = params.get("payment_status");

        if (!"COMPLETE".equals(paymentStatus)) {
            return false;
        }

        try {
            Long studentId = Long.parseLong(customStr1);

            if (customStr2 != null && customStr2.startsWith("enrollment_")) {
                Long enrollmentId = Long.parseLong(customStr2.substring(11));
                processEnrollmentPayment(enrollmentId, Double.parseDouble(amount), "payfast", paymentId);
            } else if (customStr2 != null && customStr2.startsWith("bulk_")) {
                User student = userService.findById(studentId);
                List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(student);

                double remainingAmount = Double.parseDouble(amount);
                for (CourseEnrollment enrollment : enrollments) {
                    double outstanding = enrollment.getEffectiveOutstandingBalance();
                    if (outstanding > 0 && remainingAmount >= outstanding) {
                        processEnrollmentPayment(enrollment.getId(), outstanding, "payfast", paymentId + "_" + enrollment.getId());
                        remainingAmount -= outstanding;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error processing payment notification: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public void processEnrollmentPayment(Long enrollmentId, Double amount, String paymentMethod, String paymentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Create payment record
        CoursePayment payment = new CoursePayment(enrollment, amount, paymentMethod, paymentId);
        paymentRepository.save(payment);

        // Activate 30-day subscription
        enrollment.setSubscriptionActive(true);
        enrollment.setSubscriptionStartDate(LocalDateTime.now());
        enrollment.setSubscriptionEndDate(LocalDateTime.now().plusDays(30));
        enrollment.setOutstandingBalance(0.0);
        enrollment.setPaymentStatus("paid");
        enrollment.setLastPaymentDate(LocalDateTime.now());

        enrollmentRepository.save(enrollment);

        // Send notification to student
        notificationService.sendNotification(enrollment.getStudent().getId(),
                "✅ Payment received! Your subscription for " + enrollment.getCourse().getName() + " is now active for 30 days.",
                "payment_success");

        // Send email confirmation
        emailService.sendPaymentConfirmationEmail(
                enrollment.getStudent().getEmail(),
                enrollment.getStudent().getFullName(),
                amount,
                paymentId
        );

        System.out.println("✅ Payment processed for enrollment " + enrollmentId +
                ". Subscription active until " + enrollment.getSubscriptionEndDate());
    }

    @Transactional
    public void processMockPaymentForEnrollment(Long enrollmentId, Long studentId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (!enrollment.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("Unauthorized");
        }

        double amount = enrollment.getEffectiveOutstandingBalance();

        if (amount <= 0) {
            throw new RuntimeException("No outstanding balance for this course");
        }

        // Create mock payment record
        CoursePayment payment = new CoursePayment(enrollment, amount, "mock", "mock_" + System.currentTimeMillis());
        paymentRepository.save(payment);

        // Activate 30-day subscription
        enrollment.setSubscriptionActive(true);
        enrollment.setSubscriptionStartDate(LocalDateTime.now());
        enrollment.setSubscriptionEndDate(LocalDateTime.now().plusDays(30));
        enrollment.setOutstandingBalance(0.0);
        enrollment.setPaymentStatus("paid");
        enrollment.setLastPaymentDate(LocalDateTime.now());

        enrollmentRepository.save(enrollment);

        // Send notification
        notificationService.sendNotification(studentId,
                "✅ Mock payment processed! Your subscription for " + enrollment.getCourse().getName() + " is now active for 30 days.",
                "payment_success");

        System.out.println("✅ Mock payment processed for enrollment " + enrollmentId);
    }
}