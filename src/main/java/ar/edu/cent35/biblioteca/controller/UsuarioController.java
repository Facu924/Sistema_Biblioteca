package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.DesbloqueoResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.dto.UsuarioCreateRequest;
import ar.edu.cent35.biblioteca.dto.UsuarioResponse;
import ar.edu.cent35.biblioteca.dto.UsuarioUpdateRequest;
import ar.edu.cent35.biblioteca.model.enums.TipoUsuario;
import ar.edu.cent35.biblioteca.service.SeguridadCuentaService;
import ar.edu.cent35.biblioteca.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SeguridadCuentaService seguridadCuentaService;

    public UsuarioController(UsuarioService usuarioService, SeguridadCuentaService seguridadCuentaService) {
        this.usuarioService = usuarioService;
        this.seguridadCuentaService = seguridadCuentaService;
    }

    /** HU-01: alta con rol y perfil institucional. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioCreateRequest req,
                                 Authentication auth, HttpServletRequest request) {
        return usuarioService.crear(req, auth.getName(), request.getRemoteAddr());
    }

    /** RF-ADM-03: listado con filtros opcionales y paginación. */
    @GetMapping
    public PaginaResponse<UsuarioResponse> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) TipoUsuario rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Boolean bloqueado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        return usuarioService.buscar(texto, rol, activo, bloqueado, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    /** RF-ADM-04: edición de datos, rol, perfil y (opcional) contraseña. */
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody UsuarioUpdateRequest req,
                                      Authentication auth, HttpServletRequest request) {
        return usuarioService.actualizar(id, req, auth.getName(), request.getRemoteAddr());
    }

    /** HU-02: baja lógica. */
    @PostMapping("/{id}/baja")
    public UsuarioResponse baja(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
        return usuarioService.darDeBaja(id, auth.getName(), request.getRemoteAddr());
    }

    @PostMapping("/{id}/reactivar")
    public UsuarioResponse reactivar(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
        return usuarioService.reactivar(id, auth.getName(), request.getRemoteAddr());
    }

    /** HU-03: desbloquea la cuenta y, opcionalmente, genera una contraseña temporal. */
    @PostMapping("/{id}/desbloquear")
    public DesbloqueoResponse desbloquear(@PathVariable Long id,
                                          @RequestParam(defaultValue = "false") boolean claveTemporal,
                                          Authentication auth, HttpServletRequest request) {
        String clave = seguridadCuentaService.desbloquear(
                id, claveTemporal, auth.getName(), request.getRemoteAddr());
        return new DesbloqueoResponse(id, clave);
    }
}
