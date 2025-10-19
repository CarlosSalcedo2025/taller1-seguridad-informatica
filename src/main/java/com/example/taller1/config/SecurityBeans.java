package com.example.taller1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.taller1.repository.UserRepository;

/**
 * - Un {@link PasswordEncoder} basado en BCrypt para encriptar contraseñas.
 * - Un {@link UserDetailsService} que carga usuarios desde la base de datos
 *   usando {@link UserRepository} y adapta la entidad a la representación que usa Spring Security.
 */
@Configuration
public class SecurityBeans {

    // Bean para el AuthenticationManager que gestiona la autenticación.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    /**
     * Bean para encriptar contraseñas usando BCrypt.
     * Parámetros:
     * - 12 rounds (factor de trabajo) ofrece un buen equilibrio entre
     *   seguridad y rendimiento para este taller.
     * @return PasswordEncoder que aplica BCrypt
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Servicio para cargar detalles de usuario a partir del repositorio.
     *
     * Comportamiento:
     * - Busca un usuario por email (se usa el email como username).
     * - Si existe, construye un {@link org.springframework.security.core.userdetails.User}
     *   con el email, la contraseña ya encriptada y las autoridades (roles).
     * - Si no existe, lanza {@link UsernameNotFoundException}.
     *
     * Nota: la entidad almacena roles como una cadena separada por comas
     * (por ejemplo: "ROLE_USER,ROLE_ADMIN"). Aquí se separan por comas y
     * se eliminan espacios en blanco alrededor.
     */
    @Bean
    UserDetailsService userDetailsService(UserRepository repo) {
        return username -> repo.findByEmail(username)
                .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getEmail())
                        // La contraseña almacenada ya está hasheada en la DB
                        .password(u.getPasswordHash())
                        // Convertir "ROLE_USER,ROLE_ADMIN" -> array de authorities
                        .authorities(u.getRoles().trim().split("\\s*,\\s*"))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("No existe el usuario"));
    }

    // Proveedor de autenticación que usa el UserDetailsService y el PasswordEncoder definidos.
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
