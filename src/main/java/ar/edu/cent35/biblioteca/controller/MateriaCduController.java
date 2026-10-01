package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.MateriaCduRequest;
import ar.edu.cent35.biblioteca.dto.MateriaCduResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.service.MateriaCduService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/materias")
public class MateriaCduController {

    private static final String BIBLIOTECARIA = "hasAuthority('ROLE_BIBLIOTECARIA')";

    private final MateriaCduService materiaService;

    public MateriaCduController(MateriaCduService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public PaginaResponse<MateriaCduResponse> listar(@RequestParam(required = false) String texto,
                                                     @RequestParam(defaultValue = "0") int pagina,
                                                     @RequestParam(defaultValue = "20") int tamanio) {
        return materiaService.listar(texto, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public MateriaCduResponse obtener(@PathVariable Long id) {
        return materiaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(BIBLIOTECARIA)
    public MateriaCduResponse crear(@Valid @RequestBody MateriaCduRequest req) {
        return materiaService.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize(BIBLIOTECARIA)
    public MateriaCduResponse actualizar(@PathVariable Long id, @Valid @RequestBody MateriaCduRequest req) {
        return materiaService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(BIBLIOTECARIA)
    public void eliminar(@PathVariable Long id) {
        materiaService.eliminar(id);
    }
}
