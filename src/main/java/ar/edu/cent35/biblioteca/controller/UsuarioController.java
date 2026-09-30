package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.DesbloqueoResponse;
import ar.edu.cent35.biblioteca.service.SeguridadCuentaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UsuarioController {

    private final SeguridadCuentaService seguridadCuentaService;

    public UsuarioController(SeguridadCuentaService seguridadCuentaService) {
        this.seguridadCuentaService = seguridadCuentaService;
    }

    /** HU-03: desbloquea la cuenta y, opcionalmente, genera una contraseña temporal. */
    @PostMapping("/{id}/desbloquear")
    public DesbloqueoResponse desbloquear(@PathVariable Long id,
                                          @RequestParam(defaultValue = "false") boolean claveTemporal,
                                          Authentication authentication,
                                          HttpServletRequest request) {
        String clave = seguridadCuentaService.desbloquear(
                id, claveTemporal, authentication.getName(), request.getRemoteAddr());
        return new DesbloqueoResponse(id, clave);
    }
}
