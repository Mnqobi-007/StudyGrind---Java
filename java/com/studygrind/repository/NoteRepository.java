package com.studygrind.repository;

import com.studygrind.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByTeacherId(Long teacherId);
}
