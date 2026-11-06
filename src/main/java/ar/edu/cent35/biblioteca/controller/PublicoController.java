package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.ResumenPublicoResponse;
import ar.edu.cent35.biblioteca.service.ResumenPublicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Datos que se pueden ver sin iniciar sesión (por ejemplo, en la pantalla de ingreso). */
@RestController
@RequestMapping("/api/publico")
public class PublicoController {

    private final ResumenPublicoService resumenPublicoService;

    public PublicoController(ResumenPublicoService resumenPublicoService) {
        this.resumenPublicoService = resumenPublicoService;
    }

    @GetMapping("/resumen")
    public ResumenPublicoResponse resumen() {
        return resumenPublicoService.resumen();
    }
}
