package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.SesionResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    /** Devuelve quién está logueado y con qué rol, para que el cliente arme su interfaz. */
    @GetMapping("/me")
    public SesionResponse me(Authentication authentication) {
        String rol = authentication.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority()).orElse("");
        return new SesionResponse(authentication.getName(), rol);
    }
}
