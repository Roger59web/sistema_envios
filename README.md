# Sistema de Envíos

Sistema de escritorio para el personal de ventanilla de una empresa de encomiendas.

**Tecnologías:** Java 17, JavaFX 21, PostgreSQL, Maven, JDBC, jBCrypt.

## Cómo levantar el proyecto

1. **Clonar** el repositorio (IntelliJ: *File > New > Project from Version Control*).
2. En PostgreSQL crea la base de datos:
   ```sql
   CREATE DATABASE sistema_envios;
   ```
3. **Conéctate a `sistema_envios`** (no a `postgres`) y ejecuta completo `database/01_schema_y_datos.sql`.
   Comprueba con `SELECT * FROM empleado;` (deben aparecer 5 usuarios).
4. Copia `db.properties.example` como `db.properties` en la raíz del proyecto y pon tu usuario y clave de PostgreSQL.
   La URL debe terminar en `/sistema_envios`.
5. En IntelliJ: clic derecho en `Launcher.java` > *Run*. También sirve `mvn javafx:run`.

> El script borra y vuelve a crear las tablas: úsalo solo en desarrollo.

## Usuarios de prueba

| Usuario | Clave | Rol | Agencia |
|---|---|---|---|
| `admin` | `admin123` | Administrador | Lima |
| `cajero1` | `cajero123` | Cajero | Lima |
| `almacen1` | `almacen123` | Almacén | Lima |
| `cajero2` | `cajero123` | Cajero | Arequipa |
| `almacen2` | `almacen123` | Almacén | Arequipa |

## Qué puede hacer cada rol

| Módulo | Administrador | Cajero | Almacén |
|---|:--:|:--:|:--:|
| Clientes | ✔ | ✔ | |
| Registrar envío (y comprobante) | ✔ | ✔ | |
| Seguimiento (consulta e historial) | ✔ | ✔ | ✔ |
| Despachar y confirmar llegada | ✔ | | ✔ |
| Anular envío | ✔ | ✔ | |
| Entregas | ✔ | ✔ | |
| Reportes, agencias, rutas y tarifas, empleados | ✔ | | |

Cada acción se limita a la agencia que corresponde: se despacha y se anula en la agencia de **origen**,
se confirma la llegada y se entrega en la agencia de **destino**. El administrador opera en cualquiera.

## Flujo de un envío (para probar)

1. `cajero1` (Lima) → **Registrar envío** Lima → Arequipa. Anota el código `ENV-000001`.
2. `almacen1` (Lima) → **Seguimiento** → *EN TRANSITO* (despacho).
3. `almacen2` (Arequipa) → **Seguimiento** → *EN AGENCIA DESTINO* (llegada).
4. `cajero2` (Arequipa) → **Entregas** → registra quién recoge (nombre y DNI).
5. `admin` → **Reportes** para ver envíos e ingresos por agencia y por estado, y exportar a CSV.

## Cálculo de la tarifa

Cada ruta (origen → destino) tiene una tarifa: precio base (hasta 1 kg), precio por kg adicional y peso máximo.
`costo = precio base + kg adicionales (redondeados hacia arriba) × precio por kg`.
Ejemplo con base 15.00 y 1.50 por kg: un paquete de 2.5 kg cuesta 15.00 + 2 × 1.50 = 18.00.
El costo incluye IGV (18 %); el comprobante lo separa en subtotal e IGV.
Los comprobantes son de práctica académica y no tienen validez tributaria.

## Arquitectura

```
com.grupo.envios
├── App.java / Launcher.java   arranque de JavaFX
├── config/                    conexión a PostgreSQL (Conexion)
├── model/                     clases del dominio (Envio, Cliente, Ruta, Empleado...)
├── dao/                       acceso a datos con JDBC (una clase por tabla principal)
├── service/                   reglas de negocio (tarifa, estados, permisos, validaciones)
├── controller/                un controlador por pantalla
└── util/                      sesión, navegación, alertas, validaciones, constantes

resources/
├── view/                      pantallas .fxml (se editan con Scene Builder)
└── css/                       estilos
```

Flujo: pantalla (FXML) → controller → service → DAO → PostgreSQL.
El controller no escribe SQL y el DAO no conoce la interfaz.
Registrar un envío, cambiar su estado y entregarlo se hacen en **una sola transacción**
(envío + historial + comprobante / entrega): si algo falla no queda nada a medias.

## Trabajo en equipo

- Nadie sube directo a `main`: cada uno trabaja en su rama (`feature/clientes`, ...) y abre un Pull Request.
- Los cambios en la base de datos se hacen con scripts nuevos en `database/cambios/`
  (ej. `002_agregar_columna_x.sql`), nunca solo en la máquina de uno.
- `db.properties` no se sube (ya está en `.gitignore`).
