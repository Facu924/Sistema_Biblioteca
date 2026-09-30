package ar.edu.cent35.biblioteca.model.inventario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "libro")
@Getter
@Setter
@NoArgsConstructor
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Long idLibro;

    @Column(length = 20)
    private String isbn;

    @Column(nullable = false, length = 300)
    private String titulo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_autor", nullable = false)
    private Autor autor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_editorial", nullable = false)
    private Editorial editorial;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_materia", nullable = false)
    private MateriaCdu materia;

    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    @Column(length = 100)
    private String edicion;

    /** Sinopsis o descripción del material. */
    @Column(columnDefinition = "TEXT")
    private String resumen;

    @Column(name = "es_digital", nullable = false)
    private Boolean esDigital = false;

    /** Ruta del PDF/EPUB en el sistema de archivos del servidor. */
    @Column(name = "ruta_archivo_digital", length = 500)
    private String rutaArchivoDigital;
}
