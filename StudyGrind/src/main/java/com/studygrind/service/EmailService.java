package com.studygrind.service;

import com.studygrind.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    private final java.util.concurrent.ConcurrentHashMap<String, VerificationToken> verificationTokens = new java.util.concurrent.ConcurrentHashMap<>();

    private static class VerificationToken {
        String token;
        String email;
        LocalDateTime expiry;

        VerificationToken(String token, String email) {
            this.token = token;
            this.email = email;
            this.expiry = LocalDateTime.now().plusHours(24);
        }

        boolean isValid() { return LocalDateTime.now().isBefore(expiry); }
    }

    private void sendEmail(String to, String subject, String text, String htmlContent) {
        logger.info("Sending email to: {} - Subject: {}", to, subject);

        printToConsole(to, subject, text);

        if (emailEnabled && mailSender != null && fromEmail != null && !fromEmail.isEmpty()) {
            try {
                if (htmlContent != null && !htmlContent.isEmpty()) {
                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    helper.setFrom(fromEmail);
                    helper.setTo(to);
                    helper.setSubject(subject);
                    helper.setText(htmlContent, true);
                    mailSender.send(mimeMessage);
                } else {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(fromEmail);
                    message.setTo(to);
                    message.setSubject(subject);
                    message.setText(text);
                    mailSender.send(message);
                }
                logger.info("Email sent successfully to: {}", to);
                System.out.println("✅ Email sent to: " + to);
            } catch (Exception e) {
                logger.error("Failed to send email to {}: {}", to, e.getMessage());
                System.err.println("❌ Failed to send email: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ Email sending disabled. Check console for content.");
        }
    }

    private void printToConsole(String to, String subject, String content) {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("📧 EMAIL [" + subject + "]");
        System.out.println("═".repeat(70));
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("─".repeat(70));
        System.out.println(content);
        System.out.println("═".repeat(70) + "\n");
    }

    // ==================== VERIFICATION EMAILS ====================

    public void sendRegistrationPendingEmail(String to, String name) {
        String text = "Hello " + name + ",\n\n" +
                "Thank you for registering with StudyGrind!\n\n" +
                "Your student card has been submitted for verification. An administrator will review your registration shortly.\n\n" +
                "You will receive an email once your account has been approved.\n\n" +
                "Best regards,\nStudyGrind Team";

        String html = buildRegistrationPendingHtml(name);
        sendEmail(to, "📋 Registration Pending - StudyGrind", text, html);
    }

    public void sendRegistrationApprovedEmail(String to, String name) {
        String text = "Hello " + name + ",\n\n" +
                "Congratulations! Your student account has been verified and approved.\n\n" +
                "You can now log in and start your learning journey with StudyGrind.\n\n" +
                "Log in here: " + appUrl + "/login\n\n" +
                "Best regards,\nStudyGrind Team";

        String html = buildRegistrationApprovedHtml(name);
        sendEmail(to, "✅ Account Approved - StudyGrind", text, html);
    }

    public void sendRegistrationRejectedEmail(String to, String name, String reason) {
        String text = "Hello " + name + ",\n\n" +
                "We regret to inform you that your student account verification has been rejected.\n\n" +
                "Reason: " + (reason != null ? reason : "The submitted student card could not be verified.") + "\n\n" +
                "Please contact support for further assistance.\n\n" +
                "Best regards,\nStudyGrind Team";

        String html = buildRegistrationRejectedHtml(name, reason);
        sendEmail(to, "❌ Verification Rejected - StudyGrind", text, html);
    }

    // ==================== STANDARD EMAILS ====================

    public void sendVerificationEmail(String to, String name, String verificationToken) {
        String verificationLink = appUrl + "/api/auth/verify?token=" + verificationToken;
        String text = "Hello " + name + ",\n\nPlease verify your email by clicking the link below:\n" + verificationLink + "\n\nThis link expires in 24 hours.\n\nBest regards,\nStudyGrind Team";
        String html = buildVerificationEmailHtml(name, verificationLink);
        sendEmail(to, "Verify Your StudyGrind Account", text, html);
    }

    public void sendPasswordResetEmail(String to, String code) {
        String text = "Hello,\n\nYour password reset code is: " + code + "\n\nThis code expires in 15 minutes.\n\nIf you didn't request this, please ignore this email.";
        String html = buildPasswordResetEmailHtml(code);
        sendEmail(to, "🔐 Password Reset Request", text, html);
    }

    public void sendWelcomeEmail(String to, String name) {
        String text = "Hello " + name + ",\n\nWelcome to StudyGrind! Your 14-day free trial has been activated.\n\nStart learning today!";
        String html = buildWelcomeEmailHtml(name);
        sendEmail(to, "🎉 Welcome to StudyGrind!", text, html);
    }

    public void sendAssignmentSubmittedEmail(String studentName, String assignmentTitle, String teacherEmail) {
        String text = "Student " + studentName + " has submitted assignment: " + assignmentTitle;
        sendEmail(teacherEmail, "📝 New Assignment Submission", text, null);
    }

    public void sendAssignmentGradedEmail(String assignmentTitle, Double score, String studentEmail) {
        String text = "Your assignment '" + assignmentTitle + "' has been graded. Score: " + score + "%";
        sendEmail(studentEmail, "📊 Assignment Graded", text, null);
    }

    public void sendPaymentConfirmationEmail(String to, String name, Double amount, String paymentId) {
        String text = "Hello " + name + ",\n\nPayment of $" + amount + " received. Transaction ID: " + paymentId + "\n\nThank you for your payment!";
        String html = buildPaymentConfirmationHtml(name, amount, paymentId);
        sendEmail(to, "💰 Payment Confirmation", text, html);
    }

    public void sendCourseUpdateEmail(String courseName, String updateMessage, String studentEmail) {
        String text = "Course '" + courseName + "' has been updated: " + updateMessage;
        sendEmail(studentEmail, "📚 Course Update", text, null);
    }

    public void sendCourseEnrollmentEmail(String to, String name, String courseName, int trialDays) {
        String text = "Hello " + name + ",\n\n" +
                "You have successfully enrolled in \"" + courseName + "\"!\n\n" +
                "Your " + trialDays + "-day free trial has started. You can access all course materials during this period.\n\n" +
                "After the trial ends, you can subscribe for $" + 29.99 + " per month to continue access.\n\n" +
                "Happy learning!\n\nBest regards,\nStudyGrind Team";

        String html = buildCourseEnrollmentHtml(name, courseName, trialDays);
        sendEmail(to, "🎓 Enrollment Confirmation: " + courseName, text, html);
    }

    public void sendTrialExpiringEmail(String to, String name, String courseName, long daysRemaining) {
        String text = "Hello " + name + ",\n\n" +
                "Your free trial for \"" + courseName + "\" will expire in " + daysRemaining + " days.\n\n" +
                "To continue accessing the course materials, please subscribe now.\n\n" +
                "Visit your dashboard to complete the payment.\n\n" +
                "Best regards,\nStudyGrind Team";

        String html = buildTrialExpiringHtml(name, courseName, daysRemaining);
        sendEmail(to, "⏰ Trial Ending Soon: " + courseName, text, html);
    }

    public void sendTrialExpiredEmail(String to, String name, String courseName, Double price) {
        String text = "Hello " + name + ",\n\n" +
                "Your free trial for \"" + courseName + "\" has expired.\n\n" +
                "You can no longer access the course materials.\n\n" +
                "To regain access, please subscribe for $" + price + " per month.\n\n" +
                "Visit your dashboard to complete the payment.\n\n" +
                "Best regards,\nStudyGrind Team";

        String html = buildTrialExpiredHtml(name, courseName, price);
        sendEmail(to, "⚠️ Trial Expired: " + courseName, text, html);
    }

    public void sendPaymentSuccessEmail(String to, String name, String courseName, Double amount, String paymentId) {
        String text = "Hello " + name + ",\n\n" +
                "Your payment of $" + amount + " for \"" + courseName + "\" has been received successfully.\n\n" +
                "Transaction ID: " + paymentId + "\n\n" +
                "Your subscription is now active. You can continue accessing all course materials.\n\n" +
                "Thank you for your payment!\n\nBest regards,\nStudyGrind Team";

        String html = buildPaymentSuccessHtml(name, courseName, amount, paymentId);
        sendEmail(to, "✅ Payment Confirmation: " + courseName, text, html);
    }

    public String createVerificationToken(String email) {
        String token = UUID.randomUUID().toString();
        verificationTokens.put(token, new VerificationToken(token, email));
        return token;
    }

    public String verifyEmail(String token) {
        VerificationToken vt = verificationTokens.get(token);
        if (vt == null) return null;
        if (!vt.isValid()) {
            verificationTokens.remove(token);
            return null;
        }
        verificationTokens.remove(token);
        return vt.email;
    }

    // ==================== HTML BUILDERS ====================

    private String buildRegistrationPendingHtml(String name) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2>Registration Pending Verification</h2>" +
                "<p>Hello " + name + ",</p>" +
                "<p>Thank you for registering with StudyGrind!</p>" +
                "<div style='background:#fef3c7;padding:15px;border-radius:12px;margin:20px 0;text-align:center;'>" +
                "<i class='fa-solid fa-clock' style='font-size:24px;color:#f59e0b;'></i><br>" +
                "<strong>Your student card has been submitted for verification.</strong><br>" +
                "An administrator will review your registration shortly.</div>" +
                "<p>You will receive an email once your account has been approved.</p>" +
                "<hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }

    private String buildRegistrationApprovedHtml(String name) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2 style='color:#22c55e;'>✅ Account Approved!</h2>" +
                "<p>Hello " + name + ",</p>" +
                "<p>Congratulations! Your student account has been verified and approved.</p>" +
                "<div style='background:#d1fae5;padding:15px;border-radius:12px;margin:20px 0;text-align:center;'>" +
                "<strong>You can now log in and start your learning journey!</strong></div>" +
                "<a href='" + appUrl + "/login' style='background:#3b82f6;color:white;padding:12px 24px;text-decoration:none;border-radius:8px;display:inline-block;'>Log In Now</a>" +
                "<hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }

    private String buildRegistrationRejectedHtml(String name, String reason) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2 style='color:#ef4444;'>❌ Verification Rejected</h2>" +
                "<p>Hello " + name + ",</p>" +
                "<p>We regret to inform you that your student account verification has been rejected.</p>" +
                "<div style='background:#fee2e2;padding:15px;border-radius:12px;margin:20px 0;'>" +
                "<strong>Reason:</strong> " + (reason != null ? reason : "The submitted student card could not be verified.") + "</div>" +
                "<p>Please contact support for further assistance.</p>" +
                "<hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }

    private String buildVerificationEmailHtml(String name, String verificationLink) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2>Verify Your Email Address</h2><p>Hello " + name + ",</p>" +
                "<p>Please verify your email address by clicking the button below:</p>" +
                "<div style='text-align:center;margin:30px 0;'><a href='" + verificationLink + "' style='background:#3b82f6;color:white;padding:12px 24px;text-decoration:none;border-radius:8px;'>Verify Email</a></div>" +
                "<p>This link expires in 24 hours.</p><hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }

    private String buildPasswordResetEmailHtml(String code) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2>Password Reset Request</h2><p>Your verification code is:</p>" +
                "<div style='background:#fff;padding:20px;text-align:center;border-radius:12px;margin:20px 0;border:2px solid #e2e8f0;'>" +
                "<span style='font-size:32px;font-weight:800;letter-spacing:4px;'>" + code + "</span></div>" +
                "<p>This code expires in 15 minutes.</p><hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }

    private String buildWelcomeEmailHtml(String name) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2>Welcome to StudyGrind, " + name + "! 🎉</h2>" +
                "<p>Your 14-day free trial has been activated. Start exploring our courses today!</p>" +
                "<div style='margin:20px 0;'><strong>Getting Started:</strong><ul><li>Browse available courses</li><li>Enroll and start learning</li><li>Complete quizzes and assignments</li></ul></div>" +
                "<hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }

    private String buildPaymentConfirmationHtml(String name, Double amount, String paymentId) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2>Payment Confirmation</h2><p>Hello " + name + ",</p>" +
                "<p>Your payment of <strong>$" + amount + "</strong> has been received successfully.</p>" +
                "<p><strong>Transaction ID:</strong> " + paymentId + "</p>" +
                "<p>Your subscription is now active. Happy learning!</p>" +
                "<hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }

    private String buildCourseEnrollmentHtml(String name, String courseName, int trialDays) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2>Enrollment Confirmation! 🎓</h2>" +
                "<p>Hello " + name + ",</p>" +
                "<p>You have successfully enrolled in <strong>" + courseName + "</strong>!</p>" +
                "<div style='background:#d1fae5;padding:15px;border-radius:12px;margin:20px 0;text-align:center;'>" +
                "<strong>Your " + trialDays + "-day free trial has started!</strong><br>" +
                "Enjoy full access to all course materials.</div>" +
                "<p>After your trial ends, you can subscribe to continue learning.</p>" +
                "<hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }

    private String buildTrialExpiringHtml(String name, String courseName, long daysRemaining) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2 style='color:#f59e0b;'>⚠️ Trial Ending Soon</h2>" +
                "<p>Hello " + name + ",</p>" +
                "<p>Your free trial for <strong>" + courseName + "</strong> will expire in <strong style='color:#f59e0b;'>" + daysRemaining + " days</strong>.</p>" +
                "<div style='background:#fef3c7;padding:15px;border-radius:12px;margin:20px 0;'>" +
                "<a href='" + appUrl + "/student/dashboard' style='background:#3b82f6;color:white;padding:10px 20px;text-decoration:none;border-radius:8px;'>Subscribe Now</a>" +
                "</div>" +
                "<hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }

    private String buildTrialExpiredHtml(String name, String courseName, Double price) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2 style='color:#ef4444;'>❌ Trial Expired</h2>" +
                "<p>Hello " + name + ",</p>" +
                "<p>Your free trial for <strong>" + courseName + "</strong> has expired.</p>" +
                "<div style='background:#fee2e2;padding:15px;border-radius:12px;margin:20px 0;'>" +
                "<p>To regain access, subscribe for <strong>$" + price + "/month</strong></p>" +
                "<a href='" + appUrl + "/pay-all' style='background:#3b82f6;color:white;padding:10px 20px;text-decoration:none;border-radius:8px;display:inline-block;'>Subscribe Now</a>" +
                "</div>" +
                "<hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }

    private String buildPaymentSuccessHtml(String name, String courseName, Double amount, String paymentId) {
        return "<!DOCTYPE html><html><head><style>body{font-family:'Plus Jakarta Sans',sans-serif;}</style></head><body>" +
                "<div style='max-width:600px;margin:0 auto;padding:20px;background:#f8fafc;border-radius:16px;'>" +
                "<div style='text-align:center;margin-bottom:30px;'><h1 style='color:#000;'>Study<span style='color:#facc15;'>Grind</span></h1></div>" +
                "<h2 style='color:#22c55e;'>✅ Payment Successful!</h2>" +
                "<p>Hello " + name + ",</p>" +
                "<p>Your payment of <strong>$" + amount + "</strong> for <strong>" + courseName + "</strong> has been received.</p>" +
                "<div style='background:#d1fae5;padding:15px;border-radius:12px;margin:20px 0;'>" +
                "<p><strong>Transaction ID:</strong> " + paymentId + "</p>" +
                "<p>Your subscription is now active. Happy learning!</p>" +
                "</div>" +
                "<hr><p style='font-size:12px;color:#64748b;'>StudyGrind - Engineered for Academic Growth</p>" +
                "</div></body></html>";
    }
}