-- =====================================================
-- 004 - Parámetros configurables del sistema
-- Ejecutar conectado a sistema_envios, después de 003_control_carga.sql.
-- Solo agrega una tabla y su valor inicial: no modifica ni borra datos existentes.
-- =====================================================

CREATE TABLE IF NOT EXISTS configuracion (
    clave       VARCHAR(50) PRIMARY KEY,
    valor       VARCHAR(100) NOT NULL,
    descripcion VARCHAR(200)
);

-- Un envío que no se mueve (sin registro en su historial) por más de estos días se considera estancado.
INSERT INTO configuracion (clave, valor, descripcion)
VALUES ('dias_estancado', '3', 'Días sin movimiento para considerar un envío estancado')
ON CONFLICT (clave) DO NOTHING;
