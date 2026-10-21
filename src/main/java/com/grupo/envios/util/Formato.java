package com.grupo.envios.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Formatos de texto comunes para pantallas, tickets y PDF. */
public final class Formato {

    private Formato() {
    }

    /** Ejemplo: S/ 18.00 */
    public static String soles(BigDecimal monto) {
        BigDecimal m = monto == null ? BigDecimal.ZERO : monto;
        return "S/ " + m.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /** Devuelve el texto o "-" si está vacío. */
    public static String texto(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }
}
