package com.example.taller1.services;

import com.example.taller1.dto.LoginRequest;
import com.example.taller1.dto.RegisterRequest;
import com.example.taller1.dto.TokenResponse;
import com.example.taller1.entity.User;
import com.example.taller1.entity.Token;
import com.example.taller1.repository.TokenRepository;
import com.example.taller1.repository.UserRepository;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public record Msg(String message) {}
    public record RegisterReq(String email, String password, boolean admin) {}

    public AuthService( UserRepository repo, 
                        PasswordEncoder encoder, 
                        TokenRepository tokenRepository, 
                        UserRepository userRepository,
                        JwtService jwtService,
                        AuthenticationManager authenticationManager) 
    {
        this.repo = repo;
        this.encoder = encoder;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // método de registro de usuario
    public TokenResponse register(final RegisterRequest request) {
        // If a user with the same email already exists, return 409 Conflict
        if (repo.findByEmail(request.email()).isPresent())
            return new TokenResponse(null, null, "Ya existe un usuario con ese correo electrónico");

        if (!request.password().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{10,}$"))
            return new TokenResponse(null, null, "La contraseña debe tener al menos 10 caracteres, incluyendo una letra mayúscula y un número");

        var u = new User();
        u.setEmail(request.email());
        u.setPasswordHash(encoder.encode(request.password()));
        u.setRoles(request.admin() ? "ROLE_USER,ROLE_ADMIN" : "ROLE_USER");
        repo.save(u);

        final User savedUser = repo.save(u);
        final String jwtToken = jwtService.generateToken(savedUser);
        final String refreshToken = jwtService.generateRefreshToken(savedUser);

        saveUserToken(savedUser, jwtToken);

        return new TokenResponse(jwtToken, refreshToken, "Usuario registrado correctamente");
    }

    // método de login que genera y retorna tokens JWT
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return new TokenResponse(jwtToken, refreshToken, "Login exitoso");
    }

    // guardamos el token en la base de datos
    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
            .user(user)
            .token(jwtToken)
            .tokenType(Token.TokenType.BEARER)
            .expired(false)
            .revoked(false)
            .build();
        tokenRepository.save(token);
    }
 
    // revocamos todos los tokens válidos del usuario
    private void revokeAllUserTokens(final User user) {
        final List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());

        if (!validUserTokens.isEmpty()) {
            for (final Token token : validUserTokens) {
                token.setExpired(true);
                token.setRevoked(true);
            }
            tokenRepository.saveAll(validUserTokens);
        }
    }

    public TokenResponse refreshToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Bearer token");
        }

        final String refreshToken = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        final User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException(userEmail));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }

        final String accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        
        return new TokenResponse(accessToken, refreshToken, "Token refrescado correctamente");
    }

    // método para obtener información del usuario autenticado
    public Map<String, Object> me(org.springframework.security.core.Authentication auth) {
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
