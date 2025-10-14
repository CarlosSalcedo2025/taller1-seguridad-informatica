package com.example.taller1.config;

import com.example.taller1.security.infra.JwtAuthFilter;
import com.example.taller1.security.infra.LockingAuthProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class AppSecurity {

    private final LockingAuthProvider lockingAuthProvider;
    private final JwtAuthFilter jwtFilter;

    public AppSecurity(LockingAuthProvider lockingAuthProvider, JwtAuthFilter jwtFilter) {
        this.lockingAuthProvider = lockingAuthProvider;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(lockingAuthProvider);
    }

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
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
