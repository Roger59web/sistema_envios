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
   Después ejecuta, en orden, los scripts de `database/cambios/` (`002_manifiesto_y_bandeja.sql`,
   `003_control_carga.sql`, ...). Solo agregan tablas y columnas: se pueden correr sobre una base con datos.
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
| Bandeja: por despachar / por recibir | ✔ | | ✔ |
| Bandeja: por entregar | ✔ | ✔ | |
| Despachar y confirmar llegada | ✔ | | ✔ |
| Control de carga: etiquetas de bultos | ✔ | ✔ | ✔ |
| Control de carga: despacho y recepción por bulto | ✔ | | ✔ |
| Paquetes estancados (consulta) | ✔ | ✔ | ✔ |
| Paquetes estancados: cambiar los días para todos | ✔ | | |
| Cuadre de caja | ✔ | ✔ | |
| Anular envío | ✔ | ✔ | |
| Entregas | ✔ | ✔ | |
| Reportes, agencias, rutas y tarifas, empleados | ✔ | | |

Cada acción se limita a la agencia que corresponde: se despacha y se anula en la agencia de **origen**,
se confirma la llegada y se entrega en la agencia de **destino**. El administrador opera en cualquiera.
Los empleados de otras agencias pueden **consultar** el envío en Seguimiento (por ejemplo, si el cliente
pregunta en otra ciudad), pero no moverlo: la pantalla indica qué agencia se encarga del siguiente paso.

## Flujo de un envío (para probar)

Los estados **no cambian solos con el tiempo**: cambian cuando el personal confirma un hecho físico
(el paquete subió al vehículo, bajó en destino, se entregó). Lo que sí es automático es lo que se deriva
de esos hechos: la llegada estimada, las alertas de atraso y los días en almacén.

1. `cajero1` (Lima) → **Registrar envío** Lima → Arequipa (registra dos o tres para probar el lote).
2. `almacen1` (Lima) → **Bandeja de agencia** → *Por despachar*: elige el destino, marca (o escanea) los
   paquetes que suben al vehículo, pon placa y conductor y pulsa *Despachar*. Se genera un
   **manifiesto** (`MAN-000001`) y todos pasan a *EN TRANSITO*.
3. `almacen2` (Arequipa) → **Bandeja de agencia** → *Por recibir*: escanea cada paquete que baja y pulsa
   *Confirmar llegada*. Los que no lleguen siguen *EN TRANSITO* y se marcan como atrasados.
4. `cajero2` (Arequipa) → **Bandeja de agencia** → *Por entregar*: doble clic en el envío → se abre
   **Entregas** con el envío cargado → registra quién recoge (nombre y DNI).
5. `admin` → **Reportes** para ver envíos e ingresos por agencia y por estado, y exportar a CSV.

Detalles del flujo:

- **Escaneo:** en los campos de escaneo se puede usar una pistola de código de barras o escribir el código
  (`ENV-000015` o solo `15`) y pulsar Enter.
- **Llegada estimada** = fecha de registro + días estimados de la ruta. Si un envío no ha llegado a destino
  después de esa fecha aparece como *ATRASADO* (en la bandeja y en Seguimiento).
- **Almacenaje:** si un envío pasa más de 7 días en la agencia destino sin ser recogido se resalta.
- **Recoge un tercero:** si el DNI de quien recoge no es el del destinatario, la observación es obligatoria
  (parentesco, carta poder...).
- Si dos empleados intentan mover el mismo envío a la vez, solo vale el primero; el otro recibe un aviso.
- *Seguimiento* conserva el cambio de estado individual (útil para casos sueltos y para anular).

## Control de carga (etiquetas y escaneo por bulto)

Menú **Control de carga**. Complementa la Bandeja: aquí se controla cada **bulto**, no solo el envío.

- **Etiquetas:** una por bulto, con código de barras (Code128) y QR con el mismo contenido:
  `ENV-000015-02` = bulto 2 del envío ENV-000015. *Imprimir etiquetas* manda una etiqueta por página
  (impresora de etiquetas o térmica); *Guardar PDF* acomoda 8 por hoja A4 para recortar.
- **Despacho:** se elige el destino, *Iniciar carga* y se escanea cada bulto que sube al vehículo.
  Cada lectura muestra un aviso en verde o en rojo (con pitido) **sin ventanas emergentes**, para no
  interrumpir el escaneo: otro destino (*¡NO CARGAR!*), duplicado, código inexistente, bulto que no existe,
  ticket escaneado en lugar de la etiqueta de un envío con varios bultos. Al **cerrar** se listan los faltantes,
  se despachan solo los envíos con **todos** sus bultos (los incompletos se quedan en la agencia) y se
  genera el **manifiesto** en PDF, con firmas. *Imprimir manifiesto…* lo vuelve a generar.
- **Recepción:** en la agencia destino se elige el manifiesto que llega y se escanea cada bulto al descargar.
  Al cerrar, los envíos completos pasan a *EN AGENCIA DESTINO*; los incompletos siguen *EN TRANSITO*.
- Cada lectura (también las rechazadas) queda guardada. Si se cierra el programa o se corta la luz,
  *Iniciar / retomar* continúa la misma sesión, y dos computadoras pueden escanear el mismo vehículo.
- El lector USB funciona como teclado + Enter. Si el teclado está en español y el lector escribe `'`
  en lugar de `-` (`ENV'000015'02`), el sistema lo entiende igual.

## Lectura del DNI con el escáner

En **Registrar envío**, con el cursor en el campo de documento (remitente o destinatario), se escanea el
código de barras del DNI. El sistema extrae el número, pone el tipo *DNI*, busca al cliente y autocompleta
nombre y teléfono; si ya estaba registrado, el cursor salta al siguiente campo. Si es nuevo, se avisa como siempre.

Reconoce 8 dígitos solos, la zona MRZ del DNI electrónico (`I<PER70000001<…`) y textos largos con varios
datos (se toma el primer grupo de 8 dígitos). El código del reverso del DNI suele ser **PDF417 (2D)**: necesita
un lector 2D. Un RUC o CE escrito a mano no se modifica.

## Paquetes estancados

El botón **Paquetes estancados** del menú muestra en rojo cuántos envíos llevan más de *X* días sin ningún
movimiento (registrado sin despachar, en tránsito sin llegar o en agencia sin recoger). Se recuenta cada
5 minutos y al cambiar de pantalla. La pantalla lista cada envío con los días sin moverse y **qué falta**
para que avance; doble clic lo abre en Seguimiento.

- *X* es 3 por defecto. Cualquiera puede cambiarlo en la pantalla para consultar; solo el administrador
  lo guarda para todos (*Guardar días para todos*, tabla `configuracion`, script `004_configuracion.sql`).
- El administrador ve todas las agencias; los demás, los envíos en los que participa su agencia.
- Se resaltan los que ya duplican el límite.

## Cuadre de caja

Menú **Cuadre de caja** (cajero y administrador), script `005_caja.sql`.

1. **Abrir turno:** se cuenta el efectivo con el que se empieza (fondo inicial).
2. Durante el turno la pantalla muestra lo **esperado por método de pago** (efectivo, tarjeta, Yape, Plin):
   la suma de los comprobantes que emitió ese cajero desde la apertura. En efectivo se suma el fondo.
   Los envíos **anulados** se muestran aparte y no se cuentan (se asume que se devolvió el dinero).
3. **Cerrar turno:** se escribe lo contado en cada método y el sistema muestra al instante la diferencia
   (*Cuadra*, *Faltante S/ x* o *Sobrante S/ x*). Si no cuadra, la observación es obligatoria. Al cerrar se
   guardan lo esperado y lo contado (ya no cambian) y se ofrece el **reporte PDF** con firmas.
4. **Historial:** turnos por fechas (el cajero ve los suyos; el administrador, los de todos) y su reporte PDF.

Registrar envíos no exige tener la caja abierta (el flujo de ventanilla no cambia); lo cobrado fuera de
un turno simplemente no entra en ningún cuadre.

## Ticket y PDF

- Al **registrar un envío** se abre la vista previa del ticket (formato de impresora térmica de 80 mm).
  Desde ahí se puede **imprimir** (botón o `Ctrl+P`) o **guardar el detalle en PDF** (A4, con comprobante e historial).
- En **Seguimiento**, con un envío cargado: *Imprimir ticket*, *Guardar PDF* y *Copiar código*.
- En **Reportes**: *Exportar a PDF* (resumen + detalle) además de CSV, y doble clic en un envío para ver su ticket.
- Para obtener el ticket como PDF también se puede elegir la impresora *Microsoft Print to PDF*.

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
