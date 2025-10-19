package com.example.taller1.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.taller1.security.infraestructure.LockingAuthProvider;

/**
 * Configuración central de seguridad para la aplicación.
 *
 * Esta clase expone un {@link SecurityFilterChain} que configura:
 * - CSRF (deshabilitado para facilitar pruebas con Postman/cURL en este
 *   taller educativo).
 * - Cabeceras de seguridad: Content Security Policy y frame options.
 * - Reglas de autorización por rutas (endpoints públicos y protegidos).
 * - Proveedor de autenticación personalizado ({@link LockingAuthProvider}).
 * - Form login con manejadores de éxito/fracaso que devuelven códigos HTTP
 *   simples (200/401) — útil para APIs y pruebas.
 *
 * Nota: en entornos de producción se recomienda revisar CSRF y los handlers
 * para que devuelvan respuestas más completas o redirecciones según la UI.
 */
@Configuration
@EnableMethodSecurity
public class AppSecurity {

    /**
     * Proveedor de autenticación que implementa bloqueo por intentos fallidos.
     * Se inyecta para que Spring Security lo use cuando se realice
     * la autenticación.
     */
    private final LockingAuthProvider lockingAuthProvider;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public AppSecurity(LockingAuthProvider lockingAuthProvider, JwtAuthenticationFilter jwtAuthFilter) {
        this.lockingAuthProvider = lockingAuthProvider;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Define la cadena de filtros de seguridad.
     *
     * Comportamiento clave:
     * - CSRF deshabilitado: facilita llamadas con herramientas como Postman.
     * - CSP: "default-src 'self'" restringe recursos a la misma origen.
     * - frameOptions: permite frames desde el mismo origen (sameOrigin).
     * - Autorización por rutas:
     *   - {@code /auth/**} y {@code /error} son públicos.
     *   - {@code /admin/**} requiere el role ADMIN.
     *   - Cualquier otra petición requiere autenticación.
     * - Se registra el {@code lockingAuthProvider} como proveedor de
     *   autenticación para soportar bloqueo de cuentas tras intentos fallidos.
     * - Form login con handlers que devuelven códigos 200 (éxito) y 401
     *   (fracaso). Esto es útil para integraciones sin interfaz web.
     *
     * @param http objeto para configurar seguridad HTTP
     * @return la cadena de filtros construida
     * @throws Exception si la configuración falla
     */
    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .frameOptions(fo -> fo.sameOrigin())
                )
                // <-- API sin estado: no sesiones HTTP
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Solo estos públicos
                        .requestMatchers("/auth/login", "/auth/register", "/error").permitAll()
                        // CORS preflight opcional si tienes front externo:
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/notes/**").authenticated()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(lockingAuthProvider);

        // Filtro JWT antes del de username/password
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

