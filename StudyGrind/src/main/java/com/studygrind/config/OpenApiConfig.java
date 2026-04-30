package com.studygrind.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studyGrindOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StudyGrind API")
                        .description("""
                                StudyGrind Learning Management System API
                                
                                ## Authentication
                                Use the `/api/auth/login` endpoint to obtain a JWT token.
                                Include the token in the `Authorization` header as: `Bearer {token}`
                                
                                ## Features
                                - Student registration with ID card verification
                                - Course enrollment with 14-day free trials
                                - Assignment submission and grading
                                - Quiz taking and auto-grading
                                - Payment processing (PayFast integration)
                                - Real-time notifications
                                - WhatsApp group access management
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("StudyGrind Support")
                                .email("support@studygrind.com")
                                .url("https://studygrind.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://studygrind.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.studygrind.com").description("Production Server")))
                .tags(Arrays.asList(
                        new Tag().name("Authentication").description("User authentication and registration"),
                        new Tag().name("Courses").description("Course management and enrollment"),
                        new Tag().name("Assignments").description("Assignment creation and submission"),
                        new Tag().name("Quizzes").description("Quiz creation and taking"),
                        new Tag().name("Billing").description("Payment and subscription management"),
                        new Tag().name("Admin").description("Administrative functions"),
                        new Tag().name("Verification").description("Student ID verification"),
                        new Tag().name("WhatsApp").description("WhatsApp group access management"),
                        new Tag().name("Notifications").description("User notifications"),
                        new Tag().name("Timetable").description("Schedule management"),
                        new Tag().name("Textbooks").description("Textbook management"),
                        new Tag().name("Notes").description("Study notes management"),
                        new Tag().name("Announcements").description("Course announcements")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .schemaRequirement("bearerAuth", new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter your JWT token"));
    }
}