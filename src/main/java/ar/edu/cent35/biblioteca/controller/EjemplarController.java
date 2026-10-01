package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.CambioEstadoRequest;
import ar.edu.cent35.biblioteca.dto.EjemplarRequest;
import ar.edu.cent35.biblioteca.dto.EjemplarResponse;
import ar.edu.cent35.biblioteca.service.EjemplarService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ejemplares")
public class EjemplarController {

    private static final String BIBLIOTECARIA = "hasAuthority('ROLE_BIBLIOTECARIA')";
    private static final String PERSONAL = "hasAnyAuthority('ROLE_BIBLIOTECARIA','ROLE_ADMIN')";

    private final EjemplarService ejemplarService;

    public EjemplarController(EjemplarService ejemplarService) {
        this.ejemplarService = ejemplarService;
    }

    /** Búsqueda por el número que lee el lector de códigos de barras. */
    @GetMapping("/inventario/{numero}")
    @PreAuthorize(PERSONAL)
    public EjemplarResponse porInventario(@PathVariable String numero) {
        return ejemplarService.buscarPorInventario(numero);
    }

    @PutMapping("/{id}")
    @PreAuthorize(BIBLIOTECARIA)
    public EjemplarResponse actualizar(@PathVariable Long id, @Valid @RequestBody EjemplarRequest req) {
        return ejemplarService.actualizar(id, req);
    }

    /** Pasar a DISPONIBLE, DANADO, EXTRAVIADO o BAJA_LOGICA (expurgo por donación o reciclaje). */
    @PostMapping("/{id}/estado")
    @PreAuthorize(BIBLIOTECARIA)
    public EjemplarResponse cambiarEstado(@PathVariable Long id,
                                          @Valid @RequestBody CambioEstadoRequest req,
                                          Authentication auth, HttpServletRequest request) {
        return ejemplarService.cambiarEstado(id, req, auth.getName(), request.getRemoteAddr());
    }
}
