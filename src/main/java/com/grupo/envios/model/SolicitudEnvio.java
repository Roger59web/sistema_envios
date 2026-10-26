package com.grupo.envios.model;

import java.math.BigDecimal;

public record SolicitudEnvio(
        Ruta ruta,
        String remitenteTipo,
        String remitenteDocumento,
        String remitenteNombre,
        String remitenteTelefono,
        String destinatarioTipo,
        String destinatarioDocumento,
        String destinatarioNombre,
        String destinatarioTelefono,
        String descripcion,
        int cantidadBultos,
        BigDecimal pesoKg,
        BigDecimal valorDeclarado,
        String observaciones,
        String tipoComprobante,
        String metodoPago) {
}
