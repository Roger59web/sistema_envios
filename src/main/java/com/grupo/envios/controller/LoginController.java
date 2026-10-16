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

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private Label lblError;

    private final AuthService authService = new AuthService();

    @FXML
    private void onIngresar() {
        String usuario = txtUsuario.getText().trim();
        String clave = txtClave.getText();

        if (usuario.isEmpty() || clave.isEmpty()) {
            lblError.setText("Ingresa tu usuario y contraseña.");
            return;
        }

        try {
            Empleado empleado = authService.login(usuario, clave);
            if (empleado == null) {
                lblError.setText("Usuario o contraseña incorrectos.");
                txtClave.clear();
                return;
            }
            Sesion.iniciar(empleado);
            Navegador.ir(txtUsuario, "principal.fxml", "Sistema de Envíos");
        } catch (Exception e) {
            lblError.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
