package com.example.taller1.auth.web;


import com.example.taller1.user.domain.User;
import com.example.taller1.user.infra.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public record RegisterReq(@Email String email, @Size(min = 10) String password, boolean admin) {
    }

    public record Msg(String message) {
    }

    public AuthController(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReq in) {
        if (repo.findByEmail(in.email()).isPresent())
            return ResponseEntity.status(409).body(new Msg("Ya existe"));

        if (!in.password().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{10,}$"))
            return ResponseEntity.badRequest().body(new Msg("Password débil"));

        var u = new User();
        u.setEmail(in.email());
        u.setPasswordHash(encoder.encode(in.password()));
        u.setRoles(in.admin() ? "ROLE_USER,ROLE_ADMIN" : "ROLE_USER");
        repo.save(u);
        return ResponseEntity.status(201).body(new Msg("OK"));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        boolean logged = auth != null;
        return Map.of(
                "authenticated", logged,
                "user", logged ? auth.getName() : "",
                "roles", logged
                        ? auth.getAuthorities().stream().map(Object::toString).toList()
                        : List.of()
        );
    }
}

