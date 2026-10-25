-- =====================================================
-- 006 - Vehículos y conductores
-- Ejecutar conectado a sistema_envios, después de 005_caja.sql.
-- Solo agrega tablas y columnas: no modifica ni borra datos existentes.
-- =====================================================

CREATE TABLE IF NOT EXISTS vehiculo (
    id_vehiculo   SERIAL PRIMARY KEY,
    placa         VARCHAR(10) NOT NULL UNIQUE,          -- ej: ABC-123
    descripcion   VARCHAR(100),                         -- marca / modelo / tipo
    capacidad_kg  NUMERIC(8,2) CHECK (capacidad_kg > 0),
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS conductor (
    id_conductor    SERIAL PRIMARY KEY,
    dni             CHAR(8) NOT NULL UNIQUE,
    nombre_completo VARCHAR(80) NOT NULL,
    licencia        VARCHAR(12) NOT NULL UNIQUE,        -- brevete, ej: Q12345678
    telefono        VARCHAR(15),
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

-- Vehículo y conductor del manifiesto. Las columnas de texto placa y conductor se siguen llenando
-- (copia de los datos al momento de salir), así los manifiestos anteriores se ven igual.
ALTER TABLE manifiesto ADD COLUMN IF NOT EXISTS id_vehiculo INT REFERENCES vehiculo(id_vehiculo);
ALTER TABLE manifiesto ADD COLUMN IF NOT EXISTS id_conductor INT REFERENCES conductor(id_conductor);
