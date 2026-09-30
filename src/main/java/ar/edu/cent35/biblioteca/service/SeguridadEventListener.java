package ar.edu.cent35.biblioteca.service;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/** Engancha los eventos de login de Spring Security con el control de intentos fallidos. */
@Component
public class SeguridadEventListener {

    private final SeguridadCuentaService seguridadCuentaService;

    public SeguridadEventListener(SeguridadCuentaService seguridadCuentaService) {
        this.seguridadCuentaService = seguridadCuentaService;
    }

    @EventListener
    public void alFallar(AuthenticationFailureBadCredentialsEvent evento) {
        seguridadCuentaService.registrarIntentoFallido(evento.getAuthentication().getName());
    }

    @EventListener
    public void alIngresar(AuthenticationSuccessEvent evento) {
        seguridadCuentaService.registrarLoginExitoso(evento.getAuthentication().getName());
    }
}
