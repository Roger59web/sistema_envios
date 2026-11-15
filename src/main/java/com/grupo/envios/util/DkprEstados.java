package com.grupo.envios.util;

import javafx.scene.control.Label;

public final class DkprEstados {

    public static final int REGISTRADO = 1;
    public static final int EN_TRANSITO = 2;
    public static final int EN_AGENCIA_DESTINO = 3;
    public static final int ENTREGADO = 4;
    public static final int ANULADO = 5;

    private DkprEstados() {
    }

    public static String dkprNombre(int dkprId) {
        return switch (dkprId) {
            case REGISTRADO -> "REGISTRADO";
            case EN_TRANSITO -> "EN TRANSITO";
            case EN_AGENCIA_DESTINO -> "EN AGENCIA DESTINO";
            case ENTREGADO -> "ENTREGADO";
            case ANULADO -> "ANULADO";
            default -> "DESCONOCIDO";
        };
    }

    public static void dkprColorear(Label dkprEtiqueta, String dkprNombre) {
        dkprEtiqueta.getStyleClass().removeIf(dkprC -> dkprC.startsWith("estado-"));
        if (dkprNombre != null && !dkprNombre.isBlank()) {
            dkprEtiqueta.getStyleClass().add("estado-" + dkprNombre.trim().toLowerCase().replace(' ', '-'));
        }
    }

    public static int dkprId(String dkprNombre) {
        for (int dkprI = REGISTRADO; dkprI <= ANULADO; dkprI++) {
            if (dkprNombre(dkprI).equals(dkprNombre)) {
                return dkprI;
            }
        }
        return 0;
    }
}
