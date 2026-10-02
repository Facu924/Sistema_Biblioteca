package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.HistorialResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.service.HistorialService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    private final HistorialService historialService;

    public HistorialController(HistorialService historialService) {
        this.historialService = historialService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_BIBLIOTECARIA','ROLE_ADMIN')")
    public PaginaResponse<HistorialResponse> listar(@RequestParam(required = false) String legajo,
                                                    @RequestParam(required = false) String inventario,
                                                    @RequestParam(defaultValue = "0") int pagina,
                                                    @RequestParam(defaultValue = "20") int tamanio) {
        return historialService.listar(legajo, inventario, pagina, tamanio);
    }
}
