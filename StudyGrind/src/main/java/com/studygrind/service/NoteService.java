package com.studygrind.service;

import com.studygrind.dto.request.NoteRequest;
import com.studygrind.dto.response.NoteResponse;
import com.studygrind.model.Course;
import com.studygrind.model.Note;
import com.studygrind.model.User;
import com.studygrind.repository.CourseRepository;
import com.studygrind.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Transactional
    public NoteResponse createNote(NoteRequest request, Long teacherId, Long courseId) {
        User teacher = userService.findById(teacherId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You can only add notes to your own course");
        }

        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setSubject(request.getSubject());
        note.setAuthor(teacher);
        note.setCourse(course);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        Note savedNote = noteRepository.save(note);
        System.out.println("✅ Note created: ID=" + savedNote.getId() + ", Title=" + savedNote.getTitle());

        return convertToResponse(savedNote);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesForStudent(Long studentId, Long courseId) {
        List<Course> enrolledCourses = courseRepository.findCoursesByStudentId(studentId);
        boolean isEnrolled = enrolledCourses.stream().anyMatch(c -> c.getId().equals(courseId));

        if (!isEnrolled) {
            throw new RuntimeException("You are not enrolled in this course");
        }

        return noteRepository.findByCourseId(courseId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getNotesForTeacher(Long teacherId, Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID cannot be null");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new RuntimeException("You can only view notes for your own course");
        }

        return noteRepository.findByCourseId(courseId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotesForStudent(Long studentId) {
        List<Course> enrolledCourses = courseRepository.findCoursesByStudentId(studentId);
        List<NoteResponse> allNotes = new java.util.ArrayList<>();

        System.out.println("=== DEBUG: Fetching notes for student: " + studentId);
        System.out.println("Enrolled courses count: " + enrolledCourses.size());

        if (enrolledCourses.isEmpty()) {
            System.out.println("Student has no enrolled courses");
            return allNotes;
        }

        for (Course course : enrolledCourses) {
            System.out.println("Processing course: " + course.getId() + " - " + course.getName());
            List<Note> notes = noteRepository.findByCourseId(course.getId());
            System.out.println("  Found " + notes.size() + " notes for this course");

            for (Note note : notes) {
                allNotes.add(convertToResponse(note));
                System.out.println("  Added note: " + note.getTitle());
            }
        }

        System.out.println("Total notes returned: " + allNotes.size());
        return allNotes;
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteDetails(Long noteId, Long userId, String userRole) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        System.out.println("=== getNoteDetails: noteId=" + noteId + ", userId=" + userId + ", role=" + userRole);
        System.out.println("Note author ID: " + (note.getAuthor() != null ? note.getAuthor().getId() : "null"));
        System.out.println("Note course ID: " + (note.getCourse() != null ? note.getCourse().getId() : "null"));

        // TEACHER access: Can view their own notes or notes from their courses
        if ("teacher".equals(userRole) || "admin".equals(userRole)) {
            // Allow if teacher is the author OR teacher owns the course for this note
            boolean isAuthor = note.getAuthor() != null && note.getAuthor().getId().equals(userId);
            boolean isCourseTeacher = note.getCourse() != null && note.getCourse().getTeacher() != null
                    && note.getCourse().getTeacher().getId().equals(userId);

            if (isAuthor || isCourseTeacher) {
                System.out.println("✅ Teacher " + userId + " authorized to view note " + noteId);
                return convertToResponse(note);
            } else {
                System.out.println("❌ Teacher " + userId + " NOT authorized to view note " + noteId);
                throw new RuntimeException("You don't have permission to view this note");
            }
        }

        // STUDENT access: Must be enrolled in the course
        if ("student".equals(userRole)) {
            if (note.getCourse() == null) {
                throw new RuntimeException("Note is not associated with any course");
            }

            List<Course> enrolledCourses = courseRepository.findCoursesByStudentId(userId);
            boolean isEnrolled = enrolledCourses.stream().anyMatch(c -> c.getId().equals(note.getCourse().getId()));

            if (!isEnrolled) {
                System.out.println("❌ Student " + userId + " not enrolled in course " + note.getCourse().getId());
                throw new RuntimeException("You are not enrolled in the course for this note");
            }

            System.out.println("✅ Student " + userId + " authorized to view note " + noteId);
            return convertToResponse(note);
        }

        throw new RuntimeException("Unauthorized access to note");
    }

    @Transactional
    public void deleteNote(Long noteId, Long userId, String userRole) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + noteId));

        System.out.println("=== deleteNote: noteId=" + noteId + ", userId=" + userId + ", role=" + userRole);

        // TEACHER: Can delete their own notes
        if ("teacher".equals(userRole) || "admin".equals(userRole)) {
            if (!note.getAuthor().getId().equals(userId)) {
                throw new RuntimeException("You can only delete your own notes");
            }
            noteRepository.delete(note);
            System.out.println("✅ Teacher " + userId + " deleted note " + noteId);
            return;
        }

        // STUDENTS cannot delete notes
        throw new RuntimeException("You don't have permission to delete notes");
    }

    @Transactional
    public NoteResponse updateNote(Long noteId, NoteRequest request, Long teacherId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getAuthor().getId().equals(teacherId)) {
            throw new RuntimeException("You can only edit your own notes");
        }

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setSubject(request.getSubject());
        note.setUpdatedAt(LocalDateTime.now());

        return convertToResponse(noteRepository.save(note));
    }

    private NoteResponse convertToResponse(Note note) {
        NoteResponse response = new NoteResponse();
        response.setId(note.getId());
        response.setTitle(note.getTitle());
        response.setContent(note.getContent());
        response.setSubject(note.getSubject());

        // Safely get teacher name
        if (note.getAuthor() != null) {
            response.setTeacher(note.getAuthor().getFullName());
            response.setTeacherId(note.getAuthor().getId());
        } else {
            response.setTeacher("Unknown");
            response.setTeacherId(null);
        }

        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());

        if (note.getCourse() != null) {
            response.setCourseId(note.getCourse().getId());
            response.setCourseName(note.getCourse().getName());
        }

        return response;
    }
}