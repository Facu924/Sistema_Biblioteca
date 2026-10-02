### Requisitos No Funcionales Integrados

#### Seguridad y Autenticación

- **RNF-SEC-01 (Cifrado):** Las contraseñas deben almacenarse usando hashing seguro con BCrypt mediante Spring Security.
- **RNF-SEC-02 (RBAC):** Control estricto de accesos por roles (`ADMIN`, `BIBLIOTECARIO`, `USUARIO_GENERAL`) interceptando cada petición HTTP.
- **RNF-SEC-03 (Variables de Entorno):** Las credenciales iniciales del administrador se configuran mediante variables de entorno externas (sin hardcodear).

#### Rendimiento, Hardware y Disponibilidad

- **RNF-PERF-01 (Tiempos de Respuesta):** Las consultas y búsquedas deben responder en menos de 2 segundos en red local.
- **RNF-PERF-02 (Concurrencia):** Disponibilidad garantizada para consultas masivas de alumnos sin bloquear las operaciones de escritura rápidas de la bibliotecaria en el mostrador.
- **RNF-HW-01 (Integración de Periféricos):** Compatibilidad de la interfaz de administración con lectores de códigos de barras (emulación de teclado) y diseño adaptado para la visualización en las tablets institucionales.

#### Confiabilidad e Integridad de Datos

- **RNF-REL-01 (Integridad Referencial):** Uso de restricciones de Clave Primaria, Foránea y Unicidad en MySQL (con collation `utf8mb4_unicode_ci` para búsquedas sin sensibilidad a acentos/mayúsculas).
- **RNF-REL-02 (Transacciones ACID):** Las operaciones críticas como préstamos y devoluciones deben ser transaccionales con rollback automático ante fallos.

#### Tecnológicos y de Mantenimiento

- **RNF-TEC-01 (Arquitectura en Capas):** Backend en Java con Spring Boot estructurado en Controladores, Servicios y Repositorios.
- **RNF-TEC-02 (ORM e Independencia):** Uso de Spring Data JPA / Hibernate para aislar la lógica de negocio de las sentencias SQL nativas, y almacenamiento de archivos pesados (PDFs) en el sistema de archivos del servidor, guardando solo la ruta en la base de datos.

---

### Notas de implementación y cumplimiento

| Requisito | Cómo se cumple | Estado |
|---|---|---|
| RNF-SEC-01 | Contraseñas con `BCryptPasswordEncoder`. Longitud permitida: 8 a 72 caracteres (límite de BCrypt). | Cumplido |
| RNF-SEC-02 | Autenticación por sesión con cookie. Reglas por ruta en `WebAuthorization` y por método con `@PreAuthorize`. | Cumplido |
| RNF-SEC-03 | Las credenciales iniciales del administrador y de la base de datos se leen de las variables de entorno `ADMIN_MAIL`, `ADMIN_PASSWORD`, `DB_USER` y `DB_PASSWORD`. | Cumplido |
| RNF-PERF-01 | Consultas paginadas (máximo 100 por página) y consultas con carga anticipada de relaciones. Todavía no se midieron los tiempos. | Sin medir |
| RNF-PERF-02 | Las operaciones de préstamo y devolución bloquean solo las filas del libro y del ejemplar involucrados; las consultas de lectura no bloquean. Todavía no se probó con carga. | Sin medir |
| RNF-HW-01 | El lector de códigos de barras se resuelve en la interfaz (campo de texto que recibe el número y termina con Enter). La API ya permite buscar por número de inventario. | Pendiente de los clientes |
| RNF-REL-01 | Restricciones de clave primaria, foránea y unicidad (legajo, correo, número de inventario). La base debe crearse con collation `utf8mb4_unicode_ci`. | Cumplido |
| RNF-REL-02 | Préstamos y devoluciones son transaccionales, con bloqueo de filas (`SELECT ... FOR UPDATE`) y aislamiento `READ_COMMITTED`. Ante un fallo se revierte todo. | Cumplido |
| RNF-TEC-01 | Paquetes `controller`, `service` y `repository`, además de `dto`, `model`, `config` y `exception`. | Cumplido |
| RNF-TEC-02 | Spring Data JPA con Hibernate. Los PDF y EPUB se guardan en el disco del servidor (`app.storage.path`) y la base solo guarda el nombre del archivo. | Cumplido |

**Compatibilidad de base de datos.** En desarrollo se usa XAMPP, que trae MariaDB. El sistema funciona con MariaDB y con MySQL 8; los bloqueos se escribieron en SQL directo porque Hibernate generaba una sintaxis que MariaDB no acepta.
