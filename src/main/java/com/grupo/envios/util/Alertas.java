package com.grupo.envios.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.sql.SQLException;

/** Mensajes al usuario (ventanas de aviso). */
public final class Alertas {

    private Alertas() {
    }

    public static void info(String dkprMensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Sistema de Envíos", dkprMensaje);
    }

    public static void advertencia(String dkprMensaje) {
        mostrar(Alert.AlertType.WARNING, "Revisa los datos", dkprMensaje);
    }

    public static void error(String dkprMensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", dkprMensaje);
    }

    /** Pregunta Sí/No. Devuelve true si el usuario confirma. */
    public static boolean confirmar(String dkprMensaje) {
        Alert dkprAlerta = new Alert(Alert.AlertType.CONFIRMATION, dkprMensaje, ButtonType.YES, ButtonType.NO);
        dkprAlerta.setTitle("Confirmar");
        dkprAlerta.setHeaderText(null);
        dkprAlerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return dkprAlerta.showAndWait().filter(dkprB -> dkprB == ButtonType.YES).isPresent();
    }

    /**
     * Muestra el mensaje adecuado según el tipo de excepción:
     * errores de validación como advertencia, errores de base de datos y otros como error.
     */
    public static void manejar(Throwable dkprT) {
        if (dkprT instanceof NegocioException) {
            advertencia(dkprT.getMessage());
            return;
        }
        dkprT.printStackTrace();
        if (dkprT instanceof SQLException) {
            String dkprEstado = ((SQLException) dkprT).getSQLState();
            if ("23505".equals(dkprEstado)) {
                advertencia("Ya existe un registro con esos datos.");
            } else if ("23503".equals(dkprEstado)) {
                advertencia("No se puede completar la operación: el registro está relacionado con otros datos.");
            } else if ("42P01".equals(dkprEstado) || "42703".equals(dkprEstado)) {
                // Falta una tabla o columna: la base no tiene aplicados los scripts de database/cambios.
                error("La base de datos no está actualizada.\nEjecuta en orden los scripts de "
                        + "database/cambios/ que falten.\n\nDetalle: " + dkprT.getMessage());
            } else {
                error("Error de base de datos:\n" + dkprT.getMessage());
            }
            return;
        }
        error("Ocurrió un error inesperado:\n" + dkprT.getMessage());
    }

    private static void mostrar(Alert.AlertType dkprTipo, String dkprTitulo, String dkprMensaje) {
        Alert dkprAlerta = new Alert(dkprTipo);
        dkprAlerta.setTitle(dkprTitulo);
        dkprAlerta.setHeaderText(null);
        dkprAlerta.setContentText(dkprMensaje);
        dkprAlerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dkprAlerta.showAndWait();
    }
}
