package com.grupo.envios;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage dkprStage) throws Exception {
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
