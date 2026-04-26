package com.studygrind.config;

import com.studygrind.model.User;
import com.studygrind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Create admin user if not exists
        if (!userRepository.existsByEmail("admin@studygrind.com")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@studygrind.com");
            admin.setFullName("System Administrator");
            admin.setRole("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            System.out.println("✓ Admin user created: admin@studygrind.com / admin123");
        }
        
        // Create teacher user if not exists
        if (!userRepository.existsByEmail("teacher@studygrind.com")) {
            User teacher = new User();
            teacher.setUsername("prof_harrison");
            teacher.setEmail("teacher@studygrind.com");
            teacher.setFullName("Prof. Harrison");
            teacher.setRole("teacher");
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            userRepository.save(teacher);
            System.out.println("✓ Teacher user created: teacher@studygrind.com / teacher123");
        }
        
        // Create student user if not exists
        if (!userRepository.existsByEmail("student@studygrind.com")) {
            User student = new User();
            student.setUsername("alex_smith");
            student.setEmail("student@studygrind.com");
            student.setFullName("Alex Smith");
            student.setRole("student");
            student.setPassword(passwordEncoder.encode("student123"));
            userRepository.save(student);
            System.out.println("✓ Student user created: student@studygrind.com / student123");
        }
        
        System.out.println("========================================");
        System.out.println("Database initialization completed!");
        System.out.println("========================================");
    }
}