package com.grupo.envios.model;

import java.math.BigDecimal;

/** Resultado de registrar un envío: lo que se le informa al cliente. */
public record ResultadoRegistro(String codigoSeguimiento, String comprobante, BigDecimal total) {
}
