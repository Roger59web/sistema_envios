package com.grupo.envios.util;

import com.grupo.envios.model.DkprEnvio;
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

public final class DkprEtiqueta {

    private static final double ANCHO = 283;
    private static final double ALTO = 170;
    private static final String FUENTE = "Consolas";

    private DkprEtiqueta() {
    }

    public static VBox dkprCrear(DkprEnvio dkprE, int dkprNroBulto) {
        String dkprCodigo = DkprCodigosBarras.dkprCodigoBulto(dkprE, dkprNroBulto);

        VBox dkprDatos = new VBox(1,
                dkprTexto("SISTEMA DE ENVÍOS", 9, true),
                dkprTexto("DESTINO", 8, false),
                dkprTexto(dkprE.dkprGetDestino(), 15, true),
                dkprTexto("Para: " + dkprE.dkprGetDestinatario(), 10, false),
                dkprTexto("De: " + dkprE.dkprGetOrigen(), 9, false));
        HBox.setHgrow(dkprDatos, Priority.ALWAYS);
        HBox dkprArriba = new HBox(6, dkprDatos, DkprCodigosBarras.dkprQr(dkprCodigo, 74));
        dkprArriba.setAlignment(Pos.TOP_LEFT);

        Region dkprEspacio = new Region();
        HBox.setHgrow(dkprEspacio, Priority.ALWAYS);
        HBox dkprAbajo = new HBox(dkprTexto(dkprCodigo, 11, true), dkprEspacio,
                dkprTexto("Bulto " + dkprNroBulto + " de " + dkprE.dkprGetCantidadBultos(), 11, true));

        VBox dkprEtiqueta = new VBox(3, dkprArriba, DkprCodigosBarras.dkprBarras(dkprCodigo, ANCHO - 16, 52), dkprAbajo);
        dkprEtiqueta.setPadding(new Insets(6, 8, 6, 8));
        dkprEtiqueta.setMinSize(ANCHO, ALTO);
        dkprEtiqueta.setPrefSize(ANCHO, ALTO);
        dkprEtiqueta.setMaxSize(ANCHO, ALTO);
        dkprEtiqueta.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");
        return dkprEtiqueta;
    }

    public static int dkprContar(List<DkprEnvio> dkprEnvios) {
        return dkprEnvios.stream().mapToInt(DkprEnvio::dkprGetCantidadBultos).sum();
    }

    public static void dkprImprimir(List<DkprEnvio> dkprEnvios, Window dkprOwner) {
        PrinterJob dkprJob = PrinterJob.createPrinterJob();
        if (dkprJob == null) {
            DkprAlertas.dkprAdvertencia("No se encontró ninguna impresora instalada.");
            return;
        }
        if (!dkprJob.showPrintDialog(dkprOwner)) {
            return;
        }
        PageLayout dkprPagina = dkprJob.getJobSettings().getPageLayout();
        double dkprEscala = Math.min(1, Math.min(dkprPagina.getPrintableWidth() / ANCHO,
                dkprPagina.getPrintableHeight() / ALTO));
        for (DkprEnvio dkprE : dkprEnvios) {
            for (int dkprN = 1; dkprN <= dkprE.dkprGetCantidadBultos(); dkprN++) {
                VBox dkprEtiqueta = dkprCrear(dkprE, dkprN);
                Group dkprGrupo = new Group(dkprEtiqueta);
                new Scene(dkprGrupo);
                dkprGrupo.applyCss();
                dkprGrupo.layout();
                dkprEtiqueta.getTransforms().add(new Scale(dkprEscala, dkprEscala));
                if (!dkprJob.printPage(dkprEtiqueta)) {
                    DkprAlertas.dkprError("No se pudo imprimir la etiqueta " + DkprCodigosBarras.dkprCodigoBulto(dkprE, dkprN) + ".");
                    dkprJob.cancelJob();
                    return;
                }
            }
        }
        dkprJob.endJob();
    }

    private static Label dkprTexto(String dkprValor, double dkprTamano, boolean dkprNegrita) {
        Label dkprL = new Label(dkprValor == null ? "" : dkprValor);
        dkprL.setFont(Font.font(FUENTE, dkprNegrita ? FontWeight.BOLD : FontWeight.NORMAL, dkprTamano));
        dkprL.setTextFill(Color.BLACK);
        dkprL.setMaxWidth(Double.MAX_VALUE);
        return dkprL;
    }
}
