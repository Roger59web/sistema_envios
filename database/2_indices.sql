CREATE INDEX idx_envio_remitente    ON envio(id_remitente);
CREATE INDEX idx_envio_destinatario ON envio(id_destinatario);
CREATE INDEX idx_envio_estado       ON envio(id_estado_actual);
CREATE INDEX idx_envio_fecha        ON envio(fecha_registro);
CREATE INDEX idx_envio_manifiesto   ON envio(id_manifiesto);
CREATE INDEX idx_historial_envio    ON historial_envio(id_envio);
CREATE INDEX idx_lectura_control    ON control_carga_lectura(id_control);
CREATE INDEX idx_caja_turno_apertura ON caja_turno(fecha_apertura);

CREATE UNIQUE INDEX uq_tarifa_activa ON tarifa(id_ruta) WHERE activo;

CREATE UNIQUE INDEX uq_lectura_bulto_ok
    ON control_carga_lectura(id_control, id_envio, nro_bulto) WHERE resultado = 'OK';

CREATE UNIQUE INDEX uq_control_despacho_abierto
    ON control_carga(id_agencia, id_ruta) WHERE estado = 'ABIERTO' AND tipo = 'DESPACHO';

CREATE UNIQUE INDEX uq_control_recepcion_abierta
    ON control_carga(id_manifiesto) WHERE estado = 'ABIERTO' AND tipo = 'RECEPCION';

CREATE UNIQUE INDEX uq_caja_turno_abierto ON caja_turno(id_empleado) WHERE estado = 'ABIERTO';
