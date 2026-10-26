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

public final class CodigosBarras {

    private static final Pattern LECTURA = Pattern.compile("^(?:ENV)?[^A-Z0-9]*(\\d{1,6})(?:[^A-Z0-9]+(\\d{1,3}))?[^A-Z0-9]*$");

    private CodigosBarras() {
    }

    public record CodigoBulto(int idEnvio, Integer nroBulto) {
    }

    public static String codigoBulto(Envio dkprEnvio, int dkprNroBulto) {
        return dkprEnvio.getCodigoSeguimiento() + "-" + String.format("%02d", dkprNroBulto);
    }

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

    public static String normalizarEnvio(String dkprTexto) {
        if (dkprTexto == null || dkprTexto.isBlank()) {
            return "";
        }
        CodigoBulto dkprC = leer(dkprTexto);
        return dkprC == null ? dkprTexto.trim().toUpperCase(Locale.ROOT) : String.format("ENV-%06d", dkprC.idEnvio());
    }

    private static final int ZONA_SILENCIO = 10;

    static boolean[] modulosBarras(String dkprTexto) {
        boolean[] dkprCodigo = new Code128Writer().encode(dkprTexto);
        boolean[] dkprModulos = new boolean[dkprCodigo.length + 2 * ZONA_SILENCIO];
        System.arraycopy(dkprCodigo, 0, dkprModulos, ZONA_SILENCIO, dkprCodigo.length);
        return dkprModulos;
    }

    static BitMatrix modulosQR(String dkprTexto) {
        try {
            return new QRCodeWriter().encode(dkprTexto, BarcodeFormat.QR_CODE, 0, 0,
                    Map.of(EncodeHintType.MARGIN, 0, EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M));
        } catch (WriterException dkprE) {
            throw new IllegalArgumentException("No se pudo generar el QR de " + dkprTexto, dkprE);
        }
    }

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
                dkprG.getChildren().add(new Rectangle(dkprInicio * dkprModulo, dkprY * dkprModulo,
                        (dkprX - dkprInicio) * dkprModulo, dkprModulo + 0.3));
            }
        }
        return dkprG;
    }

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
