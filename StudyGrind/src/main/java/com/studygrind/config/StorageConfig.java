package com.studygrind.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class StorageConfig {

    @Value("${app.upload.dir.textbooks:uploads/textbooks}")
    private String textbooksUploadDir;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            // Get project root directory (where the app is running from)
            String workingDir = System.getProperty("user.dir");
            System.out.println("Working directory: " + workingDir);

            // Create main upload directory
            Path uploadPath = Paths.get(workingDir, uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            } else {
                System.out.println("Upload directory already exists: " + uploadPath.toAbsolutePath());
            }

            // Create textbooks directory
            Path textbooksPath = Paths.get(workingDir, textbooksUploadDir);
            if (!Files.exists(textbooksPath)) {
                Files.createDirectories(textbooksPath);
                System.out.println("Created textbooks directory: " + textbooksPath.toAbsolutePath());
            } else {
                System.out.println("Textbooks directory already exists: " + textbooksPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to create upload directories: " + e.getMessage());
        }
    }
}