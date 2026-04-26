package com.studygrind.service;

import com.studygrind.repository.AssignmentRepository;
import com.studygrind.repository.CourseRepository;
import com.studygrind.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class UpdatesService {
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    public boolean hasUpdatesSince(Long sinceTimestamp, Long userId) {
        LocalDateTime since = LocalDateTime.ofInstant(Instant.ofEpochMilli(sinceTimestamp), ZoneId.systemDefault());
        
        // Check for new assignments
        long newAssignments = assignmentRepository.count();
        
        // Check for new submissions (for teacher)
        long newSubmissions = submissionRepository.findPendingSubmissionsForTeacher(userId).size();
        
        // Check for course updates
        long newCourses = courseRepository.count();
        
        return newAssignments > 0 || newSubmissions > 0 || newCourses > 0;
    }
}