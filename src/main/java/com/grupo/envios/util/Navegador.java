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
    public static void ir(Node origen, String fxml, String titulo) throws IOException {
        Parent root = FXMLLoader.load(Navegador.class.getResource("/view/" + fxml));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Navegador.class.getResource("/css/estilos.css").toExternalForm());
        Stage stage = (Stage) origen.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle(titulo);
        stage.setResizable(true);
        stage.centerOnScreen();
    }
}
