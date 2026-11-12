package ar.edu.cent35.biblioteca.tests;

import ar.edu.cent35.biblioteca.BaseTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

//Ejercicio 05 · Login completo: fill + click + assertion
public class ejercicio5 extends BaseTest {
    @Test
    public void loginExitoso(){
        page.navigate("http://localhost:8080/Login/login.html");

        page.getByLabel("Correo electrónico").fill("biblio@cent35.edu.ar");
        page.getByLabel("Contraseña").fill("ClaveDePrueba1");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Iniciar Sesión")).click();

        assertThat(page).hasURL("http://localhost:8080/Portal-Gestion/Dashboard/dashboard.html");
    }
    @Test
    public void loginFallido(){
        page.navigate("http://localhost:8080/Login/login.html");

        page.getByLabel("Correo electrónico").fill("biblio@cent37.edu.ar");
        page.getByLabel("Contraseña").fill("ClaveDePrueba3");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Iniciar Sesión")).click();

        assertThat(page).hasURL("http://localhost:8080/Login/login.html");
    }
}
