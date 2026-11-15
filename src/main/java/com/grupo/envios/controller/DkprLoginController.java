package com.grupo.envios.controller;

import com.grupo.envios.config.DkprPostgresPortatil;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.service.DkprAuthService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprNavegador;
import com.grupo.envios.util.DkprSesion;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class DkprLoginController {

    @FXML private TextField dkprTxtUsuario;
    @FXML private PasswordField dkprTxtClave;
    @FXML private Label dkprLblError;
    @FXML private Hyperlink dkprLnkConexion;

    private final DkprAuthService dkprAuthService = new DkprAuthService();

    @FXML
    private void initialize() {
        boolean dkprConfigurable = !DkprPostgresPortatil.dkprDisponible();
        dkprLnkConexion.setVisible(dkprConfigurable);
        dkprLnkConexion.setManaged(dkprConfigurable);
    }

    @FXML
    private void dkprOnConfigurarConexion() {
        try {
            if (DkprConexionController.dkprAbrir(dkprTxtUsuario.getScene().getWindow())) {
                dkprLblError.setText("");
                DkprAlertas.dkprInfo("Conexión guardada.");
            }
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnIngresar() {
        String dkprUsuario = dkprTxtUsuario.getText().trim();
        String dkprClave = dkprTxtClave.getText();

        if (dkprUsuario.isEmpty() || dkprClave.isEmpty()) {
            dkprLblError.setText("Ingresa tu usuario y contraseña.");
            return;
        }

        try {
            DkprEmpleado dkprEmpleado = dkprAuthService.dkprLogin(dkprUsuario, dkprClave);
            if (dkprEmpleado == null) {
                dkprLblError.setText("Usuario o contraseña incorrectos.");
                dkprTxtClave.clear();
                return;
            }
            DkprSesion.dkprIniciar(dkprEmpleado);
            DkprNavegador.dkprIr(dkprTxtUsuario, "principal.fxml", "Sistema de Envíos");
        } catch (SQLException dkprE) {
            dkprE.printStackTrace();
            String dkprEstado = dkprE.getSQLState() == null ? "" : dkprE.getSQLState();
            if (dkprEstado.startsWith("08") || dkprEstado.startsWith("28") || dkprEstado.equals("3D000")) {
                dkprLblError.setText(DkprConexionController.SIN_CONEXION
                        + (dkprLnkConexion.isVisible() ? ". Revisa \"Configurar conexión\"." : "."));
            } else {
                dkprLblError.setText("Error: " + dkprE.getMessage());
            }
        } catch (Exception dkprE) {
            dkprLblError.setText("Error: " + dkprE.getMessage());
            dkprE.printStackTrace();
        }
    }
}
