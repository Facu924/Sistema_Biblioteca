package ar.edu.cent35.biblioteca.tests;

import ar.edu.cent35.biblioteca.BaseTest;
import org.junit.jupiter.api.Test;

//Ejercicio 04 · Escribir texto en campos de formulario
public class ejercicio4 extends BaseTest {
    @Test
    public void cargarCamposFormulario(){
        page.navigate("http://localhost:8080/Login/login.html");

        page.getByLabel("Correo electrónico").fill("biblio@cent35.edu.ar");
        page.getByLabel("Contraseña").fill("ClaveDePrueba1");

        String correo = page.getByLabel("Correo electrónico").inputValue();
        String contrasema = page.getByLabel("Contraseña").inputValue();
        System.out.println("El correo es: "+correo+" y la contraseña es: "+contrasema);
    }
}
