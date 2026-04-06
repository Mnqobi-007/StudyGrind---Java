package com.studygrind.config;

import com.studygrind.model.User;
import com.studygrind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Value("${app.production:false}")
    private boolean isProduction;

    private UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (isProduction) {
            return;
        }

        Optional<User> existingAdmin = userRepository.findByEmail("admin@studygrind.com");
        if (existingAdmin.isPresent()) {
            return;
        }

        org.springframework.security.crypto.password.PasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@studygrind.com");
        admin.setPasswordHash(encoder.encode("admin123"));
        admin.setFullName("System Administrator");
        admin.setRole("admin");
        userRepository.save(admin);

        User teacher = new User();
        teacher.setUsername("harrison");
        teacher.setEmail("harrison@studygrind.com");
        teacher.setPasswordHash(encoder.encode("teacher123"));
        teacher.setFullName("Prof. Harrison");
        teacher.setRole("teacher");
        userRepository.save(teacher);

        User student = new User();
        student.setUsername("alex");
        student.setEmail("alex@studygrind.com");
        student.setPasswordHash(encoder.encode("student123"));
        student.setFullName("Alex Smith");
        student.setRole("student");
        userRepository.save(student);

        System.out.println("Sample data initialized successfully.");
    }
}
