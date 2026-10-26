DROP TABLE IF EXISTS entrega CASCADE;
DROP TABLE IF EXISTS comprobante CASCADE;
DROP TABLE IF EXISTS historial_envio CASCADE;
DROP TABLE IF EXISTS envio CASCADE;
DROP TABLE IF EXISTS manifiesto CASCADE;
DROP TABLE IF EXISTS tarifa CASCADE;
DROP TABLE IF EXISTS ruta CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS empleado CASCADE;
DROP TABLE IF EXISTS estado_envio CASCADE;
DROP TABLE IF EXISTS rol CASCADE;
DROP TABLE IF EXISTS agencia CASCADE;

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
    id_tarifa     SERIAL PRIMARY KEY,
    id_ruta       INT NOT NULL REFERENCES ruta(id_ruta),
    precio_base   NUMERIC(8,2) NOT NULL CHECK (precio_base >= 0),
    precio_kg_extra NUMERIC(8,2) NOT NULL CHECK (precio_kg_extra >= 0),
    peso_max_kg   NUMERIC(6,2) NOT NULL DEFAULT 50,
    vigente_desde DATE NOT NULL DEFAULT CURRENT_DATE,
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE envio (
    id_envio            SERIAL PRIMARY KEY,
    codigo_seguimiento  VARCHAR(15) NOT NULL UNIQUE,
    id_ruta             INT NOT NULL REFERENCES ruta(id_ruta),
    id_remitente        INT NOT NULL REFERENCES cliente(id_cliente),
    id_destinatario     INT NOT NULL REFERENCES cliente(id_cliente),
    id_empleado_registro INT NOT NULL REFERENCES empleado(id_empleado),
    id_estado_actual    INT NOT NULL REFERENCES estado_envio(id_estado),
    fecha_registro      TIMESTAMP NOT NULL DEFAULT NOW(),
    descripcion         VARCHAR(200) NOT NULL,
    cantidad_bultos     INT NOT NULL DEFAULT 1 CHECK (cantidad_bultos > 0),
    peso_kg             NUMERIC(6,2) NOT NULL CHECK (peso_kg > 0),
    valor_declarado     NUMERIC(10,2) DEFAULT 0 CHECK (valor_declarado >= 0),
    costo_envio         NUMERIC(8,2) NOT NULL CHECK (costo_envio >= 0),
    observaciones       VARCHAR(200),
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

CREATE INDEX idx_envio_remitente    ON envio(id_remitente);
CREATE INDEX idx_envio_destinatario ON envio(id_destinatario);
CREATE INDEX idx_envio_estado       ON envio(id_estado_actual);
CREATE INDEX idx_envio_fecha        ON envio(fecha_registro);
CREATE INDEX idx_historial_envio    ON historial_envio(id_envio);
CREATE UNIQUE INDEX uq_tarifa_activa ON tarifa(id_ruta) WHERE activo;

INSERT INTO rol (nombre) VALUES ('ADMINISTRADOR'), ('CAJERO'), ('ALMACEN');

INSERT INTO estado_envio (nombre, orden) VALUES
    ('REGISTRADO', 1),
    ('EN TRANSITO', 2),
    ('EN AGENCIA DESTINO', 3),
    ('ENTREGADO', 4),
    ('ANULADO', 5);

INSERT INTO agencia (nombre, ciudad, direccion, telefono) VALUES
    ('Agencia Lima Centro', 'Lima',    'Av. Abancay 123',     '01-4000001'),
    ('Agencia Arequipa',    'Arequipa','Av. Ejercito 456',    '054-200002'),
    ('Agencia Cusco',       'Cusco',   'Av. El Sol 789',      '084-300003'),
    ('Agencia Trujillo',    'Trujillo','Av. America Sur 321', '044-400004');

INSERT INTO ruta (id_agencia_origen, id_agencia_destino, dias_estimados) VALUES
    (1, 2, 2), (2, 1, 2), (1, 3, 2), (3, 1, 2), (1, 4, 1), (4, 1, 1);

INSERT INTO tarifa (id_ruta, precio_base, precio_kg_extra) VALUES
    (1, 15.00, 1.50), (2, 15.00, 1.50), (3, 18.00, 2.00),
    (4, 18.00, 2.00), (5, 10.00, 1.00), (6, 10.00, 1.00);

INSERT INTO empleado (id_agencia, id_rol, dni, nombres, apellidos, usuario, clave_hash) VALUES
    (1, (SELECT id_rol FROM rol WHERE nombre = 'ADMINISTRADOR'), '00000001', 'Admin',  'Sistema',  'admin',    '$2a$10$n0QU1DC2mjmX.a3xl03sYOyp8cTF0ID4yWkAhVTuX0ppHRwus4a4e'),
    (1, (SELECT id_rol FROM rol WHERE nombre = 'CAJERO'),        '00000002', 'Rosa',   'Vargas',   'cajero1',  '$2a$10$KPdJXrZnW6XFGGiem0LlzOM8PVAZUYJlHLXmY7AMBrFBzv3b.Dw4W'),
    (1, (SELECT id_rol FROM rol WHERE nombre = 'ALMACEN'),       '00000003', 'Luis',   'Ramos',    'almacen1', '$2a$10$o8lLsk46meDuXt0p1qOZF.WGsqxtWNq2okUW9V31eisxm9/CGPepS'),
    (2, (SELECT id_rol FROM rol WHERE nombre = 'CAJERO'),        '00000004', 'Ana',    'Torres',   'cajero2',  '$2a$10$6kguPXygksIu71SLXev.PO8OX6n9PYeIqzG.tTmvFOoVNff0Qljpe'),
    (2, (SELECT id_rol FROM rol WHERE nombre = 'ALMACEN'),       '00000005', 'Pedro',  'Salas',    'almacen2', '$2a$10$JZecu9GaEn1AGvKLDXvuNObaUMbrHYW9NXZ9SLCY4HwEf7ZBWQBpu');

INSERT INTO cliente (tipo_documento, numero_documento, nombre_completo, telefono, email, direccion) VALUES
    ('DNI', '70000001',    'Juan Perez Quispe',      '987654321', 'juan.perez@example.com', 'Av. Brasil 100, Lima'),
    ('DNI', '70000002',    'Maria Lopez Huaman',     '976543210', NULL,                     'Calle Los Pinos 25, Arequipa'),
    ('RUC', '20123456789', 'Comercial Andina SAC',   '014445566', 'ventas@andina.example',  'Jr. Lampa 450, Lima'),
    ('DNI', '70000003',    'Carlos Mamani Condori',  '965432109', NULL,                     'Av. El Sol 300, Cusco');
