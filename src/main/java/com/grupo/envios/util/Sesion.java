package com.grupo.envios.util;

import com.grupo.envios.model.Empleado;

public final class Sesion {

    private static Empleado dkprActual;

    private Sesion() {
    }

    public static Empleado getActual() {
        return dkprActual;
    }

    public static void iniciar(Empleado dkprEmpleado) {
        dkprActual = dkprEmpleado;
    }

    public static void cerrar() {
        dkprActual = null;
    }
}
