package com.grupo.envios.util;

import com.grupo.envios.model.DkprDetalleEnvio;
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

public final class DkprVistaTicket {

    private DkprVistaTicket() {
    }

    public static void dkprMostrar(DkprDetalleEnvio dkprD, Window dkprOwner) {
        dkprMostrar(dkprD, dkprOwner, null);
    }

    public static void dkprMostrar(DkprDetalleEnvio dkprD, Window dkprOwner, String dkprMensaje) {
        String dkprCodigo = dkprD.dkprEnvio().dkprGetCodigoSeguimiento();
        Stage dkprVentana = new Stage();
        dkprVentana.initOwner(dkprOwner);
        dkprVentana.initModality(Modality.WINDOW_MODAL);
        dkprVentana.setTitle("Ticket del envío " + dkprCodigo);

        StackPane dkprPapel = new StackPane(DkprTicket.dkprCrear(dkprD));
        dkprPapel.setPadding(new Insets(16));
        dkprPapel.setStyle("-fx-background-color: #d5dbe3;");
        ScrollPane dkprScroll = new ScrollPane(dkprPapel);
        dkprScroll.setFitToWidth(true);

        Button dkprBtnImprimir = new Button("Imprimir ticket");
        dkprBtnImprimir.getStyleClass().add("boton-primario");
        dkprBtnImprimir.setDefaultButton(true);
        dkprBtnImprimir.setOnAction(dkprE -> DkprTicket.dkprImprimir(dkprD, dkprVentana));

        Button dkprBtnPdf = new Button("Guardar PDF");
        dkprBtnPdf.setOnAction(dkprE -> dkprGuardarPdf(dkprD, dkprVentana));

        Button dkprBtnCerrar = new Button("Cerrar");
        dkprBtnCerrar.setCancelButton(true);
        dkprBtnCerrar.setOnAction(dkprE -> dkprVentana.close());

        int dkprBultos = dkprD.dkprEnvio().dkprGetCantidadBultos();
        Button dkprBtnEtiquetas = new Button("Etiquetas (" + dkprBultos + ")");
        dkprBtnEtiquetas.setOnAction(dkprE -> DkprEtiqueta.dkprImprimir(List.of(dkprD.dkprEnvio()), dkprVentana));
        dkprBtnEtiquetas.setDisable(dkprD.dkprEnvio().dkprGetIdEstadoActual() == DkprEstados.ANULADO);

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
        dkprScene.getStylesheets().add(DkprVistaTicket.class.getResource("/css/estilos.css").toExternalForm());
        dkprScene.setOnKeyPressed(dkprE -> {
            if (dkprE.isControlDown() && dkprE.getCode() == KeyCode.P) {
                DkprTicket.dkprImprimir(dkprD, dkprVentana);
            }
        });
        dkprVentana.setScene(dkprScene);
        dkprVentana.showAndWait();
    }

    public static void dkprGuardarPdf(DkprDetalleEnvio dkprD, Window dkprOwner) {
        String dkprCodigo = dkprD.dkprEnvio().dkprGetCodigoSeguimiento();
        File dkprArchivo = DkprGeneradorPdf.dkprElegirDestino(dkprOwner, "Guardar detalle del envío", "envio_" + dkprCodigo + ".pdf");
        if (dkprArchivo == null) {
            return;
        }
        try {
            DkprGeneradorPdf.dkprDetalleEnvio(dkprD, dkprArchivo);
            DkprGeneradorPdf.dkprOfrecerAbrir(dkprArchivo);
        } catch (Exception dkprEx) {
            DkprAlertas.dkprManejar(dkprEx);
        }
    }
}
