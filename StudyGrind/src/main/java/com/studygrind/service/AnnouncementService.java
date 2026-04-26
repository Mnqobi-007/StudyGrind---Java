package com.studygrind.service;

import com.studygrind.model.Announcement;
import com.studygrind.model.Course;
import com.studygrind.model.User;
import com.studygrind.repository.AnnouncementRepository;
import com.studygrind.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnnouncementService {
    
    @Autowired
    private AnnouncementRepository announcementRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public Announcement createAnnouncement(Long courseId, Long teacherId, String title, String content) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        User teacher = userService.findById(teacherId);
        
        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("Only course teacher can post announcements");
        }
        
        Announcement announcement = new Announcement(title, content, teacher, course);
        Announcement saved = announcementRepository.save(announcement);
        
        // Send notification to all enrolled students
        notificationService.sendAnnouncement(courseId, title, content);
        
        return saved;
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCourseAnnouncementsAsMap(Long courseId) {
        List<Announcement> announcements = announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Announcement a : announcements) {
            Map<String, Object> annMap = new HashMap<>();
            annMap.put("id", a.getId());
            annMap.put("title", a.getTitle());
            annMap.put("content", a.getContent());
            annMap.put("createdAt", a.getCreatedAt());
            
            // Only put teacher info that is safe (not lazy-loaded)
            if (a.getTeacher() != null) {
                Map<String, Object> teacherMap = new HashMap<>();
                teacherMap.put("id", a.getTeacher().getId());
                teacherMap.put("fullName", a.getTeacher().getFullName());
                teacherMap.put("email", a.getTeacher().getEmail());
                annMap.put("teacher", teacherMap);
            }
            
            if (a.getCourse() != null) {
                annMap.put("courseId", a.getCourse().getId());
                annMap.put("courseName", a.getCourse().getName());
            }
            
            result.add(annMap);
        }
        return result;
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllAnnouncementsForStudentAsMap(Long studentId) {
        List<Announcement> announcements = announcementRepository.findAnnouncementsForStudent(studentId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Announcement a : announcements) {
            Map<String, Object> annMap = new HashMap<>();
            annMap.put("id", a.getId());
            annMap.put("title", a.getTitle());
            annMap.put("content", a.getContent());
            annMap.put("createdAt", a.getCreatedAt());
            
            // Only put teacher info that is safe (not lazy-loaded)
            if (a.getTeacher() != null) {
                Map<String, Object> teacherMap = new HashMap<>();
                teacherMap.put("id", a.getTeacher().getId());
                teacherMap.put("fullName", a.getTeacher().getFullName());
                teacherMap.put("email", a.getTeacher().getEmail());
                annMap.put("teacher", teacherMap);
            }
            
            if (a.getCourse() != null) {
                annMap.put("courseId", a.getCourse().getId());
                annMap.put("courseName", a.getCourse().getName());
            }
            
            result.add(annMap);
        }
        return result;
    }
    
    // Legacy methods for backward compatibility
    @Transactional(readOnly = true)
    public List<Announcement> getCourseAnnouncements(Long courseId) {
        return announcementRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
    }
    
    @Transactional(readOnly = true)
    public List<Announcement> getAllAnnouncementsForStudent(Long studentId) {
        return announcementRepository.findAnnouncementsForStudent(studentId);
    }
    
    @Transactional
    public void deleteAnnouncement(Long announcementId, Long teacherId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new RuntimeException("Announcement not found"));
        
        if (!announcement.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You can only delete your own announcements");
        }
        
        announcementRepository.delete(announcement);
    }
}