package com.grupo.envios.util;

import com.grupo.envios.model.DetalleEnvio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;

/** Ventana de vista previa del ticket, con las opciones de imprimir y guardar en PDF. */
public final class VistaTicket {

    private VistaTicket() {
    }

    public static void mostrar(DetalleEnvio d, Window owner) {
        mostrar(d, owner, null);
    }

    /** @param mensaje aviso opcional que se muestra arriba del ticket (ej: "Envío registrado correctamente") */
    public static void mostrar(DetalleEnvio d, Window owner, String mensaje) {
        String codigo = d.envio().getCodigoSeguimiento();
        Stage ventana = new Stage();
        ventana.initOwner(owner);
        ventana.initModality(Modality.WINDOW_MODAL);
        ventana.setTitle("Ticket del envío " + codigo);

        StackPane papel = new StackPane(Ticket.crear(d));
        papel.setPadding(new Insets(16));
        papel.setStyle("-fx-background-color: #d5dbe3;");
        ScrollPane scroll = new ScrollPane(papel);
        scroll.setFitToWidth(true);

        Button btnImprimir = new Button("Imprimir ticket");
        btnImprimir.getStyleClass().add("boton-primario");
        btnImprimir.setDefaultButton(true);
        btnImprimir.setOnAction(e -> Ticket.imprimir(d, ventana));

        Button btnPdf = new Button("Guardar PDF");
        btnPdf.setOnAction(e -> guardarPdf(d, ventana));

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setCancelButton(true);
        btnCerrar.setOnAction(e -> ventana.close());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox barra = new HBox(8, btnPdf, espacio, btnCerrar, btnImprimir);
        barra.setAlignment(Pos.CENTER_RIGHT);
        barra.setPadding(new Insets(10, 12, 10, 12));
        barra.getStyleClass().add("panel-form");

        BorderPane raiz = new BorderPane(scroll);
        raiz.setBottom(barra);
        if (mensaje != null) {
            Label aviso = new Label(mensaje);
            aviso.getStyleClass().add("aviso-exito");
            aviso.setMaxWidth(Double.MAX_VALUE);
            aviso.setWrapText(true);
            raiz.setTop(aviso);
        }

        Scene scene = new Scene(raiz, 360, 660);
        scene.getStylesheets().add(VistaTicket.class.getResource("/css/estilos.css").toExternalForm());
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.P) {
                Ticket.imprimir(d, ventana);
            }
        });
        ventana.setScene(scene);
        ventana.showAndWait();
    }

    /** Guarda el detalle completo del envío (A4) en PDF. */
    public static void guardarPdf(DetalleEnvio d, Window owner) {
        String codigo = d.envio().getCodigoSeguimiento();
        File archivo = GeneradorPdf.elegirDestino(owner, "Guardar detalle del envío", "envio_" + codigo + ".pdf");
        if (archivo == null) {
            return;
        }
        try {
            GeneradorPdf.detalleEnvio(d, archivo);
            GeneradorPdf.ofrecerAbrir(archivo);
        } catch (Exception ex) {
            Alertas.manejar(ex);
        }
    }
}
