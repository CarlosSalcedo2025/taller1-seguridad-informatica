package com.example.taller1.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.example.taller1.security.infraestructure.LockingAuthProvider;

@Configuration
@EnableMethodSecurity
public class AppSecurity {

    private final LockingAuthProvider lockingAuthProvider;

    public AppSecurity(LockingAuthProvider lockingAuthProvider) {
        this.lockingAuthProvider = lockingAuthProvider;
    }

    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // para Postman/cURL
                .headers(h -> h
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .frameOptions(fo -> fo.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(lockingAuthProvider)
                .formLogin(login -> login
                        .successHandler((req, res, auth) -> res.setStatus(200))
                        .failureHandler((req, res, ex) -> res.setStatus(401))
                )
                .logout(Customizer.withDefaults());
        return http.build();
    }
}

