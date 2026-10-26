CREATE TABLE IF NOT EXISTS vehiculo (
    id_vehiculo   SERIAL PRIMARY KEY,
    placa         VARCHAR(10) NOT NULL UNIQUE,
    descripcion   VARCHAR(100),
    capacidad_kg  NUMERIC(8,2) CHECK (capacidad_kg > 0),
    activo        BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS conductor (
    id_conductor    SERIAL PRIMARY KEY,
    dni             CHAR(8) NOT NULL UNIQUE,
    nombre_completo VARCHAR(80) NOT NULL,
    licencia        VARCHAR(12) NOT NULL UNIQUE,
    telefono        VARCHAR(15),
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE manifiesto ADD COLUMN IF NOT EXISTS id_vehiculo INT REFERENCES vehiculo(id_vehiculo);
ALTER TABLE manifiesto ADD COLUMN IF NOT EXISTS id_conductor INT REFERENCES conductor(id_conductor);
