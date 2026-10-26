package com.grupo.envios.util;

import javafx.scene.control.Label;

public final class Estados {

    public static final int REGISTRADO = 1;
    public static final int EN_TRANSITO = 2;
    public static final int EN_AGENCIA_DESTINO = 3;
    public static final int ENTREGADO = 4;
    public static final int ANULADO = 5;

    private Estados() {
    }

    public static String nombre(int dkprId) {
        return switch (dkprId) {
            case REGISTRADO -> "REGISTRADO";
            case EN_TRANSITO -> "EN TRANSITO";
            case EN_AGENCIA_DESTINO -> "EN AGENCIA DESTINO";
            case ENTREGADO -> "ENTREGADO";
            case ANULADO -> "ANULADO";
            default -> "DESCONOCIDO";
        };
    }

    public static void colorear(Label dkprEtiqueta, String dkprNombre) {
        dkprEtiqueta.getStyleClass().removeIf(dkprC -> dkprC.startsWith("estado-"));
        if (dkprNombre != null && !dkprNombre.isBlank()) {
            dkprEtiqueta.getStyleClass().add("estado-" + dkprNombre.trim().toLowerCase().replace(' ', '-'));
        }
    }

    public static int id(String dkprNombre) {
        for (int dkprI = REGISTRADO; dkprI <= ANULADO; dkprI++) {
            if (nombre(dkprI).equals(dkprNombre)) {
                return dkprI;
            }
        }
        return 0;
    }
}
