package com.grupo.envios.util;

import com.grupo.envios.model.DkprComprobante;
import com.grupo.envios.model.DkprDetalleEnvio;
import com.grupo.envios.model.DkprEnvio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.stage.Window;

public final class DkprTicket {

    private static final double ANCHO = 270;
    private static final String FUENTE = "Consolas";

    private DkprTicket() {
    }

    public static VBox dkprCrear(DkprDetalleEnvio dkprD) {
        DkprEnvio dkprE = dkprD.dkprEnvio();
        DkprComprobante dkprC = dkprD.dkprComprobante();

        VBox dkprT = new VBox(3);
        dkprT.setPadding(new Insets(12));
        dkprT.setMinWidth(ANCHO);
        dkprT.setPrefWidth(ANCHO);
        dkprT.setMaxWidth(ANCHO);
        dkprT.setStyle("-fx-background-color: white;");

        dkprT.getChildren().add(dkprCentrado("SISTEMA DE ENVÍOS", 15, true));
        dkprT.getChildren().add(dkprCentrado(dkprE.dkprGetOrigen(), 11, false));
        if (dkprE.dkprGetOrigenDireccion() != null) {
            dkprT.getChildren().add(dkprCentrado(dkprE.dkprGetOrigenDireccion(), 10, false));
        }
        if (dkprE.dkprGetOrigenTelefono() != null && !dkprE.dkprGetOrigenTelefono().isBlank()) {
            dkprT.getChildren().add(dkprCentrado("Tel: " + dkprE.dkprGetOrigenTelefono(), 10, false));
        }
        dkprT.getChildren().add(dkprSeparador());

        if (dkprC != null) {
            dkprT.getChildren().add(dkprCentrado("BOLETA".equals(dkprC.dkprGetTipo()) ? "BOLETA DE VENTA" : "FACTURA", 12, true));
            dkprT.getChildren().add(dkprCentrado(dkprC.dkprGetNumeroTexto(), 12, true));
            dkprT.getChildren().add(dkprFila("Fecha:", dkprC.dkprGetFechaTexto()));
        } else {
            dkprT.getChildren().add(dkprFila("Fecha:", dkprE.dkprGetFechaTexto()));
        }
        dkprT.getChildren().add(dkprSeparador());

        dkprT.getChildren().add(dkprCentrado("CÓDIGO DE SEGUIMIENTO", 10, false));
        dkprT.getChildren().add(dkprCentrado(dkprE.dkprGetCodigoSeguimiento(), 22, true));
        dkprT.getChildren().add(dkprCentrar(DkprCodigosBarras.dkprBarras(dkprE.dkprGetCodigoSeguimiento(), ANCHO - 40, 44)));
        if (dkprE.dkprGetIdEstadoActual() == DkprEstados.ANULADO) {
            dkprT.getChildren().add(dkprCentrado("*** ENVÍO ANULADO ***", 13, true));
        }
        dkprT.getChildren().add(dkprSeparador());

        dkprT.getChildren().add(dkprFila("Origen:", dkprE.dkprGetOrigen()));
        dkprT.getChildren().add(dkprFila("Destino:", dkprE.dkprGetDestino()));
        dkprT.getChildren().add(dkprSeparador());
        dkprPersona(dkprT, "REMITENTE", dkprE.dkprGetRemitente(), dkprE.dkprGetRemitenteDocumento(), dkprE.dkprGetRemitenteTelefono());
        dkprPersona(dkprT, "DESTINATARIO", dkprE.dkprGetDestinatario(), dkprE.dkprGetDestinatarioDocumento(), dkprE.dkprGetDestinatarioTelefono());
        dkprT.getChildren().add(dkprSeparador());

        dkprT.getChildren().add(dkprTexto("Contenido: " + dkprE.dkprGetDescripcion(), 11, false));
        dkprT.getChildren().add(dkprFila("Bultos:", String.valueOf(dkprE.dkprGetCantidadBultos())));
        dkprT.getChildren().add(dkprFila("Peso:", dkprE.dkprGetPesoKg().toPlainString() + " kg"));
        dkprT.getChildren().add(dkprFila("Valor declarado:", DkprFormato.dkprSoles(dkprE.dkprGetValorDeclarado())));
        if (dkprE.dkprGetObservaciones() != null && !dkprE.dkprGetObservaciones().isBlank()) {
            dkprT.getChildren().add(dkprTexto("Obs: " + dkprE.dkprGetObservaciones(), 11, false));
        }
        dkprT.getChildren().add(dkprSeparador());

        if (dkprC != null) {
            dkprT.getChildren().add(dkprFila("Op. gravada:", DkprFormato.dkprSoles(dkprC.dkprGetSubtotal())));
            dkprT.getChildren().add(dkprFila("IGV (18%):", DkprFormato.dkprSoles(dkprC.dkprGetIgv())));
            dkprT.getChildren().add(dkprFilaTotal(DkprFormato.dkprSoles(dkprC.dkprGetTotal())));
            dkprT.getChildren().add(dkprFila("Forma de pago:", dkprC.dkprGetMetodoPago()));
        } else {
            dkprT.getChildren().add(dkprFilaTotal(DkprFormato.dkprSoles(dkprE.dkprGetCostoEnvio())));
        }
        dkprT.getChildren().add(dkprSeparador());

        dkprT.getChildren().add(dkprFila("Estado:", dkprE.dkprGetEstado()));
        dkprT.getChildren().add(dkprTexto("Atendido por: " + DkprFormato.dkprTexto(dkprE.dkprGetEmpleadoRegistro()), 11, false));
        dkprT.getChildren().add(dkprSeparador());
        Node dkprQr = dkprCentrar(DkprCodigosBarras.dkprQr(dkprE.dkprGetCodigoSeguimiento(), 84));
        VBox.setMargin(dkprQr, new Insets(16, 0, 16, 0));
        dkprT.getChildren().add(dkprQr);
        dkprT.getChildren().add(dkprCentrado("Consulte el estado de su envío", 10, false));
        dkprT.getChildren().add(dkprCentrado("con el código de seguimiento.", 10, false));
        dkprT.getChildren().add(dkprCentrado("¡Gracias por su preferencia!", 11, true));
        dkprT.getChildren().add(dkprCentrado("Comprobante de práctica, sin valor tributario.", 9, false));
        return dkprT;
    }

    public static void dkprImprimir(DkprDetalleEnvio dkprD, Window dkprOwner) {
        PrinterJob dkprJob = PrinterJob.createPrinterJob();
        if (dkprJob == null) {
            DkprAlertas.dkprAdvertencia("No se encontró ninguna impresora instalada.");
            return;
        }
        if (!dkprJob.showPrintDialog(dkprOwner)) {
            return;
        }

        VBox dkprTicket = dkprCrear(dkprD);
        Group dkprGrupo = new Group(dkprTicket);
        new Scene(dkprGrupo);
        dkprGrupo.applyCss();
        dkprGrupo.layout();

        PageLayout dkprPagina = dkprJob.getJobSettings().getPageLayout();
        double dkprEscala = Math.min(1, Math.min(
                dkprPagina.getPrintableWidth() / dkprTicket.getWidth(),
                dkprPagina.getPrintableHeight() / dkprTicket.getHeight()));
        dkprTicket.getTransforms().add(new Scale(dkprEscala, dkprEscala));

        if (dkprJob.printPage(dkprTicket)) {
            dkprJob.endJob();
        } else {
            DkprAlertas.dkprError("No se pudo imprimir el ticket.");
        }
    }

    private static void dkprPersona(VBox dkprT, String dkprRol, String dkprNombre, String dkprDocumento, String dkprTelefono) {
        dkprT.getChildren().add(dkprTexto(dkprRol, 11, true));
        dkprT.getChildren().add(dkprTexto(dkprNombre, 11, false));
        String dkprDatos = "Doc: " + dkprDocumento;
        if (dkprTelefono != null && !dkprTelefono.isBlank()) {
            dkprDatos += "   Tel: " + dkprTelefono;
        }
        dkprT.getChildren().add(dkprTexto(dkprDatos, 10, false));
    }

    private static Label dkprTexto(String dkprValor, double dkprTamano, boolean dkprNegrita) {
        Label dkprL = new Label(dkprValor == null ? "" : dkprValor);
        dkprL.setFont(Font.font(FUENTE, dkprNegrita ? FontWeight.BOLD : FontWeight.NORMAL, dkprTamano));
        dkprL.setTextFill(Color.BLACK);
        dkprL.setWrapText(true);
        dkprL.setMaxWidth(Double.MAX_VALUE);
        return dkprL;
    }

    private static Label dkprCentrado(String dkprValor, double dkprTamano, boolean dkprNegrita) {
        Label dkprL = dkprTexto(dkprValor, dkprTamano, dkprNegrita);
        dkprL.setAlignment(Pos.CENTER);
        dkprL.setTextAlignment(TextAlignment.CENTER);
        return dkprL;
    }

    private static Node dkprFila(String dkprEtiqueta, String dkprValor) {
        return dkprFila(dkprTexto(dkprEtiqueta, 11, false), dkprTexto(DkprFormato.dkprTexto(dkprValor), 11, false));
    }

    private static Node dkprFilaTotal(String dkprTotal) {
        return dkprFila(dkprTexto("TOTAL:", 14, true), dkprTexto(dkprTotal, 14, true));
    }

    private static Node dkprFila(Label dkprEtiqueta, Label dkprValor) {
        dkprEtiqueta.setMinWidth(Region.USE_PREF_SIZE);
        dkprValor.setAlignment(Pos.CENTER_RIGHT);
        dkprValor.setTextAlignment(TextAlignment.RIGHT);
        HBox.setHgrow(dkprValor, Priority.ALWAYS);
        HBox dkprFila = new HBox(6, dkprEtiqueta, dkprValor);
        dkprFila.setAlignment(Pos.TOP_LEFT);
        return dkprFila;
    }

    private static Node dkprCentrar(Node dkprNodo) {
        HBox dkprCaja = new HBox(dkprNodo);
        dkprCaja.setAlignment(Pos.CENTER);
        VBox.setMargin(dkprCaja, new Insets(4, 0, 2, 0));
        return dkprCaja;
    }

    private static Node dkprSeparador() {
        Region dkprLinea = new Region();
        dkprLinea.setStyle("-fx-border-color: black; -fx-border-style: dashed; -fx-border-width: 0 0 1 0;");
        VBox.setMargin(dkprLinea, new Insets(3, 0, 3, 0));
        return dkprLinea;
    }
}
