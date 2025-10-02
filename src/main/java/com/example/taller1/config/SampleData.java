package com.example.taller1.config;


import com.example.taller1.user.domain.User;
import com.example.taller1.user.infra.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SampleData {
    @Bean
    CommandLineRunner seed(UserRepository repo, PasswordEncoder enc) {
        return args -> repo.findByEmail("admin@demo.com").orElseGet(() -> {
            var u = new User();
            u.setEmail("admin@demo.com");
            u.setPasswordHash(enc.encode("P4ssw0rd+Larga"));
            u.setRoles("ROLE_USER,ROLE_ADMIN");
            return repo.save(u);
        });
    }
}
