# Configuración y ejecución

*Última actualización: noviembre de 2026.*

## Requisitos

- **JDK 21.** Gradle lo busca con el mecanismo de `toolchain`, así que tiene que estar instalado.
- **MySQL 8 o MariaDB.** En desarrollo se usa XAMPP, que trae MariaDB.
- **Gradle:** no hace falta instalarlo, el proyecto trae el wrapper (`gradlew`).
- **Git**, para clonar el repositorio.

## 1. Preparar la base de datos

En phpMyAdmin (pestaña SQL), o con el cliente de MySQL:

```sql
CREATE DATABASE biblioteca CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'biblioteca_app'@'localhost' IDENTIFIED BY 'una_clave_segura';
GRANT ALL PRIVILEGES ON biblioteca.* TO 'biblioteca_app'@'localhost';
FLUSH PRIVILEGES;
```

La collation `utf8mb4_unicode_ci` es la que permite que las búsquedas ignoren mayúsculas y acentos. Las tablas las crea la aplicación sola al arrancar.

## 2. Variables de entorno

Las credenciales **nunca se escriben en el código**. Se definen como variables de entorno (en IntelliJ, en *Run, Edit Configurations, Environment variables*, separadas por punto y coma y sin espacios alrededor del `=`).

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_PASSWORD` | Sí | Contraseña del usuario de la base. |
| `DB_USER` | No | Usuario de la base. Por defecto `biblioteca_app`. |
| `DB_URL` | No | URL de conexión. Por defecto `jdbc:mysql://localhost:3306/biblioteca?useSSL=false&serverTimezone=UTC`. |
| `ADMIN_MAIL` | Solo la primera vez | Correo del administrador inicial. |
| `ADMIN_PASSWORD` | Solo la primera vez | Contraseña del administrador inicial (mínimo 8 caracteres). |
| `STORAGE_PATH` | No | Carpeta de los PDF y EPUB. Por defecto `./storage`. Conviene una ruta fija fuera del proyecto, como `C:\biblioteca-archivos`. |

El administrador se crea al arrancar **solo si todavía no existe ninguno**. Después, `ADMIN_MAIL` y `ADMIN_PASSWORD` ya no se usan.

## 3. Configuración de la aplicación

Archivo `src/main/resources/application.properties`:

```properties
spring.application.name=biblioteca
server.port=8080

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/biblioteca?useSSL=false&serverTimezone=UTC}
spring.datasource.username=${DB_USER:biblioteca_app}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

app.security.max-intentos-fallidos=5
app.admin.mail=${ADMIN_MAIL:}
app.admin.password=${ADMIN_PASSWORD:}
app.storage.path=${STORAGE_PATH:./storage}
app.prestamos.plazo-domicilio-dias=7
app.prestamos.limite-por-lector=2

spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=55MB
server.servlet.session.timeout=30m
```

Cuando el sistema esté estable conviene pasar `ddl-auto` a `validate` y apagar `show-sql`.

## 4. Ejecutar

Desde IntelliJ, con el botón de ejecución de `BibliotecaApplication`, o desde la terminal en la carpeta del proyecto:

```
gradlew bootRun
```

La aplicación queda en `http://localhost:8080`. Al arrancar, la consola tiene que mostrar la creación de las tablas, y el aviso `Administrador inicial creado` la primera vez.

## 5. Probar

Para probar sin interfaz se usa Postman, con una colección por etapa (`Biblioteca-Etapa3`, `Etapa4` y `Etapa5`). La sesión se inicia con el pedido de login y Postman guarda la cookie. Hay que usar la **aplicación de escritorio** de Postman, porque la versión web no llega a `localhost`.

Orden sugerido: iniciar sesión como administrador, crear una bibliotecaria y un alumno, iniciar sesión como bibliotecaria, cargar autor, editorial, materia, libro y dos ejemplares, y recién ahí registrar préstamos, devoluciones y reservas.

## 6. Pruebas automatizadas

Además de las colecciones de Postman, se realizaron **tests automatizados con Playwright**. En el desarrollo actual se ejecutaron por separado de la tarea `test` de Gradle; por eso conviene distinguir ambas verificaciones.

**Comprobación de Gradle (Windows / PowerShell):**

```powershell
.\gradlew.bat clean test
```

**En otros sistemas:**

```bash
./gradlew clean test
```

- `clean` elimina los artefactos generados en `build/`, no los archivos fuente ni los datos de MySQL.
- `test` ejecuta las pruebas que estén registradas en la tarea de Gradle. **No garantiza que ejecute Playwright** si los tests se lanzan mediante otro comando o herramienta.
- En esta etapa se retiró el test de carga de contexto predeterminado de Spring Boot que fallaba; después, el comando de Gradle finalizó correctamente. Esto no equivale a una validación funcional integral.
- Para ejecutar **Playwright**, utilizar el comando y la configuración propios de los tests existentes. **Pendiente de documentar:** su ubicación y comando exacto; no se incluyen aquí porque esos archivos no forman parte de la documentación entregada.
- Para comprobar el detalle de los resultados de Gradle, abrir `build/reports/tests/test/index.html` cuando el informe exista.

**Antes de ejecutar pruebas contra MySQL**, confirmar que utilicen datos de prueba y no alteren registros reales. También deben comprobarse manualmente las pantallas nuevas de Historial y Lectores y Sanciones, con sesión de bibliotecaria y administrador, incluyendo los casos de error y permisos.

## Estructura del proyecto

```
src/main/java/ar/edu/cent35/biblioteca
├── config/        Seguridad (WebAuthentication, WebAuthorization) y tareas programadas
├── controller/    Endpoints REST
├── dto/           Datos de entrada y salida de la API
├── exception/     Excepciones propias y manejo global de errores
├── model/
│   ├── acceso/        Persona, Usuario, Lector
│   ├── circulacion/   Prestamo, Reserva, HistorialTransaccion, Penalizacion
│   ├── enums/         Tipos y estados
│   ├── inventario/    Autor, Editorial, MateriaCdu, Libro, EjemplarFisico
│   └── reportes/      AuditoriaSistema
├── repository/    Acceso a datos (Spring Data JPA)
├── service/       Reglas de negocio
└── BibliotecaApplication
```

## Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Access denied for user` al arrancar | La variable `DB_PASSWORD` no llegó a la aplicación o tiene un espacio de más. Revisar que no haya espacios alrededor del `=`. |
| `Unknown database` | Falta crear la base `biblioteca`. |
| `Connection refused` | MySQL no está encendido en XAMPP. |
| El administrador no se crea | Falta `ADMIN_MAIL` o `ADMIN_PASSWORD` tiene menos de 8 caracteres. Queda un aviso en la consola. |
| Login con `423` | La cuenta se bloqueó por intentos fallidos. La desbloquea el Administrador. Si se bloqueó el único administrador: `UPDATE usuario SET cuenta_bloqueada = 0, intentos_fallidos = 0 WHERE mail = '...';` |
| Error `for update of` al prestar | Se resolvió usando SQL directo para los bloqueos. Si reaparece, revisar `LibroRepository` y `EjemplarFisicoRepository`. |
| Subida de archivo con `413` | El archivo supera los 50 MB, o falta la configuración `spring.servlet.multipart` y hay que reiniciar la aplicación. |
| Las búsquedas distinguen acentos | La base se creó con otra collation. Cambiarla con `ALTER DATABASE` y recrear las tablas. |
