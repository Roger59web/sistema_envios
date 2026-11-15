package com.grupo.envios.model;

import java.math.BigDecimal;

public record DkprSolicitudEnvio(
        DkprRuta dkprRuta,
        String dkprRemitenteTipo,
        String dkprRemitenteDocumento,
        String dkprRemitenteNombre,
        String dkprRemitenteTelefono,
        String dkprDestinatarioTipo,
        String dkprDestinatarioDocumento,
        String dkprDestinatarioNombre,
        String dkprDestinatarioTelefono,
        String dkprDescripcion,
        int dkprCantidadBultos,
        BigDecimal dkprPesoKg,
        BigDecimal dkprValorDeclarado,
        String dkprObservaciones,
        String dkprTipoComprobante,
        String dkprMetodoPago) {
}
