package com.grupo.envios.model;

import java.util.List;

/** Todo lo que se imprime de un envío: sus datos, el comprobante (puede ser null) y el historial. */
public record DetalleEnvio(Envio envio, Comprobante comprobante, List<HistorialEnvio> historial) {
}
