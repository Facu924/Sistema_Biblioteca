package ar.edu.cent35.biblioteca.tests;

import ar.edu.cent35.biblioteca.BaseTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;


//Ejercicio 08 · Navegar entre páginas: links, goBack() y reload()
public class ejercicio8 extends BaseTest {
    @Test
    public void navegacionEntrePaginas(){
        page.navigate("http://localhost:8080/Login/login.html");

        page.getByLabel("Correo electrónico").fill("biblio@cent35.edu.ar");
        page.getByLabel("Contraseña").fill("ClaveDePrueba1");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Iniciar Sesión")).click();
        assertThat(page).hasURL("http://localhost:8080/Portal-Gestion/Dashboard/dashboard.html");

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Catálogo de Libros")).click();
        assertThat(page).hasURL("http://localhost:8080/Portal-Gestion/Catalogo-libro/catalogo.html");

        page.goBack();
        assertThat(page).hasURL("http://localhost:8080/Portal-Gestion/Dashboard/dashboard.html");

        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Préstamos")).click();
        assertThat(page).hasURL("http://localhost:8080/Portal-Gestion/Prestamos/prestamo.html");

        page.reload();
        assertThat(page).hasURL("http://localhost:8080/Portal-Gestion/Prestamos/prestamo.html");




    }
}
