package com.grupo.envios.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.grupo.envios.model.Envio;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Códigos de las etiquetas de bultos: ENV-000015-01 = bulto 1 del envío ENV-000015.
 * Se dibujan como Code128 y QR con el mismo contenido, así sirve cualquier tipo de lector.
 */
public final class CodigosBarras {

    /**
     * Acepta lo que escribe el lector aunque cambie los separadores: muchos lectores USB escriben
     * ' o ? en lugar de - cuando el teclado está en español. Ejemplos válidos: ENV-000015-01,
     * ENV'000015'01, 15-1, 000015 (sin número de bulto).
     */
    private static final Pattern LECTURA = Pattern.compile("^(?:ENV)?[^A-Z0-9]*(\\d{1,6})(?:[^A-Z0-9]+(\\d{1,3}))?[^A-Z0-9]*$");

    private CodigosBarras() {
    }

    /** Envío y número de bulto leídos de una etiqueta (nroBulto es null si se leyó solo el envío). */
    public record CodigoBulto(int idEnvio, Integer nroBulto) {
    }

    public static String codigoBulto(Envio dkprEnvio, int dkprNroBulto) {
        return dkprEnvio.getCodigoSeguimiento() + "-" + String.format("%02d", dkprNroBulto);
    }

    /** Interpreta el texto leído por el escáner. Devuelve null si no es un código de envío o bulto. */
    public static CodigoBulto leer(String dkprTexto) {
        if (dkprTexto == null) {
            return null;
        }
        Matcher dkprM = LECTURA.matcher(dkprTexto.trim().toUpperCase(Locale.ROOT));
        if (!dkprM.matches()) {
            return null;
        }
        int dkprIdEnvio = Integer.parseInt(dkprM.group(1));
        if (dkprIdEnvio == 0) {
            return null;
        }
        return new CodigoBulto(dkprIdEnvio, dkprM.group(2) == null ? null : Integer.parseInt(dkprM.group(2)));
    }

    /**
     * Código de seguimiento del envío a partir de lo escaneado o escrito: el ticket (ENV-000015),
     * la etiqueta de un bulto (ENV-000015-02), solo el número (15) o con otros separadores.
     * Si no se reconoce, devuelve el texto en mayúsculas (para que el mensaje de "no existe" lo muestre).
     */
    public static String normalizarEnvio(String dkprTexto) {
        if (dkprTexto == null || dkprTexto.isBlank()) {
            return "";
        }
        CodigoBulto dkprC = leer(dkprTexto);
        return dkprC == null ? dkprTexto.trim().toUpperCase(Locale.ROOT) : String.format("ENV-%06d", dkprC.idEnvio());
    }

    // ------------------------------------------------------------ módulos

    /** Módulos en blanco a cada lado del Code128: sin este margen muchos lectores no lo leen. */
    private static final int ZONA_SILENCIO = 10;

    /** Barras del Code128 con su zona de silencio (true = barra negra), un elemento por módulo. */
    static boolean[] modulosBarras(String dkprTexto) {
        boolean[] dkprCodigo = new Code128Writer().encode(dkprTexto);
        boolean[] dkprModulos = new boolean[dkprCodigo.length + 2 * ZONA_SILENCIO];
        System.arraycopy(dkprCodigo, 0, dkprModulos, ZONA_SILENCIO, dkprCodigo.length);
        return dkprModulos;
    }

    /** Matriz del QR sin margen, un pixel por módulo. */
    static BitMatrix modulosQR(String dkprTexto) {
        try {
            return new QRCodeWriter().encode(dkprTexto, BarcodeFormat.QR_CODE, 0, 0,
                    Map.of(EncodeHintType.MARGIN, 0, EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M));
        } catch (WriterException dkprE) {
            throw new IllegalArgumentException("No se pudo generar el QR de " + dkprTexto, dkprE);
        }
    }

    // ------------------------------------------------------------ JavaFX (vista previa e impresión)

    /**
     * Code128 como figuras vectoriales: al imprimir se dibuja a la resolución de la impresora
     * y las barras salen nítidas.
     */
    public static Group barras(String dkprTexto, double dkprAncho, double dkprAlto) {
        boolean[] dkprModulos = modulosBarras(dkprTexto);
        double dkprModulo = dkprAncho / dkprModulos.length;
        Group dkprG = new Group(new Rectangle(dkprAncho, dkprAlto, Color.WHITE));
        int dkprI = 0;
        while (dkprI < dkprModulos.length) {
            if (!dkprModulos[dkprI]) {
                dkprI++;
                continue;
            }
            int dkprInicio = dkprI;
            while (dkprI < dkprModulos.length && dkprModulos[dkprI]) {
                dkprI++;
            }
            dkprG.getChildren().add(new Rectangle(dkprInicio * dkprModulo, 0, (dkprI - dkprInicio) * dkprModulo, dkprAlto));
        }
        return dkprG;
    }

    /** QR como figuras vectoriales (una por tramo horizontal de módulos negros). */
    public static Group qr(String dkprTexto, double dkprLado) {
        BitMatrix dkprM = modulosQR(dkprTexto);
        double dkprModulo = dkprLado / dkprM.getWidth();
        Group dkprG = new Group(new Rectangle(dkprLado, dkprLado, Color.WHITE));
        for (int dkprY = 0; dkprY < dkprM.getHeight(); dkprY++) {
            int dkprX = 0;
            while (dkprX < dkprM.getWidth()) {
                if (!dkprM.get(dkprX, dkprY)) {
                    dkprX++;
                    continue;
                }
                int dkprInicio = dkprX;
                while (dkprX < dkprM.getWidth() && dkprM.get(dkprX, dkprY)) {
                    dkprX++;
                }
                // Un poco más alto que el módulo para que no queden líneas blancas entre filas.
                dkprG.getChildren().add(new Rectangle(dkprInicio * dkprModulo, dkprY * dkprModulo,
                        (dkprX - dkprInicio) * dkprModulo, dkprModulo + 0.3));
            }
        }
        return dkprG;
    }

    // ------------------------------------------------------------ imágenes (PDF)

    /** Code128 como imagen, con {@code escala} pixeles por módulo. */
    public static BufferedImage imagenBarras(String dkprTexto, int dkprEscala, int dkprAlto) {
        boolean[] dkprModulos = modulosBarras(dkprTexto);
        BufferedImage dkprImg = new BufferedImage(dkprModulos.length * dkprEscala, dkprAlto, BufferedImage.TYPE_BYTE_BINARY);
        for (int dkprX = 0; dkprX < dkprImg.getWidth(); dkprX++) {
            int dkprColor = dkprModulos[dkprX / dkprEscala] ? 0x000000 : 0xFFFFFF;
            for (int dkprY = 0; dkprY < dkprAlto; dkprY++) {
                dkprImg.setRGB(dkprX, dkprY, dkprColor);
            }
        }
        return dkprImg;
    }

    /** QR como imagen, con {@code escala} pixeles por módulo. */
    public static BufferedImage imagenQR(String dkprTexto, int dkprEscala) {
        BitMatrix dkprM = modulosQR(dkprTexto);
        BufferedImage dkprImg = new BufferedImage(dkprM.getWidth() * dkprEscala, dkprM.getHeight() * dkprEscala,
                BufferedImage.TYPE_BYTE_BINARY);
        for (int dkprX = 0; dkprX < dkprImg.getWidth(); dkprX++) {
            for (int dkprY = 0; dkprY < dkprImg.getHeight(); dkprY++) {
                dkprImg.setRGB(dkprX, dkprY, dkprM.get(dkprX / dkprEscala, dkprY / dkprEscala) ? 0x000000 : 0xFFFFFF);
            }
        }
        return dkprImg;
    }
}
