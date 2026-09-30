package ar.edu.cent35.biblioteca.model.acceso;

import ar.edu.cent35.biblioteca.model.enums.TipoLector;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lector")
@Getter
@Setter
@NoArgsConstructor
public class Lector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lector")
    private Long idLector;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_persona", nullable = false, unique = true)
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_lector", nullable = false, length = 30)
    private TipoLector tipoLector;

    /** Carrera (alumno) o "DOCENTE" (profesor). Libre para personal administrativo. */
    @Column(name = "perfil_institucional", length = 150)
    private String perfilInstitucional;
}
