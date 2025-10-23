package com.example.taller1.security.app;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String SECRET_KEY = "SantiHallysPalomaMurcia2320202025062945"; 
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // Generar token
    public String generarToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hora
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Obtener usuario del token
    public String extraerUsername(String token) {
        return getClaims(token).getSubject();
    }

    // Validar token
    public boolean validarToken(String token, String username) {
        return username.equals(extraerUsername(token)) && !estaExpirado(token);
    }

    private boolean estaExpirado(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}