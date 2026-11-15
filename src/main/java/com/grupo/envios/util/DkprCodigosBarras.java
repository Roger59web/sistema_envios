package com.grupo.envios.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.grupo.envios.model.DkprEnvio;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DkprCodigosBarras {

    private static final Pattern LECTURA = Pattern.compile("^(?:ENV)?[^A-Z0-9]*(\\d{1,6})(?:[^A-Z0-9]+(\\d{1,3}))?[^A-Z0-9]*$");

    private DkprCodigosBarras() {
    }

    public record DkprCodigoBulto(int dkprIdEnvio, Integer dkprNroBulto) {
    }

    public static String dkprCodigoBulto(DkprEnvio dkprEnvio, int dkprNroBulto) {
        return dkprEnvio.dkprGetCodigoSeguimiento() + "-" + String.format("%02d", dkprNroBulto);
    }

    public static DkprCodigoBulto dkprLeer(String dkprTexto) {
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
        return new DkprCodigoBulto(dkprIdEnvio, dkprM.group(2) == null ? null : Integer.parseInt(dkprM.group(2)));
    }

    public static String dkprNormalizarEnvio(String dkprTexto) {
        if (dkprTexto == null || dkprTexto.isBlank()) {
            return "";
        }
        DkprCodigoBulto dkprC = dkprLeer(dkprTexto);
        return dkprC == null ? dkprTexto.trim().toUpperCase(Locale.ROOT) : String.format("ENV-%06d", dkprC.dkprIdEnvio());
    }

    private static final int ZONA_SILENCIO = 10;

    static boolean[] dkprModulosBarras(String dkprTexto) {
        boolean[] dkprCodigo = new Code128Writer().encode(dkprTexto);
        boolean[] dkprModulos = new boolean[dkprCodigo.length + 2 * ZONA_SILENCIO];
        System.arraycopy(dkprCodigo, 0, dkprModulos, ZONA_SILENCIO, dkprCodigo.length);
        return dkprModulos;
    }

    static BitMatrix dkprModulosQR(String dkprTexto) {
        try {
            return new QRCodeWriter().encode(dkprTexto, BarcodeFormat.QR_CODE, 0, 0,
                    Map.of(EncodeHintType.MARGIN, 0, EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M));
        } catch (WriterException dkprE) {
            throw new IllegalArgumentException("No se pudo generar el QR de " + dkprTexto, dkprE);
        }
    }

    public static Group dkprBarras(String dkprTexto, double dkprAncho, double dkprAlto) {
        boolean[] dkprModulos = dkprModulosBarras(dkprTexto);
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

    public static Group dkprQr(String dkprTexto, double dkprLado) {
        BitMatrix dkprM = dkprModulosQR(dkprTexto);
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

    public static BufferedImage dkprImagenBarras(String dkprTexto, int dkprEscala, int dkprAlto) {
        boolean[] dkprModulos = dkprModulosBarras(dkprTexto);
        BufferedImage dkprImg = new BufferedImage(dkprModulos.length * dkprEscala, dkprAlto, BufferedImage.TYPE_BYTE_BINARY);
        for (int dkprX = 0; dkprX < dkprImg.getWidth(); dkprX++) {
            int dkprColor = dkprModulos[dkprX / dkprEscala] ? 0x000000 : 0xFFFFFF;
            for (int dkprY = 0; dkprY < dkprAlto; dkprY++) {
                dkprImg.setRGB(dkprX, dkprY, dkprColor);
            }
        }
        return dkprImg;
    }

    public static BufferedImage dkprImagenQR(String dkprTexto, int dkprEscala) {
        BitMatrix dkprM = dkprModulosQR(dkprTexto);
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
