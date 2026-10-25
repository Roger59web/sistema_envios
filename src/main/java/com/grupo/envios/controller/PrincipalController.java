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

    @FXML private Label dkprLblUsuario;
    @FXML private Button dkprBtnCerrarSesion;
    @FXML private StackPane dkprContenido;

    @FXML private Button dkprBtnClientes;
    @FXML private Button dkprBtnEnvio;
    @FXML private Button dkprBtnEntregas;
    @FXML private VBox dkprSeccionAdmin;

    @FXML
    private void initialize() {
        Empleado dkprE = Sesion.getActual();
        if (dkprE == null) {
            return;
        }
        dkprLblUsuario.setText(dkprE.getNombreCompleto() + "  |  " + dkprE.getRol() + "  |  " + dkprE.getAgencia());
        aplicarPermisos(dkprE);
        mostrarInicio(dkprE);
    }

    /** Oculta del menú lo que el rol del empleado no puede usar. */
    private void aplicarPermisos(Empleado dkprE) {
        boolean dkprAdmin = Roles.ADMINISTRADOR.equals(dkprE.getRol());
        boolean dkprCajero = Roles.CAJERO.equals(dkprE.getRol());
        mostrarNodo(dkprBtnClientes, dkprAdmin || dkprCajero);
        mostrarNodo(dkprBtnEnvio, dkprAdmin || dkprCajero);
        mostrarNodo(dkprBtnEntregas, dkprAdmin || dkprCajero);
        mostrarNodo(dkprSeccionAdmin, dkprAdmin);
    }

    private void mostrarNodo(Node dkprNodo, boolean dkprVisible) {
        dkprNodo.setVisible(dkprVisible);
        dkprNodo.setManaged(dkprVisible);
    }

    private void mostrarInicio(Empleado dkprE) {
        Label dkprSaludo = new Label("Bienvenido, " + dkprE.getNombreCompleto());
        dkprSaludo.getStyleClass().add("titulo");
        Label dkprDetalle = new Label("Selecciona una opción del menú para comenzar.");
        VBox dkprCaja = new VBox(8, dkprSaludo, dkprDetalle);
        dkprCaja.setAlignment(Pos.CENTER);
        dkprContenido.getChildren().setAll(dkprCaja);
    }

    private void mostrar(String dkprFxml) {
        try {
            Node dkprVista = FXMLLoader.load(getClass().getResource("/view/" + dkprFxml));
            dkprContenido.getChildren().setAll(dkprVista);
        } catch (IOException dkprEx) {
            dkprEx.printStackTrace();
            Alertas.error("No se pudo abrir la pantalla: " + dkprEx.getMessage());
        }
    }

    @FXML private void onInicio() { mostrarInicio(Sesion.getActual()); }
    @FXML private void onBandeja() { mostrar("bandeja.fxml"); }
    @FXML private void onControlCarga() { mostrar("control_carga.fxml"); }
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
        Navegador.ir(dkprBtnCerrarSesion, "login.fxml", "Sistema de Envíos - Iniciar sesión");
    }
}
