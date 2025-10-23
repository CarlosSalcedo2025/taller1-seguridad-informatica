package com.example.taller1.auth.web;

import com.example.taller1.security.jwt.JwtUtil;
import com.example.taller1.user.domain.User;
import com.example.taller1.user.infra.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    // --- Registro de usuarios ---
    public record RegisterReq(@Email String email, @Size(min = 10) String password, boolean admin) {}
    public record Msg(String message) {}

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
        return ResponseEntity.status(201).body(new Msg("Usuario registrado"));
    }

    // --- Login (genera token JWT) ---
    public record LoginReq(@Email String email, @Size(min = 3) String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq loginReq) {
        try {
            // Autenticar usuario (usa LockingAuthProvider internamente)
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginReq.email(), loginReq.password())
            );

            // Si la autenticación fue exitosa, generar token JWT
            String token = jwtUtil.generateToken(loginReq.email());

            // Devolver token en el cuerpo y en el header
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(Map.of(
                            "token", token,
                            "tokenType", "Bearer",
                            "user", loginReq.email()
                    ));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Msg("Credenciales inválidas"));
        }
    }

    // --- Perfil actual (/auth/me) ---
    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        boolean logged = auth != null && auth.isAuthenticated();
        return Map.of(
                "authenticated", logged,
                "user", logged ? auth.getName() : "",
                "roles", logged
                        ? auth.getAuthorities().stream().map(Object::toString).toList()
                        : List.of()
        );
    }
}
