package ar.edu.cent35.biblioteca.service;

import ar.edu.cent35.biblioteca.exception.ReglaNegocioException;
import ar.edu.cent35.biblioteca.exception.ResourceNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Guarda los PDF/EPUB en el sistema de archivos del servidor (RNF-TEC-02).
 * En la base solo queda el nombre del archivo; el nombre lo genera el sistema,
 * nunca el que manda el cliente, para evitar rutas maliciosas.
 */
@Service
public class ArchivoService {

    private static final Logger log = LoggerFactory.getLogger(ArchivoService.class);

    private final Path raiz;

    public ArchivoService(@Value("${app.storage.path:./storage}") String ruta) {
        this.raiz = Paths.get(ruta).toAbsolutePath().normalize().resolve("libros");
        try {
            Files.createDirectories(raiz);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de almacenamiento: " + raiz, e);
        }
    }

    /** Valida que sea un PDF o EPUB real, lo guarda y devuelve el nombre generado. */
    public String guardar(Long idLibro, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ReglaNegocioException("Falta el archivo");
        }
        String extension = detectarExtension(archivo);
        String nombre = idLibro + "-" + UUID.randomUUID() + "." + extension;
        Path destino = raiz.resolve(nombre).normalize();
        try (InputStream in = archivo.getInputStream()) {
            Files.copy(in, destino);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo", e);
        }
        return nombre;
    }

    public Resource cargar(String nombre) {
        Path ruta = raiz.resolve(nombre).normalize();
        if (!ruta.startsWith(raiz) || !Files.isReadable(ruta)) {
            throw new ResourceNotFoundException("Archivo no encontrado en el servidor");
        }
        return new FileSystemResource(ruta);
    }

    public void eliminar(String nombre) {
        Path ruta = raiz.resolve(nombre).normalize();
        if (!ruta.startsWith(raiz)) {
            return;
        }
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            log.warn("No se pudo borrar el archivo {}", nombre);
        }
    }

    /** Se decide por el contenido (firma del archivo), no solo por la extensión. */
    private String detectarExtension(MultipartFile archivo) {
        byte[] c = new byte[5];
        int leidos;
        try (InputStream in = archivo.getInputStream()) {
            leidos = in.readNBytes(c, 0, 5);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo", e);
        }
        boolean pdf = leidos >= 5 && c[0] == '%' && c[1] == 'P' && c[2] == 'D' && c[3] == 'F' && c[4] == '-';
        boolean zip = leidos >= 4 && c[0] == 'P' && c[1] == 'K' && c[2] == 3 && c[3] == 4;
        String original = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase();
        if (pdf) {
            return "pdf";
        }
        if (zip && original.endsWith(".epub")) {
            return "epub";
        }
        throw new ReglaNegocioException("Formato no permitido: solo se aceptan archivos PDF o EPUB válidos");
    }
}
