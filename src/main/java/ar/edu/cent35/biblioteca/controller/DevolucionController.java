package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.DevolucionRequest;
import ar.edu.cent35.biblioteca.dto.DevolucionResponse;
import ar.edu.cent35.biblioteca.service.DevolucionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devoluciones")
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    /** HU-06: devuelve el material, actualiza el stock y, si corresponde, penaliza. */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_BIBLIOTECARIA')")
    public DevolucionResponse devolver(@Valid @RequestBody DevolucionRequest req,
                                       Authentication auth, HttpServletRequest request) {
        return devolucionService.devolver(req, auth.getName(), request.getRemoteAddr());
    }
}
