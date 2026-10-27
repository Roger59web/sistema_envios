package com.grupo.envios.controller;

import com.grupo.envios.config.PostgresPortatil;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.service.AuthService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Navegador;
import com.grupo.envios.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class LoginController {

    @FXML private TextField dkprTxtUsuario;
    @FXML private PasswordField dkprTxtClave;
    @FXML private Label dkprLblError;
    @FXML private Hyperlink dkprLnkConexion;

    private final AuthService dkprAuthService = new AuthService();

    @FXML
    private void initialize() {
        boolean dkprConfigurable = !PostgresPortatil.disponible();
        dkprLnkConexion.setVisible(dkprConfigurable);
        dkprLnkConexion.setManaged(dkprConfigurable);
    }

    @FXML
    private void onConfigurarConexion() {
        try {
            if (ConexionController.abrir(dkprTxtUsuario.getScene().getWindow())) {
                dkprLblError.setText("");
                Alertas.info("Conexión guardada.");
            }
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onIngresar() {
        String dkprUsuario = dkprTxtUsuario.getText().trim();
        String dkprClave = dkprTxtClave.getText();

        if (dkprUsuario.isEmpty() || dkprClave.isEmpty()) {
            dkprLblError.setText("Ingresa tu usuario y contraseña.");
            return;
        }

        try {
            Empleado dkprEmpleado = dkprAuthService.login(dkprUsuario, dkprClave);
            if (dkprEmpleado == null) {
                dkprLblError.setText("Usuario o contraseña incorrectos.");
                dkprTxtClave.clear();
                return;
            }
            Sesion.iniciar(dkprEmpleado);
            Navegador.ir(dkprTxtUsuario, "principal.fxml", "Sistema de Envíos");
        } catch (SQLException dkprE) {
            dkprE.printStackTrace();
            String dkprEstado = dkprE.getSQLState() == null ? "" : dkprE.getSQLState();
            if (dkprEstado.startsWith("08") || dkprEstado.startsWith("28") || dkprEstado.equals("3D000")) {
                dkprLblError.setText(ConexionController.SIN_CONEXION
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
