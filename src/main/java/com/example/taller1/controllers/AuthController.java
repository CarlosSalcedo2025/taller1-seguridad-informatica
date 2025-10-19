package com.example.taller1.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.taller1.dto.LoginRequest;
import com.example.taller1.dto.TokenResponse;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final com.example.taller1.services.AuthService authService;

    public record RegisterReq(@Email String email, @Size(min = 10) String password, boolean admin) {
    }

    public record Msg(String message) {
    }

    public AuthController(com.example.taller1.services.AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    /**
     * Registra un nuevo usuario.
     * <p>
     * Valida el request usando las anotaciones de `RegisterReq` y delega la creación y
     * validación adicional al servicio `AuthService`.
     *
     * Respuestas posibles:
     * - 201 Created: usuario creado correctamente (body: {"message":"OK"})
     * - 400 Bad Request: password no cumple la política (body: {"message":"Password débil"})
     * - 409 Conflict: el email ya existe (body: {"message":"Ya existe"})
     *
     * @param in datos de registro (email, password, admin)
     * @return ResponseEntity con código y mensaje correspondiente
     */
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReq in) {
        return authService.register(new com.example.taller1.services.AuthService.RegisterReq(in.email(), in.password(), in.admin()));
    }

    @GetMapping("/me")
    /**
     * Devuelve información del usuario autenticado.
     *
     * El endpoint devuelve un objeto con las claves: `authenticated`, `user` y `roles`.
     * Si no hay autenticación, `authenticated` es false y `user` es cadena vacía.
     *
     * @param auth objeto de Spring Security (puede ser null)
     * @return mapa con estado de autenticación y roles
     */
    public Map<String, Object> me(Authentication auth) {
        return authService.me(auth);
    }

    @GetMapping("/login")
    // Endpoint de login.
    public ResponseEntity<TokenResponse> login(@RequestBody final LoginRequest request) {
        final TokenResponse token = authService.login(request);
        return ResponseEntity.ok(token);
    }
}

