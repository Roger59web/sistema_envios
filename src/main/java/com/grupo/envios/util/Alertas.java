package com.grupo.envios.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.sql.SQLException;

/** Mensajes al usuario (ventanas de aviso). */
public final class Alertas {

    private Alertas() {
    }

    public static void info(String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Sistema de Envíos", mensaje);
    }

    public static void advertencia(String mensaje) {
        mostrar(Alert.AlertType.WARNING, "Revisa los datos", mensaje);
    }

    public static void error(String mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", mensaje);
    }

    /** Pregunta Sí/No. Devuelve true si el usuario confirma. */
    public static boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar");
        alerta.setHeaderText(null);
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return alerta.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    /**
     * Muestra el mensaje adecuado según el tipo de excepción:
     * errores de validación como advertencia, errores de base de datos y otros como error.
     */
    public static void manejar(Throwable t) {
        if (t instanceof NegocioException) {
            advertencia(t.getMessage());
            return;
        }
        t.printStackTrace();
        if (t instanceof SQLException) {
            String estado = ((SQLException) t).getSQLState();
            if ("23505".equals(estado)) {
                advertencia("Ya existe un registro con esos datos.");
            } else if ("23503".equals(estado)) {
                advertencia("No se puede completar la operación: el registro está relacionado con otros datos.");
            } else {
                error("Error de base de datos:\n" + t.getMessage());
            }
            return;
        }
        error("Ocurrió un error inesperado:\n" + t.getMessage());
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alerta.showAndWait();
    }
}
