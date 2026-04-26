package com.studygrind.service;

import com.studygrind.dto.request.RegisterRequest;
import com.studygrind.dto.request.UpdateUserRequest;
import com.studygrind.dto.response.UserResponse;
import com.studygrind.model.Course;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.model.LoginHistory;
import com.studygrind.model.User;
import com.studygrind.repository.CourseEnrollmentRepository;
import com.studygrind.repository.CourseRepository;
import com.studygrind.repository.LoginHistoryRepository;
import com.studygrind.repository.UserRepository;
import com.studygrind.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Value("${app.upload.dir.student-cards:uploads/student-cards}")
    private String studentCardUploadDir;

    private final ConcurrentHashMap<String, PasswordResetToken> resetTokens = new ConcurrentHashMap<>();
    private final Random random = new Random();

    private static class PasswordResetToken {
        String code;
        LocalDateTime expiry;
        String email;

        PasswordResetToken(String code, String email) {
            this.code = code;
            this.email = email;
            this.expiry = LocalDateTime.now().plusMinutes(15);
        }

        boolean isValid() { return LocalDateTime.now().isBefore(expiry); }
        String getCode() { return code; }
        LocalDateTime getExpiry() { return expiry; }
        String getEmail() { return email; }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredResetTokens() {
        int beforeSize = resetTokens.size();
        resetTokens.entrySet().removeIf(entry -> !entry.getValue().isValid());
        int afterSize = resetTokens.size();
        if (beforeSize > afterSize) {
            System.out.println("Cleaned up " + (beforeSize - afterSize) + " expired password reset tokens");
        }
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (request.getStudentNumber() != null && userRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new RuntimeException("Student number already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername() != null ? request.getUsername() : request.getEmail().split("@")[0]);
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setRole(Constants.ROLE_STUDENT);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setStudentNumber(request.getStudentNumber());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth().atStartOfDay());
        }
        user.setEmailVerified(false);
        user.setVerificationStatus("pending");
        user.setIsActive(false); // Inactive until verified

        return userRepository.save(user);
    }

    @Transactional
    public User registerUserWithVerification(RegisterRequest request, MultipartFile studentCardFile) throws IOException {
        if (studentCardFile == null || studentCardFile.isEmpty()) {
            throw new RuntimeException("Student card image is required for registration");
        }

        String contentType = studentCardFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files (JPEG, PNG) are accepted for student card");
        }

        if (studentCardFile.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Student card image must be less than 5MB");
        }

        User user = registerUser(request);

        String studentCardPath = saveStudentCardImage(studentCardFile, user.getId());
        user.setStudentCardPath(studentCardPath);
        user.setStudentCardFileName(studentCardFile.getOriginalFilename());
        user.setVerificationStatus("pending");
        user.setIsActive(false);

        User savedUser = userRepository.save(user);

        // Notify admins (in production, this would send real notifications)
        System.out.println("📋 New student registration pending verification: " + savedUser.getFullName() + " (" + savedUser.getEmail() + ")");

        // Send email to user
        emailService.sendRegistrationPendingEmail(user.getEmail(), user.getFullName());

        return savedUser;
    }

    private String saveStudentCardImage(MultipartFile file, Long userId) throws IOException {
        String workingDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(workingDir, studentCardUploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Created student cards directory: " + uploadPath.toAbsolutePath());
        }

        String extension = "";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = "student_card_" + userId + "_" + System.currentTimeMillis() + extension;
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        return studentCardUploadDir + "/" + filename;
    }

    @Transactional
    public User verifyStudent(Long studentId, Long adminId, boolean approve, String notes) {
        User student = findById(studentId);

        if (!"student".equals(student.getRole())) {
            throw new RuntimeException("Only student accounts can be verified");
        }

        if (approve) {
            student.setVerificationStatus("approved");
            student.setVerifiedAt(LocalDateTime.now());
            student.setVerifiedBy(adminId);
            student.setVerificationNotes(notes);
            student.setIsActive(true);

            emailService.sendRegistrationApprovedEmail(student.getEmail(), student.getFullName());
            notificationService.sendNotification(studentId,
                    "✅ Your account has been approved! You can now log in and start learning.",
                    "verification");

            System.out.println("✅ Student verified: " + student.getFullName() + " by admin " + adminId);
        } else {
            student.setVerificationStatus("rejected");
            student.setVerificationNotes(notes);
            student.setIsActive(false);

            emailService.sendRegistrationRejectedEmail(student.getEmail(), student.getFullName(), notes);

            System.out.println("❌ Student verification rejected: " + student.getFullName());
        }

        return userRepository.save(student);
    }

    @Transactional(readOnly = true)
    public List<User> getPendingVerificationStudents() {
        return userRepository.findByRoleAndVerificationStatus("student", "pending");
    }

    @Transactional(readOnly = true)
    public byte[] getStudentCardImage(Long studentId) throws IOException {
        User student = findById(studentId);

        if (student.getStudentCardPath() == null) {
            throw new RuntimeException("No student card image found");
        }

        String workingDir = System.getProperty("user.dir");
        Path filePath = Paths.get(workingDir, student.getStudentCardPath());

        if (!Files.exists(filePath)) {
            throw new RuntimeException("Student card image not found at: " + filePath.toAbsolutePath());
        }

        return Files.readAllBytes(filePath);
    }

    @Transactional
    public User resubmitStudentCard(Long userId, MultipartFile studentCardFile) throws IOException {
        User user = findById(userId);

        if (!"student".equals(user.getRole())) {
            throw new RuntimeException("Only students can submit student cards");
        }

        if (studentCardFile == null || studentCardFile.isEmpty()) {
            throw new RuntimeException("Student card image is required");
        }

        String contentType = studentCardFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files (JPEG, PNG) are accepted");
        }

        if (studentCardFile.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Student card image must be less than 5MB");
        }

        if (user.getStudentCardPath() != null) {
            String workingDir = System.getProperty("user.dir");
            Path oldPath = Paths.get(workingDir, user.getStudentCardPath());
            Files.deleteIfExists(oldPath);
        }

        String studentCardPath = saveStudentCardImage(studentCardFile, user.getId());
        user.setStudentCardPath(studentCardPath);
        user.setStudentCardFileName(studentCardFile.getOriginalFilename());
        user.setVerificationStatus("pending");
        user.setIsActive(false);

        User savedUser = userRepository.save(user);

        System.out.println("📋 Student resubmitted card for verification: " + savedUser.getFullName());
        emailService.sendRegistrationPendingEmail(user.getEmail(), user.getFullName());

        return savedUser;
    }

    @Transactional
    public User registerUserWithCourses(RegisterRequest request) {
        User user = registerUser(request);

        if (request.getCourseIds() != null && !request.getCourseIds().isEmpty()) {
            for (Long courseId : request.getCourseIds()) {
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

                CourseEnrollment enrollment = new CourseEnrollment(user, course);
                enrollment.startTrial();
                enrollmentRepository.save(enrollment);
            }
        }

        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());

        return user;
    }

    public boolean verifyEmail(String token) {
        String email = emailService.verifyEmail(token);
        if (email == null || email.isEmpty()) return false;

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        user.setEmailVerified(true);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public void recordLoginHistory(Long userId, HttpServletRequest request) {
        User user = findById(userId);
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        user.setLastLoginIp(ipAddress);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        LoginHistory history = new LoginHistory(ipAddress, userAgent, user);
        loginHistoryRepository.save(history);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Transactional(readOnly = true)
    public List<LoginHistory> getUserLoginHistory(Long userId) {
        return loginHistoryRepository.findByUserIdOrderByLoginTimeDesc(userId);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = findById(userId);
        user.setIsActive(false);
        user.setEmail(user.getEmail() + "_deleted_" + System.currentTimeMillis());
        userRepository.save(user);
    }

    @Transactional
    public void changeEmail(Long userId, String newEmail, String password) {
        User user = findById(userId);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email already in use");
        }

        user.setEmail(newEmail);
        user.setEmailVerified(false);
        userRepository.save(user);

        String token = emailService.createVerificationToken(newEmail);
        emailService.sendVerificationEmail(newEmail, user.getFullName(), token);
    }

    @Transactional
    public User createTeacher(String fullName, String email, String username, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User teacher = new User();
        teacher.setUsername(username);
        teacher.setEmail(email);
        teacher.setFullName(fullName);
        teacher.setRole(Constants.ROLE_TEACHER);
        teacher.setPassword(passwordEncoder.encode(password));
        teacher.setCreatedAt(LocalDateTime.now());
        teacher.setEmailVerified(true);
        teacher.setVerificationStatus("approved");
        teacher.setIsActive(true);

        return userRepository.save(teacher);
    }

    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request, String requestingRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"admin".equals(requestingRole) && !user.getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this user");
        }

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getStudentNumber() != null && Constants.ROLE_STUDENT.equals(user.getRole())) {
            user.setStudentNumber(request.getStudentNumber());
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address"));

        String code = String.format("%06d", random.nextInt(1000000));
        PasswordResetToken token = new PasswordResetToken(code, email);
        resetTokens.put(email, token);

        emailService.sendPasswordResetEmail(email, code);

        return code;
    }

    @Transactional
    public void verifyAndResetPassword(String email, String code, String newPassword) {
        PasswordResetToken token = resetTokens.get(email);

        if (token == null) {
            throw new RuntimeException("No password reset request found. Please request a new code.");
        }

        if (!token.isValid()) {
            resetTokens.remove(email);
            throw new RuntimeException("Reset code has expired. Please request a new one.");
        }

        if (!token.getCode().equals(code)) {
            throw new RuntimeException("Invalid verification code. Please check and try again.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokens.remove(email);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllTeachers() {
        List<User> teachers = userRepository.findByRole("teacher");
        List<UserResponse> responses = new ArrayList<>();
        for (User teacher : teachers) {
            responses.add(convertToUserResponse(teacher));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllStudents() {
        List<User> students = userRepository.findByRole(Constants.ROLE_STUDENT);
        List<UserResponse> responses = new ArrayList<>();
        for (User student : students) {
            responses.add(convertToUserResponse(student));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUserUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setFullName(user.getFullName());
        response.setCreatedAt(user.getCreatedAt());
        response.setStudentNumber(user.getStudentNumber());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setAddress(user.getAddress());
        response.setEmailVerified(user.getEmailVerified());
        response.setVerificationStatus(user.getVerificationStatus());
        return response;
    }
}