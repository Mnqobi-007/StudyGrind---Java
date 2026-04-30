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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
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

    @Autowired
    private MetricsService metricsService;

    @Operation(
            summary = "Authenticate user",
            description = "Login with email and password to receive JWT access and refresh tokens"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                        "success": true,
                        "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
                        "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
                        "tokenType": "Bearer",
                        "redirect": "/student/dashboard",
                        "role": "student",
                        "userId": 1,
                        "fullName": "John Doe",
                        "email": "john@studygrind.com"
                    }
                """))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or account not verified"),
            @ApiResponse(responseCode = "403", description = "Account is locked or pending verification")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        metricsService.recordApiCall("/auth/login", "POST", 0);

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
                metricsService.recordLoginFailure();
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

            metricsService.recordLoginSuccess();
            metricsService.recordApiCall("/auth/login", "POST", 200);

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            metricsService.recordLoginFailure();
            metricsService.recordApiCall("/auth/login", "POST", 401);
            metricsService.recordError("INVALID_CREDENTIALS", "/auth/login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Invalid email or password"));

        } catch (Exception e) {
            metricsService.recordLoginFailure();
            metricsService.recordApiCall("/auth/login", "POST", 500);
            metricsService.recordError("LOGIN_ERROR", "/auth/login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Login failed: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Refresh access token",
            description = "Get a new access token using a valid refresh token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        metricsService.recordApiCall("/auth/refresh", "POST", 0);

        try {
            RefreshToken verifiedToken = refreshTokenService.verifyRefreshToken(refreshToken);
            String newAccessToken = tokenProvider.generateTokenFromUserId(verifiedToken.getUser().getId(), 86400000);

            metricsService.recordApiCall("/auth/refresh", "POST", 200);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "accessToken", newAccessToken,
                    "tokenType", "Bearer"
            ));

        } catch (RuntimeException e) {
            metricsService.recordApiCall("/auth/refresh", "POST", 401);
            metricsService.recordError("REFRESH_TOKEN_ERROR", "/auth/refresh");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Logout user",
            description = "Invalidate the refresh token and clear security context"
    )
    @ApiResponse(responseCode = "200", description = "Logged out successfully")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody(required = false) Map<String, String> request) {
        if (request != null && request.containsKey("refreshToken")) {
            refreshTokenService.revokeRefreshToken(request.get("refreshToken"));
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    @Operation(
            summary = "Logout from all devices",
            description = "Revoke all refresh tokens for the current user"
    )
    @ApiResponse(responseCode = "200", description = "Logged out from all devices")
    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> logoutAllDevices(@AuthenticationPrincipal UserPrincipal currentUser) {
        refreshTokenService.revokeAllUserTokens(currentUser.getId());
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out from all devices"));
    }

    @Operation(
            summary = "Register new student",
            description = "Register a new student account with student ID card verification"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registration submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data or missing student card")
    })
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> registerUserWithCard(
            @Parameter(description = "User registration data as JSON")
            @RequestPart("user") String userJson,
            @Parameter(description = "Student ID card image file (JPEG or PNG, max 5MB)")
            @RequestPart(value = "studentCard", required = false) MultipartFile studentCardFile) {

        metricsService.recordApiCall("/auth/register", "POST", 0);

        try {
            ObjectMapper mapper = new ObjectMapper();
            RegisterRequest registerRequest = mapper.readValue(userJson, RegisterRequest.class);

            if (studentCardFile == null || studentCardFile.isEmpty()) {
                metricsService.recordApiCall("/auth/register", "POST", 400);
                metricsService.recordError("MISSING_STUDENT_CARD", "/auth/register");
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Student card image is required"));
            }

            User user = userService.registerUserWithVerification(registerRequest, studentCardFile);

            metricsService.recordRegistration();
            metricsService.recordRegistrationWithCard();
            metricsService.recordApiCall("/auth/register", "POST", 200);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Registration submitted successfully! Please wait for admin approval.",
                    "userId", user.getId(),
                    "verificationStatus", "pending"
            ));

        } catch (IOException e) {
            metricsService.recordApiCall("/auth/register", "POST", 500);
            metricsService.recordError("FILE_UPLOAD_ERROR", "/auth/register");
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Failed to upload student card: " + e.getMessage()));

        } catch (RuntimeException e) {
            metricsService.recordApiCall("/auth/register", "POST", 400);
            metricsService.recordError("REGISTRATION_ERROR", "/auth/register");
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Get current user info",
            description = "Retrieve the authenticated user's profile information"
    )
    @ApiResponse(responseCode = "200", description = "User info retrieved successfully")
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

    @Operation(
            summary = "Get profile completion status",
            description = "Check if user profile is complete and get trial information"
    )
    @ApiResponse(responseCode = "200", description = "Profile status retrieved")
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

    @Operation(
            summary = "Resend verification email",
            description = "Resend the email verification link to the user's email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email sent"),
            @ApiResponse(responseCode = "400", description = "Email already verified or error sending")
    })
    @PostMapping("/resend-verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            User user = userService.findById(currentUser.getId());

            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "Email already verified"));
            }

            String token = emailService.createVerificationToken(user.getEmail());
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), token);

            return ResponseEntity.ok(Map.of("success", true, "message", "Verification email sent"));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Verify email address",
            description = "Verify user's email address using the verification token"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean verified = userService.verifyEmail(token);

        if (verified) {
            return ResponseEntity.ok("Email verified successfully! You can now close this window and login.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired verification token.");
        }
    }

    @Operation(
            summary = "Request password reset",
            description = "Send a password reset verification code to the user's email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification code sent"),
            @ApiResponse(responseCode = "400", description = "Email not found")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = userService.requestPasswordReset(email);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Verification code sent to your email"
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Reset password with verification code",
            description = "Reset password using the verification code sent to email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid code or expired")
    })
    @PostMapping("/verify-reset")
    public ResponseEntity<Map<String, Object>> verifyReset(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            String newPassword = request.get("newPassword");

            userService.verifyAndResetPassword(email, code, newPassword);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password reset successfully"
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private String getRedirectUrl(String role) {
        if (Constants.ROLE_ADMIN.equals(role)) return "/admin/dashboard";
        if (Constants.ROLE_TEACHER.equals(role)) return "/teacher/dashboard";
        return "/student/dashboard";
    }
}