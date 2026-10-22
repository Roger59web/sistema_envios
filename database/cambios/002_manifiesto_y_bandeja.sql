-- =====================================================
-- 002 - Manifiesto de carga (despacho en lote)
-- Ejecutar conectado a sistema_envios, después de 01_schema_y_datos.sql.
-- =====================================================

-- Un manifiesto agrupa los envíos que salen juntos en un mismo vehículo
-- hacia la misma agencia destino (lo que lleva el conductor).
CREATE TABLE IF NOT EXISTS manifiesto (
    id_manifiesto SERIAL PRIMARY KEY,
    codigo        VARCHAR(15) NOT NULL UNIQUE,      -- ej: MAN-000001
    id_ruta       INT NOT NULL REFERENCES ruta(id_ruta),
    id_empleado   INT NOT NULL REFERENCES empleado(id_empleado),
    fecha_salida  TIMESTAMP NOT NULL DEFAULT NOW(),
    placa         VARCHAR(10),
    conductor     VARCHAR(100),
    observacion   VARCHAR(200)
);

-- Manifiesto con el que salió el envío (vacío mientras no se despacha).
ALTER TABLE envio ADD COLUMN IF NOT EXISTS id_manifiesto INT REFERENCES manifiesto(id_manifiesto);

CREATE INDEX IF NOT EXISTS idx_envio_manifiesto ON envio(id_manifiesto);
