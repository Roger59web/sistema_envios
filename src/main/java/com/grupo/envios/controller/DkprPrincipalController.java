package com.grupo.envios.controller;

import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.service.DkprEstancadoService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprNavegador;
import com.grupo.envios.util.DkprRoles;
import com.grupo.envios.util.DkprSesion;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class DkprPrincipalController {

    @FXML private Label dkprLblUsuario;
    @FXML private Button dkprBtnCerrarSesion;
    @FXML private StackPane dkprContenido;

    @FXML private Button dkprBtnClientes;
    @FXML private Button dkprBtnEnvio;
    @FXML private Button dkprBtnEntregas;
    @FXML private VBox dkprSeccionAdmin;
    @FXML private Button dkprBtnEstancados;
    @FXML private Button dkprBtnCaja;

    private static final Duration INTERVALO_ESTANCADOS = Duration.minutes(5);

    private final DkprEstancadoService dkprEstancadoService = new DkprEstancadoService();
    private Timeline dkprRelojEstancados;

    @FXML
    private void initialize() {
        DkprEmpleado dkprE = DkprSesion.dkprGetActual();
        if (dkprE == null) {
            return;
        }
        dkprLblUsuario.setText(dkprE.dkprGetNombreCompleto() + "  |  " + dkprE.dkprGetRol() + "  |  " + dkprE.dkprGetAgencia());
        dkprAplicarPermisos(dkprE);
        dkprMostrarInicio(dkprE);

        dkprActualizarEstancados();
        dkprRelojEstancados = new Timeline(new KeyFrame(INTERVALO_ESTANCADOS, dkprEv -> dkprActualizarEstancados()));
        dkprRelojEstancados.setCycleCount(Animation.INDEFINITE);
        dkprRelojEstancados.play();
    }

    private void dkprActualizarEstancados() {
        DkprEmpleado dkprE = DkprSesion.dkprGetActual();
        if (dkprE == null) {
            return;
        }
        Thread dkprHilo = new Thread(() -> {
            int dkprCantidad;
            try {
                dkprCantidad = dkprEstancadoService.dkprContar(dkprE);
            } catch (Exception dkprEx) {
                dkprCantidad = 0;
            }
            int dkprTotal = dkprCantidad;
            Platform.runLater(() -> {
                dkprBtnEstancados.setText(dkprTotal == 0 ? "Paquetes estancados" : "Paquetes estancados (" + dkprTotal + ")");
                dkprBtnEstancados.setStyle(dkprTotal == 0 ? "" : "-fx-text-fill: #c0392b; -fx-font-weight: bold;");
            });
        });
        dkprHilo.setDaemon(true);
        dkprHilo.start();
    }

    private void dkprAplicarPermisos(DkprEmpleado dkprE) {
        boolean dkprAdmin = DkprRoles.ADMINISTRADOR.equals(dkprE.dkprGetRol());
        boolean dkprCajero = DkprRoles.CAJERO.equals(dkprE.dkprGetRol());
        dkprMostrarNodo(dkprBtnClientes, dkprAdmin || dkprCajero);
        dkprMostrarNodo(dkprBtnEnvio, dkprAdmin || dkprCajero);
        dkprMostrarNodo(dkprBtnEntregas, dkprAdmin || dkprCajero);
        dkprMostrarNodo(dkprBtnCaja, dkprAdmin || dkprCajero);
        dkprMostrarNodo(dkprSeccionAdmin, dkprAdmin);
    }

    private void dkprMostrarNodo(Node dkprNodo, boolean dkprVisible) {
        dkprNodo.setVisible(dkprVisible);
        dkprNodo.setManaged(dkprVisible);
    }

    private void dkprMostrarInicio(DkprEmpleado dkprE) {
        Label dkprSaludo = new Label("Bienvenido, " + dkprE.dkprGetNombreCompleto());
        dkprSaludo.getStyleClass().add("titulo");
        Label dkprDetalle = new Label("Selecciona una opción del menú para comenzar.");
        VBox dkprCaja = new VBox(8, dkprSaludo, dkprDetalle);
        dkprCaja.setAlignment(Pos.CENTER);
        dkprContenido.getChildren().setAll(dkprCaja);
    }

    private void dkprMostrar(String dkprFxml) {
        try {
            Node dkprVista = FXMLLoader.load(getClass().getResource("/view/" + dkprFxml));
            dkprContenido.getChildren().setAll(dkprVista);
            dkprActualizarEstancados();
        } catch (IOException dkprEx) {
            dkprEx.printStackTrace();
            DkprAlertas.dkprError("No se pudo abrir la pantalla: " + dkprEx.getMessage());
        }
    }

    @FXML private void dkprOnInicio() { dkprMostrarInicio(DkprSesion.dkprGetActual()); }
    @FXML private void dkprOnBandeja() { dkprMostrar("bandeja.fxml"); }
    @FXML private void dkprOnControlCarga() { dkprMostrar("control_carga.fxml"); }
    @FXML private void dkprOnEstancados() { dkprMostrar("estancados.fxml"); }
    @FXML private void dkprOnCaja() { dkprMostrar("caja.fxml"); }
    @FXML private void dkprOnClientes() { dkprMostrar("clientes.fxml"); }
    @FXML private void dkprOnRegistrarEnvio() { dkprMostrar("registrar_envio.fxml"); }
    @FXML private void dkprOnSeguimiento() { dkprMostrar("seguimiento.fxml"); }
    @FXML private void dkprOnEntregas() { dkprMostrar("entregas.fxml"); }
    @FXML private void dkprOnReportes() { dkprMostrar("reportes.fxml"); }
    @FXML private void dkprOnAgencias() { dkprMostrar("agencias.fxml"); }
    @FXML private void dkprOnRutas() { dkprMostrar("rutas.fxml"); }
    @FXML private void dkprOnVehiculos() { dkprMostrar("vehiculos.fxml"); }
    @FXML private void dkprOnEmpleados() { dkprMostrar("empleados.fxml"); }

    @FXML
    private void dkprOnCerrarSesion() throws IOException {
        if (dkprRelojEstancados != null) {
            dkprRelojEstancados.stop();
        }
        DkprSesion.dkprCerrar();
        DkprNavegador.dkprIr(dkprBtnCerrarSesion, "login.fxml", "Sistema de Envíos - Iniciar sesión");
    }
}
