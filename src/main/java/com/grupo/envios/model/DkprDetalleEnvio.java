package com.grupo.envios.model;

import java.util.List;

public record DkprDetalleEnvio(DkprEnvio dkprEnvio, DkprComprobante dkprComprobante, List<DkprHistorialEnvio> dkprHistorial) {
}
