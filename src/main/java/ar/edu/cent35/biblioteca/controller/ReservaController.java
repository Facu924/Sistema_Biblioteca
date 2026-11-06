package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.dto.ReservaRequest;
import ar.edu.cent35.biblioteca.dto.ReservaResponse;
import ar.edu.cent35.biblioteca.model.enums.EstadoReserva;
import ar.edu.cent35.biblioteca.service.ReservaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private static final String LECTOR = "hasAuthority('ROLE_LECTOR')";
    private static final String PERSONAL = "hasAnyAuthority('ROLE_BIBLIOTECARIA','ROLE_ADMIN')";

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(LECTOR)
    public ReservaResponse crear(@Valid @RequestBody ReservaRequest req, Authentication auth) {
        return reservaService.crear(req.idLibro(), auth.getName());
    }

    /** Listado general de reservas para el personal. Filtro opcional: estado. */
    @GetMapping
    @PreAuthorize(PERSONAL)
    public PaginaResponse<ReservaResponse> listar(@RequestParam(required = false) EstadoReserva estado,
                                                  @RequestParam(defaultValue = "0") int pagina,
                                                  @RequestParam(defaultValue = "20") int tamanio) {
        return reservaService.listar(estado, pagina, tamanio);
    }

    @GetMapping("/mias")
    @PreAuthorize(LECTOR)
    public List<ReservaResponse> mias(Authentication auth) {
        return reservaService.mias(auth.getName());
    }

    /** La bibliotecaria ve la cola de un libro en el orden en que debe atenderse. */
    @GetMapping("/libro/{idLibro}")
    @PreAuthorize(PERSONAL)
    public List<ReservaResponse> cola(@PathVariable Long idLibro) {
        return reservaService.colaDelLibro(idLibro);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyAuthority('ROLE_LECTOR','ROLE_BIBLIOTECARIA')")
    public ReservaResponse cancelar(@PathVariable Long id, Authentication auth) {
        boolean esPersonal = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BIBLIOTECARIA"));
        return reservaService.cancelar(id, auth.getName(), esPersonal);
    }
}
