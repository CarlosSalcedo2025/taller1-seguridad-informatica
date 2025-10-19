package com.example.taller1.services;

import com.example.taller1.entity.Note;
import com.example.taller1.repository.NoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class NotesService {

    private final NoteRepository notes;

    public NotesService(NoteRepository notes) {
        this.notes = notes;
    }

    public Note create(String title, String content, String ownerEmail) {
        var n = new Note();
        n.setTitle(title);
        n.setContent(content);
        n.setOwnerEmail(ownerEmail);
        notes.save(n);
        return n;
    }

    /**
     * Devuelve las notas del usuario ordenadas por fecha de creación (desc).
     *
     * @param ownerEmail email del propietario
     * @return lista de notas del usuario
     */
    public List<Note> myNotes(String ownerEmail) {
        return notes.findByOwnerEmailOrderByCreatedAtDesc(ownerEmail);
    }

    /**
     * Recupera una nota por id verificando que el usuario sea el propietario.
     *
     * Lanza 404 si no existe y 403 si el propietario no coincide.
     *
     * @param id id de la nota
     * @param ownerEmail email del usuario que solicita la nota
     * @return la nota encontrada
     */
    public Note getOne(Long id, String ownerEmail) {
        var n = notes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!n.getOwnerEmail().equals(ownerEmail)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return n;
    }

    /**
     * Actualiza el título y contenido de una nota tras verificar propiedad.
     *
     * Se lanza 404 si no existe y 403 si no es el propietario.
     *
     * @param id id de la nota
     * @param title nuevo título
     * @param content nuevo contenido
     * @param ownerEmail email del usuario que realiza la petición
     * @return nota actualizada
     */
    public Note update(Long id, String title, String content, String ownerEmail) {
        var n = notes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!n.getOwnerEmail().equals(ownerEmail)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        n.setTitle(title);
        n.setContent(content);
        return notes.save(n);
    }

    /**
     * Elimina una nota si el email provisto coincide con el propietario.
     *
     * Lanza 404 si no existe y 403 si no coincide el propietario.
     *
     * @param id id de la nota a eliminar
     * @param ownerEmail email del usuario que intenta eliminar
     */
    public void delete(Long id, String ownerEmail) {
        var n = notes.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!n.getOwnerEmail().equals(ownerEmail)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        notes.delete(n);
    }
}
