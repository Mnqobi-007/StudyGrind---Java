package com.studygrind.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studygrind.dto.request.LoginRequest;
import com.studygrind.dto.request.RegisterRequest;
import com.studygrind.security.JwtTokenProvider;
import com.studygrind.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @Test
    void login_ShouldReturnOk_WhenValidRequest() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("student@studygrind.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void register_ShouldReturnOk_WhenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("New Student");
        request.setEmail("new@studygrind.com");
        request.setPassword("password123");
        request.setStudentNumber("STU99999");
        request.setPhoneNumber("+27 123 456 789");
        request.setAddress("123 Test St");

        MockMultipartFile userPart = new MockMultipartFile("user", "", "application/json", 
                objectMapper.writeValueAsString(request).getBytes());
        MockMultipartFile cardFile = new MockMultipartFile("studentCard", "card.jpg", "image/jpeg", 
                "fake image content".getBytes());

        mockMvc.perform(multipart("/api/auth/register")
                .file(userPart)
                .file(cardFile))
                .andExpect(status().isOk());
    }
}