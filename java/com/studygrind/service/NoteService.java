package com.studygrind.service;

import com.studygrind.dto.CreateNoteRequest;
import com.studygrind.model.Note;
import com.studygrind.model.User;
import com.studygrind.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    private NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<Note> getAllNotes(User user) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (user.isAdmin()) {
            return noteRepository.findAll();
        }

        if (user.isTeacher()) {
            return noteRepository.findByTeacherId(user.getId());
        }

        return noteRepository.findAll();
    }

    public Note getNoteById(Long id) {
        if (id == null) {
            throw new RuntimeException("Note ID is required");
        }

        Optional<Note> note = noteRepository.findById(id);
        if (!note.isPresent()) {
            throw new RuntimeException("Note not found with id: " + id);
        }

        return note.get();
    }

    public Note createNote(CreateNoteRequest request, User user) {
        if (request == null) {
            throw new RuntimeException("Request body is required");
        }

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("Note title is required");
        }

        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setTeacherId(user.getId());

        return noteRepository.save(note);
    }

    public void deleteNote(Long id, User user) {
        if (id == null) {
            throw new RuntimeException("Note ID is required");
        }

        Note note = getNoteById(id);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.isAdmin() && !note.getTeacherId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this note");
        }

        noteRepository.delete(note);
    }
}
