package com.studygrind.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studygrind.dto.request.*;
import com.studygrind.dto.response.UserResponse;
import com.studygrind.model.RefreshToken;
import com.studygrind.model.User;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.repository.CourseEnrollmentRepository;
import com.studygrind.security.JwtTokenProvider;
import com.studygrind.security.UserPrincipal;
import com.studygrind.service.*;
import com.studygrind.util.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                                                HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // Check if student is verified
            User user = userService.findById(userPrincipal.getId());
            if ("student".equals(user.getRole()) && !user.isVerified()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "error", "Your account is pending verification. Please wait for admin approval."));
            }

            refreshTokenService.createRefreshToken(userPrincipal.getId());
            userService.recordLoginHistory(userPrincipal.getId(), request);
            String redirectUrl = getRedirectUrl(userPrincipal.getRole());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("redirect", redirectUrl);
            response.put("role", userPrincipal.getRole());
            response.put("userId", userPrincipal.getId());
            response.put("fullName", userPrincipal.getFullName());
            response.put("email", userPrincipal.getUsername());
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        try {
            RefreshToken verifiedToken = refreshTokenService.verifyRefreshToken(refreshToken);
            String newAccessToken = tokenProvider.generateTokenFromUserId(verifiedToken.getUser().getId(), 86400000);
            return ResponseEntity.ok(Map.of("success", true, "accessToken", newAccessToken, "tokenType", "Bearer"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody(required = false) Map<String, String> request) {
        if (request != null && request.containsKey("refreshToken")) {
            refreshTokenService.revokeRefreshToken(request.get("refreshToken"));
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> logoutAllDevices(@AuthenticationPrincipal UserPrincipal currentUser) {
        refreshTokenService.revokeAllUserTokens(currentUser.getId());
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out from all devices"));
    }

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> registerUserWithCard(
            @RequestPart("user") String userJson,
            @RequestPart(value = "studentCard", required = false) MultipartFile studentCardFile) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            RegisterRequest registerRequest = mapper.readValue(userJson, RegisterRequest.class);

            if (studentCardFile == null || studentCardFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Student card image is required"));
            }

            User user = userService.registerUserWithVerification(registerRequest, studentCardFile);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Registration submitted successfully! Please wait for admin approval.",
                    "userId", user.getId(),
                    "verificationStatus", "pending"
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Failed to upload student card: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userService.findById(userPrincipal.getId());
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setFullName(user.getFullName());
        response.setCreatedAt(user.getCreatedAt());
        response.setStudentNumber(user.getStudentNumber());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setEmailVerified(user.getEmailVerified());
        response.setVerificationStatus(user.getVerificationStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getProfileStatus(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userService.findById(currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("profileCompleted", user.getProfileCompleted() != null && user.getProfileCompleted());
        response.put("emailVerified", user.getEmailVerified() != null && user.getEmailVerified());
        response.put("verificationStatus", user.getVerificationStatus());
        response.put("isVerified", user.isVerified());

        boolean onTrial = false;
        Long trialEndsAt = null;
        Long trialStartedAt = null;
        int trialDaysRemaining = 0;
        if ("student".equals(user.getRole())) {
            List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(user);
            for (CourseEnrollment enrollment : enrollments) {
                if (enrollment.isTrialActive()) {
                    onTrial = true;
                    if (enrollment.getTrialEndDate() != null) {
                        trialEndsAt = enrollment.getTrialEndDate().toInstant(ZoneOffset.UTC).toEpochMilli();
                        trialStartedAt = enrollment.getTrialStartDate().toInstant(ZoneOffset.UTC).toEpochMilli();
                        long daysRemaining = enrollment.getTrialDaysRemaining();
                        trialDaysRemaining = daysRemaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) daysRemaining;
                    }
                    break;
                }
            }
        }
        response.put("onTrial", onTrial);
        response.put("trialEndsAt", trialEndsAt);
        response.put("trialStartedAt", trialStartedAt);
        response.put("trialDaysRemaining", trialDaysRemaining);
        response.put("role", user.getRole());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            User user = userService.findById(currentUser.getId());
            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Email already verified"));
            }
            String token = emailService.createVerificationToken(user.getEmail());
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token);
            return ResponseEntity.ok(Map.of("success", true, "message", "Verification email sent"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean verified = userService.verifyEmail(token);
        if (verified) {
            return ResponseEntity.ok("Email verified successfully! You can now close this window and login.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification token.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = userService.requestPasswordReset(email);
            return ResponseEntity.ok(Map.of("success", true, "message", "Verification code sent to your email"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/verify-reset")
    public ResponseEntity<Map<String, Object>> verifyReset(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            String newPassword = request.get("newPassword");
            userService.verifyAndResetPassword(email, code, newPassword);
            return ResponseEntity.ok(Map.of("success", true, "message", "Password reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private String getRedirectUrl(String role) {
        if (Constants.ROLE_ADMIN.equals(role)) return "/admin/dashboard";
        if (Constants.ROLE_TEACHER.equals(role)) return "/teacher/dashboard";
        return "/student/dashboard";
    }
}