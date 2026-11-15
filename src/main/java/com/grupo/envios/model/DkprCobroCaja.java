package com.grupo.envios.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DkprCobroCaja {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private String dkprComprobante;
    private String dkprCodigoEnvio;
    private LocalDateTime dkprFecha;
    private String dkprMetodoPago;
    private BigDecimal dkprTotal;
    private boolean dkprAnulado;

    public DkprCobroCaja(String dkprComprobante, String dkprCodigoEnvio, LocalDateTime dkprFecha, String dkprMetodoPago,
                     BigDecimal dkprTotal, boolean dkprAnulado) {
        this.dkprComprobante = dkprComprobante;
        this.dkprCodigoEnvio = dkprCodigoEnvio;
        this.dkprFecha = dkprFecha;
        this.dkprMetodoPago = dkprMetodoPago;
        this.dkprTotal = dkprTotal;
        this.dkprAnulado = dkprAnulado;
    }

    public String dkprGetComprobante() { return dkprComprobante; }
    public String dkprGetCodigoEnvio() { return dkprCodigoEnvio; }
    public LocalDateTime dkprGetFecha() { return dkprFecha; }
    public String dkprGetMetodoPago() { return dkprMetodoPago; }
    public BigDecimal dkprGetTotal() { return dkprTotal; }
    public boolean dkprIsAnulado() { return dkprAnulado; }

    public String dkprGetFechaTexto() {
        return dkprFecha == null ? "" : dkprFecha.format(FORMATO);
    }

    public String dkprGetEstadoTexto() {
        return dkprAnulado ? "ANULADO (no se cuenta)" : "Cobrado";
    }
}
