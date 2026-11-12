package ar.edu.cent35.biblioteca.tests;

import ar.edu.cent35.biblioteca.BaseTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

//Ejercicio 06 · Verificar mensajes de estado en pantalla
public class ejercicio6 extends BaseTest {
    @Test
    public void mensajePorPantalla(){
        page.navigate("http://localhost:8080/Login/login.html");

        Locator mensaje = page.getByRole(AriaRole.ALERT);

        page.getByLabel("Correo electrónico").fill("biblio@cent37.edu.ar");
        page.getByLabel("Contraseña").fill("ClaveDePrueba3");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Iniciar Sesión")).click();

        assertThat(mensaje).isVisible();
        assertThat(mensaje).containsText("Credenciales inválidas.");

    }
}
