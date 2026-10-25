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
import java.util.List;

/** Ventana de vista previa del ticket, con las opciones de imprimir y guardar en PDF. */
public final class VistaTicket {

    private VistaTicket() {
    }

    public static void mostrar(DetalleEnvio dkprD, Window dkprOwner) {
        mostrar(dkprD, dkprOwner, null);
    }

    /** @param mensaje aviso opcional que se muestra arriba del ticket (ej: "Envío registrado correctamente") */
    public static void mostrar(DetalleEnvio dkprD, Window dkprOwner, String dkprMensaje) {
        String dkprCodigo = dkprD.envio().getCodigoSeguimiento();
        Stage dkprVentana = new Stage();
        dkprVentana.initOwner(dkprOwner);
        dkprVentana.initModality(Modality.WINDOW_MODAL);
        dkprVentana.setTitle("Ticket del envío " + dkprCodigo);

        StackPane dkprPapel = new StackPane(Ticket.crear(dkprD));
        dkprPapel.setPadding(new Insets(16));
        dkprPapel.setStyle("-fx-background-color: #d5dbe3;");
        ScrollPane dkprScroll = new ScrollPane(dkprPapel);
        dkprScroll.setFitToWidth(true);

        Button dkprBtnImprimir = new Button("Imprimir ticket");
        dkprBtnImprimir.getStyleClass().add("boton-primario");
        dkprBtnImprimir.setDefaultButton(true);
        dkprBtnImprimir.setOnAction(dkprE -> Ticket.imprimir(dkprD, dkprVentana));

        Button dkprBtnPdf = new Button("Guardar PDF");
        dkprBtnPdf.setOnAction(dkprE -> guardarPdf(dkprD, dkprVentana));

        Button dkprBtnCerrar = new Button("Cerrar");
        dkprBtnCerrar.setCancelButton(true);
        dkprBtnCerrar.setOnAction(dkprE -> dkprVentana.close());

        // Las etiquetas de los bultos salen desde aquí mismo: no hace falta ir a Control de carga.
        int dkprBultos = dkprD.envio().getCantidadBultos();
        Button dkprBtnEtiquetas = new Button("Etiquetas (" + dkprBultos + ")");
        dkprBtnEtiquetas.setOnAction(dkprE -> Etiqueta.imprimir(List.of(dkprD.envio()), dkprVentana));
        dkprBtnEtiquetas.setDisable(dkprD.envio().getIdEstadoActual() == Estados.ANULADO);

        Region dkprEspacio = new Region();
        HBox.setHgrow(dkprEspacio, Priority.ALWAYS);
        HBox dkprBarra = new HBox(8, dkprBtnPdf, dkprBtnEtiquetas, dkprEspacio, dkprBtnCerrar, dkprBtnImprimir);
        dkprBarra.setAlignment(Pos.CENTER_RIGHT);
        dkprBarra.setPadding(new Insets(10, 12, 10, 12));
        dkprBarra.getStyleClass().add("panel-form");

        BorderPane dkprRaiz = new BorderPane(dkprScroll);
        dkprRaiz.setBottom(dkprBarra);
        if (dkprMensaje != null) {
            Label dkprAviso = new Label(dkprMensaje);
            dkprAviso.getStyleClass().add("aviso-exito");
            dkprAviso.setMaxWidth(Double.MAX_VALUE);
            dkprAviso.setWrapText(true);
            dkprRaiz.setTop(dkprAviso);
        }

        Scene dkprScene = new Scene(dkprRaiz, 440, 700);
        dkprScene.getStylesheets().add(VistaTicket.class.getResource("/css/estilos.css").toExternalForm());
        dkprScene.setOnKeyPressed(dkprE -> {
            if (dkprE.isControlDown() && dkprE.getCode() == KeyCode.P) {
                Ticket.imprimir(dkprD, dkprVentana);
            }
        });
        dkprVentana.setScene(dkprScene);
        dkprVentana.showAndWait();
    }

    /** Guarda el detalle completo del envío (A4) en PDF. */
    public static void guardarPdf(DetalleEnvio dkprD, Window dkprOwner) {
        String dkprCodigo = dkprD.envio().getCodigoSeguimiento();
        File dkprArchivo = GeneradorPdf.elegirDestino(dkprOwner, "Guardar detalle del envío", "envio_" + dkprCodigo + ".pdf");
        if (dkprArchivo == null) {
            return;
        }
        try {
            GeneradorPdf.detalleEnvio(dkprD, dkprArchivo);
            GeneradorPdf.ofrecerAbrir(dkprArchivo);
        } catch (Exception dkprEx) {
            Alertas.manejar(dkprEx);
        }
    }
}
