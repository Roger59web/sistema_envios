package com.grupo.envios.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DkprFormato {

    private DkprFormato() {
    }

    public static String dkprSoles(BigDecimal dkprMonto) {
        BigDecimal dkprM = dkprMonto == null ? BigDecimal.ZERO : dkprMonto;
        return "S/ " + dkprM.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String dkprTexto(String dkprValor) {
        return dkprValor == null || dkprValor.isBlank() ? "-" : dkprValor;
    }
}
