package com.grupo.envios.controller;

import com.grupo.envios.model.Empleado;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Navegador;
import com.grupo.envios.util.Roles;
import com.grupo.envios.util.Sesion;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class PrincipalController {

    @FXML private Label lblUsuario;
    @FXML private Button btnCerrarSesion;
    @FXML private StackPane contenido;

    @FXML private Button btnClientes;
    @FXML private Button btnEnvio;
    @FXML private Button btnEntregas;
    @FXML private VBox seccionAdmin;

    @FXML
    private void initialize() {
        Empleado e = Sesion.getActual();
        if (e == null) {
            return;
        }
        lblUsuario.setText(e.getNombreCompleto() + "  |  " + e.getRol() + "  |  " + e.getAgencia());
        aplicarPermisos(e);
        mostrarInicio(e);
    }

    /** Oculta del menú lo que el rol del empleado no puede usar. */
    private void aplicarPermisos(Empleado e) {
        boolean admin = Roles.ADMINISTRADOR.equals(e.getRol());
        boolean cajero = Roles.CAJERO.equals(e.getRol());
        mostrarNodo(btnClientes, admin || cajero);
        mostrarNodo(btnEnvio, admin || cajero);
        mostrarNodo(btnEntregas, admin || cajero);
        mostrarNodo(seccionAdmin, admin);
    }

    private void mostrarNodo(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }

    private void mostrarInicio(Empleado e) {
        Label saludo = new Label("Bienvenido, " + e.getNombreCompleto());
        saludo.getStyleClass().add("titulo");
        Label detalle = new Label("Selecciona una opción del menú para comenzar.");
        VBox caja = new VBox(8, saludo, detalle);
        caja.setAlignment(Pos.CENTER);
        contenido.getChildren().setAll(caja);
    }

    private void mostrar(String fxml) {
        try {
            Node vista = FXMLLoader.load(getClass().getResource("/view/" + fxml));
            contenido.getChildren().setAll(vista);
        } catch (IOException ex) {
            ex.printStackTrace();
            Alertas.error("No se pudo abrir la pantalla: " + ex.getMessage());
        }
    }

    @FXML private void onInicio() { mostrarInicio(Sesion.getActual()); }
    @FXML private void onBandeja() { mostrar("bandeja.fxml"); }
    @FXML private void onClientes() { mostrar("clientes.fxml"); }
    @FXML private void onRegistrarEnvio() { mostrar("registrar_envio.fxml"); }
    @FXML private void onSeguimiento() { mostrar("seguimiento.fxml"); }
    @FXML private void onEntregas() { mostrar("entregas.fxml"); }
    @FXML private void onReportes() { mostrar("reportes.fxml"); }
    @FXML private void onAgencias() { mostrar("agencias.fxml"); }
    @FXML private void onRutas() { mostrar("rutas.fxml"); }
    @FXML private void onEmpleados() { mostrar("empleados.fxml"); }

    @FXML
    private void onCerrarSesion() throws IOException {
        Sesion.cerrar();
        Navegador.ir(btnCerrarSesion, "login.fxml", "Sistema de Envíos - Iniciar sesión");
    }
}
