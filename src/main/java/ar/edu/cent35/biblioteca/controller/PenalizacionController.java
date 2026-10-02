package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.dto.PenalizacionRequest;
import ar.edu.cent35.biblioteca.dto.PenalizacionResponse;
import ar.edu.cent35.biblioteca.service.PenalizacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penalizaciones")
public class PenalizacionController {

    private static final String BIBLIOTECARIA = "hasAuthority('ROLE_BIBLIOTECARIA')";
    private static final String PERSONAL = "hasAnyAuthority('ROLE_BIBLIOTECARIA','ROLE_ADMIN')";

    private final PenalizacionService penalizacionService;

    public PenalizacionController(PenalizacionService penalizacionService) {
        this.penalizacionService = penalizacionService;
    }

    @GetMapping
    @PreAuthorize(PERSONAL)
    public PaginaResponse<PenalizacionResponse> listar(@RequestParam(required = false) String legajo,
                                                       @RequestParam(defaultValue = "false") boolean soloVigentes,
                                                       @RequestParam(defaultValue = "0") int pagina,
                                                       @RequestParam(defaultValue = "20") int tamanio) {
        return penalizacionService.listar(legajo, soloVigentes, pagina, tamanio);
    }

    /** Cada lector ve sus propias sanciones. */
    @GetMapping("/mias")
    @PreAuthorize("hasAuthority('ROLE_LECTOR')")
    public List<PenalizacionResponse> mias(Authentication auth) {
        return penalizacionService.mias(auth.getName());
    }

    /** Sanción manual, para los casos que no se resuelven en la devolución. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(BIBLIOTECARIA)
    public PenalizacionResponse crear(@Valid @RequestBody PenalizacionRequest req,
                                      Authentication auth, HttpServletRequest request) {
        return penalizacionService.crearManual(req, auth.getName(), request.getRemoteAddr());
    }

    @PostMapping("/{id}/levantar")
    @PreAuthorize(BIBLIOTECARIA)
    public PenalizacionResponse levantar(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
        return penalizacionService.levantar(id, auth.getName(), request.getRemoteAddr());
    }
}
