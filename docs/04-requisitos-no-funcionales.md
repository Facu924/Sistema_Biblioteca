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