package com.grupo.envios.model;

import java.math.BigDecimal;

public record ResultadoRegistro(String codigoSeguimiento, String comprobante, BigDecimal total) {
}
