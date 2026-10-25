package com.grupo.envios.util;

/**
 * Error de regla de negocio o de validación (dato incorrecto, permiso insuficiente...).
 * Es una excepción no verificada: no hace falta declararla en cada método.
 * Su mensaje se muestra tal cual al usuario (ver {@link Alertas#manejar(Throwable)}).
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String dkprMensaje) {
        super(dkprMensaje);
    }
}
