package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.dto.PrestamoRequest;
import ar.edu.cent35.biblioteca.dto.PrestamoResponse;
import ar.edu.cent35.biblioteca.dto.ResumenPrestamosResponse;
import ar.edu.cent35.biblioteca.model.enums.EstadoPrestamo;
import ar.edu.cent35.biblioteca.service.PrestamoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private static final String BIBLIOTECARIA = "hasAuthority('ROLE_BIBLIOTECARIA')";
    private static final String PERSONAL = "hasAnyAuthority('ROLE_BIBLIOTECARIA','ROLE_ADMIN')";

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    /** HU-04: registra el préstamo con el legajo del lector y el inventario escaneado. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(BIBLIOTECARIA)
    public PrestamoResponse registrar(@Valid @RequestBody PrestamoRequest req, Authentication auth) {
        return prestamoService.registrar(req, auth.getName());
    }

    @GetMapping
    @PreAuthorize(PERSONAL)
    public PaginaResponse<PrestamoResponse> listar(@RequestParam(required = false) EstadoPrestamo estado,
                                                   @RequestParam(required = false) String texto,
                                                   @RequestParam(defaultValue = "0") int pagina,
                                                   @RequestParam(defaultValue = "20") int tamanio) {
        return prestamoService.listar(estado, texto, pagina, tamanio);
    }

    /** Totales por estado para las tarjetas de la pantalla de préstamos. */
    @GetMapping("/resumen")
    @PreAuthorize(PERSONAL)
    public ResumenPrestamosResponse resumen() {
        return prestamoService.resumen();
    }

    /** RF-BIB-07: préstamos en mora con los datos de contacto del lector. */
    @GetMapping("/vencidos")
    @PreAuthorize(PERSONAL)
    public List<PrestamoResponse> vencidos() {
        return prestamoService.enMora();
    }

    @GetMapping("/inventario/{numero}")
    @PreAuthorize(PERSONAL)
    public PrestamoResponse abiertoPorInventario(@PathVariable String numero) {
        return prestamoService.abiertoPorInventario(numero);
    }

    /** RF-ALU-03 / HU-10: cada lector ve solo sus propios préstamos. */
    @GetMapping("/mios")
    @PreAuthorize("hasAuthority('ROLE_LECTOR')")
    public List<PrestamoResponse> mios(Authentication auth) {
        return prestamoService.mios(auth.getName());
    }
}
