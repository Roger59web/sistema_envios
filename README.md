# Sistema de Envíos

Sistema de escritorio para el personal de ventanilla de una empresa de encomiendas (tipo Shalom).

**Tecnologías:** Java 17, JavaFX 21, PostgreSQL, Maven, JDBC.

## Requisitos

- JDK 17 o superior
- PostgreSQL 14 o superior
- IntelliJ IDEA (Maven viene incluido)

## Cómo levantar el proyecto

1. **Clonar el repositorio** (IntelliJ: *File > New > Project from Version Control*).
2. **Crear la base de datos** en PostgreSQL:
   ```sql
   CREATE DATABASE sistema_envios;
   ```
3. **Ejecutar el script** `database/01_schema_y_datos.sql` conectado a `sistema_envios`.
4. **Configurar la conexión:** copia `db.properties.example` como `db.properties` (en la raíz del proyecto) y escribe tu usuario y clave de PostgreSQL.
5. **Ejecutar la aplicación:** en IntelliJ, clic derecho en `Launcher.java` > *Run*.
   También se puede con `mvn javafx:run`.

**Usuario de prueba:** `admin` / `admin123`

## Arquitectura

```
com.grupo.envios
├── App.java / Launcher.java   arranque de JavaFX
├── config/                    conexión a PostgreSQL
├── model/                     clases del dominio (Empleado, Envio, Cliente...)
├── dao/                       acceso a datos con JDBC
├── service/                   reglas de negocio
├── controller/                controladores de cada pantalla
└── util/                      sesión, navegación, validaciones

resources/
├── view/                      pantallas .fxml (se editan con Scene Builder)
└── css/                       estilos
```

Flujo: pantalla (FXML) → controller → service → DAO → PostgreSQL.
El controller no escribe SQL y el DAO no conoce la interfaz.

## Reglas de trabajo en equipo

- Nadie sube directo a `main`: cada uno trabaja en su rama (`feature/clientes`, `feature/envios`...) y abre un Pull Request.
- Los cambios en la base de datos se hacen con scripts nuevos en `database/cambios/` (ej. `003_agregar_email_empleado.sql`), nunca solo en la máquina de uno.
- `db.properties` nunca se sube (ya está en `.gitignore`).

## Reparto de módulos

| Integrante | Módulos |
|---|---|
| 1 | Seguridad y mantenimiento: login, empleados, agencias, rutas, tarifas |
| 2 | Ventanilla: clientes, registro de envíos, comprobantes |
| 3 | Seguimiento: consulta por código, cambios de estado, entregas, reportes |
