package com.example.taller1.security.infraestructure;


import com.example.taller1.security.app.LoginAttemptService;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class LockingAuthProvider implements AuthenticationProvider {

    private final UserDetailsService uds;
    private final PasswordEncoder encoder;
    private final LoginAttemptService attempts;

    public LockingAuthProvider(UserDetailsService uds, PasswordEncoder encoder, LoginAttemptService attempts) {
        this.uds = uds;
        this.encoder = encoder;
        this.attempts = attempts;
    }

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        var user = auth.getName();
        if (attempts.isBlocked(user)) {
            throw new LockedException("Bloqueado temporalmente por intentos fallidos");
        }

        var details = uds.loadUserByUsername(user);
        var raw = auth.getCredentials() == null ? "" : auth.getCredentials().toString();

        if (!encoder.matches(raw, details.getPassword())) {
            attempts.onFailure(user);
            throw new BadCredentialsException("Credenciales inválidas");
        }

        attempts.onSuccess(user);
        return new UsernamePasswordAuthenticationToken(
                details.getUsername(), details.getPassword(), details.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(aClass);
    }
}

