package com.example.taller1.note.infra;


import com.example.taller1.note.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);
}

