package com.grupo.envios.util;

import com.grupo.envios.model.Comprobante;
import com.grupo.envios.model.DetalleEnvio;
import com.grupo.envios.model.Envio;
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

/**
 * Ticket del envío con el formato de una impresora térmica de 80 mm.
 * El mismo nodo se usa para la vista previa y para imprimir.
 */
public final class Ticket {

    /** Ancho del ticket en píxeles (≈ 72 mm de área imprimible en papel de 80 mm). */
    private static final double ANCHO = 270;
    private static final String FUENTE = "Consolas";

    private Ticket() {
    }

    public static VBox crear(DetalleEnvio dkprD) {
        Envio dkprE = dkprD.envio();
        Comprobante dkprC = dkprD.comprobante();

        VBox dkprT = new VBox(3);
        dkprT.setPadding(new Insets(12));
        dkprT.setMinWidth(ANCHO);
        dkprT.setPrefWidth(ANCHO);
        dkprT.setMaxWidth(ANCHO);
        dkprT.setStyle("-fx-background-color: white;");

        // Cabecera
        dkprT.getChildren().add(centrado("SISTEMA DE ENVÍOS", 15, true));
        dkprT.getChildren().add(centrado(dkprE.getOrigen(), 11, false));
        if (dkprE.getOrigenDireccion() != null) {
            dkprT.getChildren().add(centrado(dkprE.getOrigenDireccion(), 10, false));
        }
        if (dkprE.getOrigenTelefono() != null && !dkprE.getOrigenTelefono().isBlank()) {
            dkprT.getChildren().add(centrado("Tel: " + dkprE.getOrigenTelefono(), 10, false));
        }
        dkprT.getChildren().add(separador());

        // Comprobante
        if (dkprC != null) {
            dkprT.getChildren().add(centrado("BOLETA".equals(dkprC.getTipo()) ? "BOLETA DE VENTA" : "FACTURA", 12, true));
            dkprT.getChildren().add(centrado(dkprC.getNumeroTexto(), 12, true));
            dkprT.getChildren().add(fila("Fecha:", dkprC.getFechaTexto()));
        } else {
            dkprT.getChildren().add(fila("Fecha:", dkprE.getFechaTexto()));
        }
        dkprT.getChildren().add(separador());

        // Código de seguimiento
        dkprT.getChildren().add(centrado("CÓDIGO DE SEGUIMIENTO", 10, false));
        dkprT.getChildren().add(centrado(dkprE.getCodigoSeguimiento(), 22, true));
        // Se escanea en Seguimiento, Entregas, Bandeja y Control de carga (envíos de un solo bulto).
        dkprT.getChildren().add(centrar(CodigosBarras.barras(dkprE.getCodigoSeguimiento(), ANCHO - 40, 44)));
        if (dkprE.getIdEstadoActual() == Estados.ANULADO) {
            dkprT.getChildren().add(centrado("*** ENVÍO ANULADO ***", 13, true));
        }
        dkprT.getChildren().add(separador());

        // Ruta y personas
        dkprT.getChildren().add(fila("Origen:", dkprE.getOrigen()));
        dkprT.getChildren().add(fila("Destino:", dkprE.getDestino()));
        dkprT.getChildren().add(separador());
        persona(dkprT, "REMITENTE", dkprE.getRemitente(), dkprE.getRemitenteDocumento(), dkprE.getRemitenteTelefono());
        persona(dkprT, "DESTINATARIO", dkprE.getDestinatario(), dkprE.getDestinatarioDocumento(), dkprE.getDestinatarioTelefono());
        dkprT.getChildren().add(separador());

        // Paquete
        dkprT.getChildren().add(texto("Contenido: " + dkprE.getDescripcion(), 11, false));
        dkprT.getChildren().add(fila("Bultos:", String.valueOf(dkprE.getCantidadBultos())));
        dkprT.getChildren().add(fila("Peso:", dkprE.getPesoKg().toPlainString() + " kg"));
        dkprT.getChildren().add(fila("Valor declarado:", Formato.soles(dkprE.getValorDeclarado())));
        if (dkprE.getObservaciones() != null && !dkprE.getObservaciones().isBlank()) {
            dkprT.getChildren().add(texto("Obs: " + dkprE.getObservaciones(), 11, false));
        }
        dkprT.getChildren().add(separador());

        // Importes
        if (dkprC != null) {
            dkprT.getChildren().add(fila("Op. gravada:", Formato.soles(dkprC.getSubtotal())));
            dkprT.getChildren().add(fila("IGV (18%):", Formato.soles(dkprC.getIgv())));
            dkprT.getChildren().add(filaTotal(Formato.soles(dkprC.getTotal())));
            dkprT.getChildren().add(fila("Forma de pago:", dkprC.getMetodoPago()));
        } else {
            dkprT.getChildren().add(filaTotal(Formato.soles(dkprE.getCostoEnvio())));
        }
        dkprT.getChildren().add(separador());

        // Pie
        dkprT.getChildren().add(fila("Estado:", dkprE.getEstado()));
        dkprT.getChildren().add(texto("Atendido por: " + Formato.texto(dkprE.getEmpleadoRegistro()), 11, false));
        dkprT.getChildren().add(separador());
        // El QR necesita un margen blanco alrededor (≈ 4 módulos) para que los lectores lo encuentren.
        Node dkprQr = centrar(CodigosBarras.qr(dkprE.getCodigoSeguimiento(), 84));
        VBox.setMargin(dkprQr, new Insets(16, 0, 16, 0));
        dkprT.getChildren().add(dkprQr);
        dkprT.getChildren().add(centrado("Consulte el estado de su envío", 10, false));
        dkprT.getChildren().add(centrado("con el código de seguimiento.", 10, false));
        dkprT.getChildren().add(centrado("¡Gracias por su preferencia!", 11, true));
        dkprT.getChildren().add(centrado("Comprobante de práctica, sin valor tributario.", 9, false));
        return dkprT;
    }

    /**
     * Abre el diálogo de impresión y manda el ticket a la impresora elegida
     * (sirve también "Microsoft Print to PDF"). Si no entra en la página, se reduce.
     */
    public static void imprimir(DetalleEnvio dkprD, Window dkprOwner) {
        PrinterJob dkprJob = PrinterJob.createPrinterJob();
        if (dkprJob == null) {
            Alertas.advertencia("No se encontró ninguna impresora instalada.");
            return;
        }
        if (!dkprJob.showPrintDialog(dkprOwner)) {
            return;
        }

        // El nodo debe estar en una escena para calcular su tamaño antes de imprimir.
        VBox dkprTicket = crear(dkprD);
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
            Alertas.error("No se pudo imprimir el ticket.");
        }
    }

    // ----------------------------------------------------------------- ayudas

    private static void persona(VBox dkprT, String dkprRol, String dkprNombre, String dkprDocumento, String dkprTelefono) {
        dkprT.getChildren().add(texto(dkprRol, 11, true));
        dkprT.getChildren().add(texto(dkprNombre, 11, false));
        String dkprDatos = "Doc: " + dkprDocumento;
        if (dkprTelefono != null && !dkprTelefono.isBlank()) {
            dkprDatos += "   Tel: " + dkprTelefono;
        }
        dkprT.getChildren().add(texto(dkprDatos, 10, false));
    }

    private static Label texto(String dkprValor, double dkprTamano, boolean dkprNegrita) {
        Label dkprL = new Label(dkprValor == null ? "" : dkprValor);
        dkprL.setFont(Font.font(FUENTE, dkprNegrita ? FontWeight.BOLD : FontWeight.NORMAL, dkprTamano));
        dkprL.setTextFill(Color.BLACK);
        dkprL.setWrapText(true);
        dkprL.setMaxWidth(Double.MAX_VALUE);
        return dkprL;
    }

    private static Label centrado(String dkprValor, double dkprTamano, boolean dkprNegrita) {
        Label dkprL = texto(dkprValor, dkprTamano, dkprNegrita);
        dkprL.setAlignment(Pos.CENTER);
        dkprL.setTextAlignment(TextAlignment.CENTER);
        return dkprL;
    }

    private static Node fila(String dkprEtiqueta, String dkprValor) {
        return fila(texto(dkprEtiqueta, 11, false), texto(Formato.texto(dkprValor), 11, false));
    }

    private static Node filaTotal(String dkprTotal) {
        return fila(texto("TOTAL:", 14, true), texto(dkprTotal, 14, true));
    }

    private static Node fila(Label dkprEtiqueta, Label dkprValor) {
        dkprEtiqueta.setMinWidth(Region.USE_PREF_SIZE);
        dkprValor.setAlignment(Pos.CENTER_RIGHT);
        dkprValor.setTextAlignment(TextAlignment.RIGHT);
        HBox.setHgrow(dkprValor, Priority.ALWAYS);
        HBox dkprFila = new HBox(6, dkprEtiqueta, dkprValor);
        dkprFila.setAlignment(Pos.TOP_LEFT);
        return dkprFila;
    }

    private static Node centrar(Node dkprNodo) {
        HBox dkprCaja = new HBox(dkprNodo);
        dkprCaja.setAlignment(Pos.CENTER);
        VBox.setMargin(dkprCaja, new Insets(4, 0, 2, 0));
        return dkprCaja;
    }

    private static Node separador() {
        Region dkprLinea = new Region();
        dkprLinea.setStyle("-fx-border-color: black; -fx-border-style: dashed; -fx-border-width: 0 0 1 0;");
        VBox.setMargin(dkprLinea, new Insets(3, 0, 3, 0));
        return dkprLinea;
    }
}
