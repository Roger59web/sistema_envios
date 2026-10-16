package com.grupo.envios.util;

import com.grupo.envios.model.Empleado;

/** Guarda el empleado que inició sesión mientras la aplicación está abierta. */
public final class Sesion {

    private static Empleado actual;

    private Sesion() {
    }

    public static Empleado getActual() {
        return actual;
    }

    public static void iniciar(Empleado empleado) {
        actual = empleado;
    }

    public static void cerrar() {
        actual = null;
    }
}
