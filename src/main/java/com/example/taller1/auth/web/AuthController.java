package com.example.taller1.auth.web;

import com.example.taller1.security.infra.JwtService;
import com.example.taller1.user.domain.User;
import com.example.taller1.user.infra.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    // ==== Request/Response records visibles para este controlador ====
    public record RegisterReq(@Email String email, @Size(min = 10) String password, boolean admin) {}
    public record LoginReq(@Email String email, @Size(min = 1) String password) {}
    public record TokenRes(String token) {}
    public record Msg(String message) {}
    // ==================================================================

    public AuthController(UserRepository repo,
                          PasswordEncoder encoder,
                          AuthenticationManager authManager,
                          JwtService jwt) {
        this.repo = repo;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
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

    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq in) {
        try {
            var auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(in.email(), in.password())
            );
            var token = jwt.generate(
                    auth.getName(),
                    Map.of("roles", auth.getAuthorities().stream().map(Object::toString).toList())
            );
            return ResponseEntity.ok(new TokenRes(token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(new Msg("Credenciales inválidas"));
        }
    }


    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        return Map.of(
                "user",  auth == null ? null : auth.getName(),
                "roles", auth == null ? List.of() : auth.getAuthorities().stream().map(Object::toString).toList()
        );
    }
}
