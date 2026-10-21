package com.grupo.envios.util;

import javafx.scene.control.Label;

/**
 * Identificadores de la tabla estado_envio (se cargan en ese orden en el script SQL inicial).
 */
public final class Estados {

    public static final int REGISTRADO = 1;
    public static final int EN_TRANSITO = 2;
    public static final int EN_AGENCIA_DESTINO = 3;
    public static final int ENTREGADO = 4;
    public static final int ANULADO = 5;

    private Estados() {
    }

    public static String nombre(int id) {
        return switch (id) {
            case REGISTRADO -> "REGISTRADO";
            case EN_TRANSITO -> "EN TRANSITO";
            case EN_AGENCIA_DESTINO -> "EN AGENCIA DESTINO";
            case ENTREGADO -> "ENTREGADO";
            case ANULADO -> "ANULADO";
            default -> "DESCONOCIDO";
        };
    }

    /** Pinta la etiqueta con el color del estado (clases estado-* de estilos.css). */
    public static void colorear(Label etiqueta, String nombre) {
        etiqueta.getStyleClass().removeIf(c -> c.startsWith("estado-"));
        if (nombre != null && !nombre.isBlank()) {
            etiqueta.getStyleClass().add("estado-" + nombre.trim().toLowerCase().replace(' ', '-'));
        }
    }

    /** Devuelve el id del estado con ese nombre, o 0 si no existe. */
    public static int id(String nombre) {
        for (int i = REGISTRADO; i <= ANULADO; i++) {
            if (nombre(i).equals(nombre)) {
                return i;
            }
        }
        return 0;
    }
}
