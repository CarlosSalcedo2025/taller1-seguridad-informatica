package com.example.taller1.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.taller1.dto.LoginRequest;
import com.example.taller1.dto.RegisterRequest;
import com.example.taller1.dto.TokenResponse;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final com.example.taller1.services.AuthService authService;

    public record RegisterReq(@Email String email, @Size(min = 10) String password, boolean admin) {
    }

    public record Msg(String message) {
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterRequest request) {
        final TokenResponse response = authService.register(request);
        return ResponseEntity.ok(response);
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

    @PostMapping("/login")
    // Endpoint de login.
    public ResponseEntity<TokenResponse> login(@RequestBody final LoginRequest request) {
        final TokenResponse token = authService.login(request);
        return ResponseEntity.ok(token);
    }
}

