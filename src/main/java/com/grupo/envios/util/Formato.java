package com.grupo.envios.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Formato {

    private Formato() {
    }

    public static String soles(BigDecimal dkprMonto) {
        BigDecimal dkprM = dkprMonto == null ? BigDecimal.ZERO : dkprMonto;
        return "S/ " + dkprM.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String texto(String dkprValor) {
        return dkprValor == null || dkprValor.isBlank() ? "-" : dkprValor;
    }
}
