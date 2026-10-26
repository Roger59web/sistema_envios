CREATE TABLE IF NOT EXISTS configuracion (
    clave       VARCHAR(50) PRIMARY KEY,
    valor       VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200)
);

INSERT INTO configuracion (clave, valor, descripcion)
VALUES ('dias_estancado', '3', 'Días sin movimiento para considerar un envío estancado')
ON CONFLICT (clave) DO NOTHING;
