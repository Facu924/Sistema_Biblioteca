package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.ActualizarMailRequest;
import ar.edu.cent35.biblioteca.dto.ActualizarTelefonoRequest;
import ar.edu.cent35.biblioteca.dto.CambiarContrasenaRequest;
import ar.edu.cent35.biblioteca.dto.UsuarioResponse;
import ar.edu.cent35.biblioteca.service.PerfilService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** RF-GEN-01: cualquier usuario autenticado gestiona solo su propio perfil. */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public UsuarioResponse ver(Authentication auth) {
        return perfilService.obtener(auth.getName());
    }

    @PutMapping("/telefono")
    public UsuarioResponse telefono(@Valid @RequestBody ActualizarTelefonoRequest req, Authentication auth) {
        return perfilService.actualizarTelefono(auth.getName(), req.telefono());
    }

    @PutMapping("/contrasena")
    public Map<String, String> contrasena(@Valid @RequestBody CambiarContrasenaRequest req,
                                          Authentication auth, HttpServletRequest request) {
        perfilService.cambiarContrasena(auth.getName(), req.contrasenaActual(),
                req.contrasenaNueva(), request.getRemoteAddr());
        return Map.of("mensaje", "Contraseña actualizada");
    }

    /** El mail es el nombre de usuario de la sesión, así que se cierra la sesión y hay que volver a entrar. */
    @PutMapping("/mail")
    public Map<String, String> mail(@Valid @RequestBody ActualizarMailRequest req,
                                    Authentication auth, HttpServletRequest request) {
        perfilService.cambiarMail(auth.getName(), req.contrasenaActual(), req.mail(), request.getRemoteAddr());
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        return Map.of("mensaje", "Correo actualizado. Vuelva a iniciar sesión con el nuevo correo.");
    }
}
