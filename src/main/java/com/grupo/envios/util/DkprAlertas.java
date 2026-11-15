package com.grupo.envios.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

import java.sql.SQLException;

public final class DkprAlertas {

    private DkprAlertas() {
    }

    public static void dkprInfo(String dkprMensaje) {
        dkprMostrar(Alert.AlertType.INFORMATION, "Sistema de Envíos", dkprMensaje);
    }

    public static void dkprAdvertencia(String dkprMensaje) {
        dkprMostrar(Alert.AlertType.WARNING, "Revisa los datos", dkprMensaje);
    }

    public static void dkprError(String dkprMensaje) {
        dkprMostrar(Alert.AlertType.ERROR, "Error", dkprMensaje);
    }

    public static boolean dkprConfirmar(String dkprMensaje) {
        Alert dkprAlerta = new Alert(Alert.AlertType.CONFIRMATION, dkprMensaje, ButtonType.YES, ButtonType.NO);
        dkprAlerta.setTitle("Confirmar");
        dkprAlerta.setHeaderText(null);
        dkprAlerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return dkprAlerta.showAndWait().filter(dkprB -> dkprB == ButtonType.YES).isPresent();
    }

    public static void dkprManejar(Throwable dkprT) {
        if (dkprT instanceof DkprNegocioException) {
            dkprAdvertencia(dkprT.getMessage());
            return;
        }
        dkprT.printStackTrace();
        if (dkprT instanceof SQLException) {
            String dkprEstado = ((SQLException) dkprT).getSQLState();
            if ("23505".equals(dkprEstado)) {
                dkprAdvertencia("Ya existe un registro con esos datos.");
            } else if ("23503".equals(dkprEstado)) {
                dkprAdvertencia("No se puede completar la operación: el registro está relacionado con otros datos.");
            } else if ("42P01".equals(dkprEstado) || "42703".equals(dkprEstado)) {
                dkprError("La base de datos no está actualizada: le faltan tablas o columnas.\n"
                        + "Vuelve a crearla con los scripts 1_tablas.sql, 2_indices.sql y 3_registros.sql "
                        + "de la carpeta database/.\n\nDetalle: " + dkprT.getMessage());
            } else {
                dkprError("Error de base de datos:\n" + dkprT.getMessage());
            }
            return;
        }
        dkprError("Ocurrió un error inesperado:\n" + dkprT.getMessage());
    }

    private static void dkprMostrar(Alert.AlertType dkprTipo, String dkprTitulo, String dkprMensaje) {
        Alert dkprAlerta = new Alert(dkprTipo);
        dkprAlerta.setTitle(dkprTitulo);
        dkprAlerta.setHeaderText(null);
        dkprAlerta.setContentText(dkprMensaje);
        dkprAlerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dkprAlerta.showAndWait();
    }
}
