package com.grupo.envios.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LectorDni {

    private static final Pattern MRZ = Pattern.compile("PER(\\d{8})");
    private static final Pattern GRUPO_DIGITOS = Pattern.compile("\\d+");

    private LectorDni() {
    }

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

    public static boolean pareceEscaneado(String dkprTexto) {
        if (dkprTexto == null) {
            return false;
        }
        String dkprT = dkprTexto.trim();
        return dkprT.length() > 12 || dkprT.contains("<") || (!dkprT.matches("[A-Za-z0-9]*"));
    }
}
