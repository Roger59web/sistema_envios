package com.grupo.envios.model;

import java.math.BigDecimal;

/** Fila de un resumen de reportes (por agencia o por estado). */
public class ResumenFila {

    private final String etiqueta;
    private final int cantidad;
    private final BigDecimal monto;

    public ResumenFila(String etiqueta, int cantidad, BigDecimal monto) {
        this.etiqueta = etiqueta;
        this.cantidad = cantidad;
        this.monto = monto;
    }

    public String getEtiqueta() { return etiqueta; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getMonto() { return monto; }
}
