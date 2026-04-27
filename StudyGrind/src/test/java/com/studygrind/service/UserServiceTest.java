package com.studygrind.service;

import com.studygrind.dto.request.RegisterRequest;
import com.studygrind.model.User;
import com.studygrind.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setFullName("Test Student");
        validRequest.setEmail("test@studygrind.com");
        validRequest.setPassword("password123");
        validRequest.setStudentNumber("STU12345");
        validRequest.setPhoneNumber("+27 123 456 789");
        validRequest.setAddress("123 Test Street");
        validRequest.setDateOfBirth(LocalDate.of(2000, 1, 1));

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test Student");
        testUser.setEmail("test@studygrind.com");
        testUser.setRole("student");
        testUser.setVerificationStatus("pending");
    }

    @Test
    void registerUser_ShouldCreateUser_WhenValidData() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByStudentNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.registerUser(validRequest);

        assertNotNull(result);
        assertEquals("Test Student", result.getFullName());
        assertEquals("test@studygrind.com", result.getEmail());
        
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByEmail("test@studygrind.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUser(validRequest));
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.findById(1L);

        assertNotNull(result);
        assertEquals("Test Student", result.getFullName());
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.findById(99L));
    }
}