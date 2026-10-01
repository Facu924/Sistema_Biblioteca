package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.EditorialRequest;
import ar.edu.cent35.biblioteca.dto.EditorialResponse;
import ar.edu.cent35.biblioteca.dto.PaginaResponse;
import ar.edu.cent35.biblioteca.service.EditorialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/editoriales")
public class EditorialController {

    private static final String BIBLIOTECARIA = "hasAuthority('ROLE_BIBLIOTECARIA')";

    private final EditorialService editorialService;

    public EditorialController(EditorialService editorialService) {
        this.editorialService = editorialService;
    }

    @GetMapping
    public PaginaResponse<EditorialResponse> listar(@RequestParam(required = false) String texto,
                                                    @RequestParam(defaultValue = "0") int pagina,
                                                    @RequestParam(defaultValue = "20") int tamanio) {
        return editorialService.listar(texto, pagina, tamanio);
    }

    @GetMapping("/{id}")
    public EditorialResponse obtener(@PathVariable Long id) {
        return editorialService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(BIBLIOTECARIA)
    public EditorialResponse crear(@Valid @RequestBody EditorialRequest req) {
        return editorialService.crear(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize(BIBLIOTECARIA)
    public EditorialResponse actualizar(@PathVariable Long id, @Valid @RequestBody EditorialRequest req) {
        return editorialService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(BIBLIOTECARIA)
    public void eliminar(@PathVariable Long id) {
        editorialService.eliminar(id);
    }
}
