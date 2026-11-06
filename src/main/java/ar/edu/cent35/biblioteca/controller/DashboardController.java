package ar.edu.cent35.biblioteca.controller;

import ar.edu.cent35.biblioteca.dto.DashboardResponse;
import ar.edu.cent35.biblioteca.service.DashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** Indicadores y gráficos del panel principal. */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_BIBLIOTECARIA','ROLE_ADMIN')")
    public DashboardResponse resumen() {
        return dashboardService.resumen();
    }
}
