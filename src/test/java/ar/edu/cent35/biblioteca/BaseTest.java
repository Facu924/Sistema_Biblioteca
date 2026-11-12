package ar.edu.cent35.biblioteca;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.TestInfo;

public class BaseTest {

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;

    private String nombreTestActual;

    @BeforeEach
    void setUp(TestInfo testInfo) {

        nombreTestActual = testInfo.getTestMethod().map(java.lang.reflect.Method::getName).orElse("test_desconocido");
        // Inicializamos Playwright y lanzamos el navegador en modo con interfaz gráfica (headed)
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));

        // Creamos un contexto aislado para la prueba
        context = browser.newContext();

        // Iniciamos el registro de trazas (screenshots, snapshots y fuentes)
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        // Abrimos una nueva pestaña
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        try {
            // Apuntamos la ruta para que la carpeta 'grabaciones' se ubique dentro de src/test/java/
            Path directorio = Paths.get("src", "test", "java", "grabaciones");
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
            }

            // Usamos el nombre del test actual en lugar de System.currentTimeMillis()
            Path archivoDestino = directorio.resolve(nombreTestActual+"_"+System.currentTimeMillis()+".zip");

            // Detenemos el tracing y guardamos el archivo con el nombre del ejercicio
            context.tracing().stop(new Tracing.StopOptions()
                    .setPath(archivoDestino));

            System.out.println("Trace guardado exitosamente para el test: " + nombreTestActual + " en: " + archivoDestino.toAbsolutePath());

        } catch (Exception e) {
            System.out.println("Hubo un error al guardar el trace: " + e.getMessage());
        } finally {
            // Cerramos los recursos en orden para liberar memoria
            context.close();
            browser.close();
            playwright.close();
        }
    }
}