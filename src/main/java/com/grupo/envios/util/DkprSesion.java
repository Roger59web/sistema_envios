package com.grupo.envios.util;

import com.grupo.envios.model.DkprEmpleado;

public final class DkprSesion {

    private static DkprEmpleado dkprActual;

    private DkprSesion() {
    }

    public static DkprEmpleado dkprGetActual() {
        return dkprActual;
    }

    public static void dkprIniciar(DkprEmpleado dkprEmpleado) {
        dkprActual = dkprEmpleado;
    }

    public static void dkprCerrar() {
        dkprActual = null;
    }
}
