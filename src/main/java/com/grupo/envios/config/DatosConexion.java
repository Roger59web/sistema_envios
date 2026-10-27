package com.grupo.envios.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record DatosConexion(String servidor, int puerto, String base, String usuario, String clave) {

    private static final Pattern URL = Pattern.compile("^jdbc:postgresql://([^:/]+)(?::(\\d+))?/([^?]+).*$");

    public String url() {
        return url(base);
    }

    public String url(String dkprBase) {
        return "jdbc:postgresql://" + servidor + ":" + puerto + "/" + dkprBase;
    }

    public static DatosConexion desdeUrl(String dkprUrl, String dkprUsuario, String dkprClave) {
        Matcher dkprM = URL.matcher(dkprUrl == null ? "" : dkprUrl.trim());
        if (!dkprM.matches()) {
            return new DatosConexion("localhost", 5432, "sistema_envios", dkprUsuario, dkprClave);
        }
        int dkprPuerto = dkprM.group(2) == null ? 5432 : Integer.parseInt(dkprM.group(2));
        return new DatosConexion(dkprM.group(1), dkprPuerto, dkprM.group(3), dkprUsuario, dkprClave);
    }
}
