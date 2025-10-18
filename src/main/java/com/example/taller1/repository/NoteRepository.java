package com.example.taller1.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taller1.entity.Note;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);
}

