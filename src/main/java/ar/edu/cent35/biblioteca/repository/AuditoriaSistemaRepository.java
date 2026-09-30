package ar.edu.cent35.biblioteca.repository;

import java.util.List;

import ar.edu.cent35.biblioteca.model.reportes.AuditoriaSistema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaSistemaRepository extends JpaRepository<AuditoriaSistema, Long> {

    List<AuditoriaSistema> findByUsuario_IdUsuario(Long idUsuario);
}
