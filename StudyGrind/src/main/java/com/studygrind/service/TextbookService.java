package com.studygrind.service;

import com.studygrind.dto.response.TextbookResponse;
import com.studygrind.model.Course;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.model.Textbook;
import com.studygrind.model.User;
import com.studygrind.repository.CourseEnrollmentRepository;
import com.studygrind.repository.CourseRepository;
import com.studygrind.repository.TextbookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TextbookService {

    @Autowired
    private TextbookRepository textbookRepository;

    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Autowired
    private UserService userService;

    @Value("${app.upload.dir.textbooks:uploads/textbooks}")
    private String uploadDir;

    @Transactional
    public Textbook uploadTextbook(MultipartFile file, String title, String description, Long courseId, Long teacherId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select a file to upload");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("Title is required");
        }

        if (courseId == null) {
            throw new RuntimeException("Course ID is required");
        }

        User teacher = userService.findById(teacherId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You can only upload textbooks to your own course");
        }

        String workingDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(workingDir, uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(filename);

        file.transferTo(filePath.toFile());
        System.out.println("File saved to: " + filePath.toAbsolutePath());

        String relativePath = uploadDir + "/" + filename;

        Textbook textbook = new Textbook();
        textbook.setTitle(title);
        textbook.setDescription(description != null ? description : "");
        textbook.setFilePath(relativePath);
        textbook.setFileName(originalFilename);
        textbook.setFileSize(file.getSize());
        textbook.setUploadedBy(teacher);
        textbook.setCourse(course);
        textbook.setUploadDate(LocalDateTime.now());

        return textbookRepository.save(textbook);
    }

    @Transactional(readOnly = true)
    public List<TextbookResponse> getTextbooksForCourse(Long courseId) {
        return textbookRepository.findByCourseId(courseId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TextbookResponse getTextbookDetails(Long textbookId) {
        Textbook textbook = textbookRepository.findById(textbookId)
                .orElseThrow(() -> new RuntimeException("Textbook not found"));
        return convertToResponse(textbook);
    }

    @Transactional(readOnly = true)
    public byte[] downloadTextbook(Long textbookId, Long userId, String userRole) throws IOException {
        Textbook textbook = textbookRepository.findById(textbookId)
                .orElseThrow(() -> new RuntimeException("Textbook not found"));
        
        // Teachers can always download their own textbooks
        if ("teacher".equals(userRole) || "admin".equals(userRole)) {
            if (textbook.getUploadedBy().getId().equals(userId) || "admin".equals(userRole)) {
                // Teacher or admin can download
                String workingDir = System.getProperty("user.dir");
                Path filePath = Paths.get(workingDir, textbook.getFilePath());
                if (!Files.exists(filePath)) {
                    throw new RuntimeException("File not found at: " + filePath.toAbsolutePath());
                }
                return Files.readAllBytes(filePath);
            }
        }
        
        // For students, verify enrollment
        if (userId != null) {
            User student = userService.findById(userId);
            Optional<CourseEnrollment> enrollment = enrollmentRepository.findByStudentAndCourse(student, textbook.getCourse());
            
            if (enrollment.isEmpty() || enrollment.get().getIsEnrolled() == null || !enrollment.get().getIsEnrolled()) {
                throw new RuntimeException("You are not enrolled in this course. Please enroll to access textbooks.");
            }
            
            // Check if trial is active or subscription is active
            CourseEnrollment ce = enrollment.get();
            boolean canAccess = ce.isTrialActive() || 
                               (ce.getSubscriptionActive() != null && ce.getSubscriptionActive());
            
            if (!canAccess) {
                throw new RuntimeException("Your trial has expired. Please subscribe to access textbooks.");
            }
        } else {
            throw new RuntimeException("Please login to download textbooks");
        }

        String workingDir = System.getProperty("user.dir");
        Path filePath = Paths.get(workingDir, textbook.getFilePath());

        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found at: " + filePath.toAbsolutePath());
        }

        return Files.readAllBytes(filePath);
    }

    private TextbookResponse convertToResponse(Textbook textbook) {
        TextbookResponse response = new TextbookResponse();
        response.setId(textbook.getId());
        response.setTitle(textbook.getTitle());
        response.setDescription(textbook.getDescription());
        response.setFileName(textbook.getFileName());
        response.setFileSize(textbook.getFileSize());
        response.setUploadedBy(textbook.getUploadedBy().getFullName());
        response.setCourseName(textbook.getCourse() != null ? textbook.getCourse().getName() : "General");
        response.setUploadDate(textbook.getUploadDate());
        response.setDownloadUrl("/api/textbooks/download/" + textbook.getId());
        return response;
    }
}