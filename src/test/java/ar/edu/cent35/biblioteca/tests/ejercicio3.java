package ar.edu.cent35.biblioteca.tests;

import ar.edu.cent35.biblioteca.BaseTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

//Ejercicio 03 · Hacer clic en un elemento de la página
public class ejercicio3 extends BaseTest {
    @Test
    public void hacerClickEnElemento(){
        page.navigate("http://localhost:8080/Login/login.html");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Iniciar Sesión")).click();

        assertThat(page).hasURL("http://localhost:8080/Login/login.html");
    }
}
