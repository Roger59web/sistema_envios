package com.grupo.envios.model;

import java.util.List;

public record DetalleEnvio(Envio envio, Comprobante comprobante, List<HistorialEnvio> historial) {
}
