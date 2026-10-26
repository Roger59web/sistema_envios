package com.grupo.envios.model;

import java.math.BigDecimal;

public class ResumenFila {

    private final String dkprEtiqueta;
    private final int dkprCantidad;
    private final BigDecimal dkprMonto;

    public ResumenFila(String dkprEtiqueta, int dkprCantidad, BigDecimal dkprMonto) {
        this.dkprEtiqueta = dkprEtiqueta;
        this.dkprCantidad = dkprCantidad;
        this.dkprMonto = dkprMonto;
    }

    public String getEtiqueta() { return dkprEtiqueta; }
    public int getCantidad() { return dkprCantidad; }
    public BigDecimal getMonto() { return dkprMonto; }
}
