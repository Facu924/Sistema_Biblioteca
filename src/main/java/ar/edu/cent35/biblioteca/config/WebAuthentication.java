package ar.edu.cent35.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Autenticación: Spring Security usa automáticamente el bean UsuarioDetailsService
 * junto con este PasswordEncoder (RNF-SEC-01: hashing BCrypt).
 */
@Configuration
public class WebAuthentication {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
