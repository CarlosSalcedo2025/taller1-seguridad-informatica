package com.example.taller1.controllers;


import com.example.taller1.entity.Note;
import com.example.taller1.repository.NoteRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notes")
public class NotesController {

    private final NoteRepository notes;

    public NotesController(NoteRepository notes) {
        this.notes = notes;
    }

    public record NoteReq(@NotBlank String title, @NotBlank String content) {}

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody NoteReq in, Authentication auth) {
        var n = new Note();
        n.setTitle(in.title());
        n.setContent(in.content());
        n.setOwnerEmail(auth.getName());
        notes.save(n);
        return ResponseEntity.status(201).body(Map.of("id", n.getId()));
    }

    @GetMapping
    public List<Note> myNotes(Authentication auth) {
        return notes.findByOwnerEmailOrderByCreatedAtDesc(auth.getName());
    }

    @GetMapping("/{id}")
    public Note getOne(@PathVariable Long id, Authentication auth) {
        var n = notes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!n.getOwnerEmail().equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return n;
    }

    @PutMapping("/{id}")
    public Note update(@PathVariable Long id, @Valid @RequestBody NoteReq in, Authentication auth) {
        var n = notes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!n.getOwnerEmail().equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        n.setTitle(in.title());
        n.setContent(in.content());
        return notes.save(n);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        var n = notes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!n.getOwnerEmail().equals(auth.getName()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        notes.delete(n);
        return ResponseEntity.noContent().build();
    }
}
