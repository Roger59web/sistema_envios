package com.grupo.envios;

import com.grupo.envios.config.DkprConfiguracionConexion;
import com.grupo.envios.config.DkprPostgresPortatil;
import com.grupo.envios.controller.DkprConexionController;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprNegocioException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DkprApp extends Application {

    @Override
    public void start(Stage dkprStage) throws Exception {
        if (DkprPostgresPortatil.dkprDisponible()) {
            dkprIniciarConBaseIncluida(dkprStage);
            return;
        }
        if (!DkprConfiguracionConexion.dkprExiste()) {
            Platform.setImplicitExit(false);
            boolean dkprGuardado = DkprConexionController.dkprAbrir(null);
            Platform.setImplicitExit(true);
            if (!dkprGuardado) {
                Platform.exit();
                return;
            }
        }
        dkprMostrarLogin(dkprStage);
    }

    @Override
    public void stop() {
        DkprPostgresPortatil.dkprDetener();
    }

    private void dkprIniciarConBaseIncluida(Stage dkprStage) {
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
                DkprPostgresPortatil.dkprIniciar();
                Platform.runLater(() -> {
                    try {
                        dkprMostrarLogin(dkprStage);
                    } catch (Exception dkprE) {
                        DkprAlertas.dkprManejar(dkprE);
                    }
                    dkprEspera.close();
                });
            } catch (Exception dkprE) {
                Platform.runLater(() -> {
                    if (dkprE instanceof DkprNegocioException) {
                        DkprAlertas.dkprAdvertencia(dkprE.getMessage());
                    } else {
                        dkprE.printStackTrace();
                        DkprAlertas.dkprError(DkprConexionController.SIN_CONEXION + ".\n" + dkprE.getMessage());
                    }
                    Platform.exit();
                });
            }
        }, "inicio-base");
        dkprHilo.setDaemon(true);
        dkprHilo.start();
    }

    private void dkprMostrarLogin(Stage dkprStage) throws Exception {
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
