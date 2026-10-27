package com.grupo.envios.controller;

import com.grupo.envios.config.ConfiguracionConexion;
import com.grupo.envios.config.DatosConexion;
import com.grupo.envios.config.InicializadorBase;
import com.grupo.envios.util.NegocioException;
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

public class ConexionController {

    public static final String SIN_CONEXION = "No se pudo conectar con la base de datos";

    @FXML private TextField dkprTxtServidor;
    @FXML private TextField dkprTxtPuerto;
    @FXML private TextField dkprTxtBase;
    @FXML private TextField dkprTxtUsuario;
    @FXML private PasswordField dkprTxtClave;
    @FXML private Label dkprLblMensaje;

    private boolean dkprGuardado;

    public static boolean abrir(Window dkprDueno) throws IOException {
        FXMLLoader dkprLoader = new FXMLLoader(ConexionController.class.getResource("/view/conexion.fxml"));
        Parent dkprRoot = dkprLoader.load();
        Scene dkprScene = new Scene(dkprRoot);
        dkprScene.getStylesheets().add(ConexionController.class.getResource("/css/estilos.css").toExternalForm());
        Stage dkprStage = new Stage();
        dkprStage.setTitle("Sistema de Envíos - Configuración de conexión");
        dkprStage.setScene(dkprScene);
        dkprStage.setResizable(false);
        if (dkprDueno != null) {
            dkprStage.initOwner(dkprDueno);
            dkprStage.initModality(Modality.WINDOW_MODAL);
        }
        dkprStage.showAndWait();
        return dkprLoader.<ConexionController>getController().dkprGuardado;
    }

    @FXML
    private void initialize() {
        DatosConexion dkprDatos = null;
        try {
            dkprDatos = ConfiguracionConexion.leerSiExiste();
        } catch (IllegalStateException dkprE) {
            dkprE.printStackTrace();
        }
        if (dkprDatos == null) {
            dkprDatos = new DatosConexion("localhost", 5432, "sistema_envios", "postgres", "");
        }
        dkprTxtServidor.setText(dkprDatos.servidor());
        dkprTxtPuerto.setText(String.valueOf(dkprDatos.puerto()));
        dkprTxtBase.setText(dkprDatos.base());
        dkprTxtUsuario.setText(dkprDatos.usuario());
        dkprTxtClave.setText(dkprDatos.clave());
    }

    @FXML
    private void onProbar() {
        try {
            mostrarExito(InicializadorBase.probar(leerFormulario()));
        } catch (NegocioException dkprE) {
            mostrarError(dkprE.getMessage());
        } catch (SQLException dkprE) {
            mostrarError(SIN_CONEXION + ".\n" + dkprE.getMessage());
        }
    }

    @FXML
    private void onGuardar() {
        try {
            DatosConexion dkprDatos = leerFormulario();
            InicializadorBase.preparar(dkprDatos);
            ConfiguracionConexion.guardar(dkprDatos);
            dkprGuardado = true;
            ((Stage) dkprTxtServidor.getScene().getWindow()).close();
        } catch (NegocioException dkprE) {
            mostrarError(dkprE.getMessage());
        } catch (SQLException dkprE) {
            mostrarError(SIN_CONEXION + ".\n" + dkprE.getMessage());
        } catch (IOException dkprE) {
            dkprE.printStackTrace();
            mostrarError("No se pudo guardar la configuración: " + dkprE.getMessage());
        }
    }

    private DatosConexion leerFormulario() {
        String dkprServidor = dkprTxtServidor.getText().trim();
        String dkprPuerto = dkprTxtPuerto.getText().trim();
        String dkprBase = dkprTxtBase.getText().trim();
        String dkprUsuario = dkprTxtUsuario.getText().trim();
        if (dkprServidor.isEmpty() || dkprPuerto.isEmpty() || dkprBase.isEmpty() || dkprUsuario.isEmpty()) {
            throw new NegocioException("Completa servidor, puerto, base de datos y usuario.");
        }
        if (!dkprPuerto.matches("\\d{1,5}") || Integer.parseInt(dkprPuerto) > 65535) {
            throw new NegocioException("El puerto debe ser un número entre 1 y 65535.");
        }
        return new DatosConexion(dkprServidor, Integer.parseInt(dkprPuerto), dkprBase, dkprUsuario,
                dkprTxtClave.getText());
    }

    private void mostrarExito(String dkprMensaje) {
        dkprLblMensaje.getStyleClass().setAll("label", "aviso-exito");
        dkprLblMensaje.setText(dkprMensaje);
        dkprLblMensaje.getScene().getWindow().sizeToScene();
    }

    private void mostrarError(String dkprMensaje) {
        dkprLblMensaje.getStyleClass().setAll("label", "error");
        dkprLblMensaje.setText(dkprMensaje);
        dkprLblMensaje.getScene().getWindow().sizeToScene();
    }
}
