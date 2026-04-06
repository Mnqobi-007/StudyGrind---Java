package com.studygrind.controller;

import com.studygrind.config.SessionHelper;
import com.studygrind.dto.CreateNoteRequest;
import com.studygrind.model.Note;
import com.studygrind.model.User;
import com.studygrind.service.AuthService;
import com.studygrind.service.NoteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private NoteService noteService;
    private SessionHelper sessionHelper;
    private AuthService authService;

    @Autowired
    public NoteController(NoteService noteService, SessionHelper sessionHelper, AuthService authService) {
        this.noteService = noteService;
        this.sessionHelper = sessionHelper;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        List<Note> notes = noteService.getAllNotes(user);
        return ResponseEntity.ok(notes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Long id, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        Note note = noteService.getNoteById(id);
        return ResponseEntity.ok(note);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createNote(@RequestBody CreateNoteRequest request, HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }
        User user = authService.getCurrentUser(email);
        if (!user.isTeacher() && !user.isAdmin()) {
            return ResponseEntity.status(403).body(Map.of("error", "Only teachers and admins can create notes"));
        }
        Note note = noteService.createNote(request, user);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", note.getId());
        return ResponseEntity.ok(result);
    }
}
