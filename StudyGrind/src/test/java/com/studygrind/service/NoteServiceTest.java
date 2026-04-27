package com.studygrind.service;

import com.studygrind.dto.request.NoteRequest;
import com.studygrind.dto.response.NoteResponse;
import com.studygrind.model.Course;
import com.studygrind.model.Note;
import com.studygrind.model.User;
import com.studygrind.repository.CourseRepository;
import com.studygrind.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private NoteService noteService;

    private User testTeacher;
    private Course testCourse;
    private Note testNote;
    private NoteRequest noteRequest;

    @BeforeEach
    void setUp() {
        testTeacher = new User();
        testTeacher.setId(1L);
        testTeacher.setFullName("Test Teacher");

        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setName("Test Course");
        testCourse.setTeacher(testTeacher);

        testNote = new Note();
        testNote.setId(1L);
        testNote.setTitle("Test Note");
        testNote.setContent("Test Content");
        testNote.setSubject("Testing");
        testNote.setAuthor(testTeacher);
        testNote.setCourse(testCourse);

        noteRequest = new NoteRequest();
        noteRequest.setTitle("New Note");
        noteRequest.setContent("New Content");
        noteRequest.setSubject("New Subject");
        noteRequest.setCourseId(1L);
    }

    @Test
    void createNote_ShouldCreateNote_WhenValidRequest() {
        when(userService.findById(1L)).thenReturn(testTeacher);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        NoteResponse result = noteService.createNote(noteRequest, 1L, 1L);

        assertNotNull(result);
        assertEquals("Test Note", result.getTitle());
        assertEquals("Test Teacher", result.getTeacher());
    }

    @Test
    void createNote_ShouldThrowException_WhenNotCourseTeacher() {
        User differentTeacher = new User();
        differentTeacher.setId(2L);
        testCourse.setTeacher(differentTeacher);
        
        when(userService.findById(1L)).thenReturn(testTeacher);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        assertThrows(RuntimeException.class, () -> noteService.createNote(noteRequest, 1L, 1L));
    }

    @Test
    void getNoteDetails_ShouldReturnNote_WhenTeacherIsAuthor() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        NoteResponse result = noteService.getNoteDetails(1L, 1L, "teacher");

        assertNotNull(result);
        assertEquals("Test Note", result.getTitle());
    }

    @Test
    void getNoteDetails_ShouldThrowException_WhenTeacherNotAuthor() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        assertThrows(RuntimeException.class, () -> noteService.getNoteDetails(1L, 99L, "teacher"));
    }

    @Test
    void deleteNote_ShouldDeleteNote_WhenTeacherIsAuthor() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));
        doNothing().when(noteRepository).delete(testNote);

        assertDoesNotThrow(() -> noteService.deleteNote(1L, 1L, "teacher"));
        
        verify(noteRepository).delete(testNote);
    }

    @Test
    void deleteNote_ShouldThrowException_WhenTeacherNotAuthor() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(testNote));

        assertThrows(RuntimeException.class, () -> noteService.deleteNote(1L, 99L, "teacher"));
    }
}