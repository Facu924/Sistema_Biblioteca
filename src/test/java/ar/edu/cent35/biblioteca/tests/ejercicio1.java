package ar.edu.cent35.biblioteca.tests;

import ar.edu.cent35.biblioteca.BaseTest;
import org.junit.jupiter.api.Test;

//Ejercicio 01 · Abrir el navegador y navegar a una página
public class ejercicio1 extends BaseTest{
    @Test
    public void testNavegarAlSitio() {
        // 1.2 page.navigate() usa la URL correcta del trabajo final
        page.navigate("http://localhost:8080/Login/login.html");

        // 1.3 Imprime el título con System.out.println(page.title())
        System.out.println("Título de la página actual: " + page.title());

        // El test termina con éxito al cargar correctamente (BUILD SUCCESSFUL)
    }
}