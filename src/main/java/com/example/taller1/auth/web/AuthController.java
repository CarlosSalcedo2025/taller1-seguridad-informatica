package com.example.taller1.auth.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taller1.note.domain.Msg;
import com.example.taller1.security.app.JwtService;
import com.example.taller1.user.infra.UserRepository;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
	@Autowired 
	UserRepository repo;
	@Autowired
	PasswordEncoder encoder;
	@Autowired
	JwtService jwtService; 

	@PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        var userOpt = repo.findByEmail(body.get("email"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(new Msg("Usuario no encontrado"));
        }

        var user = userOpt.get();
        if (!encoder.matches(body.get("password"), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(new Msg("Contraseña incorrecta"));
        }
        
        String token = jwtService.generarToken(user.getEmail());

        ResponseCookie cookie = ResponseCookie.from("JWT", token)
                .httpOnly(true)
                .path("/")
                .maxAge(3600)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(new Msg("Login exitoso"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(new Msg("Logout exitoso"));
    }
}
