package com.grupo.envios.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Cambia la pantalla mostrada en la ventana actual. */
public final class Navegador {

    private Navegador() {
    }

    /**
     * @param origen cualquier nodo de la pantalla actual (un botón, por ejemplo)
     * @param fxml   nombre del archivo en resources/view, ej: "principal.fxml"
     * @param titulo título de la ventana
     */
    public static void ir(Node dkprOrigen, String dkprFxml, String dkprTitulo) throws IOException {
        Parent dkprRoot = FXMLLoader.load(Navegador.class.getResource("/view/" + dkprFxml));
        Scene dkprScene = new Scene(dkprRoot);
        dkprScene.getStylesheets().add(Navegador.class.getResource("/css/estilos.css").toExternalForm());
        Stage dkprStage = (Stage) dkprOrigen.getScene().getWindow();
        dkprStage.setScene(dkprScene);
        dkprStage.setTitle(dkprTitulo);
        dkprStage.setResizable(!"login.fxml".equals(dkprFxml));
        dkprStage.sizeToScene();
        dkprStage.centerOnScreen();
    }
}
