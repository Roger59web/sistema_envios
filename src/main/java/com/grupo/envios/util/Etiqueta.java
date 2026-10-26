package com.grupo.envios.util;

import com.grupo.envios.model.Envio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Scale;
import javafx.stage.Window;

import java.util.List;

public final class Etiqueta {

    private static final double ANCHO = 283;
    private static final double ALTO = 170;
    private static final String FUENTE = "Consolas";

    private Etiqueta() {
    }

    public static VBox crear(Envio dkprE, int dkprNroBulto) {
        String dkprCodigo = CodigosBarras.codigoBulto(dkprE, dkprNroBulto);

        VBox dkprDatos = new VBox(1,
                texto("SISTEMA DE ENVÍOS", 9, true),
                texto("DESTINO", 8, false),
                texto(dkprE.getDestino(), 15, true),
                texto("Para: " + dkprE.getDestinatario(), 10, false),
                texto("De: " + dkprE.getOrigen(), 9, false));
        HBox.setHgrow(dkprDatos, Priority.ALWAYS);
        HBox dkprArriba = new HBox(6, dkprDatos, CodigosBarras.qr(dkprCodigo, 74));
        dkprArriba.setAlignment(Pos.TOP_LEFT);

        Region dkprEspacio = new Region();
        HBox.setHgrow(dkprEspacio, Priority.ALWAYS);
        HBox dkprAbajo = new HBox(texto(dkprCodigo, 11, true), dkprEspacio,
                texto("Bulto " + dkprNroBulto + " de " + dkprE.getCantidadBultos(), 11, true));

        VBox dkprEtiqueta = new VBox(3, dkprArriba, CodigosBarras.barras(dkprCodigo, ANCHO - 16, 52), dkprAbajo);
        dkprEtiqueta.setPadding(new Insets(6, 8, 6, 8));
        dkprEtiqueta.setMinSize(ANCHO, ALTO);
        dkprEtiqueta.setPrefSize(ANCHO, ALTO);
        dkprEtiqueta.setMaxSize(ANCHO, ALTO);
        dkprEtiqueta.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");
        return dkprEtiqueta;
    }

    public static int contar(List<Envio> dkprEnvios) {
        return dkprEnvios.stream().mapToInt(Envio::getCantidadBultos).sum();
    }

    public static void imprimir(List<Envio> dkprEnvios, Window dkprOwner) {
        PrinterJob dkprJob = PrinterJob.createPrinterJob();
        if (dkprJob == null) {
            Alertas.advertencia("No se encontró ninguna impresora instalada.");
            return;
        }
        if (!dkprJob.showPrintDialog(dkprOwner)) {
            return;
        }
        PageLayout dkprPagina = dkprJob.getJobSettings().getPageLayout();
        double dkprEscala = Math.min(1, Math.min(dkprPagina.getPrintableWidth() / ANCHO,
                dkprPagina.getPrintableHeight() / ALTO));
        for (Envio dkprE : dkprEnvios) {
            for (int dkprN = 1; dkprN <= dkprE.getCantidadBultos(); dkprN++) {
                VBox dkprEtiqueta = crear(dkprE, dkprN);
                Group dkprGrupo = new Group(dkprEtiqueta);
                new Scene(dkprGrupo);
                dkprGrupo.applyCss();
                dkprGrupo.layout();
                dkprEtiqueta.getTransforms().add(new Scale(dkprEscala, dkprEscala));
                if (!dkprJob.printPage(dkprEtiqueta)) {
                    Alertas.error("No se pudo imprimir la etiqueta " + CodigosBarras.codigoBulto(dkprE, dkprN) + ".");
                    dkprJob.cancelJob();
                    return;
                }
            }
        }
        dkprJob.endJob();
    }

    private static Label texto(String dkprValor, double dkprTamano, boolean dkprNegrita) {
        Label dkprL = new Label(dkprValor == null ? "" : dkprValor);
        dkprL.setFont(Font.font(FUENTE, dkprNegrita ? FontWeight.BOLD : FontWeight.NORMAL, dkprTamano));
        dkprL.setTextFill(Color.BLACK);
        dkprL.setMaxWidth(Double.MAX_VALUE);
        return dkprL;
    }
}
