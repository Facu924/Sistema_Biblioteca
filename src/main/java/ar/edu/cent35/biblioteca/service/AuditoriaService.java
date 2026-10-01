package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import ar.edu.cent35.biblioteca.model.reportes.AuditoriaSistema;
import ar.edu.cent35.biblioteca.repository.AuditoriaSistemaRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private final AuditoriaSistemaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaSistemaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    /** actor = null indica un evento automático del sistema. */
    @Transactional
    public void registrar(Usuario actor, String accion, String detalles, String ip) {
        AuditoriaSistema a = new AuditoriaSistema();
        a.setUsuario(actor);
        a.setAccion(accion);
        a.setDetalles(detalles);
        a.setFechaHora(LocalDateTime.now());
        a.setIpOrigen(ip);
        auditoriaRepository.save(a);
    }
}
