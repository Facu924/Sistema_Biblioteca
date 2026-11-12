package ar.edu.cent35.biblioteca.tests;

import ar.edu.cent35.biblioteca.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.*;

//Ejercicio 02 · Verificar el título de la página (assertion)
public class ejercicio2 extends BaseTest {
    @Test
    public void verificarTitulo(){
        page.navigate("http://localhost:8080/Login/login.html");
        assertThat(page).hasTitle("Biblioteca CENT 35 - Iniciar Sesión");

        assertThat(page).hasTitle(Pattern.compile("Biblioteca"));
    }
}
