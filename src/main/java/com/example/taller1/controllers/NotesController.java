package com.example.taller1.controllers;


import com.example.taller1.entity.Note;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notes")
public class NotesController {

    private final com.example.taller1.services.NotesService notesService;

    public NotesController(com.example.taller1.services.NotesService notesService) {
        this.notesService = notesService;
    }

    public record NoteReq(@NotBlank String title, @NotBlank String content) {}

    @PostMapping
    /**
     * Crea una nueva nota para el usuario autenticado.
     *
     * @param in body con `title` y `content` (ambos no vacíos)
     * @param auth authentication principal con el email en `auth.getName()`
     * @return 201 Created con el id de la nueva nota
     */
    public ResponseEntity<?> create(@Valid @RequestBody NoteReq in, Authentication auth) {
        var n = notesService.create(in.title(), in.content(), auth.getName());
        return ResponseEntity.status(201).body(Map.of("id", n.getId()));
    }

    @GetMapping
    /**
     * Devuelve la lista de notas del usuario autenticado, ordenadas por creación descendente.
     *
     * @param auth authentication principal
     * @return lista de notas del usuario
     */
    public List<Note> myNotes(Authentication auth) {
        return notesService.myNotes(auth.getName());
    }

    @GetMapping("/{id}")
    /**
     * Recupera una nota por id solo si el usuario autenticado es el propietario.
     *
     * @param id id de la nota
     * @param auth authentication principal
     * @return la nota solicitada
     */
    public Note getOne(@PathVariable Long id, Authentication auth) {
        return notesService.getOne(id, auth.getName());
    }

    @PutMapping("/{id}")
    /**
     * Actualiza una nota existente (title y content) si el usuario es el propietario.
     *
     * @param id id de la nota
     * @param in body con nuevos `title` y `content`
     * @param auth authentication principal
     * @return la nota actualizada
     */
    public Note update(@PathVariable Long id, @Valid @RequestBody NoteReq in, Authentication auth) {
        return notesService.update(id, in.title(), in.content(), auth.getName());
    }

    @DeleteMapping("/{id}")
    /**
     * Elimina una nota por id si el usuario autenticado es el propietario.
     *
     * @param id id de la nota a eliminar
     * @param auth authentication principal
     * @return 204 No Content en caso de éxito
     */
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        notesService.delete(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
