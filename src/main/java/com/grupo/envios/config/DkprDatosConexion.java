package com.grupo.envios.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record DkprDatosConexion(String dkprServidor, int dkprPuerto, String dkprBase, String dkprUsuario, String dkprClave) {

    private static final Pattern URL = Pattern.compile("^jdbc:postgresql://([^:/]+)(?::(\\d+))?/([^?]+).*$");

    public String dkprUrl() {
        return dkprUrl(dkprBase);
    }

    public String dkprUrl(String dkprBase) {
        return "jdbc:postgresql://" + dkprServidor + ":" + dkprPuerto + "/" + dkprBase;
    }

    public static DkprDatosConexion dkprDesdeUrl(String dkprUrl, String dkprUsuario, String dkprClave) {
        Matcher dkprM = URL.matcher(dkprUrl == null ? "" : dkprUrl.trim());
        if (!dkprM.matches()) {
            return new DkprDatosConexion("localhost", 5432, "sistema_envios", dkprUsuario, dkprClave);
        }
        int dkprPuerto = dkprM.group(2) == null ? 5432 : Integer.parseInt(dkprM.group(2));
        return new DkprDatosConexion(dkprM.group(1), dkprPuerto, dkprM.group(3), dkprUsuario, dkprClave);
    }
}
