package ar.edu.cent35.biblioteca.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize en los controladores (RNF-SEC-02)
public class WebAuthorization {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Pantallas (HTML, CSS, JS e imágenes): públicas. Los datos los protege la API.
                .requestMatchers("/", "/Login/**", "/comun/**", "/Portal-Gestion/**",
                        "/Portal-Usuario/**", "/favicon.ico", "/web/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/login", "/api/logout").permitAll()
                // Consulta pública del catálogo, según el alcance del sistema.
                // Los archivos digitales y los ejemplares siguen exigiendo sesión.
                .requestMatchers(HttpMethod.GET, "/api/libros", "/api/libros/{id}",
                        "/api/materias", "/api/publico/**").permitAll()
                // Gestión de usuarios: solo administrador
                .requestMatchers("/api/usuarios/**").hasAuthority("ROLE_ADMIN")
                // El resto de la API exige sesión; el rol fino va en @PreAuthorize
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll())
            .formLogin(form -> form
                .usernameParameter("mail")
                .passwordParameter("clave")
                .loginProcessingUrl("/api/login")
                .successHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))
                .failureHandler((req, res, exc) -> {
                    int status;
                    String mensaje;
                    if (exc instanceof LockedException) {
                        status = HttpStatus.LOCKED.value();
                        mensaje = "Cuenta bloqueada por intentos fallidos. Contacte al administrador.";
                    } else if (exc instanceof DisabledException) {
                        status = HttpStatus.FORBIDDEN.value();
                        mensaje = "Usuario inactivo.";
                    } else {
                        status = HttpStatus.UNAUTHORIZED.value();
                        mensaje = "Credenciales inválidas.";
                    }
                    escribirError(res, status, mensaje);
                }))
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            // Permite mostrar los PDF dentro de una página del mismo sitio (visor del portal)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    // Se usa setStatus y no sendError para que el código no se pierda en el despacho a /error.
    private void escribirError(HttpServletResponse res, int status, String mensaje) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"error\":\"" + mensaje + "\"}");
    }
}
