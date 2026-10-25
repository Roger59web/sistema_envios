package com.grupo.envios.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Saca el número de DNI de lo que escribe un lector de código de barras al escanear el documento.
 * Cada modelo de DNI y de lector entrega un texto distinto, así que se prueban varios formatos:
 * <ul>
 *   <li>8 dígitos solos: {@code 70000001}</li>
 *   <li>zona MRZ del DNI electrónico: {@code I<PER70000001<4<<<<...}</li>
 *   <li>texto largo con datos separados (PDF417), donde el DNI es el único grupo de 8 dígitos
 *       o el que va primero</li>
 * </ul>
 */
public final class LectorDni {

    private static final Pattern MRZ = Pattern.compile("PER(\\d{8})");
    private static final Pattern GRUPO_DIGITOS = Pattern.compile("\\d+");

    private LectorDni() {
    }

    /** Devuelve el DNI (8 dígitos) o null si en el texto no se reconoce ninguno. */
    public static String extraer(String dkprTexto) {
        if (dkprTexto == null) {
            return null;
        }
        String dkprT = dkprTexto.trim().toUpperCase(Locale.ROOT);
        if (dkprT.matches("\\d{8}")) {
            return dkprT;
        }
        Matcher dkprMrz = MRZ.matcher(dkprT);
        if (dkprMrz.find()) {
            return dkprMrz.group(1);
        }
        List<String> dkprGrupos = new ArrayList<>();
        Matcher dkprM = GRUPO_DIGITOS.matcher(dkprT);
        while (dkprM.find()) {
            if (dkprM.group().length() == 8) {
                dkprGrupos.add(dkprM.group());
            }
        }
        return dkprGrupos.isEmpty() ? null : dkprGrupos.get(0);
    }

    /**
     * true si el texto parece venir del lector y no tipeado a mano: trae letras o separadores,
     * o es más largo que cualquier documento. Así no se toca un RUC o CE escrito en el campo.
     */
    public static boolean pareceEscaneado(String dkprTexto) {
        if (dkprTexto == null) {
            return false;
        }
        String dkprT = dkprTexto.trim();
        return dkprT.length() > 12 || dkprT.contains("<") || (!dkprT.matches("[A-Za-z0-9]*"));
    }
}
