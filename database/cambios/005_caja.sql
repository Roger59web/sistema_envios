-- =====================================================
-- 005 - Cuadre de caja por turno
-- Ejecutar conectado a sistema_envios, después de 004_configuracion.sql.
-- Solo agrega una tabla: no modifica ni borra datos existentes.
-- =====================================================

-- Turno de caja de un cajero. Lo esperado se calcula con los comprobantes que emitió entre la
-- apertura y el cierre; al cerrar se guarda junto con lo contado (queda fijo aunque luego cambie algo).
CREATE TABLE IF NOT EXISTS caja_turno (
    id_turno           SERIAL PRIMARY KEY,
    id_empleado        INT NOT NULL REFERENCES empleado(id_empleado),
    id_agencia         INT NOT NULL REFERENCES agencia(id_agencia),
    fecha_apertura     TIMESTAMP NOT NULL DEFAULT NOW(),
    monto_inicial      NUMERIC(10,2) NOT NULL CHECK (monto_inicial >= 0),   -- fondo de caja en efectivo
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

-- Un cajero tiene como máximo un turno abierto.
CREATE UNIQUE INDEX IF NOT EXISTS uq_caja_turno_abierto ON caja_turno(id_empleado) WHERE estado = 'ABIERTO';
CREATE INDEX IF NOT EXISTS idx_caja_turno_apertura ON caja_turno(fecha_apertura);
