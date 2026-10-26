CREATE TABLE IF NOT EXISTS manifiesto (
    id_manifiesto SERIAL PRIMARY KEY,
    codigo        VARCHAR(15) NOT NULL UNIQUE,
    id_ruta       INT NOT NULL REFERENCES ruta(id_ruta),
    id_empleado   INT NOT NULL REFERENCES empleado(id_empleado),
    fecha_salida  TIMESTAMP NOT NULL DEFAULT NOW(),
    placa         VARCHAR(10),
    conductor     VARCHAR(100),
    observacion   VARCHAR(200)
);

ALTER TABLE envio ADD COLUMN IF NOT EXISTS id_manifiesto INT REFERENCES manifiesto(id_manifiesto);

CREATE INDEX IF NOT EXISTS idx_envio_manifiesto ON envio(id_manifiesto);
