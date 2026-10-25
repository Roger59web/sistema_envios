package com.grupo.envios.util;

import com.grupo.envios.model.CajaTurno;
import com.grupo.envios.model.CobroCaja;
import com.grupo.envios.model.Comprobante;
import com.grupo.envios.model.DetalleEnvio;
import com.grupo.envios.model.Envio;
import com.grupo.envios.model.HistorialEnvio;
import com.grupo.envios.model.Manifiesto;
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

/** Genera los documentos PDF del sistema: detalle de un envío, reporte, etiquetas, manifiesto y cuadre de caja. */
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
    public static File elegirDestino(Window dkprOwner, String dkprTitulo, String dkprNombreInicial) {
        FileChooser dkprSelector = new FileChooser();
        dkprSelector.setTitle(dkprTitulo);
        dkprSelector.setInitialFileName(dkprNombreInicial);
        dkprSelector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));
        return dkprSelector.showSaveDialog(dkprOwner);
    }

    /** Avisa dónde se guardó el PDF y, si el usuario quiere, lo abre con el visor del sistema. */
    public static void ofrecerAbrir(File dkprArchivo) {
        if (!Alertas.confirmar("PDF guardado en:\n" + dkprArchivo.getAbsolutePath() + "\n\n¿Deseas abrirlo ahora?")) {
            return;
        }
        // Desktop es de AWT: se abre fuera del hilo de JavaFX para no congelar la ventana.
        Thread dkprHilo = new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(dkprArchivo);
                }
            } catch (IOException dkprE) {
                dkprE.printStackTrace();
            }
        });
        dkprHilo.setDaemon(true);
        dkprHilo.start();
    }

    // --------------------------------------------------------- detalle de envío

    public static void detalleEnvio(DetalleEnvio dkprD, File dkprArchivo) throws IOException {
        Envio dkprE = dkprD.envio();
        Comprobante dkprC = dkprD.comprobante();

        Document dkprDoc = new Document(PageSize.A4, 40, 40, 36, 36);
        try (OutputStream dkprOut = new FileOutputStream(dkprArchivo)) {
            PdfWriter.getInstance(dkprDoc, dkprOut);
            dkprDoc.addTitle("Envío " + dkprE.getCodigoSeguimiento());
            dkprDoc.open();

            encabezado(dkprDoc, "Detalle de envío", dkprE.getOrigen()
                    + (dkprE.getOrigenDireccion() == null ? "" : " - " + dkprE.getOrigenDireccion()));

            // Código y estado
            PdfPTable dkprCodigo = new PdfPTable(new float[]{3, 2});
            dkprCodigo.setWidthPercentage(100);
            PdfPCell dkprCeldaCodigo = new PdfPCell();
            dkprCeldaCodigo.addElement(new Paragraph("CÓDIGO DE SEGUIMIENTO", ETIQUETA));
            dkprCeldaCodigo.addElement(new Paragraph(dkprE.getCodigoSeguimiento(), CODIGO));
            PdfPCell dkprCeldaEstado = new PdfPCell();
            dkprCeldaEstado.addElement(new Paragraph("ESTADO ACTUAL", ETIQUETA));
            dkprCeldaEstado.addElement(new Paragraph(dkprE.getEstado(),
                    dkprE.getIdEstadoActual() == Estados.ANULADO ? ANULADO : SUBTITULO));
            dkprCeldaEstado.addElement(new Paragraph("Registrado: " + dkprE.getFechaTexto(), PEQUENA));
            for (PdfPCell dkprCelda : List.of(dkprCeldaCodigo, dkprCeldaEstado)) {
                dkprCelda.setBackgroundColor(GRIS_FONDO);
                dkprCelda.setBorder(Rectangle.NO_BORDER);
                dkprCelda.setPadding(10);
                dkprCodigo.addCell(dkprCelda);
            }
            dkprCodigo.setSpacingAfter(12);
            dkprDoc.add(dkprCodigo);

            // Datos del envío
            seccion(dkprDoc, "Datos del envío");
            PdfPTable dkprDatos = new PdfPTable(new float[]{1.5f, 3, 1.5f, 3});
            dkprDatos.setWidthPercentage(100);
            par(dkprDatos, "Origen", dkprE.getOrigen());
            par(dkprDatos, "Destino", dkprE.getDestino());
            par(dkprDatos, "Remitente", dkprE.getRemitente());
            par(dkprDatos, "Destinatario", dkprE.getDestinatario());
            par(dkprDatos, "Documento", dkprE.getRemitenteDocumento());
            par(dkprDatos, "Documento", dkprE.getDestinatarioDocumento());
            par(dkprDatos, "Teléfono", dkprE.getRemitenteTelefono());
            par(dkprDatos, "Teléfono", dkprE.getDestinatarioTelefono());
            par(dkprDatos, "Contenido", dkprE.getDescripcion());
            par(dkprDatos, "Bultos / peso", dkprE.getCantidadBultos() + " bulto(s) / " + dkprE.getPesoKg().toPlainString() + " kg");
            par(dkprDatos, "Valor declarado", Formato.soles(dkprE.getValorDeclarado()));
            par(dkprDatos, "Atendido por", dkprE.getEmpleadoRegistro());
            if (dkprE.getObservaciones() != null && !dkprE.getObservaciones().isBlank()) {
                dkprDatos.addCell(celdaEtiqueta("Observaciones"));
                PdfPCell dkprObs = celdaValor(dkprE.getObservaciones());
                dkprObs.setColspan(3);
                dkprDatos.addCell(dkprObs);
            }
            dkprDatos.setSpacingAfter(12);
            dkprDoc.add(dkprDatos);

            // Comprobante
            seccion(dkprDoc, "Comprobante de pago");
            PdfPTable dkprPago = new PdfPTable(new float[]{1.5f, 3, 1.5f, 3});
            dkprPago.setWidthPercentage(100);
            if (dkprC != null) {
                par(dkprPago, "Tipo", dkprC.getTipo());
                par(dkprPago, "Número", dkprC.getNumeroTexto());
                par(dkprPago, "Fecha emisión", dkprC.getFechaTexto());
                par(dkprPago, "Forma de pago", dkprC.getMetodoPago());
                par(dkprPago, "Op. gravada", Formato.soles(dkprC.getSubtotal()));
                par(dkprPago, "IGV (18 %)", Formato.soles(dkprC.getIgv()));
                dkprPago.addCell(celdaEtiqueta("TOTAL"));
                PdfPCell dkprTotal = new PdfPCell(new Phrase(Formato.soles(dkprC.getTotal()), TOTAL));
                dkprTotal.setBorder(Rectangle.NO_BORDER);
                dkprTotal.setColspan(3);
                dkprTotal.setPadding(4);
                dkprPago.addCell(dkprTotal);
            } else {
                par(dkprPago, "Costo", Formato.soles(dkprE.getCostoEnvio()));
                par(dkprPago, "Comprobante", "No registrado");
            }
            dkprPago.setSpacingAfter(12);
            dkprDoc.add(dkprPago);

            // Historial
            seccion(dkprDoc, "Historial de movimientos");
            PdfPTable dkprHist = new PdfPTable(new float[]{1.6f, 1.8f, 1.6f, 2, 3});
            dkprHist.setWidthPercentage(100);
            dkprHist.setHeaderRows(1);
            for (String dkprTitulo : List.of("Fecha", "Estado", "Agencia", "Empleado", "Observación")) {
                dkprHist.addCell(celdaCabecera(dkprTitulo));
            }
            boolean dkprAlterna = false;
            for (HistorialEnvio dkprH : dkprD.historial()) {
                for (String dkprValor : List.of(dkprH.getFechaTexto(), dkprH.getEstado(), dkprH.getAgencia(),
                        dkprH.getEmpleado(), Formato.texto(dkprH.getObservacion()))) {
                    dkprHist.addCell(celdaTabla(dkprValor, dkprAlterna, Element.ALIGN_LEFT));
                }
                dkprAlterna = !dkprAlterna;
            }
            dkprDoc.add(dkprHist);

            pie(dkprDoc);
            dkprDoc.close();
        } catch (DocumentException dkprEx) {
            throw new IOException("No se pudo generar el PDF: " + dkprEx.getMessage(), dkprEx);
        }
    }

    // ------------------------------------------------------------------ reporte

    public static void reporte(LocalDate dkprDesde, LocalDate dkprHasta, List<ResumenFila> dkprPorAgencia,
                               List<ResumenFila> dkprPorEstado, List<Envio> dkprDetalle, File dkprArchivo)
            throws IOException {
        Document dkprDoc = new Document(PageSize.A4.rotate(), 36, 36, 30, 30);
        try (OutputStream dkprOut = new FileOutputStream(dkprArchivo)) {
            PdfWriter.getInstance(dkprDoc, dkprOut);
            dkprDoc.addTitle("Reporte de envíos");
            dkprDoc.open();

            encabezado(dkprDoc, "Reporte de envíos",
                    "Del " + dkprDesde.format(FECHA) + " al " + dkprHasta.format(FECHA));

            BigDecimal dkprIngresos = dkprPorAgencia.stream()
                    .map(ResumenFila::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            PdfPTable dkprTotales = new PdfPTable(2);
            dkprTotales.setWidthPercentage(50);
            dkprTotales.setHorizontalAlignment(Element.ALIGN_LEFT);
            PdfPCell dkprEnvios = new PdfPCell();
            dkprEnvios.addElement(new Paragraph("Envíos registrados", ETIQUETA));
            dkprEnvios.addElement(new Paragraph(String.valueOf(dkprDetalle.size()), TOTAL));
            PdfPCell dkprMonto = new PdfPCell();
            dkprMonto.addElement(new Paragraph("Ingresos (sin anulados)", ETIQUETA));
            dkprMonto.addElement(new Paragraph(Formato.soles(dkprIngresos), TOTAL));
            for (PdfPCell dkprCelda : List.of(dkprEnvios, dkprMonto)) {
                dkprCelda.setBackgroundColor(GRIS_FONDO);
                dkprCelda.setBorder(Rectangle.NO_BORDER);
                dkprCelda.setPadding(8);
                dkprTotales.addCell(dkprCelda);
            }
            dkprTotales.setSpacingAfter(12);
            dkprDoc.add(dkprTotales);

            // Resúmenes lado a lado
            PdfPTable dkprResumenes = new PdfPTable(new float[]{1, 0.08f, 1});
            dkprResumenes.setWidthPercentage(100);
            dkprResumenes.addCell(contenedor(tablaResumen("Por agencia de origen", "Agencia", "Ingresos (S/)", dkprPorAgencia)));
            dkprResumenes.addCell(contenedor(null));
            dkprResumenes.addCell(contenedor(tablaResumen("Por estado", "Estado", "Monto (S/)", dkprPorEstado)));
            dkprResumenes.setSpacingAfter(12);
            dkprDoc.add(dkprResumenes);

            seccion(dkprDoc, "Detalle de envíos");
            PdfPTable dkprTabla = new PdfPTable(new float[]{1.1f, 1.3f, 1.4f, 1.4f, 2.2f, 2.2f, 1.6f, 1});
            dkprTabla.setWidthPercentage(100);
            dkprTabla.setHeaderRows(1);
            for (String dkprTitulo : List.of("Código", "Fecha", "Origen", "Destino", "Remitente",
                    "Destinatario", "Estado", "Costo (S/)")) {
                dkprTabla.addCell(celdaCabecera(dkprTitulo));
            }
            boolean dkprAlterna = false;
            for (Envio dkprE : dkprDetalle) {
                for (String dkprValor : List.of(dkprE.getCodigoSeguimiento(), dkprE.getFechaTexto(), dkprE.getOrigen(),
                        dkprE.getDestino(), dkprE.getRemitente(), dkprE.getDestinatario(), dkprE.getEstado())) {
                    dkprTabla.addCell(celdaTabla(dkprValor, dkprAlterna, Element.ALIGN_LEFT));
                }
                dkprTabla.addCell(celdaTabla(dkprE.getCostoEnvio().toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
                dkprAlterna = !dkprAlterna;
            }
            dkprDoc.add(dkprTabla);

            pie(dkprDoc);
            dkprDoc.close();
        } catch (DocumentException dkprEx) {
            throw new IOException("No se pudo generar el PDF: " + dkprEx.getMessage(), dkprEx);
        }
    }

    // -------------------------------------------------------- etiquetas de bultos

    /** Etiquetas de bultos en hojas A4: 8 por hoja (2 × 4), con líneas para recortar. */
    public static void etiquetas(List<Envio> dkprEnvios, File dkprArchivo) throws IOException {
        Document dkprDoc = new Document(PageSize.A4, 20, 20, 20, 20);
        try (OutputStream dkprOut = new FileOutputStream(dkprArchivo)) {
            PdfWriter.getInstance(dkprDoc, dkprOut);
            dkprDoc.addTitle("Etiquetas de bultos");
            dkprDoc.open();

            PdfPTable dkprHoja = new PdfPTable(2);
            dkprHoja.setWidthPercentage(100);
            int dkprCantidad = 0;
            for (Envio dkprE : dkprEnvios) {
                for (int dkprN = 1; dkprN <= dkprE.getCantidadBultos(); dkprN++) {
                    dkprHoja.addCell(etiqueta(dkprE, dkprN));
                    dkprCantidad++;
                }
            }
            if (dkprCantidad % 2 == 1) {
                dkprHoja.addCell(contenedor(null));
            }
            dkprDoc.add(dkprHoja);
            dkprDoc.close();
        } catch (DocumentException dkprEx) {
            throw new IOException("No se pudo generar el PDF: " + dkprEx.getMessage(), dkprEx);
        }
    }

    private static PdfPCell etiqueta(Envio dkprE, int dkprNroBulto) throws IOException {
        String dkprCodigo = CodigosBarras.codigoBulto(dkprE, dkprNroBulto);

        PdfPTable dkprArriba = new PdfPTable(new float[]{3, 1.1f});
        dkprArriba.setWidthPercentage(100);
        PdfPCell dkprDatos = new PdfPCell();
        dkprDatos.setBorder(Rectangle.NO_BORDER);
        dkprDatos.addElement(new Paragraph("SISTEMA DE ENVÍOS", ETIQUETA));
        dkprDatos.addElement(new Paragraph("DESTINO", PEQUENA));
        dkprDatos.addElement(new Paragraph(dkprE.getDestino(), SUBTITULO));
        dkprDatos.addElement(new Paragraph("Para: " + dkprE.getDestinatario(), NORMAL));
        dkprDatos.addElement(new Paragraph("De: " + dkprE.getOrigen(), PEQUENA));
        dkprArriba.addCell(dkprDatos);
        PdfPCell dkprQr = new PdfPCell(imagen(CodigosBarras.imagenQR(dkprCodigo, 8), 68, 68));
        dkprQr.setBorder(Rectangle.NO_BORDER);
        dkprQr.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dkprArriba.addCell(dkprQr);

        PdfPTable dkprAbajo = new PdfPTable(2);
        dkprAbajo.setWidthPercentage(100);
        PdfPCell dkprTextoCodigo = new PdfPCell(new Phrase(dkprCodigo, ETIQUETA));
        PdfPCell dkprTextoBulto = new PdfPCell(new Phrase("Bulto " + dkprNroBulto + " de " + dkprE.getCantidadBultos(), ETIQUETA));
        dkprTextoBulto.setHorizontalAlignment(Element.ALIGN_RIGHT);
        for (PdfPCell dkprC : List.of(dkprTextoCodigo, dkprTextoBulto)) {
            dkprC.setBorder(Rectangle.NO_BORDER);
            dkprAbajo.addCell(dkprC);
        }

        PdfPCell dkprCelda = new PdfPCell();
        dkprCelda.addElement(dkprArriba);
        dkprCelda.addElement(imagen(CodigosBarras.imagenBarras(dkprCodigo, 3, 60), 245, 38));
        dkprCelda.addElement(dkprAbajo);
        dkprCelda.setFixedHeight(196);
        dkprCelda.setPadding(8);
        dkprCelda.setBorderColor(GRIS_TEXTO);
        dkprCelda.setBorderWidth(0.5f);
        return dkprCelda;
    }

    private static com.lowagie.text.Image imagen(java.awt.image.BufferedImage dkprImg, float dkprAncho, float dkprAlto)
            throws IOException {
        com.lowagie.text.Image dkprI = com.lowagie.text.Image.getInstance(dkprImg, null);
        dkprI.scaleAbsolute(dkprAncho, dkprAlto);
        return dkprI;
    }

    // ------------------------------------------------------- manifiesto de carga

    /** Manifiesto de carga (lo que lleva el conductor): envíos, bultos, peso y firmas. */
    public static void manifiesto(Manifiesto dkprM, List<Envio> dkprEnvios, File dkprArchivo) throws IOException {
        Document dkprDoc = new Document(PageSize.A4, 40, 40, 36, 36);
        try (OutputStream dkprOut = new FileOutputStream(dkprArchivo)) {
            PdfWriter.getInstance(dkprDoc, dkprOut);
            dkprDoc.addTitle("Manifiesto " + dkprM.getCodigo());
            dkprDoc.open();

            encabezado(dkprDoc, "Manifiesto de carga", dkprM.getOrigen() + " → " + dkprM.getDestino());

            int dkprBultos = dkprEnvios.stream().mapToInt(Envio::getCantidadBultos).sum();
            BigDecimal dkprPeso = dkprEnvios.stream().map(Envio::getPesoKg).reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPTable dkprCodigo = new PdfPTable(new float[]{3, 2});
            dkprCodigo.setWidthPercentage(100);
            PdfPCell dkprCeldaCodigo = new PdfPCell();
            dkprCeldaCodigo.addElement(new Paragraph("MANIFIESTO", ETIQUETA));
            dkprCeldaCodigo.addElement(new Paragraph(dkprM.getCodigo(), CODIGO));
            dkprCeldaCodigo.addElement(imagen(CodigosBarras.imagenBarras(dkprM.getCodigo(), 3, 60), 190, 34));
            PdfPCell dkprCeldaTotales = new PdfPCell();
            dkprCeldaTotales.addElement(new Paragraph("CARGA", ETIQUETA));
            dkprCeldaTotales.addElement(new Paragraph(dkprEnvios.size() + " envío(s)  ·  " + dkprBultos + " bulto(s)", SUBTITULO));
            dkprCeldaTotales.addElement(new Paragraph("Peso total: " + dkprPeso.toPlainString() + " kg", NORMAL));
            for (PdfPCell dkprCelda : List.of(dkprCeldaCodigo, dkprCeldaTotales)) {
                dkprCelda.setBackgroundColor(GRIS_FONDO);
                dkprCelda.setBorder(Rectangle.NO_BORDER);
                dkprCelda.setPadding(10);
                dkprCodigo.addCell(dkprCelda);
            }
            dkprCodigo.setSpacingAfter(12);
            dkprDoc.add(dkprCodigo);

            seccion(dkprDoc, "Datos del viaje");
            PdfPTable dkprDatos = new PdfPTable(new float[]{1.5f, 3, 1.5f, 3});
            dkprDatos.setWidthPercentage(100);
            par(dkprDatos, "Origen", dkprM.getOrigen());
            par(dkprDatos, "Destino", dkprM.getDestino());
            par(dkprDatos, "Salida", dkprM.getFechaSalidaTexto());
            par(dkprDatos, "Despachado por", dkprM.getEmpleado());
            par(dkprDatos, "Placa", dkprM.getPlaca());
            par(dkprDatos, "Conductor", dkprM.getConductor());
            if (dkprM.getObservacion() != null && !dkprM.getObservacion().isBlank()) {
                dkprDatos.addCell(celdaEtiqueta("Observación"));
                PdfPCell dkprObs = celdaValor(dkprM.getObservacion());
                dkprObs.setColspan(3);
                dkprDatos.addCell(dkprObs);
            }
            dkprDatos.setSpacingAfter(12);
            dkprDoc.add(dkprDatos);

            seccion(dkprDoc, "Envíos");
            PdfPTable dkprTabla = new PdfPTable(new float[]{0.45f, 1.4f, 2.3f, 1.35f, 2.1f, 0.95f, 0.8f, 1.6f});
            dkprTabla.setWidthPercentage(100);
            dkprTabla.setHeaderRows(1);
            for (String dkprTitulo : List.of("#", "Código", "Destinatario", "Documento", "Contenido", "Bultos",
                    "Kg", "Estado")) {
                dkprTabla.addCell(celdaCabecera(dkprTitulo));
            }
            boolean dkprAlterna = false;
            int dkprN = 1;
            for (Envio dkprE : dkprEnvios) {
                dkprTabla.addCell(celdaTabla(String.valueOf(dkprN++), dkprAlterna, Element.ALIGN_RIGHT));
                for (String dkprValor : List.of(dkprE.getCodigoSeguimiento(), dkprE.getDestinatario(),
                        dkprE.getDestinatarioDocumento(), dkprE.getDescripcion())) {
                    dkprTabla.addCell(celdaTabla(dkprValor, dkprAlterna, Element.ALIGN_LEFT));
                }
                dkprTabla.addCell(celdaTabla(String.valueOf(dkprE.getCantidadBultos()), dkprAlterna, Element.ALIGN_RIGHT));
                dkprTabla.addCell(celdaTabla(dkprE.getPesoKg().toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
                dkprTabla.addCell(celdaTabla(dkprE.getEstado(), dkprAlterna, Element.ALIGN_LEFT));
                dkprAlterna = !dkprAlterna;
            }
            dkprTabla.setSpacingAfter(36);
            dkprDoc.add(dkprTabla);

            PdfPTable dkprFirmas = new PdfPTable(3);
            dkprFirmas.setWidthPercentage(100);
            for (String dkprFirma : List.of("Despachado por (almacén origen)", "Conductor", "Recibido por (almacén destino)")) {
                PdfPCell dkprC = new PdfPCell(new Phrase(dkprFirma, PEQUENA));
                dkprC.setBorder(Rectangle.TOP);
                dkprC.setBorderColor(GRIS_TEXTO);
                dkprC.setHorizontalAlignment(Element.ALIGN_CENTER);
                dkprC.setPaddingTop(4);
                PdfPCell dkprEspacio = contenedor(null);
                PdfPTable dkprBloque = new PdfPTable(new float[]{0.1f, 1, 0.1f});
                dkprBloque.addCell(dkprEspacio);
                dkprBloque.addCell(dkprC);
                dkprBloque.addCell(contenedor(null));
                dkprFirmas.addCell(contenedor(dkprBloque));
            }
            dkprDoc.add(dkprFirmas);

            pie(dkprDoc);
            dkprDoc.close();
        } catch (DocumentException dkprEx) {
            throw new IOException("No se pudo generar el PDF: " + dkprEx.getMessage(), dkprEx);
        }
    }

    // ------------------------------------------------------------ cuadre de caja

    /** Reporte del turno de caja: esperado vs. contado por método de pago, diferencia, cobros y firmas. */
    public static void cuadreCaja(CajaTurno dkprT, List<CobroCaja> dkprCobros, File dkprArchivo) throws IOException {
        Document dkprDoc = new Document(PageSize.A4, 40, 40, 36, 36);
        try (OutputStream dkprOut = new FileOutputStream(dkprArchivo)) {
            PdfWriter.getInstance(dkprDoc, dkprOut);
            dkprDoc.addTitle("Cuadre de caja " + dkprT.getFechaAperturaTexto());
            dkprDoc.open();

            encabezado(dkprDoc, "Cuadre de caja", dkprT.getAgencia());

            BigDecimal dkprDiferencia = dkprT.getDiferenciaTotal();
            PdfPTable dkprResumen = new PdfPTable(new float[]{3, 2});
            dkprResumen.setWidthPercentage(100);
            PdfPCell dkprCeldaResultado = new PdfPCell();
            dkprCeldaResultado.addElement(new Paragraph("RESULTADO", ETIQUETA));
            dkprCeldaResultado.addElement(new Paragraph(CajaTurno.resultado(dkprDiferencia),
                    dkprDiferencia != null && dkprDiferencia.signum() == 0 ? TOTAL : ANULADO));
            dkprCeldaResultado.addElement(new Paragraph(dkprT.isAbierto() ? "Turno ABIERTO (arqueo parcial)" : "Turno cerrado", PEQUENA));
            PdfPCell dkprCeldaTotales = new PdfPCell();
            dkprCeldaTotales.addElement(new Paragraph("TOTALES", ETIQUETA));
            dkprCeldaTotales.addElement(new Paragraph("Esperado: " + Formato.soles(dkprT.getTotalEsperado()), NORMAL));
            dkprCeldaTotales.addElement(new Paragraph("Contado: " + (dkprT.getTotalContado() == null ? "-"
                    : Formato.soles(dkprT.getTotalContado())), NORMAL));
            for (PdfPCell dkprCelda : List.of(dkprCeldaResultado, dkprCeldaTotales)) {
                dkprCelda.setBackgroundColor(GRIS_FONDO);
                dkprCelda.setBorder(Rectangle.NO_BORDER);
                dkprCelda.setPadding(10);
                dkprResumen.addCell(dkprCelda);
            }
            dkprResumen.setSpacingAfter(12);
            dkprDoc.add(dkprResumen);

            seccion(dkprDoc, "Datos del turno");
            PdfPTable dkprDatos = new PdfPTable(new float[]{1.5f, 3, 1.5f, 3});
            dkprDatos.setWidthPercentage(100);
            par(dkprDatos, "Cajero", dkprT.getEmpleado());
            par(dkprDatos, "Agencia", dkprT.getAgencia());
            par(dkprDatos, "Apertura", dkprT.getFechaAperturaTexto());
            par(dkprDatos, "Cierre", dkprT.getFechaCierreTexto());
            par(dkprDatos, "Fondo inicial", Formato.soles(dkprT.getMontoInicial()));
            par(dkprDatos, "Anulados", dkprT.getAnuladosCantidad() + " (" + Formato.soles(dkprT.getAnuladosMonto()) + ", no se cuentan)");
            if (dkprT.getObservacion() != null && !dkprT.getObservacion().isBlank()) {
                dkprDatos.addCell(celdaEtiqueta("Observación"));
                PdfPCell dkprObs = celdaValor(dkprT.getObservacion());
                dkprObs.setColspan(3);
                dkprDatos.addCell(dkprObs);
            }
            dkprDatos.setSpacingAfter(12);
            dkprDoc.add(dkprDatos);

            seccion(dkprDoc, "Por método de pago");
            PdfPTable dkprMetodos = new PdfPTable(new float[]{2, 1, 1.6f, 1.6f, 2});
            dkprMetodos.setWidthPercentage(100);
            for (String dkprTitulo : List.of("Método", "Cobros", "Esperado (S/)", "Contado (S/)", "Diferencia")) {
                dkprMetodos.addCell(celdaCabecera(dkprTitulo));
            }
            boolean dkprAlterna = false;
            for (String dkprM : CajaTurno.METODOS) {
                long dkprCantidad = dkprCobros.stream().filter(dkprC -> !dkprC.isAnulado() && dkprM.equals(dkprC.getMetodoPago())).count();
                dkprMetodos.addCell(celdaTabla("EFECTIVO".equals(dkprM) ? "EFECTIVO (incl. fondo)" : dkprM, dkprAlterna, Element.ALIGN_LEFT));
                dkprMetodos.addCell(celdaTabla(String.valueOf(dkprCantidad), dkprAlterna, Element.ALIGN_RIGHT));
                dkprMetodos.addCell(celdaTabla(dkprT.getEsperado(dkprM).setScale(2).toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
                BigDecimal dkprContado = dkprT.getContado(dkprM);
                dkprMetodos.addCell(celdaTabla(dkprContado == null ? "-" : dkprContado.setScale(2).toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
                dkprMetodos.addCell(celdaTabla(CajaTurno.resultado(dkprT.getDiferencia(dkprM)), dkprAlterna, Element.ALIGN_RIGHT));
                dkprAlterna = !dkprAlterna;
            }
            dkprMetodos.addCell(celdaEtiqueta("TOTAL"));
            dkprMetodos.addCell(celdaTabla(String.valueOf(dkprCobros.stream().filter(dkprC -> !dkprC.isAnulado()).count()), false, Element.ALIGN_RIGHT));
            dkprMetodos.addCell(celdaTabla(dkprT.getTotalEsperado().setScale(2).toPlainString(), false, Element.ALIGN_RIGHT));
            dkprMetodos.addCell(celdaTabla(dkprT.getTotalContado() == null ? "-" : dkprT.getTotalContado().setScale(2).toPlainString(), false, Element.ALIGN_RIGHT));
            dkprMetodos.addCell(celdaTabla(CajaTurno.resultado(dkprDiferencia), false, Element.ALIGN_RIGHT));
            dkprMetodos.setSpacingAfter(12);
            dkprDoc.add(dkprMetodos);

            seccion(dkprDoc, "Cobros del turno (" + dkprCobros.size() + ")");
            PdfPTable dkprDetalle = new PdfPTable(new float[]{1.2f, 1.8f, 1.5f, 1.3f, 1.2f, 2});
            dkprDetalle.setWidthPercentage(100);
            dkprDetalle.setHeaderRows(1);
            for (String dkprTitulo : List.of("Hora", "Comprobante", "Envío", "Método", "Total (S/)", "Estado")) {
                dkprDetalle.addCell(celdaCabecera(dkprTitulo));
            }
            dkprAlterna = false;
            for (CobroCaja dkprC : dkprCobros) {
                for (String dkprValor : List.of(dkprC.getFechaTexto(), dkprC.getComprobante(), dkprC.getCodigoEnvio(), dkprC.getMetodoPago())) {
                    dkprDetalle.addCell(celdaTabla(dkprValor, dkprAlterna, Element.ALIGN_LEFT));
                }
                dkprDetalle.addCell(celdaTabla(dkprC.getTotal().toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
                dkprDetalle.addCell(celdaTabla(dkprC.getEstadoTexto(), dkprAlterna, Element.ALIGN_LEFT));
                dkprAlterna = !dkprAlterna;
            }
            dkprDetalle.setSpacingAfter(36);
            dkprDoc.add(dkprDetalle);

            PdfPTable dkprFirmas = new PdfPTable(new float[]{1, 0.3f, 1});
            dkprFirmas.setWidthPercentage(80);
            for (String dkprFirma : List.of("Cajero", "", "Supervisor / administrador")) {
                PdfPCell dkprC = new PdfPCell(new Phrase(dkprFirma, PEQUENA));
                dkprC.setBorder(dkprFirma.isEmpty() ? Rectangle.NO_BORDER : Rectangle.TOP);
                dkprC.setBorderColor(GRIS_TEXTO);
                dkprC.setHorizontalAlignment(Element.ALIGN_CENTER);
                dkprC.setPaddingTop(4);
                dkprFirmas.addCell(dkprC);
            }
            dkprDoc.add(dkprFirmas);

            pie(dkprDoc);
            dkprDoc.close();
        } catch (DocumentException dkprEx) {
            throw new IOException("No se pudo generar el PDF: " + dkprEx.getMessage(), dkprEx);
        }
    }

    private static PdfPTable tablaResumen(String dkprTitulo, String dkprColumna, String dkprColumnaMonto,
                                          List<ResumenFila> dkprFilas) {
        PdfPTable dkprT = new PdfPTable(new float[]{3, 1, 1.5f});
        dkprT.setWidthPercentage(100);
        PdfPCell dkprCab = new PdfPCell(new Phrase(dkprTitulo, SUBTITULO));
        dkprCab.setColspan(3);
        dkprCab.setBorder(Rectangle.NO_BORDER);
        dkprCab.setPaddingBottom(6);
        dkprT.addCell(dkprCab);
        dkprT.addCell(celdaCabecera(dkprColumna));
        dkprT.addCell(celdaCabecera("Envíos"));
        dkprT.addCell(celdaCabecera(dkprColumnaMonto));
        boolean dkprAlterna = false;
        for (ResumenFila dkprF : dkprFilas) {
            dkprT.addCell(celdaTabla(dkprF.getEtiqueta(), dkprAlterna, Element.ALIGN_LEFT));
            dkprT.addCell(celdaTabla(String.valueOf(dkprF.getCantidad()), dkprAlterna, Element.ALIGN_RIGHT));
            dkprT.addCell(celdaTabla(dkprF.getMonto().setScale(2).toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
            dkprAlterna = !dkprAlterna;
        }
        return dkprT;
    }

    // ------------------------------------------------------------------ ayudas

    private static void encabezado(Document dkprDoc, String dkprTitulo, String dkprSubtitulo) throws DocumentException {
        PdfPTable dkprT = new PdfPTable(new float[]{3, 2});
        dkprT.setWidthPercentage(100);

        PdfPCell dkprIzq = new PdfPCell();
        dkprIzq.addElement(new Paragraph("SISTEMA DE ENVÍOS", TITULO));
        dkprIzq.addElement(new Paragraph(dkprTitulo + "  |  " + dkprSubtitulo, NORMAL));
        dkprIzq.setBorder(Rectangle.BOTTOM);
        dkprIzq.setBorderColor(AZUL);
        dkprIzq.setBorderWidth(1.5f);
        dkprIzq.setPaddingBottom(8);

        Paragraph dkprFecha = new Paragraph("Generado: " + LocalDateTime.now().format(FECHA_HORA), PEQUENA);
        dkprFecha.setAlignment(Element.ALIGN_RIGHT);
        PdfPCell dkprDer = new PdfPCell();
        dkprDer.addElement(dkprFecha);
        dkprDer.setBorder(Rectangle.BOTTOM);
        dkprDer.setBorderColor(AZUL);
        dkprDer.setBorderWidth(1.5f);
        dkprDer.setVerticalAlignment(Element.ALIGN_BOTTOM);
        dkprDer.setPaddingBottom(8);

        dkprT.addCell(dkprIzq);
        dkprT.addCell(dkprDer);
        dkprT.setSpacingAfter(14);
        dkprDoc.add(dkprT);
    }

    private static void seccion(Document dkprDoc, String dkprTitulo) throws DocumentException {
        Paragraph dkprP = new Paragraph(dkprTitulo, SUBTITULO);
        dkprP.setSpacingAfter(6);
        dkprDoc.add(dkprP);
    }

    private static void pie(Document dkprDoc) throws DocumentException {
        Paragraph dkprP = new Paragraph("Documento generado por el Sistema de Envíos. "
                + "Comprobante de práctica académica, sin valor tributario.", PEQUENA);
        dkprP.setAlignment(Element.ALIGN_CENTER);
        dkprP.setSpacingBefore(18);
        dkprDoc.add(dkprP);
    }

    private static void par(PdfPTable dkprT, String dkprEtiqueta, String dkprValor) {
        dkprT.addCell(celdaEtiqueta(dkprEtiqueta));
        dkprT.addCell(celdaValor(dkprValor));
    }

    private static PdfPCell celdaEtiqueta(String dkprTexto) {
        PdfPCell dkprC = new PdfPCell(new Phrase(dkprTexto + ":", ETIQUETA));
        dkprC.setBorder(Rectangle.NO_BORDER);
        dkprC.setPadding(4);
        return dkprC;
    }

    private static PdfPCell celdaValor(String dkprTexto) {
        PdfPCell dkprC = new PdfPCell(new Phrase(Formato.texto(dkprTexto), NORMAL));
        dkprC.setBorder(Rectangle.NO_BORDER);
        dkprC.setPadding(4);
        return dkprC;
    }

    private static PdfPCell celdaCabecera(String dkprTexto) {
        PdfPCell dkprC = new PdfPCell(new Phrase(dkprTexto, CABECERA));
        dkprC.setBackgroundColor(AZUL);
        dkprC.setBorderColor(AZUL);
        dkprC.setPadding(5);
        return dkprC;
    }

    private static PdfPCell celdaTabla(String dkprTexto, boolean dkprAlterna, int dkprAlineacion) {
        PdfPCell dkprC = new PdfPCell(new Phrase(dkprTexto == null ? "" : dkprTexto, NORMAL));
        dkprC.setBorderColor(new Color(0xdd, 0xe3, 0xea));
        dkprC.setPadding(4);
        dkprC.setHorizontalAlignment(dkprAlineacion);
        if (dkprAlterna) {
            dkprC.setBackgroundColor(GRIS_FONDO);
        }
        return dkprC;
    }

    private static PdfPCell contenedor(PdfPTable dkprTabla) {
        PdfPCell dkprC = new PdfPCell();
        if (dkprTabla != null) {
            dkprC.addElement(dkprTabla); // así la tabla no se estira al alto de la fila
        }
        dkprC.setBorder(Rectangle.NO_BORDER);
        dkprC.setPadding(0);
        return dkprC;
    }
}
