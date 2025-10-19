package com.example.taller1.config;


import com.example.taller1.entity.User;
import com.example.taller1.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase de configuración que inserta datos iniciales en la base de datos.
 *
 * Este componente define un bean de tipo {@link CommandLineRunner} que se
 * ejecuta al iniciar la aplicación Spring Boot. Su propósito es asegurar que
 * exista un usuario administrador por defecto (email: {@code admin@demo.com}).
 *
 * Comportamiento:
 * - Busca un usuario con el email {@code admin@demo.com} usando
 *   {@link UserRepository#findByEmail(String)}.
 * - Si el usuario no existe, crea uno nuevo con una contraseña encriptada y
 *   roles {@code ROLE_USER,ROLE_ADMIN} y lo guarda en el repositorio.
 *
 * Notas de seguridad:
 * - La contraseña por defecto se define en claro en el código para propósitos
 *   de demostración. En entornos de producción, cambie esta contraseña
 *   inmediatamente o use un mecanismo seguro para inyectar secretos
 *   (variables de entorno, vault, etc.).
 */
@Configuration
public class SampleData {

    /**
     * Bean que inicializa datos de ejemplo en el arranque.
     *
     * @param repo repositorio de usuarios usado para buscar/guardar el usuario
     * @param enc  encoder de contraseñas inyectado por Spring Security
     * @return un {@link CommandLineRunner} que inserta el usuario admin si no existe
     */
    @Bean
    CommandLineRunner seed(UserRepository repo, PasswordEncoder enc) {
        return args -> repo.findByEmail("admin@demo.com").orElseGet(() -> {
            // Crear usuario por defecto
            var u = new User();
            u.setEmail("admin@demo.com");
            // Guardar la contraseña en forma encriptada
            u.setPasswordHash(enc.encode("P4ssw0rd+Larga"));
            // Asignar roles separados por coma
            u.setRoles("ROLE_USER,ROLE_ADMIN");
            return repo.save(u);
        });
    }
}
