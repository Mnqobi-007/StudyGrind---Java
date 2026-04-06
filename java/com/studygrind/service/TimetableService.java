package com.studygrind.service;

import com.studygrind.dto.CreateTimetableRequest;
import com.studygrind.model.Timetable;
import com.studygrind.model.User;
import com.studygrind.repository.TimetableRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TimetableService {

    private TimetableRepository timetableRepository;

    public TimetableService(TimetableRepository timetableRepository) {
        this.timetableRepository = timetableRepository;
    }

    public List<Timetable> getTimetable(Long studentId) {
        if (studentId == null) {
            throw new RuntimeException("Student ID is required");
        }

        return timetableRepository.findByStudentId(studentId);
    }

    public Timetable createEntry(CreateTimetableRequest request, User user) {
        if (request == null) {
            throw new RuntimeException("Request body is required");
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (request.getDayOfWeek() == null || request.getDayOfWeek().isBlank()) {
            throw new RuntimeException("Day of week is required");
        }

        if (request.getStartTime() == null || request.getStartTime().isBlank()) {
            throw new RuntimeException("Start time is required");
        }

        if (request.getEndTime() == null || request.getEndTime().isBlank()) {
            throw new RuntimeException("End time is required");
        }

        if (request.getSubject() == null || request.getSubject().isBlank()) {
            throw new RuntimeException("Subject is required");
        }

        Timetable entry = new Timetable();
        entry.setStudentId(user.getId());
        entry.setDayOfWeek(request.getDayOfWeek());
        entry.setStartTime(request.getStartTime());
        entry.setEndTime(request.getEndTime());
        entry.setSubject(request.getSubject());
        entry.setLocation(request.getLocation());
        entry.setNotes(request.getNotes());

        return timetableRepository.save(entry);
    }

    public Timetable updateEntry(Long entryId, CreateTimetableRequest request, User user) {
        if (entryId == null) {
            throw new RuntimeException("Entry ID is required");
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (request == null) {
            throw new RuntimeException("Request body is required");
        }

        Optional<Timetable> entryOpt = timetableRepository.findById(entryId);
        if (!entryOpt.isPresent()) {
            throw new RuntimeException("Timetable entry not found with id: " + entryId);
        }

        Timetable entry = entryOpt.get();

        if (!user.isAdmin() && !entry.getStudentId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this timetable entry");
        }

        if (request.getDayOfWeek() != null && !request.getDayOfWeek().isBlank()) {
            entry.setDayOfWeek(request.getDayOfWeek());
        }

        if (request.getStartTime() != null && !request.getStartTime().isBlank()) {
            entry.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null && !request.getEndTime().isBlank()) {
            entry.setEndTime(request.getEndTime());
        }

        if (request.getSubject() != null && !request.getSubject().isBlank()) {
            entry.setSubject(request.getSubject());
        }

        if (request.getLocation() != null) {
            entry.setLocation(request.getLocation());
        }

        if (request.getNotes() != null) {
            entry.setNotes(request.getNotes());
        }

        return timetableRepository.save(entry);
    }

    public void deleteEntry(Long entryId, User user) {
        if (entryId == null) {
            throw new RuntimeException("Entry ID is required");
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Optional<Timetable> entryOpt = timetableRepository.findById(entryId);
        if (!entryOpt.isPresent()) {
            throw new RuntimeException("Timetable entry not found with id: " + entryId);
        }

        Timetable entry = entryOpt.get();

        if (!user.isAdmin() && !entry.getStudentId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this timetable entry");
        }

        timetableRepository.delete(entry);
    }
}
