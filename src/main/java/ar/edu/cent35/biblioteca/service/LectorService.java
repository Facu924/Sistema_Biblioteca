package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.dto.LectorResponse;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import ar.edu.cent35.biblioteca.model.acceso.Lector;
import ar.edu.cent35.biblioteca.model.acceso.Persona;
import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.repository.LectorRepository;
import ar.edu.cent35.biblioteca.repository.PrestamoRepository;
import ar.edu.cent35.biblioteca.repository.UsuarioRepository;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de un lector por legajo antes de prestar: muestra quién es y si hoy se le puede prestar. */
@Service
public class LectorService {

    private final LectorRepository lectorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final HabilitacionService habilitacionService;
    private final int limitePorLector;

    public LectorService(LectorRepository lectorRepository,
                         UsuarioRepository usuarioRepository,
                         PrestamoRepository prestamoRepository,
                         HabilitacionService habilitacionService,
                         @Value("${app.prestamos.limite-por-lector:2}") int limitePorLector) {
        this.lectorRepository = lectorRepository;
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
        this.habilitacionService = habilitacionService;
        this.limitePorLector = limitePorLector;
    }

    @Transactional(readOnly = true)
    public LectorResponse consultar(String legajo) {
        Lector lector = lectorRepository.findByPersona_Legajo(legajo.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un lector con el legajo " + legajo.trim()));
        Persona persona = lector.getPersona();
        Usuario usuario = usuarioRepository.findByPersona_IdPersona(persona.getIdPersona()).orElse(null);

        long abiertos = prestamoRepository.countByLector_IdLectorAndEstadoPrestamoIn(
                lector.getIdLector(), PrestamoService.ABIERTOS);

        // Se revisa en orden: cuenta y sanciones, mora y límite de materiales
        String motivo = habilitacionService.motivoNoPuedeOperar(lector);
        if (motivo == null
                && prestamoRepository.contarEnMora(lector.getIdLector(), PrestamoService.ABIERTOS, LocalDate.now()) > 0) {
            motivo = "Tiene un préstamo vencido sin devolver";
        }
        if (motivo == null && abiertos >= limitePorLector) {
            motivo = "Ya tiene " + limitePorLector + " materiales en préstamo";
        }

        return new LectorResponse(lector.getIdLector(), persona.getLegajo(),
                persona.getNombre() + " " + persona.getApellido(),
                lector.getTipoLector(), lector.getPerfilInstitucional(),
                persona.getTelefono(), usuario == null ? null : usuario.getMail(),
                abiertos, limitePorLector, motivo == null, motivo);
    }
}
