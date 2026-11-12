# Sistema de Gestión Bibliotecaria — CENT 35

Plataforma para modernizar y automatizar la gestión de la biblioteca del CENT 35. Reemplaza las planillas de cálculo descentralizadas por un sistema centralizado: préstamos con lector de códigos de barras, control automático de las reglas de la biblioteca, catálogo físico y digital, y autogestión para alumnos y profesores.

> **Estado (noviembre de 2026):** el backend (API REST) está implementado y verificado manualmente. El frontend de gestión cuenta con ingreso, panel, catálogo y préstamos; se preparó la integración del historial y se agregó «Lectores y Sanciones», ambas pendientes de verificación integral con datos reales. Ya se realizaron pruebas automatizadas con Playwright, pero su cobertura debe documentarse. Siguen pendientes partes de la autogestión y la importación masiva desde Aguapey. Ver [`docs/00-estado-de-implementacion.md`](docs/00-estado-de-implementacion.md).

## Características

- **Préstamos y devoluciones:** modalidades Aula, Sala y Domicilio, con plazo calculado automáticamente (mismo día para Aula y Sala, 7 días para Domicilio), límite de 2 materiales por lector y **stock mínimo** que reserva siempre un ejemplar para consulta en sala.
- **Penalizaciones flexibles:** el sistema sugiere un día de penalización por cada día de atraso y la bibliotecaria decide si la aplica, la ajusta o la anula. Los daños y pérdidas se penalizan hasta que ella los levante.
- **Reservas con cola de espera:** cuando no quedan ejemplares disponibles, atendidas por orden de llegada.
- **Seguridad:** acceso por roles (administrador, bibliotecaria y lector) con Spring Security, contraseñas con BCrypt y **bloqueo automático de la cuenta tras 5 intentos fallidos**, que desbloquea el administrador. Auditoría de los eventos críticos.
- **Catálogo mixto y digital:** libros, autores, editoriales y materias CDU, ejemplares físicos con número de inventario (código de barras), y archivos PDF y EPUB para ver o descargar.
- **Búsqueda sin acentos ni mayúsculas**, con el stock de cada libro en tiempo real.
- **Control de mora:** listado de préstamos vencidos con teléfono y correo del lector, y marcado automático diario.
- **Integridad:** préstamos y devoluciones transaccionales, con bloqueo de filas para evitar conflictos entre operaciones simultáneas.

## Roles

| Rol | Qué puede hacer |
|---|---|
| `ROLE_ADMIN` | Gestiona usuarios y desbloquea cuentas. Consulta todo lo demás en solo lectura. |
| `ROLE_BIBLIOTECARIA` | Gestiona el catálogo, los ejemplares, los préstamos, las devoluciones, las penalizaciones y las reservas. |
| `ROLE_LECTOR` | Alumnos, profesores y personal administrativo: consultan el catálogo y el material digital, reservan, y ven su historial y sus sanciones. |

## Stack tecnológico

- **Backend:** Java 21, Spring Boot 4, Spring Data JPA / Hibernate, Spring Security, Bean Validation y Lombok.
- **Construcción:** Gradle.
- **Base de datos:** MySQL 8 o MariaDB, con collation `utf8mb4_unicode_ci`. En desarrollo se usa XAMPP.
- **Archivos digitales:** sistema de archivos del servidor; la base guarda solo el nombre.
- **Pruebas:** colecciones de Postman y pruebas automatizadas con Playwright ejecutadas por separado. La tarea `gradlew test` no implica por sí sola ejecutar Playwright.

## Interfaces de gestión

- **Historial de préstamos:** consulta movimientos de préstamos y devoluciones a través de `/api/prestamos` y `/api/historial`; el frontend incorpora filtros, estadísticas y detalle. Integración aún pendiente de validación de punta a punta.
- **Lectores y Sanciones:** pantalla con búsqueda de lectores por legajo, consulta de penalizaciones y operaciones de creación y levantamiento para bibliotecaria. No implica nuevas tablas ni endpoints.
- **Permisos:** «Usuarios» corresponde al administrador; la gestión operativa de sanciones corresponde a bibliotecaria. La ocultación de menús no reemplaza el control de acceso de Spring Security. Falta verificar la uniformidad del menú en todas las páginas.

## Cómo ejecutarlo

1. Instalar el JDK 21 y tener MySQL o MariaDB funcionando.
2. Crear la base `biblioteca` con collation `utf8mb4_unicode_ci` y un usuario propio.
3. Definir las variables de entorno `DB_PASSWORD`, `ADMIN_MAIL` y `ADMIN_PASSWORD`.
4. Ejecutar `gradlew bootRun`, o la clase `BibliotecaApplication` desde el IDE.

La API queda en `http://localhost:8080`. Los pasos completos, con todas las variables y los problemas frecuentes, están en [`docs/09-configuracion-y-ejecucion.md`](docs/09-configuracion-y-ejecucion.md).

## Documentación

| Documento | Contenido |
|---|---|
| [`docs/00-estado-de-implementacion.md`](docs/00-estado-de-implementacion.md) | Qué está hecho y qué falta, requisito por requisito |
| [`docs/01-planteamiento-del-problema.md`](docs/01-planteamiento-del-problema.md) | Problemática y solución propuesta |
| [`docs/02-alcance-del-sistema.md`](docs/02-alcance-del-sistema.md) | Alcance, exclusiones y ampliaciones |
| [`docs/03-requisitos-funcionales.md`](docs/03-requisitos-funcionales.md) | Requisitos funcionales |
| [`docs/04-requisitos-no-funcionales.md`](docs/04-requisitos-no-funcionales.md) | Requisitos no funcionales y su cumplimiento |
| [`docs/05-casos-de-uso.md`](docs/05-casos-de-uso.md) | Historias de usuario |
| [`docs/06-entidades-y-relaciones.md`](docs/06-entidades-y-relaciones.md) | Modelo de datos |
| [`docs/07-decisiones-de-diseno.md`](docs/07-decisiones-de-diseno.md) | Decisiones, motivos y limitaciones conocidas |
| [`docs/08-api-referencia.md`](docs/08-api-referencia.md) | Todos los endpoints |
| [`docs/09-configuracion-y-ejecucion.md`](docs/09-configuracion-y-ejecucion.md) | Instalación, configuración y solución de problemas |

## Fuera del alcance

Notificaciones automáticas por correo o mensajería, cobro de multas en dinero, autenticación con servicios externos, gestión de compras y proveedores, aplicaciones móviles nativas y renovaciones de préstamos.
