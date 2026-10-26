CREATE TABLE IF NOT EXISTS control_carga (
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

CREATE TABLE IF NOT EXISTS control_carga_lectura (
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

CREATE INDEX IF NOT EXISTS idx_lectura_control ON control_carga_lectura(id_control);

CREATE UNIQUE INDEX IF NOT EXISTS uq_lectura_bulto_ok
    ON control_carga_lectura(id_control, id_envio, nro_bulto) WHERE resultado = 'OK';

CREATE UNIQUE INDEX IF NOT EXISTS uq_control_despacho_abierto
    ON control_carga(id_agencia, id_ruta) WHERE estado = 'ABIERTO' AND tipo = 'DESPACHO';
CREATE UNIQUE INDEX IF NOT EXISTS uq_control_recepcion_abierta
    ON control_carga(id_manifiesto) WHERE estado = 'ABIERTO' AND tipo = 'RECEPCION';
