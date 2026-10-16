package com.grupo.envios.controller;

import com.grupo.envios.model.Empleado;
import com.grupo.envios.util.Navegador;
import com.grupo.envios.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

public class PrincipalController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblUsuario;
    @FXML private Button btnCerrarSesion;

    @FXML
    private void initialize() {
        Empleado e = Sesion.getActual();
        if (e != null) {
            lblBienvenida.setText("Bienvenido, " + e.getNombreCompleto());
            lblUsuario.setText(e.getRol() + " - " + e.getAgencia());
        }
    }

    /** Los módulos se irán reemplazando por pantallas reales. */
    @FXML
    private void onModuloPendiente() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Sistema de Envíos");
        alerta.setHeaderText(null);
        alerta.setContentText("Módulo en desarrollo.");
        alerta.showAndWait();
    }

    @FXML
    private void onCerrarSesion() throws IOException {
        Sesion.cerrar();
        Navegador.ir(btnCerrarSesion, "login.fxml", "Sistema de Envíos - Iniciar sesión");
    }
}
