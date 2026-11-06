package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.LectorResponse;
import ar.edu.cent35.biblioteca.service.LectorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lectores")
public class LectorController {

    private final LectorService lectorService;

    public LectorController(LectorService lectorService) {
        this.lectorService = lectorService;
    }

    /** Ficha de un lector por legajo, con su estado para el mostrador. */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_BIBLIOTECARIA','ROLE_ADMIN')")
    public LectorResponse consultar(@RequestParam String legajo) {
        return lectorService.consultar(legajo);
    }
}
