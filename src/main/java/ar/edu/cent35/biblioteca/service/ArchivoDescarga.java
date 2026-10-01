package ar.edu.cent35.biblioteca.service;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record ArchivoDescarga(Resource recurso, String nombre, MediaType tipo) {
}
