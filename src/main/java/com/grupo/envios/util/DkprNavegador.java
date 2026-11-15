package com.grupo.envios.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class DkprNavegador {

    private DkprNavegador() {
    }

    public static void dkprIr(Node dkprOrigen, String dkprFxml, String dkprTitulo) throws IOException {
        Parent dkprRoot = FXMLLoader.load(DkprNavegador.class.getResource("/view/" + dkprFxml));
        Scene dkprScene = new Scene(dkprRoot);
        dkprScene.getStylesheets().add(DkprNavegador.class.getResource("/css/estilos.css").toExternalForm());
        Stage dkprStage = (Stage) dkprOrigen.getScene().getWindow();
        dkprStage.setScene(dkprScene);
        dkprStage.setTitle(dkprTitulo);
        dkprStage.setResizable(!"login.fxml".equals(dkprFxml));
        dkprStage.sizeToScene();
        dkprStage.centerOnScreen();
    }
}
