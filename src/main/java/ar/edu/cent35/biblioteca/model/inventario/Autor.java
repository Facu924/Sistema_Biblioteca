package ar.edu.cent35.biblioteca.model.inventario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "autor")
@Getter
@Setter
@NoArgsConstructor
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_autor")
    private Long idAutor;

    /** Formato "Apellido, Nombre". */
    @Column(name = "nombre_autor", nullable = false, length = 200)
    private String nombreAutor;
}
