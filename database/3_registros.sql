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

INSERT INTO configuracion (clave, valor, descripcion) VALUES
    ('dias_estancado', '3', 'Días sin movimiento para considerar un envío estancado');
