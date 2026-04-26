package com.studygrind.service;

import com.studygrind.dto.request.TimetableRequest;
import com.studygrind.dto.response.TimetableResponse;
import com.studygrind.model.Timetable;
import com.studygrind.model.User;
import com.studygrind.model.CourseEnrollment;
import com.studygrind.repository.TimetableRepository;
import com.studygrind.repository.CourseEnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimetableService {

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @Transactional
    public Timetable createEntry(TimetableRequest request, Long studentId) {
        User student = userService.findById(studentId);

        // Check for conflict
        checkForConflict(studentId, request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), null);

        Timetable entry = new Timetable();
        entry.setStudent(student);
        entry.setDayOfWeek(request.getDayOfWeek());
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setSubject(request.getSubject());
        entry.setLocation(request.getLocation());
        entry.setNotes(request.getNotes());
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());

        return timetableRepository.save(entry);
    }

    @Transactional
    public Timetable createTeacherEntry(TimetableRequest request, Long teacherId) {
        User teacher = userService.findById(teacherId);

        if (!"teacher".equals(teacher.getRole()) && !"admin".equals(teacher.getRole())) {
            throw new RuntimeException("Only teachers can create timetable entries");
        }

        // Check for conflict for teacher
        checkForConflict(teacherId, request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), null);

        Timetable entry = new Timetable();
        entry.setStudent(teacher);
        entry.setDayOfWeek(request.getDayOfWeek());
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setSubject(request.getSubject());
        entry.setLocation(request.getLocation());
        entry.setNotes(request.getNotes());
        entry.setCreatedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());

        return timetableRepository.save(entry);
    }

    private void checkForConflict(Long userId, String dayOfWeek, String startTime, String endTime, Long excludeId) {
        List<Timetable> existingEntries = timetableRepository.findByUserIdAndDayOfWeek(userId, dayOfWeek);

        for (Timetable existing : existingEntries) {
            if (excludeId != null && existing.getId().equals(excludeId)) {
                continue;
            }

            if (isTimeOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime())) {
                throw new RuntimeException("Time slot conflicts with existing entry: " +
                        existing.getSubject() + " (" + existing.getStartTime() + " - " + existing.getEndTime() + ")");
            }
        }
    }

    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        return !(end1.compareTo(start2) <= 0 || start1.compareTo(end2) >= 0);
    }

    @Transactional
    public Timetable updateEntry(Long entryId, TimetableRequest request, Long userId) {
        Timetable entry = timetableRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Timetable entry not found with id: " + entryId));

        if (!entry.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only edit your own timetable entries");
        }

        checkForConflict(userId, request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), entryId);

        entry.setDayOfWeek(request.getDayOfWeek());
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setSubject(request.getSubject());
        entry.setLocation(request.getLocation());
        entry.setNotes(request.getNotes());
        entry.setUpdatedAt(LocalDateTime.now());

        return timetableRepository.save(entry);
    }

    @Transactional
    public void deleteEntry(Long entryId, Long userId) {
        Timetable entry = timetableRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Timetable entry not found with id: " + entryId));

        if (!entry.getStudent().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own timetable entries");
        }

        timetableRepository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<TimetableResponse> getEntriesForStudent(Long studentId) {
        User student = userService.findById(studentId);
        List<Timetable> entries = timetableRepository.findByStudent(student);
        return entries.stream()
                .map(this::convertToTimetableResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimetableResponse> getTeacherTimetable(Long teacherId) {
        User teacher = userService.findById(teacherId);
        List<Timetable> entries = timetableRepository.findByStudent(teacher);
        return entries.stream()
                .map(this::convertToTimetableResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimetableResponse> getStudentTimetableView(Long studentId) {
        User student = userService.findById(studentId);
        List<Timetable> studentEntries = timetableRepository.findByStudent(student);

        // Get all courses the student is enrolled in
        List<CourseEnrollment> enrollments = enrollmentRepository.findByStudent(student);
        List<Timetable> teacherEntries = new ArrayList<>();

        for (CourseEnrollment enrollment : enrollments) {
            if (enrollment.getCourse() != null && enrollment.getCourse().getTeacher() != null) {
                List<Timetable> teacherSchedule = timetableRepository.findByStudent(enrollment.getCourse().getTeacher());
                teacherEntries.addAll(teacherSchedule);
            }
        }

        List<Timetable> allEntries = new ArrayList<>();
        allEntries.addAll(studentEntries);
        allEntries.addAll(teacherEntries);

        // Remove duplicates based on ID
        allEntries = allEntries.stream()
                .distinct()
                .collect(Collectors.toList());

        return allEntries.stream()
                .map(this::convertToTimetableResponse)
                .collect(Collectors.toList());
    }

    private TimetableResponse convertToTimetableResponse(Timetable entry) {
        TimetableResponse response = new TimetableResponse();
        response.setId(entry.getId());
        response.setDayOfWeek(entry.getDayOfWeek());
        response.setStartTime(entry.getStartTime());
        response.setEndTime(entry.getEndTime());
        response.setSubject(entry.getSubject());
        response.setLocation(entry.getLocation());
        response.setNotes(entry.getNotes());
        response.setCreatedAt(entry.getCreatedAt());
        response.setTeacherName(entry.getStudent().getFullName());
        response.setTeacherRole(entry.getStudent().getRole());
        return response;
    }
}