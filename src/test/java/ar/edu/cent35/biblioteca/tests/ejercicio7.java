package ar.edu.cent35.biblioteca.tests;

import ar.edu.cent35.biblioteca.BaseTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//Ejercicio 07 · Tomar capturas de pantalla como evidencia
public class ejercicio7 extends BaseTest {
    @Test
    public void tomarCapturasEvidencia(){
        // Definir la ruta de la carpeta y asegurar que exista físicamente
        Path carpetaScreenshots = Paths.get("src", "test","java", "screenshots");
        try {
            if (!Files.exists(carpetaScreenshots)) {
                Files.createDirectories(carpetaScreenshots);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        page.navigate("http://localhost:8080/Login/login.html");

        page.screenshot(new Page.ScreenshotOptions().setPath(carpetaScreenshots.resolve("login-normal.png")));

        page.screenshot(new Page.ScreenshotOptions().setPath(carpetaScreenshots.resolve("login-pantalla-completa.png")));

        page.getByLabel("Correo electrónico").fill("biblio@cent37.edu.ar");
        page.getByLabel("Contraseña").fill("ClaveDePrueba3");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Iniciar Sesión")).click();

        page.screenshot(new Page.ScreenshotOptions().setPath(carpetaScreenshots.resolve("login-error.png")));

        System.out.println("¡Capturas de pantalla tomadas y guardadas exitosamente en la carpeta 'screenshots'!");
    }
}
