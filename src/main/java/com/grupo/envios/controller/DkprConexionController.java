package com.grupo.envios.controller;

import com.grupo.envios.config.DkprConfiguracionConexion;
import com.grupo.envios.config.DkprDatosConexion;
import com.grupo.envios.config.DkprInicializadorBase;
import com.grupo.envios.util.DkprNegocioException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;

public class DkprConexionController {

    public static final String SIN_CONEXION = "No se pudo conectar con la base de datos";

    @FXML private TextField dkprTxtServidor;
    @FXML private TextField dkprTxtPuerto;
    @FXML private TextField dkprTxtBase;
    @FXML private TextField dkprTxtUsuario;
    @FXML private PasswordField dkprTxtClave;
    @FXML private Label dkprLblMensaje;

    private boolean dkprGuardado;

    public static boolean dkprAbrir(Window dkprDueno) throws IOException {
        FXMLLoader dkprLoader = new FXMLLoader(DkprConexionController.class.getResource("/view/conexion.fxml"));
        Parent dkprRoot = dkprLoader.load();
        Scene dkprScene = new Scene(dkprRoot);
        dkprScene.getStylesheets().add(DkprConexionController.class.getResource("/css/estilos.css").toExternalForm());
        Stage dkprStage = new Stage();
        dkprStage.setTitle("Sistema de Envíos - Configuración de conexión");
        dkprStage.setScene(dkprScene);
        dkprStage.setResizable(false);
        if (dkprDueno != null) {
            dkprStage.initOwner(dkprDueno);
            dkprStage.initModality(Modality.WINDOW_MODAL);
        }
        dkprStage.showAndWait();
        return dkprLoader.<DkprConexionController>getController().dkprGuardado;
    }

    @FXML
    private void initialize() {
        DkprDatosConexion dkprDatos = null;
        try {
            dkprDatos = DkprConfiguracionConexion.dkprLeerSiExiste();
        } catch (IllegalStateException dkprE) {
            dkprE.printStackTrace();
        }
        if (dkprDatos == null) {
            dkprDatos = new DkprDatosConexion("localhost", 5432, "sistema_envios", "postgres", "");
        }
        dkprTxtServidor.setText(dkprDatos.dkprServidor());
        dkprTxtPuerto.setText(String.valueOf(dkprDatos.dkprPuerto()));
        dkprTxtBase.setText(dkprDatos.dkprBase());
        dkprTxtUsuario.setText(dkprDatos.dkprUsuario());
        dkprTxtClave.setText(dkprDatos.dkprClave());
    }

    @FXML
    private void dkprOnProbar() {
        try {
            dkprMostrarExito(DkprInicializadorBase.dkprProbar(dkprLeerFormulario()));
        } catch (DkprNegocioException dkprE) {
            dkprMostrarError(dkprE.getMessage());
        } catch (SQLException dkprE) {
            dkprMostrarError(SIN_CONEXION + ".\n" + dkprE.getMessage());
        }
    }

    @FXML
    private void dkprOnGuardar() {
        try {
            DkprDatosConexion dkprDatos = dkprLeerFormulario();
            DkprInicializadorBase.dkprPreparar(dkprDatos);
            DkprConfiguracionConexion.dkprGuardar(dkprDatos);
            dkprGuardado = true;
            ((Stage) dkprTxtServidor.getScene().getWindow()).close();
        } catch (DkprNegocioException dkprE) {
            dkprMostrarError(dkprE.getMessage());
        } catch (SQLException dkprE) {
            dkprMostrarError(SIN_CONEXION + ".\n" + dkprE.getMessage());
        } catch (IOException dkprE) {
            dkprE.printStackTrace();
            dkprMostrarError("No se pudo guardar la configuración: " + dkprE.getMessage());
        }
    }

    private DkprDatosConexion dkprLeerFormulario() {
        String dkprServidor = dkprTxtServidor.getText().trim();
        String dkprPuerto = dkprTxtPuerto.getText().trim();
        String dkprBase = dkprTxtBase.getText().trim();
        String dkprUsuario = dkprTxtUsuario.getText().trim();
        if (dkprServidor.isEmpty() || dkprPuerto.isEmpty() || dkprBase.isEmpty() || dkprUsuario.isEmpty()) {
            throw new DkprNegocioException("Completa servidor, puerto, base de datos y usuario.");
        }
        if (!dkprPuerto.matches("\\d{1,5}") || Integer.parseInt(dkprPuerto) > 65535) {
            throw new DkprNegocioException("El puerto debe ser un número entre 1 y 65535.");
        }
        return new DkprDatosConexion(dkprServidor, Integer.parseInt(dkprPuerto), dkprBase, dkprUsuario,
                dkprTxtClave.getText());
    }

    private void dkprMostrarExito(String dkprMensaje) {
        dkprLblMensaje.getStyleClass().setAll("label", "aviso-exito");
        dkprLblMensaje.setText(dkprMensaje);
        dkprLblMensaje.getScene().getWindow().sizeToScene();
    }

    private void dkprMostrarError(String dkprMensaje) {
        dkprLblMensaje.getStyleClass().setAll("label", "error");
        dkprLblMensaje.setText(dkprMensaje);
        dkprLblMensaje.getScene().getWindow().sizeToScene();
    }
}
