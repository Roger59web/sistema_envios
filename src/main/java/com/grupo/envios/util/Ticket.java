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

    public static VBox crear(DetalleEnvio d) {
        Envio e = d.envio();
        Comprobante c = d.comprobante();

        VBox t = new VBox(3);
        t.setPadding(new Insets(12));
        t.setMinWidth(ANCHO);
        t.setPrefWidth(ANCHO);
        t.setMaxWidth(ANCHO);
        t.setStyle("-fx-background-color: white;");

        // Cabecera
        t.getChildren().add(centrado("SISTEMA DE ENVÍOS", 15, true));
        t.getChildren().add(centrado(e.getOrigen(), 11, false));
        if (e.getOrigenDireccion() != null) {
            t.getChildren().add(centrado(e.getOrigenDireccion(), 10, false));
        }
        if (e.getOrigenTelefono() != null && !e.getOrigenTelefono().isBlank()) {
            t.getChildren().add(centrado("Tel: " + e.getOrigenTelefono(), 10, false));
        }
        t.getChildren().add(separador());

        // Comprobante
        if (c != null) {
            t.getChildren().add(centrado("BOLETA".equals(c.getTipo()) ? "BOLETA DE VENTA" : "FACTURA", 12, true));
            t.getChildren().add(centrado(c.getNumeroTexto(), 12, true));
            t.getChildren().add(fila("Fecha:", c.getFechaTexto()));
        } else {
            t.getChildren().add(fila("Fecha:", e.getFechaTexto()));
        }
        t.getChildren().add(separador());

        // Código de seguimiento
        t.getChildren().add(centrado("CÓDIGO DE SEGUIMIENTO", 10, false));
        t.getChildren().add(centrado(e.getCodigoSeguimiento(), 22, true));
        if (e.getIdEstadoActual() == Estados.ANULADO) {
            t.getChildren().add(centrado("*** ENVÍO ANULADO ***", 13, true));
        }
        t.getChildren().add(separador());

        // Ruta y personas
        t.getChildren().add(fila("Origen:", e.getOrigen()));
        t.getChildren().add(fila("Destino:", e.getDestino()));
        t.getChildren().add(separador());
        persona(t, "REMITENTE", e.getRemitente(), e.getRemitenteDocumento(), e.getRemitenteTelefono());
        persona(t, "DESTINATARIO", e.getDestinatario(), e.getDestinatarioDocumento(), e.getDestinatarioTelefono());
        t.getChildren().add(separador());

        // Paquete
        t.getChildren().add(texto("Contenido: " + e.getDescripcion(), 11, false));
        t.getChildren().add(fila("Bultos:", String.valueOf(e.getCantidadBultos())));
        t.getChildren().add(fila("Peso:", e.getPesoKg().toPlainString() + " kg"));
        t.getChildren().add(fila("Valor declarado:", Formato.soles(e.getValorDeclarado())));
        if (e.getObservaciones() != null && !e.getObservaciones().isBlank()) {
            t.getChildren().add(texto("Obs: " + e.getObservaciones(), 11, false));
        }
        t.getChildren().add(separador());

        // Importes
        if (c != null) {
            t.getChildren().add(fila("Op. gravada:", Formato.soles(c.getSubtotal())));
            t.getChildren().add(fila("IGV (18%):", Formato.soles(c.getIgv())));
            t.getChildren().add(filaTotal(Formato.soles(c.getTotal())));
            t.getChildren().add(fila("Forma de pago:", c.getMetodoPago()));
        } else {
            t.getChildren().add(filaTotal(Formato.soles(e.getCostoEnvio())));
        }
        t.getChildren().add(separador());

        // Pie
        t.getChildren().add(fila("Estado:", e.getEstado()));
        t.getChildren().add(texto("Atendido por: " + Formato.texto(e.getEmpleadoRegistro()), 11, false));
        t.getChildren().add(separador());
        t.getChildren().add(centrado("Consulte el estado de su envío", 10, false));
        t.getChildren().add(centrado("con el código de seguimiento.", 10, false));
        t.getChildren().add(centrado("¡Gracias por su preferencia!", 11, true));
        t.getChildren().add(centrado("Comprobante de práctica, sin valor tributario.", 9, false));
        return t;
    }

    /**
     * Abre el diálogo de impresión y manda el ticket a la impresora elegida
     * (sirve también "Microsoft Print to PDF"). Si no entra en la página, se reduce.
     */
    public static void imprimir(DetalleEnvio d, Window owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Alertas.advertencia("No se encontró ninguna impresora instalada.");
            return;
        }
        if (!job.showPrintDialog(owner)) {
            return;
        }

        // El nodo debe estar en una escena para calcular su tamaño antes de imprimir.
        VBox ticket = crear(d);
        Group grupo = new Group(ticket);
        new Scene(grupo);
        grupo.applyCss();
        grupo.layout();

        PageLayout pagina = job.getJobSettings().getPageLayout();
        double escala = Math.min(1, Math.min(
                pagina.getPrintableWidth() / ticket.getWidth(),
                pagina.getPrintableHeight() / ticket.getHeight()));
        ticket.getTransforms().add(new Scale(escala, escala));

        if (job.printPage(ticket)) {
            job.endJob();
        } else {
            Alertas.error("No se pudo imprimir el ticket.");
        }
    }

    // ----------------------------------------------------------------- ayudas

    private static void persona(VBox t, String rol, String nombre, String documento, String telefono) {
        t.getChildren().add(texto(rol, 11, true));
        t.getChildren().add(texto(nombre, 11, false));
        String datos = "Doc: " + documento;
        if (telefono != null && !telefono.isBlank()) {
            datos += "   Tel: " + telefono;
        }
        t.getChildren().add(texto(datos, 10, false));
    }

    private static Label texto(String valor, double tamano, boolean negrita) {
        Label l = new Label(valor == null ? "" : valor);
        l.setFont(Font.font(FUENTE, negrita ? FontWeight.BOLD : FontWeight.NORMAL, tamano));
        l.setTextFill(Color.BLACK);
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private static Label centrado(String valor, double tamano, boolean negrita) {
        Label l = texto(valor, tamano, negrita);
        l.setAlignment(Pos.CENTER);
        l.setTextAlignment(TextAlignment.CENTER);
        return l;
    }

    private static Node fila(String etiqueta, String valor) {
        return fila(texto(etiqueta, 11, false), texto(Formato.texto(valor), 11, false));
    }

    private static Node filaTotal(String total) {
        return fila(texto("TOTAL:", 14, true), texto(total, 14, true));
    }

    private static Node fila(Label etiqueta, Label valor) {
        etiqueta.setMinWidth(Region.USE_PREF_SIZE);
        valor.setAlignment(Pos.CENTER_RIGHT);
        valor.setTextAlignment(TextAlignment.RIGHT);
        HBox.setHgrow(valor, Priority.ALWAYS);
        HBox fila = new HBox(6, etiqueta, valor);
        fila.setAlignment(Pos.TOP_LEFT);
        return fila;
    }

    private static Node separador() {
        Region linea = new Region();
        linea.setStyle("-fx-border-color: black; -fx-border-style: dashed; -fx-border-width: 0 0 1 0;");
        VBox.setMargin(linea, new Insets(3, 0, 3, 0));
        return linea;
    }
}
