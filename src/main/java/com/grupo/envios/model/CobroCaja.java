package com.grupo.envios.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Un cobro hecho en caja: el comprobante emitido al registrar un envío. */
public class CobroCaja {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private String dkprComprobante;
    private String dkprCodigoEnvio;
    private LocalDateTime dkprFecha;
    private String dkprMetodoPago;
    private BigDecimal dkprTotal;
    private boolean dkprAnulado;

    public CobroCaja(String dkprComprobante, String dkprCodigoEnvio, LocalDateTime dkprFecha, String dkprMetodoPago,
                     BigDecimal dkprTotal, boolean dkprAnulado) {
        this.dkprComprobante = dkprComprobante;
        this.dkprCodigoEnvio = dkprCodigoEnvio;
        this.dkprFecha = dkprFecha;
        this.dkprMetodoPago = dkprMetodoPago;
        this.dkprTotal = dkprTotal;
        this.dkprAnulado = dkprAnulado;
    }

    public String getComprobante() { return dkprComprobante; }
    public String getCodigoEnvio() { return dkprCodigoEnvio; }
    public LocalDateTime getFecha() { return dkprFecha; }
    public String getMetodoPago() { return dkprMetodoPago; }
    public BigDecimal getTotal() { return dkprTotal; }
    public boolean isAnulado() { return dkprAnulado; }

    public String getFechaTexto() {
        return dkprFecha == null ? "" : dkprFecha.format(FORMATO);
    }

    public String getEstadoTexto() {
        return dkprAnulado ? "ANULADO (no se cuenta)" : "Cobrado";
    }
}
