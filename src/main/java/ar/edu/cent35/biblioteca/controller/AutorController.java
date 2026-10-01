package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.AutorRequest;
import ar.edu.cent35.biblioteca.dto.AutorResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.service.AutorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/autores")
public class AutorController {

    private static final String BIBLIOTECARIA = "hasAuthority('ROLE_BIBLIOTECARIA')";

    private final AutorService autorService;

    public AutorController(AutorService autorService) {
        this.autorService = autorService;
    }

    @GetMapping
    public PaginaResponse<AutorResponse> listar(@RequestParam(required = false) String texto,
                                                @RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "20") int tamanio) {
        return autorService.listar(texto, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public AutorResponse obtener(@PathVariable Long id) {
        return autorService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(BIBLIOTECARIA)
    public AutorResponse crear(@Valid @RequestBody AutorRequest req) {
        return autorService.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize(BIBLIOTECARIA)
    public AutorResponse actualizar(@PathVariable Long id, @Valid @RequestBody AutorRequest req) {
        return autorService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(BIBLIOTECARIA)
    public void eliminar(@PathVariable Long id) {
        autorService.eliminar(id);
    }
}
