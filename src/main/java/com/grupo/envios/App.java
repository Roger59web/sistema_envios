package com.grupo.envios;

import com.grupo.envios.config.ConfiguracionConexion;
import com.grupo.envios.config.PostgresPortatil;
import com.grupo.envios.controller.ConexionController;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.NegocioException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage dkprStage) throws Exception {
        if (PostgresPortatil.disponible()) {
            iniciarConBaseIncluida(dkprStage);
            return;
        }
        if (!ConfiguracionConexion.existe()) {
            Platform.setImplicitExit(false);
            boolean dkprGuardado = ConexionController.abrir(null);
            Platform.setImplicitExit(true);
            if (!dkprGuardado) {
                Platform.exit();
                return;
            }
        }
        mostrarLogin(dkprStage);
    }

    @Override
    public void stop() {
        PostgresPortatil.detener();
    }

    private void iniciarConBaseIncluida(Stage dkprStage) {
        Label dkprMensaje = new Label("Iniciando la base de datos, espera un momento...");
        dkprMensaje.getStyleClass().add("subtitulo");
        VBox dkprCaja = new VBox(dkprMensaje);
        dkprCaja.getStyleClass().add("login-box");
        dkprCaja.setPadding(new Insets(30, 40, 30, 40));
        Scene dkprScene = new Scene(dkprCaja);
        dkprScene.getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());
        Stage dkprEspera = new Stage();
        dkprEspera.setTitle("Sistema de Envíos");
        dkprEspera.setScene(dkprScene);
        dkprEspera.setResizable(false);
        dkprEspera.show();

        Thread dkprHilo = new Thread(() -> {
            try {
                PostgresPortatil.iniciar();
                Platform.runLater(() -> {
                    try {
                        mostrarLogin(dkprStage);
                    } catch (Exception dkprE) {
                        Alertas.manejar(dkprE);
                    }
                    dkprEspera.close();
                });
            } catch (Exception dkprE) {
                Platform.runLater(() -> {
                    if (dkprE instanceof NegocioException) {
                        Alertas.advertencia(dkprE.getMessage());
                    } else {
                        dkprE.printStackTrace();
                        Alertas.error(ConexionController.SIN_CONEXION + ".\n" + dkprE.getMessage());
                    }
                    Platform.exit();
                });
            }
        }, "inicio-base");
        dkprHilo.setDaemon(true);
        dkprHilo.start();
    }

    private void mostrarLogin(Stage dkprStage) throws Exception {
        Parent dkprRoot = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        Scene dkprScene = new Scene(dkprRoot);
        dkprScene.getStylesheets().add(getClass().getResource("/css/estilos.css").toExternalForm());
        dkprStage.setTitle("Sistema de Envíos - Iniciar sesión");
        dkprStage.setScene(dkprScene);
        dkprStage.setResizable(false);
        dkprStage.show();
    }

    public static void main(String[] dkprArgs) {
        launch(dkprArgs);
    }
}
