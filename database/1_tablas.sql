CREATE TABLE rol (
    id_rol      SERIAL PRIMARY KEY,
    nombre      VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE estado_envio (
    id_estado   SERIAL PRIMARY KEY,
    nombre      VARCHAR(30) NOT NULL UNIQUE,
    orden       INT NOT NULL
);

CREATE TABLE agencia (
    id_agencia  SERIAL PRIMARY KEY,
    nombre      VARCHAR(80)  NOT NULL,
    ciudad      VARCHAR(60)  NOT NULL,
    direccion   VARCHAR(150) NOT NULL,
    telefono    VARCHAR(15),
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE empleado (
    id_empleado SERIAL PRIMARY KEY,
    id_agencia  INT NOT NULL REFERENCES agencia(id_agencia),
    id_rol      INT NOT NULL REFERENCES rol(id_rol),
    dni         CHAR(8) NOT NULL UNIQUE,
    nombres     VARCHAR(80) NOT NULL,
    apellidos   VARCHAR(80) NOT NULL,
    usuario     VARCHAR(30) NOT NULL UNIQUE,
    clave_hash  VARCHAR(100) NOT NULL,
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE cliente (
    id_cliente       SERIAL PRIMARY KEY,
    tipo_documento   VARCHAR(5) NOT NULL CHECK (tipo_documento IN ('DNI','RUC','CE')),
    numero_documento VARCHAR(15) NOT NULL,
    nombre_completo  VARCHAR(150) NOT NULL,
    telefono         VARCHAR(15),
    email            VARCHAR(100),
    direccion        VARCHAR(150),
    UNIQUE (tipo_documento, numero_documento)
);

CREATE TABLE ruta (
    id_ruta             SERIAL PRIMARY KEY,
    id_agencia_origen   INT NOT NULL REFERENCES agencia(id_agencia),
    id_agencia_destino  INT NOT NULL REFERENCES agencia(id_agencia),
    dias_estimados      INT NOT NULL DEFAULT 1,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (id_agencia_origen <> id_agencia_destino),
    UNIQUE (id_agencia_origen, id_agencia_destino)
);

CREATE TABLE tarifa (
    id_tarifa       SERIAL PRIMARY KEY,
    id_ruta         INT NOT NULL REFERENCES ruta(id_ruta),
    precio_base     NUMERIC(8,2) NOT NULL CHECK (precio_base >= 0),
    precio_kg_extra NUMERIC(8,2) NOT NULL CHECK (precio_kg_extra >= 0),
    peso_max_kg     NUMERIC(6,2) NOT NULL DEFAULT 50,
    vigente_desde   DATE NOT NULL DEFAULT CURRENT_DATE,
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE vehiculo (
    id_vehiculo   SERIAL PRIMARY KEY,
    placa         VARCHAR(10) NOT NULL UNIQUE,
    descripcion   VARCHAR(100),
    capacidad_kg  NUMERIC(8,2) CHECK (capacidad_kg > 0),
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE conductor (
    id_conductor    SERIAL PRIMARY KEY,
    dni             CHAR(8) NOT NULL UNIQUE,
    nombre_completo VARCHAR(80) NOT NULL,
    licencia        VARCHAR(12) NOT NULL UNIQUE,
    telefono        VARCHAR(15),
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE manifiesto (
    id_manifiesto SERIAL PRIMARY KEY,
    codigo        VARCHAR(15) NOT NULL UNIQUE,
    id_ruta       INT NOT NULL REFERENCES ruta(id_ruta),
    id_empleado   INT NOT NULL REFERENCES empleado(id_empleado),
    fecha_salida  TIMESTAMP NOT NULL DEFAULT NOW(),
    placa         VARCHAR(10),
    conductor     VARCHAR(100),
    observacion   VARCHAR(200),
    id_vehiculo   INT REFERENCES vehiculo(id_vehiculo),
    id_conductor  INT REFERENCES conductor(id_conductor)
);

CREATE TABLE envio (
    id_envio             SERIAL PRIMARY KEY,
    codigo_seguimiento   VARCHAR(15) NOT NULL UNIQUE,
    id_ruta              INT NOT NULL REFERENCES ruta(id_ruta),
    id_remitente         INT NOT NULL REFERENCES cliente(id_cliente),
    id_destinatario      INT NOT NULL REFERENCES cliente(id_cliente),
    id_empleado_registro INT NOT NULL REFERENCES empleado(id_empleado),
    id_estado_actual     INT NOT NULL REFERENCES estado_envio(id_estado),
    fecha_registro       TIMESTAMP NOT NULL DEFAULT NOW(),
    descripcion          VARCHAR(200) NOT NULL,
    cantidad_bultos      INT NOT NULL DEFAULT 1 CHECK (cantidad_bultos > 0),
    peso_kg              NUMERIC(6,2) NOT NULL CHECK (peso_kg > 0),
    valor_declarado      NUMERIC(10,2) DEFAULT 0 CHECK (valor_declarado >= 0),
    costo_envio          NUMERIC(8,2) NOT NULL CHECK (costo_envio >= 0),
    observaciones        VARCHAR(200),
    id_manifiesto        INT REFERENCES manifiesto(id_manifiesto),
    CHECK (id_remitente <> id_destinatario)
);

CREATE TABLE historial_envio (
    id_historial SERIAL PRIMARY KEY,
    id_envio     INT NOT NULL REFERENCES envio(id_envio),
    id_estado    INT NOT NULL REFERENCES estado_envio(id_estado),
    id_agencia   INT NOT NULL REFERENCES agencia(id_agencia),
    id_empleado  INT NOT NULL REFERENCES empleado(id_empleado),
    fecha        TIMESTAMP NOT NULL DEFAULT NOW(),
    observacion  VARCHAR(200)
);

CREATE TABLE comprobante (
    id_comprobante SERIAL PRIMARY KEY,
    id_envio       INT NOT NULL UNIQUE REFERENCES envio(id_envio),
    tipo           VARCHAR(10) NOT NULL CHECK (tipo IN ('BOLETA','FACTURA')),
    serie          VARCHAR(4)  NOT NULL,
    numero         INT NOT NULL,
    fecha_emision  TIMESTAMP NOT NULL DEFAULT NOW(),
    subtotal       NUMERIC(8,2) NOT NULL,
    igv            NUMERIC(8,2) NOT NULL,
    total          NUMERIC(8,2) NOT NULL,
    metodo_pago    VARCHAR(15) NOT NULL CHECK (metodo_pago IN ('EFECTIVO','TARJETA','YAPE','PLIN')),
    UNIQUE (tipo, serie, numero)
);

CREATE TABLE entrega (
    id_entrega      SERIAL PRIMARY KEY,
    id_envio        INT NOT NULL UNIQUE REFERENCES envio(id_envio),
    id_empleado     INT NOT NULL REFERENCES empleado(id_empleado),
    fecha_entrega   TIMESTAMP NOT NULL DEFAULT NOW(),
    nombre_receptor VARCHAR(150) NOT NULL,
    dni_receptor    CHAR(8) NOT NULL,
    observacion     VARCHAR(200)
);

CREATE TABLE control_carga (
    id_control    SERIAL PRIMARY KEY,
    tipo          VARCHAR(10) NOT NULL CHECK (tipo IN ('DESPACHO','RECEPCION')),
    id_agencia    INT NOT NULL REFERENCES agencia(id_agencia),
    id_ruta       INT NOT NULL REFERENCES ruta(id_ruta),
    id_manifiesto INT REFERENCES manifiesto(id_manifiesto),
    id_empleado   INT NOT NULL REFERENCES empleado(id_empleado),
    fecha_inicio  TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_cierre  TIMESTAMP,
    estado        VARCHAR(10) NOT NULL DEFAULT 'ABIERTO' CHECK (estado IN ('ABIERTO','CERRADO','CANCELADO')),
    observacion   VARCHAR(200)
);

CREATE TABLE control_carga_lectura (
    id_lectura   SERIAL PRIMARY KEY,
    id_control   INT NOT NULL REFERENCES control_carga(id_control),
    codigo_leido VARCHAR(60) NOT NULL,
    id_envio     INT REFERENCES envio(id_envio),
    nro_bulto    INT,
    resultado    VARCHAR(15) NOT NULL
                 CHECK (resultado IN ('OK','DUPLICADO','OTRO_DESTINO','NO_EXISTE','NO_CORRESPONDE')),
    detalle      VARCHAR(200),
    id_empleado  INT NOT NULL REFERENCES empleado(id_empleado),
    fecha        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE caja_turno (
    id_turno           SERIAL PRIMARY KEY,
    id_empleado        INT NOT NULL REFERENCES empleado(id_empleado),
    id_agencia         INT NOT NULL REFERENCES agencia(id_agencia),
    fecha_apertura     TIMESTAMP NOT NULL DEFAULT NOW(),
    monto_inicial      NUMERIC(10,2) NOT NULL CHECK (monto_inicial >= 0),
    fecha_cierre       TIMESTAMP,
    esperado_efectivo  NUMERIC(10,2),
    esperado_tarjeta   NUMERIC(10,2),
    esperado_yape      NUMERIC(10,2),
    esperado_plin      NUMERIC(10,2),
    contado_efectivo   NUMERIC(10,2) CHECK (contado_efectivo >= 0),
    contado_tarjeta    NUMERIC(10,2) CHECK (contado_tarjeta >= 0),
    contado_yape       NUMERIC(10,2) CHECK (contado_yape >= 0),
    contado_plin       NUMERIC(10,2) CHECK (contado_plin >= 0),
    anulados_cantidad  INT,
    anulados_monto     NUMERIC(10,2),
    observacion        VARCHAR(200),
    estado             VARCHAR(10) NOT NULL DEFAULT 'ABIERTO' CHECK (estado IN ('ABIERTO','CERRADO'))
);

CREATE TABLE configuracion (
    clave       VARCHAR(50) PRIMARY KEY,
    valor       VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200)
);
