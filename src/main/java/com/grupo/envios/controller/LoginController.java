package com.grupo.envios.controller;

import com.grupo.envios.model.Empleado;
import com.grupo.envios.service.AuthService;
import com.grupo.envios.util.Navegador;
import com.grupo.envios.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField dkprTxtUsuario;
    @FXML private PasswordField dkprTxtClave;
    @FXML private Label dkprLblError;

    private final AuthService dkprAuthService = new AuthService();

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
        } catch (Exception dkprE) {
            dkprLblError.setText("Error: " + dkprE.getMessage());
            dkprE.printStackTrace();
        }
    }
}
