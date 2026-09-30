package ar.edu.cent35.biblioteca.model.reportes;

import java.time.LocalDateTime;

import ar.edu.cent35.biblioteca.model.acceso.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auditoria_sistema")
@Getter
@Setter
@NoArgsConstructor
public class AuditoriaSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    /** Nulo si el evento lo disparó el sistema automáticamente. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    /** Ej: DESBLOQUEO_CUENTA, BAJA_LOGICA_USUARIO, EXPORTACION_REPORTE */
    @Column(nullable = false, length = 100)
    private String accion;

    @Column(length = 500)
    private String detalles;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;
}
