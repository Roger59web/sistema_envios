package com.grupo.envios.util;

import com.grupo.envios.model.DkprCajaTurno;
import com.grupo.envios.model.DkprCobroCaja;
import com.grupo.envios.model.DkprComprobante;
import com.grupo.envios.model.DkprDetalleEnvio;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.model.DkprHistorialEnvio;
import com.grupo.envios.model.DkprManifiesto;
import com.grupo.envios.model.DkprResumenFila;
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

public final class DkprGeneradorPdf {

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

    private DkprGeneradorPdf() {
    }

    public static File dkprElegirDestino(Window dkprOwner, String dkprTitulo, String dkprNombreInicial) {
        FileChooser dkprSelector = new FileChooser();
        dkprSelector.setTitle(dkprTitulo);
        dkprSelector.setInitialFileName(dkprNombreInicial);
        dkprSelector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));
        return dkprSelector.showSaveDialog(dkprOwner);
    }

    public static void dkprOfrecerAbrir(File dkprArchivo) {
        if (!DkprAlertas.dkprConfirmar("PDF guardado en:\n" + dkprArchivo.getAbsolutePath() + "\n\n¿Deseas abrirlo ahora?")) {
            return;
        }
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

    public static void dkprDetalleEnvio(DkprDetalleEnvio dkprD, File dkprArchivo) throws IOException {
        DkprEnvio dkprE = dkprD.dkprEnvio();
        DkprComprobante dkprC = dkprD.dkprComprobante();

        Document dkprDoc = new Document(PageSize.A4, 40, 40, 36, 36);
        try (OutputStream dkprOut = new FileOutputStream(dkprArchivo)) {
            PdfWriter.getInstance(dkprDoc, dkprOut);
            dkprDoc.addTitle("Envío " + dkprE.dkprGetCodigoSeguimiento());
            dkprDoc.open();

            dkprEncabezado(dkprDoc, "Detalle de envío", dkprE.dkprGetOrigen()
                    + (dkprE.dkprGetOrigenDireccion() == null ? "" : " - " + dkprE.dkprGetOrigenDireccion()));

            PdfPTable dkprCodigo = new PdfPTable(new float[]{3, 2});
            dkprCodigo.setWidthPercentage(100);
            PdfPCell dkprCeldaCodigo = new PdfPCell();
            dkprCeldaCodigo.addElement(new Paragraph("CÓDIGO DE SEGUIMIENTO", ETIQUETA));
            dkprCeldaCodigo.addElement(new Paragraph(dkprE.dkprGetCodigoSeguimiento(), CODIGO));
            dkprCeldaCodigo.addElement(dkprImagen(DkprCodigosBarras.dkprImagenBarras(dkprE.dkprGetCodigoSeguimiento(), 3, 60), 180, 34));
            PdfPCell dkprCeldaEstado = new PdfPCell();
            dkprCeldaEstado.addElement(new Paragraph("ESTADO ACTUAL", ETIQUETA));
            dkprCeldaEstado.addElement(new Paragraph(dkprE.dkprGetEstado(),
                    dkprE.dkprGetIdEstadoActual() == DkprEstados.ANULADO ? ANULADO : SUBTITULO));
            dkprCeldaEstado.addElement(new Paragraph("Registrado: " + dkprE.dkprGetFechaTexto(), PEQUENA));
            for (PdfPCell dkprCelda : List.of(dkprCeldaCodigo, dkprCeldaEstado)) {
                dkprCelda.setBackgroundColor(GRIS_FONDO);
                dkprCelda.setBorder(Rectangle.NO_BORDER);
                dkprCelda.setPadding(10);
                dkprCodigo.addCell(dkprCelda);
            }
            dkprCodigo.setSpacingAfter(12);
            dkprDoc.add(dkprCodigo);

            dkprSeccion(dkprDoc, "Datos del envío");
            PdfPTable dkprDatos = new PdfPTable(new float[]{1.5f, 3, 1.5f, 3});
            dkprDatos.setWidthPercentage(100);
            dkprPar(dkprDatos, "Origen", dkprE.dkprGetOrigen());
            dkprPar(dkprDatos, "Destino", dkprE.dkprGetDestino());
            dkprPar(dkprDatos, "Remitente", dkprE.dkprGetRemitente());
            dkprPar(dkprDatos, "Destinatario", dkprE.dkprGetDestinatario());
            dkprPar(dkprDatos, "Documento", dkprE.dkprGetRemitenteDocumento());
            dkprPar(dkprDatos, "Documento", dkprE.dkprGetDestinatarioDocumento());
            dkprPar(dkprDatos, "Teléfono", dkprE.dkprGetRemitenteTelefono());
            dkprPar(dkprDatos, "Teléfono", dkprE.dkprGetDestinatarioTelefono());
            dkprPar(dkprDatos, "Contenido", dkprE.dkprGetDescripcion());
            dkprPar(dkprDatos, "Bultos / peso", dkprE.dkprGetCantidadBultos() + " bulto(s) / " + dkprE.dkprGetPesoKg().toPlainString() + " kg");
            dkprPar(dkprDatos, "Valor declarado", DkprFormato.dkprSoles(dkprE.dkprGetValorDeclarado()));
            dkprPar(dkprDatos, "Atendido por", dkprE.dkprGetEmpleadoRegistro());
            if (dkprE.dkprGetManifiesto() != null) {
                dkprPar(dkprDatos, "Vehículo", dkprE.dkprGetVehiculoTexto());
                dkprPar(dkprDatos, "Conductor", dkprE.dkprGetConductor());
            }
            if (dkprE.dkprGetObservaciones() != null && !dkprE.dkprGetObservaciones().isBlank()) {
                dkprDatos.addCell(dkprCeldaEtiqueta("Observaciones"));
                PdfPCell dkprObs = dkprCeldaValor(dkprE.dkprGetObservaciones());
                dkprObs.setColspan(3);
                dkprDatos.addCell(dkprObs);
            }
            dkprDatos.setSpacingAfter(12);
            dkprDoc.add(dkprDatos);

            dkprSeccion(dkprDoc, "Comprobante de pago");
            PdfPTable dkprPago = new PdfPTable(new float[]{1.5f, 3, 1.5f, 3});
            dkprPago.setWidthPercentage(100);
            if (dkprC != null) {
                dkprPar(dkprPago, "Tipo", dkprC.dkprGetTipo());
                dkprPar(dkprPago, "Número", dkprC.dkprGetNumeroTexto());
                dkprPar(dkprPago, "Fecha emisión", dkprC.dkprGetFechaTexto());
                dkprPar(dkprPago, "Forma de pago", dkprC.dkprGetMetodoPago());
                dkprPar(dkprPago, "Op. gravada", DkprFormato.dkprSoles(dkprC.dkprGetSubtotal()));
                dkprPar(dkprPago, "IGV (18 %)", DkprFormato.dkprSoles(dkprC.dkprGetIgv()));
                dkprPago.addCell(dkprCeldaEtiqueta("TOTAL"));
                PdfPCell dkprTotal = new PdfPCell(new Phrase(DkprFormato.dkprSoles(dkprC.dkprGetTotal()), TOTAL));
                dkprTotal.setBorder(Rectangle.NO_BORDER);
                dkprTotal.setColspan(3);
                dkprTotal.setPadding(4);
                dkprPago.addCell(dkprTotal);
            } else {
                dkprPar(dkprPago, "Costo", DkprFormato.dkprSoles(dkprE.dkprGetCostoEnvio()));
                dkprPar(dkprPago, "Comprobante", "No registrado");
            }
            dkprPago.setSpacingAfter(12);
            dkprDoc.add(dkprPago);

            dkprSeccion(dkprDoc, "Historial de movimientos");
            PdfPTable dkprHist = new PdfPTable(new float[]{1.6f, 1.8f, 1.6f, 2, 3});
            dkprHist.setWidthPercentage(100);
            dkprHist.setHeaderRows(1);
            for (String dkprTitulo : List.of("Fecha", "Estado", "Agencia", "Empleado", "Observación")) {
                dkprHist.addCell(dkprCeldaCabecera(dkprTitulo));
            }
            boolean dkprAlterna = false;
            for (DkprHistorialEnvio dkprH : dkprD.dkprHistorial()) {
                for (String dkprValor : List.of(dkprH.dkprGetFechaTexto(), dkprH.dkprGetEstado(), dkprH.dkprGetAgencia(),
                        dkprH.dkprGetEmpleado(), DkprFormato.dkprTexto(dkprH.dkprGetObservacion()))) {
                    dkprHist.addCell(dkprCeldaTabla(dkprValor, dkprAlterna, Element.ALIGN_LEFT));
                }
                dkprAlterna = !dkprAlterna;
            }
            dkprDoc.add(dkprHist);

            dkprPie(dkprDoc);
            dkprDoc.close();
        } catch (DocumentException dkprEx) {
            throw new IOException("No se pudo generar el PDF: " + dkprEx.getMessage(), dkprEx);
        }
    }

    public static void dkprReporte(LocalDate dkprDesde, LocalDate dkprHasta, List<DkprResumenFila> dkprPorAgencia,
                               List<DkprResumenFila> dkprPorEstado, List<DkprEnvio> dkprDetalle, File dkprArchivo)
            throws IOException {
        Document dkprDoc = new Document(PageSize.A4.rotate(), 36, 36, 30, 30);
        try (OutputStream dkprOut = new FileOutputStream(dkprArchivo)) {
            PdfWriter.getInstance(dkprDoc, dkprOut);
            dkprDoc.addTitle("Reporte de envíos");
            dkprDoc.open();

            dkprEncabezado(dkprDoc, "Reporte de envíos",
                    "Del " + dkprDesde.format(FECHA) + " al " + dkprHasta.format(FECHA));

            BigDecimal dkprIngresos = dkprPorAgencia.stream()
                    .map(DkprResumenFila::dkprGetMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            PdfPTable dkprTotales = new PdfPTable(2);
            dkprTotales.setWidthPercentage(50);
            dkprTotales.setHorizontalAlignment(Element.ALIGN_LEFT);
            PdfPCell dkprEnvios = new PdfPCell();
            dkprEnvios.addElement(new Paragraph("Envíos registrados", ETIQUETA));
            dkprEnvios.addElement(new Paragraph(String.valueOf(dkprDetalle.size()), TOTAL));
            PdfPCell dkprMonto = new PdfPCell();
            dkprMonto.addElement(new Paragraph("Ingresos (sin anulados)", ETIQUETA));
            dkprMonto.addElement(new Paragraph(DkprFormato.dkprSoles(dkprIngresos), TOTAL));
            for (PdfPCell dkprCelda : List.of(dkprEnvios, dkprMonto)) {
                dkprCelda.setBackgroundColor(GRIS_FONDO);
                dkprCelda.setBorder(Rectangle.NO_BORDER);
                dkprCelda.setPadding(8);
                dkprTotales.addCell(dkprCelda);
            }
            dkprTotales.setSpacingAfter(12);
            dkprDoc.add(dkprTotales);

            PdfPTable dkprResumenes = new PdfPTable(new float[]{1, 0.08f, 1});
            dkprResumenes.setWidthPercentage(100);
            dkprResumenes.addCell(dkprContenedor(dkprTablaResumen("Por agencia de origen", "Agencia", "Ingresos (S/)", dkprPorAgencia)));
            dkprResumenes.addCell(dkprContenedor(null));
            dkprResumenes.addCell(dkprContenedor(dkprTablaResumen("Por estado", "Estado", "Monto (S/)", dkprPorEstado)));
            dkprResumenes.setSpacingAfter(12);
            dkprDoc.add(dkprResumenes);

            dkprSeccion(dkprDoc, "Detalle de envíos");
            PdfPTable dkprTabla = new PdfPTable(new float[]{1.1f, 1.3f, 1.4f, 1.4f, 2.2f, 2.2f, 1.6f, 1});
            dkprTabla.setWidthPercentage(100);
            dkprTabla.setHeaderRows(1);
            for (String dkprTitulo : List.of("Código", "Fecha", "Origen", "Destino", "Remitente",
                    "Destinatario", "Estado", "Costo (S/)")) {
                dkprTabla.addCell(dkprCeldaCabecera(dkprTitulo));
            }
            boolean dkprAlterna = false;
            for (DkprEnvio dkprE : dkprDetalle) {
                for (String dkprValor : List.of(dkprE.dkprGetCodigoSeguimiento(), dkprE.dkprGetFechaTexto(), dkprE.dkprGetOrigen(),
                        dkprE.dkprGetDestino(), dkprE.dkprGetRemitente(), dkprE.dkprGetDestinatario(), dkprE.dkprGetEstado())) {
                    dkprTabla.addCell(dkprCeldaTabla(dkprValor, dkprAlterna, Element.ALIGN_LEFT));
                }
                dkprTabla.addCell(dkprCeldaTabla(dkprE.dkprGetCostoEnvio().toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
                dkprAlterna = !dkprAlterna;
            }
            dkprDoc.add(dkprTabla);

            dkprPie(dkprDoc);
            dkprDoc.close();
        } catch (DocumentException dkprEx) {
            throw new IOException("No se pudo generar el PDF: " + dkprEx.getMessage(), dkprEx);
        }
    }

    public static void dkprEtiquetas(List<DkprEnvio> dkprEnvios, File dkprArchivo) throws IOException {
        Document dkprDoc = new Document(PageSize.A4, 20, 20, 20, 20);
        try (OutputStream dkprOut = new FileOutputStream(dkprArchivo)) {
            PdfWriter.getInstance(dkprDoc, dkprOut);
            dkprDoc.addTitle("Etiquetas de bultos");
            dkprDoc.open();

            PdfPTable dkprHoja = new PdfPTable(2);
            dkprHoja.setWidthPercentage(100);
            int dkprCantidad = 0;
            for (DkprEnvio dkprE : dkprEnvios) {
                for (int dkprN = 1; dkprN <= dkprE.dkprGetCantidadBultos(); dkprN++) {
                    dkprHoja.addCell(dkprEtiqueta(dkprE, dkprN));
                    dkprCantidad++;
                }
            }
            if (dkprCantidad % 2 == 1) {
                dkprHoja.addCell(dkprContenedor(null));
            }
            dkprDoc.add(dkprHoja);
            dkprDoc.close();
        } catch (DocumentException dkprEx) {
            throw new IOException("No se pudo generar el PDF: " + dkprEx.getMessage(), dkprEx);
        }
    }

    private static PdfPCell dkprEtiqueta(DkprEnvio dkprE, int dkprNroBulto) throws IOException {
        String dkprCodigo = DkprCodigosBarras.dkprCodigoBulto(dkprE, dkprNroBulto);

        PdfPTable dkprArriba = new PdfPTable(new float[]{3, 1.1f});
        dkprArriba.setWidthPercentage(100);
        PdfPCell dkprDatos = new PdfPCell();
        dkprDatos.setBorder(Rectangle.NO_BORDER);
        dkprDatos.addElement(new Paragraph("SISTEMA DE ENVÍOS", ETIQUETA));
        dkprDatos.addElement(new Paragraph("DESTINO", PEQUENA));
        dkprDatos.addElement(new Paragraph(dkprE.dkprGetDestino(), SUBTITULO));
        dkprDatos.addElement(new Paragraph("Para: " + dkprE.dkprGetDestinatario(), NORMAL));
        dkprDatos.addElement(new Paragraph("De: " + dkprE.dkprGetOrigen(), PEQUENA));
        dkprArriba.addCell(dkprDatos);
        PdfPCell dkprQr = new PdfPCell(dkprImagen(DkprCodigosBarras.dkprImagenQR(dkprCodigo, 8), 68, 68));
        dkprQr.setBorder(Rectangle.NO_BORDER);
        dkprQr.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dkprArriba.addCell(dkprQr);

        PdfPTable dkprAbajo = new PdfPTable(2);
        dkprAbajo.setWidthPercentage(100);
        PdfPCell dkprTextoCodigo = new PdfPCell(new Phrase(dkprCodigo, ETIQUETA));
        PdfPCell dkprTextoBulto = new PdfPCell(new Phrase("Bulto " + dkprNroBulto + " de " + dkprE.dkprGetCantidadBultos(), ETIQUETA));
        dkprTextoBulto.setHorizontalAlignment(Element.ALIGN_RIGHT);
        for (PdfPCell dkprC : List.of(dkprTextoCodigo, dkprTextoBulto)) {
            dkprC.setBorder(Rectangle.NO_BORDER);
            dkprAbajo.addCell(dkprC);
        }

        PdfPCell dkprCelda = new PdfPCell();
        dkprCelda.addElement(dkprArriba);
        dkprCelda.addElement(dkprImagen(DkprCodigosBarras.dkprImagenBarras(dkprCodigo, 3, 60), 245, 38));
        dkprCelda.addElement(dkprAbajo);
        dkprCelda.setFixedHeight(196);
        dkprCelda.setPadding(8);
        dkprCelda.setBorderColor(GRIS_TEXTO);
        dkprCelda.setBorderWidth(0.5f);
        return dkprCelda;
    }

    private static com.lowagie.text.Image dkprImagen(java.awt.image.BufferedImage dkprImg, float dkprAncho, float dkprAlto)
            throws IOException {
        com.lowagie.text.Image dkprI = com.lowagie.text.Image.getInstance(dkprImg, null);
        dkprI.scaleAbsolute(dkprAncho, dkprAlto);
        return dkprI;
    }

    public static void dkprManifiesto(DkprManifiesto dkprM, List<DkprEnvio> dkprEnvios, File dkprArchivo) throws IOException {
        Document dkprDoc = new Document(PageSize.A4, 40, 40, 36, 36);
        try (OutputStream dkprOut = new FileOutputStream(dkprArchivo)) {
            PdfWriter.getInstance(dkprDoc, dkprOut);
            dkprDoc.addTitle("Manifiesto " + dkprM.dkprGetCodigo());
            dkprDoc.open();

            dkprEncabezado(dkprDoc, "Manifiesto de carga", dkprM.dkprGetOrigen() + " → " + dkprM.dkprGetDestino());

            int dkprBultos = dkprEnvios.stream().mapToInt(DkprEnvio::dkprGetCantidadBultos).sum();
            BigDecimal dkprPeso = dkprEnvios.stream().map(DkprEnvio::dkprGetPesoKg).reduce(BigDecimal.ZERO, BigDecimal::add);

            PdfPTable dkprCodigo = new PdfPTable(new float[]{3, 2});
            dkprCodigo.setWidthPercentage(100);
            PdfPCell dkprCeldaCodigo = new PdfPCell();
            dkprCeldaCodigo.addElement(new Paragraph("MANIFIESTO", ETIQUETA));
            dkprCeldaCodigo.addElement(new Paragraph(dkprM.dkprGetCodigo(), CODIGO));
            dkprCeldaCodigo.addElement(dkprImagen(DkprCodigosBarras.dkprImagenBarras(dkprM.dkprGetCodigo(), 3, 60), 190, 34));
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

            dkprSeccion(dkprDoc, "Datos del viaje");
            PdfPTable dkprDatos = new PdfPTable(new float[]{1.5f, 3, 1.5f, 3});
            dkprDatos.setWidthPercentage(100);
            dkprPar(dkprDatos, "Origen", dkprM.dkprGetOrigen());
            dkprPar(dkprDatos, "Destino", dkprM.dkprGetDestino());
            dkprPar(dkprDatos, "Salida", dkprM.dkprGetFechaSalidaTexto());
            dkprPar(dkprDatos, "Despachado por", dkprM.dkprGetEmpleado());
            dkprPar(dkprDatos, "Placa", dkprM.dkprGetPlaca());
            dkprPar(dkprDatos, "Conductor", dkprM.dkprGetConductor());
            if (dkprM.dkprGetObservacion() != null && !dkprM.dkprGetObservacion().isBlank()) {
                dkprDatos.addCell(dkprCeldaEtiqueta("Observación"));
                PdfPCell dkprObs = dkprCeldaValor(dkprM.dkprGetObservacion());
                dkprObs.setColspan(3);
                dkprDatos.addCell(dkprObs);
            }
            dkprDatos.setSpacingAfter(12);
            dkprDoc.add(dkprDatos);

            dkprSeccion(dkprDoc, "Envíos");
            PdfPTable dkprTabla = new PdfPTable(new float[]{0.45f, 1.4f, 2.3f, 1.35f, 2.1f, 0.95f, 0.8f, 1.6f});
            dkprTabla.setWidthPercentage(100);
            dkprTabla.setHeaderRows(1);
            for (String dkprTitulo : List.of("#", "Código", "Destinatario", "Documento", "Contenido", "Bultos",
                    "Kg", "Estado")) {
                dkprTabla.addCell(dkprCeldaCabecera(dkprTitulo));
            }
            boolean dkprAlterna = false;
            int dkprN = 1;
            for (DkprEnvio dkprE : dkprEnvios) {
                dkprTabla.addCell(dkprCeldaTabla(String.valueOf(dkprN++), dkprAlterna, Element.ALIGN_RIGHT));
                for (String dkprValor : List.of(dkprE.dkprGetCodigoSeguimiento(), dkprE.dkprGetDestinatario(),
                        dkprE.dkprGetDestinatarioDocumento(), dkprE.dkprGetDescripcion())) {
                    dkprTabla.addCell(dkprCeldaTabla(dkprValor, dkprAlterna, Element.ALIGN_LEFT));
                }
                dkprTabla.addCell(dkprCeldaTabla(String.valueOf(dkprE.dkprGetCantidadBultos()), dkprAlterna, Element.ALIGN_RIGHT));
                dkprTabla.addCell(dkprCeldaTabla(dkprE.dkprGetPesoKg().toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
                dkprTabla.addCell(dkprCeldaTabla(dkprE.dkprGetEstado(), dkprAlterna, Element.ALIGN_LEFT));
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
                PdfPCell dkprEspacio = dkprContenedor(null);
                PdfPTable dkprBloque = new PdfPTable(new float[]{0.1f, 1, 0.1f});
                dkprBloque.addCell(dkprEspacio);
                dkprBloque.addCell(dkprC);
                dkprBloque.addCell(dkprContenedor(null));
                dkprFirmas.addCell(dkprContenedor(dkprBloque));
            }
            dkprDoc.add(dkprFirmas);

            dkprPie(dkprDoc);
            dkprDoc.close();
        } catch (DocumentException dkprEx) {
            throw new IOException("No se pudo generar el PDF: " + dkprEx.getMessage(), dkprEx);
        }
    }

    public static void dkprCuadreCaja(DkprCajaTurno dkprT, List<DkprCobroCaja> dkprCobros, File dkprArchivo) throws IOException {
        Document dkprDoc = new Document(PageSize.A4, 40, 40, 36, 36);
        try (OutputStream dkprOut = new FileOutputStream(dkprArchivo)) {
            PdfWriter.getInstance(dkprDoc, dkprOut);
            dkprDoc.addTitle("Cuadre de caja " + dkprT.dkprGetFechaAperturaTexto());
            dkprDoc.open();

            dkprEncabezado(dkprDoc, "Cuadre de caja", dkprT.dkprGetAgencia());

            BigDecimal dkprDiferencia = dkprT.dkprGetDiferenciaTotal();
            PdfPTable dkprResumen = new PdfPTable(new float[]{3, 2});
            dkprResumen.setWidthPercentage(100);
            PdfPCell dkprCeldaResultado = new PdfPCell();
            dkprCeldaResultado.addElement(new Paragraph("RESULTADO", ETIQUETA));
            dkprCeldaResultado.addElement(new Paragraph(DkprCajaTurno.dkprResultado(dkprDiferencia),
                    dkprDiferencia != null && dkprDiferencia.signum() == 0 ? TOTAL : ANULADO));
            dkprCeldaResultado.addElement(new Paragraph(dkprT.dkprIsAbierto() ? "Turno ABIERTO (arqueo parcial)" : "Turno cerrado", PEQUENA));
            PdfPCell dkprCeldaTotales = new PdfPCell();
            dkprCeldaTotales.addElement(new Paragraph("TOTALES", ETIQUETA));
            dkprCeldaTotales.addElement(new Paragraph("Esperado: " + DkprFormato.dkprSoles(dkprT.dkprGetTotalEsperado()), NORMAL));
            dkprCeldaTotales.addElement(new Paragraph("Contado: " + (dkprT.dkprGetTotalContado() == null ? "-"
                    : DkprFormato.dkprSoles(dkprT.dkprGetTotalContado())), NORMAL));
            for (PdfPCell dkprCelda : List.of(dkprCeldaResultado, dkprCeldaTotales)) {
                dkprCelda.setBackgroundColor(GRIS_FONDO);
                dkprCelda.setBorder(Rectangle.NO_BORDER);
                dkprCelda.setPadding(10);
                dkprResumen.addCell(dkprCelda);
            }
            dkprResumen.setSpacingAfter(12);
            dkprDoc.add(dkprResumen);

            dkprSeccion(dkprDoc, "Datos del turno");
            PdfPTable dkprDatos = new PdfPTable(new float[]{1.5f, 3, 1.5f, 3});
            dkprDatos.setWidthPercentage(100);
            dkprPar(dkprDatos, "Cajero", dkprT.dkprGetEmpleado());
            dkprPar(dkprDatos, "Agencia", dkprT.dkprGetAgencia());
            dkprPar(dkprDatos, "Apertura", dkprT.dkprGetFechaAperturaTexto());
            dkprPar(dkprDatos, "Cierre", dkprT.dkprGetFechaCierreTexto());
            dkprPar(dkprDatos, "Fondo inicial", DkprFormato.dkprSoles(dkprT.dkprGetMontoInicial()));
            dkprPar(dkprDatos, "Anulados", dkprT.dkprGetAnuladosCantidad() + " (" + DkprFormato.dkprSoles(dkprT.dkprGetAnuladosMonto()) + ", no se cuentan)");
            if (dkprT.dkprGetObservacion() != null && !dkprT.dkprGetObservacion().isBlank()) {
                dkprDatos.addCell(dkprCeldaEtiqueta("Observación"));
                PdfPCell dkprObs = dkprCeldaValor(dkprT.dkprGetObservacion());
                dkprObs.setColspan(3);
                dkprDatos.addCell(dkprObs);
            }
            dkprDatos.setSpacingAfter(12);
            dkprDoc.add(dkprDatos);

            dkprSeccion(dkprDoc, "Por método de pago");
            PdfPTable dkprMetodos = new PdfPTable(new float[]{2, 1, 1.6f, 1.6f, 2});
            dkprMetodos.setWidthPercentage(100);
            for (String dkprTitulo : List.of("Método", "Cobros", "Esperado (S/)", "Contado (S/)", "Diferencia")) {
                dkprMetodos.addCell(dkprCeldaCabecera(dkprTitulo));
            }
            boolean dkprAlterna = false;
            for (String dkprM : DkprCajaTurno.METODOS) {
                long dkprCantidad = dkprCobros.stream().filter(dkprC -> !dkprC.dkprIsAnulado() && dkprM.equals(dkprC.dkprGetMetodoPago())).count();
                dkprMetodos.addCell(dkprCeldaTabla("EFECTIVO".equals(dkprM) ? "EFECTIVO (incl. fondo)" : dkprM, dkprAlterna, Element.ALIGN_LEFT));
                dkprMetodos.addCell(dkprCeldaTabla(String.valueOf(dkprCantidad), dkprAlterna, Element.ALIGN_RIGHT));
                dkprMetodos.addCell(dkprCeldaTabla(dkprT.dkprGetEsperado(dkprM).setScale(2).toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
                BigDecimal dkprContado = dkprT.dkprGetContado(dkprM);
                dkprMetodos.addCell(dkprCeldaTabla(dkprContado == null ? "-" : dkprContado.setScale(2).toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
                dkprMetodos.addCell(dkprCeldaTabla(DkprCajaTurno.dkprResultado(dkprT.dkprGetDiferencia(dkprM)), dkprAlterna, Element.ALIGN_RIGHT));
                dkprAlterna = !dkprAlterna;
            }
            dkprMetodos.addCell(dkprCeldaEtiqueta("TOTAL"));
            dkprMetodos.addCell(dkprCeldaTabla(String.valueOf(dkprCobros.stream().filter(dkprC -> !dkprC.dkprIsAnulado()).count()), false, Element.ALIGN_RIGHT));
            dkprMetodos.addCell(dkprCeldaTabla(dkprT.dkprGetTotalEsperado().setScale(2).toPlainString(), false, Element.ALIGN_RIGHT));
            dkprMetodos.addCell(dkprCeldaTabla(dkprT.dkprGetTotalContado() == null ? "-" : dkprT.dkprGetTotalContado().setScale(2).toPlainString(), false, Element.ALIGN_RIGHT));
            dkprMetodos.addCell(dkprCeldaTabla(DkprCajaTurno.dkprResultado(dkprDiferencia), false, Element.ALIGN_RIGHT));
            dkprMetodos.setSpacingAfter(12);
            dkprDoc.add(dkprMetodos);

            dkprSeccion(dkprDoc, "Cobros del turno (" + dkprCobros.size() + ")");
            PdfPTable dkprDetalle = new PdfPTable(new float[]{1.2f, 1.8f, 1.5f, 1.3f, 1.2f, 2});
            dkprDetalle.setWidthPercentage(100);
            dkprDetalle.setHeaderRows(1);
            for (String dkprTitulo : List.of("Hora", "Comprobante", "Envío", "Método", "Total (S/)", "Estado")) {
                dkprDetalle.addCell(dkprCeldaCabecera(dkprTitulo));
            }
            dkprAlterna = false;
            for (DkprCobroCaja dkprC : dkprCobros) {
                for (String dkprValor : List.of(dkprC.dkprGetFechaTexto(), dkprC.dkprGetComprobante(), dkprC.dkprGetCodigoEnvio(), dkprC.dkprGetMetodoPago())) {
                    dkprDetalle.addCell(dkprCeldaTabla(dkprValor, dkprAlterna, Element.ALIGN_LEFT));
                }
                dkprDetalle.addCell(dkprCeldaTabla(dkprC.dkprGetTotal().toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
                dkprDetalle.addCell(dkprCeldaTabla(dkprC.dkprGetEstadoTexto(), dkprAlterna, Element.ALIGN_LEFT));
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

            dkprPie(dkprDoc);
            dkprDoc.close();
        } catch (DocumentException dkprEx) {
            throw new IOException("No se pudo generar el PDF: " + dkprEx.getMessage(), dkprEx);
        }
    }

    private static PdfPTable dkprTablaResumen(String dkprTitulo, String dkprColumna, String dkprColumnaMonto,
                                          List<DkprResumenFila> dkprFilas) {
        PdfPTable dkprT = new PdfPTable(new float[]{3, 1, 1.5f});
        dkprT.setWidthPercentage(100);
        PdfPCell dkprCab = new PdfPCell(new Phrase(dkprTitulo, SUBTITULO));
        dkprCab.setColspan(3);
        dkprCab.setBorder(Rectangle.NO_BORDER);
        dkprCab.setPaddingBottom(6);
        dkprT.addCell(dkprCab);
        dkprT.addCell(dkprCeldaCabecera(dkprColumna));
        dkprT.addCell(dkprCeldaCabecera("Envíos"));
        dkprT.addCell(dkprCeldaCabecera(dkprColumnaMonto));
        boolean dkprAlterna = false;
        for (DkprResumenFila dkprF : dkprFilas) {
            dkprT.addCell(dkprCeldaTabla(dkprF.dkprGetEtiqueta(), dkprAlterna, Element.ALIGN_LEFT));
            dkprT.addCell(dkprCeldaTabla(String.valueOf(dkprF.dkprGetCantidad()), dkprAlterna, Element.ALIGN_RIGHT));
            dkprT.addCell(dkprCeldaTabla(dkprF.dkprGetMonto().setScale(2).toPlainString(), dkprAlterna, Element.ALIGN_RIGHT));
            dkprAlterna = !dkprAlterna;
        }
        return dkprT;
    }

    private static void dkprEncabezado(Document dkprDoc, String dkprTitulo, String dkprSubtitulo) throws DocumentException {
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

    private static void dkprSeccion(Document dkprDoc, String dkprTitulo) throws DocumentException {
        Paragraph dkprP = new Paragraph(dkprTitulo, SUBTITULO);
        dkprP.setSpacingAfter(6);
        dkprDoc.add(dkprP);
    }

    private static void dkprPie(Document dkprDoc) throws DocumentException {
        Paragraph dkprP = new Paragraph("Documento generado por el Sistema de Envíos. "
                + "Comprobante de práctica académica, sin valor tributario.", PEQUENA);
        dkprP.setAlignment(Element.ALIGN_CENTER);
        dkprP.setSpacingBefore(18);
        dkprDoc.add(dkprP);
    }

    private static void dkprPar(PdfPTable dkprT, String dkprEtiqueta, String dkprValor) {
        dkprT.addCell(dkprCeldaEtiqueta(dkprEtiqueta));
        dkprT.addCell(dkprCeldaValor(dkprValor));
    }

    private static PdfPCell dkprCeldaEtiqueta(String dkprTexto) {
        PdfPCell dkprC = new PdfPCell(new Phrase(dkprTexto + ":", ETIQUETA));
        dkprC.setBorder(Rectangle.NO_BORDER);
        dkprC.setPadding(4);
        return dkprC;
    }

    private static PdfPCell dkprCeldaValor(String dkprTexto) {
        PdfPCell dkprC = new PdfPCell(new Phrase(DkprFormato.dkprTexto(dkprTexto), NORMAL));
        dkprC.setBorder(Rectangle.NO_BORDER);
        dkprC.setPadding(4);
        return dkprC;
    }

    private static PdfPCell dkprCeldaCabecera(String dkprTexto) {
        PdfPCell dkprC = new PdfPCell(new Phrase(dkprTexto, CABECERA));
        dkprC.setBackgroundColor(AZUL);
        dkprC.setBorderColor(AZUL);
        dkprC.setPadding(5);
        return dkprC;
    }

    private static PdfPCell dkprCeldaTabla(String dkprTexto, boolean dkprAlterna, int dkprAlineacion) {
        PdfPCell dkprC = new PdfPCell(new Phrase(dkprTexto == null ? "" : dkprTexto, NORMAL));
        dkprC.setBorderColor(new Color(0xdd, 0xe3, 0xea));
        dkprC.setPadding(4);
        dkprC.setHorizontalAlignment(dkprAlineacion);
        if (dkprAlterna) {
            dkprC.setBackgroundColor(GRIS_FONDO);
        }
        return dkprC;
    }

    private static PdfPCell dkprContenedor(PdfPTable dkprTabla) {
        PdfPCell dkprC = new PdfPCell();
        if (dkprTabla != null) {
            dkprC.addElement(dkprTabla);
        }
        dkprC.setBorder(Rectangle.NO_BORDER);
        dkprC.setPadding(0);
        return dkprC;
    }
}
