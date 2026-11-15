package com.grupo.envios.model;

import java.math.BigDecimal;

public class DkprResumenFila {

    private final String dkprEtiqueta;
    private final int dkprCantidad;
    private final BigDecimal dkprMonto;

    public DkprResumenFila(String dkprEtiqueta, int dkprCantidad, BigDecimal dkprMonto) {
        this.dkprEtiqueta = dkprEtiqueta;
        this.dkprCantidad = dkprCantidad;
        this.dkprMonto = dkprMonto;
    }

    public String dkprGetEtiqueta() { return dkprEtiqueta; }
    public int dkprGetCantidad() { return dkprCantidad; }
    public BigDecimal dkprGetMonto() { return dkprMonto; }
}
