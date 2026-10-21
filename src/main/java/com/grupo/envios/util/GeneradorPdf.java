package com.grupo.envios.util;

import com.grupo.envios.model.Comprobante;
import com.grupo.envios.model.DetalleEnvio;
import com.grupo.envios.model.Envio;
import com.grupo.envios.model.HistorialEnvio;
import com.grupo.envios.model.ResumenFila;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Genera los documentos PDF del sistema: detalle de un envío y reporte de envíos. */
public final class GeneradorPdf {

    private static final Color AZUL = new Color(0x1f, 0x3a, 0x5f);
    private static final Color GRIS_FONDO = new Color(0xf4, 0xf6, 0xf9);
    private static final Color GRIS_TEXTO = new Color(0x44, 0x54, 0x6a);
    private static final Color ROJO = new Color(0xc0, 0x39, 0x2b);

    private static final Font TITULO = new Font(Font.HELVETICA, 18, Font.BOLD, AZUL);
    private static final Font SUBTITULO = new Font(Font.HELVETICA, 12, Font.BOLD, AZUL);
    private static final Font ETIQUETA = new Font(Font.HELVETICA, 9, Font.BOLD, GRIS_TEXTO);
    private static final Font NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font PEQUENA = new Font(Font.HELVETICA, 8, Font.NORMAL, GRIS_TEXTO);
    private static final Font CABECERA = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font CODIGO = new Font(Font.HELVETICA, 22, Font.BOLD, AZUL);
    private static final Font TOTAL = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(0x1e, 0x84, 0x49));
    private static final Font ANULADO = new Font(Font.HELVETICA, 12, Font.BOLD, ROJO);

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private GeneradorPdf() {
    }

    /** Muestra el diálogo "Guardar como" para un PDF. Devuelve null si el usuario cancela. */
    public static File elegirDestino(Window owner, String titulo, String nombreInicial) {
        FileChooser selector = new FileChooser();
        selector.setTitle(titulo);
        selector.setInitialFileName(nombreInicial);
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));
        return selector.showSaveDialog(owner);
    }

    /** Avisa dónde se guardó el PDF y, si el usuario quiere, lo abre con el visor del sistema. */
    public static void ofrecerAbrir(File archivo) {
        if (!Alertas.confirmar("PDF guardado en:\n" + archivo.getAbsolutePath() + "\n\n¿Deseas abrirlo ahora?")) {
            return;
        }
        // Desktop es de AWT: se abre fuera del hilo de JavaFX para no congelar la ventana.
        Thread hilo = new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(archivo);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    // --------------------------------------------------------- detalle de envío

    public static void detalleEnvio(DetalleEnvio d, File archivo) throws IOException {
        Envio e = d.envio();
        Comprobante c = d.comprobante();

        Document doc = new Document(PageSize.A4, 40, 40, 36, 36);
        try (OutputStream out = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(doc, out);
            doc.addTitle("Envío " + e.getCodigoSeguimiento());
            doc.open();

            encabezado(doc, "Detalle de envío", e.getOrigen()
                    + (e.getOrigenDireccion() == null ? "" : " - " + e.getOrigenDireccion()));

            // Código y estado
            PdfPTable codigo = new PdfPTable(new float[]{3, 2});
            codigo.setWidthPercentage(100);
            PdfPCell celdaCodigo = new PdfPCell();
            celdaCodigo.addElement(new Paragraph("CÓDIGO DE SEGUIMIENTO", ETIQUETA));
            celdaCodigo.addElement(new Paragraph(e.getCodigoSeguimiento(), CODIGO));
            PdfPCell celdaEstado = new PdfPCell();
            celdaEstado.addElement(new Paragraph("ESTADO ACTUAL", ETIQUETA));
            celdaEstado.addElement(new Paragraph(e.getEstado(),
                    e.getIdEstadoActual() == Estados.ANULADO ? ANULADO : SUBTITULO));
            celdaEstado.addElement(new Paragraph("Registrado: " + e.getFechaTexto(), PEQUENA));
            for (PdfPCell celda : List.of(celdaCodigo, celdaEstado)) {
                celda.setBackgroundColor(GRIS_FONDO);
                celda.setBorder(Rectangle.NO_BORDER);
                celda.setPadding(10);
                codigo.addCell(celda);
            }
            codigo.setSpacingAfter(12);
            doc.add(codigo);

            // Datos del envío
            seccion(doc, "Datos del envío");
            PdfPTable datos = new PdfPTable(new float[]{1.5f, 3, 1.5f, 3});
            datos.setWidthPercentage(100);
            par(datos, "Origen", e.getOrigen());
            par(datos, "Destino", e.getDestino());
            par(datos, "Remitente", e.getRemitente());
            par(datos, "Destinatario", e.getDestinatario());
            par(datos, "Documento", e.getRemitenteDocumento());
            par(datos, "Documento", e.getDestinatarioDocumento());
            par(datos, "Teléfono", e.getRemitenteTelefono());
            par(datos, "Teléfono", e.getDestinatarioTelefono());
            par(datos, "Contenido", e.getDescripcion());
            par(datos, "Bultos / peso", e.getCantidadBultos() + " bulto(s) / " + e.getPesoKg().toPlainString() + " kg");
            par(datos, "Valor declarado", Formato.soles(e.getValorDeclarado()));
            par(datos, "Atendido por", e.getEmpleadoRegistro());
            if (e.getObservaciones() != null && !e.getObservaciones().isBlank()) {
                datos.addCell(celdaEtiqueta("Observaciones"));
                PdfPCell obs = celdaValor(e.getObservaciones());
                obs.setColspan(3);
                datos.addCell(obs);
            }
            datos.setSpacingAfter(12);
            doc.add(datos);

            // Comprobante
            seccion(doc, "Comprobante de pago");
            PdfPTable pago = new PdfPTable(new float[]{1.5f, 3, 1.5f, 3});
            pago.setWidthPercentage(100);
            if (c != null) {
                par(pago, "Tipo", c.getTipo());
                par(pago, "Número", c.getNumeroTexto());
                par(pago, "Fecha emisión", c.getFechaTexto());
                par(pago, "Forma de pago", c.getMetodoPago());
                par(pago, "Op. gravada", Formato.soles(c.getSubtotal()));
                par(pago, "IGV (18 %)", Formato.soles(c.getIgv()));
                pago.addCell(celdaEtiqueta("TOTAL"));
                PdfPCell total = new PdfPCell(new Phrase(Formato.soles(c.getTotal()), TOTAL));
                total.setBorder(Rectangle.NO_BORDER);
                total.setColspan(3);
                total.setPadding(4);
                pago.addCell(total);
            } else {
                par(pago, "Costo", Formato.soles(e.getCostoEnvio()));
                par(pago, "Comprobante", "No registrado");
            }
            pago.setSpacingAfter(12);
            doc.add(pago);

            // Historial
            seccion(doc, "Historial de movimientos");
            PdfPTable hist = new PdfPTable(new float[]{1.6f, 1.8f, 1.6f, 2, 3});
            hist.setWidthPercentage(100);
            hist.setHeaderRows(1);
            for (String titulo : List.of("Fecha", "Estado", "Agencia", "Empleado", "Observación")) {
                hist.addCell(celdaCabecera(titulo));
            }
            boolean alterna = false;
            for (HistorialEnvio h : d.historial()) {
                for (String valor : List.of(h.getFechaTexto(), h.getEstado(), h.getAgencia(),
                        h.getEmpleado(), Formato.texto(h.getObservacion()))) {
                    hist.addCell(celdaTabla(valor, alterna, Element.ALIGN_LEFT));
                }
                alterna = !alterna;
            }
            doc.add(hist);

            pie(doc);
            doc.close();
        } catch (DocumentException ex) {
            throw new IOException("No se pudo generar el PDF: " + ex.getMessage(), ex);
        }
    }

    // ------------------------------------------------------------------ reporte

    public static void reporte(LocalDate desde, LocalDate hasta, List<ResumenFila> porAgencia,
                               List<ResumenFila> porEstado, List<Envio> detalle, File archivo)
            throws IOException {
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 30, 30);
        try (OutputStream out = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(doc, out);
            doc.addTitle("Reporte de envíos");
            doc.open();

            encabezado(doc, "Reporte de envíos",
                    "Del " + desde.format(FECHA) + " al " + hasta.format(FECHA));

            BigDecimal ingresos = porAgencia.stream()
                    .map(ResumenFila::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            PdfPTable totales = new PdfPTable(2);
            totales.setWidthPercentage(50);
            totales.setHorizontalAlignment(Element.ALIGN_LEFT);
            PdfPCell envios = new PdfPCell();
            envios.addElement(new Paragraph("Envíos registrados", ETIQUETA));
            envios.addElement(new Paragraph(String.valueOf(detalle.size()), TOTAL));
            PdfPCell monto = new PdfPCell();
            monto.addElement(new Paragraph("Ingresos (sin anulados)", ETIQUETA));
            monto.addElement(new Paragraph(Formato.soles(ingresos), TOTAL));
            for (PdfPCell celda : List.of(envios, monto)) {
                celda.setBackgroundColor(GRIS_FONDO);
                celda.setBorder(Rectangle.NO_BORDER);
                celda.setPadding(8);
                totales.addCell(celda);
            }
            totales.setSpacingAfter(12);
            doc.add(totales);

            // Resúmenes lado a lado
            PdfPTable resumenes = new PdfPTable(new float[]{1, 0.08f, 1});
            resumenes.setWidthPercentage(100);
            resumenes.addCell(contenedor(tablaResumen("Por agencia de origen", "Agencia", "Ingresos (S/)", porAgencia)));
            resumenes.addCell(contenedor(null));
            resumenes.addCell(contenedor(tablaResumen("Por estado", "Estado", "Monto (S/)", porEstado)));
            resumenes.setSpacingAfter(12);
            doc.add(resumenes);

            seccion(doc, "Detalle de envíos");
            PdfPTable tabla = new PdfPTable(new float[]{1.1f, 1.3f, 1.4f, 1.4f, 2.2f, 2.2f, 1.6f, 1});
            tabla.setWidthPercentage(100);
            tabla.setHeaderRows(1);
            for (String titulo : List.of("Código", "Fecha", "Origen", "Destino", "Remitente",
                    "Destinatario", "Estado", "Costo (S/)")) {
                tabla.addCell(celdaCabecera(titulo));
            }
            boolean alterna = false;
            for (Envio e : detalle) {
                for (String valor : List.of(e.getCodigoSeguimiento(), e.getFechaTexto(), e.getOrigen(),
                        e.getDestino(), e.getRemitente(), e.getDestinatario(), e.getEstado())) {
                    tabla.addCell(celdaTabla(valor, alterna, Element.ALIGN_LEFT));
                }
                tabla.addCell(celdaTabla(e.getCostoEnvio().toPlainString(), alterna, Element.ALIGN_RIGHT));
                alterna = !alterna;
            }
            doc.add(tabla);

            pie(doc);
            doc.close();
        } catch (DocumentException ex) {
            throw new IOException("No se pudo generar el PDF: " + ex.getMessage(), ex);
        }
    }

    private static PdfPTable tablaResumen(String titulo, String columna, String columnaMonto,
                                          List<ResumenFila> filas) {
        PdfPTable t = new PdfPTable(new float[]{3, 1, 1.5f});
        t.setWidthPercentage(100);
        PdfPCell cab = new PdfPCell(new Phrase(titulo, SUBTITULO));
        cab.setColspan(3);
        cab.setBorder(Rectangle.NO_BORDER);
        cab.setPaddingBottom(6);
        t.addCell(cab);
        t.addCell(celdaCabecera(columna));
        t.addCell(celdaCabecera("Envíos"));
        t.addCell(celdaCabecera(columnaMonto));
        boolean alterna = false;
        for (ResumenFila f : filas) {
            t.addCell(celdaTabla(f.getEtiqueta(), alterna, Element.ALIGN_LEFT));
            t.addCell(celdaTabla(String.valueOf(f.getCantidad()), alterna, Element.ALIGN_RIGHT));
            t.addCell(celdaTabla(f.getMonto().setScale(2).toPlainString(), alterna, Element.ALIGN_RIGHT));
            alterna = !alterna;
        }
        return t;
    }

    // ------------------------------------------------------------------ ayudas

    private static void encabezado(Document doc, String titulo, String subtitulo) throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{3, 2});
        t.setWidthPercentage(100);

        PdfPCell izq = new PdfPCell();
        izq.addElement(new Paragraph("SISTEMA DE ENVÍOS", TITULO));
        izq.addElement(new Paragraph(titulo + "  |  " + subtitulo, NORMAL));
        izq.setBorder(Rectangle.BOTTOM);
        izq.setBorderColor(AZUL);
        izq.setBorderWidth(1.5f);
        izq.setPaddingBottom(8);

        Paragraph fecha = new Paragraph("Generado: " + LocalDateTime.now().format(FECHA_HORA), PEQUENA);
        fecha.setAlignment(Element.ALIGN_RIGHT);
        PdfPCell der = new PdfPCell();
        der.addElement(fecha);
        der.setBorder(Rectangle.BOTTOM);
        der.setBorderColor(AZUL);
        der.setBorderWidth(1.5f);
        der.setVerticalAlignment(Element.ALIGN_BOTTOM);
        der.setPaddingBottom(8);

        t.addCell(izq);
        t.addCell(der);
        t.setSpacingAfter(14);
        doc.add(t);
    }

    private static void seccion(Document doc, String titulo) throws DocumentException {
        Paragraph p = new Paragraph(titulo, SUBTITULO);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private static void pie(Document doc) throws DocumentException {
        Paragraph p = new Paragraph("Documento generado por el Sistema de Envíos. "
                + "Comprobante de práctica académica, sin valor tributario.", PEQUENA);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(18);
        doc.add(p);
    }

    private static void par(PdfPTable t, String etiqueta, String valor) {
        t.addCell(celdaEtiqueta(etiqueta));
        t.addCell(celdaValor(valor));
    }

    private static PdfPCell celdaEtiqueta(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto + ":", ETIQUETA));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4);
        return c;
    }

    private static PdfPCell celdaValor(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(Formato.texto(texto), NORMAL));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(4);
        return c;
    }

    private static PdfPCell celdaCabecera(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, CABECERA));
        c.setBackgroundColor(AZUL);
        c.setBorderColor(AZUL);
        c.setPadding(5);
        return c;
    }

    private static PdfPCell celdaTabla(String texto, boolean alterna, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, NORMAL));
        c.setBorderColor(new Color(0xdd, 0xe3, 0xea));
        c.setPadding(4);
        c.setHorizontalAlignment(alineacion);
        if (alterna) {
            c.setBackgroundColor(GRIS_FONDO);
        }
        return c;
    }

    private static PdfPCell contenedor(PdfPTable tabla) {
        PdfPCell c = new PdfPCell();
        if (tabla != null) {
            c.addElement(tabla); // así la tabla no se estira al alto de la fila
        }
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(0);
        return c;
    }
}
