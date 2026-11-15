package com.grupo.envios.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DkprComprobante {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int dkprIdEnvio;
    private String dkprTipo;
    private String dkprSerie;
    private int dkprNumero;
    private LocalDateTime dkprFechaEmision;
    private BigDecimal dkprSubtotal;
    private BigDecimal dkprIgv;
    private BigDecimal dkprTotal;
    private String dkprMetodoPago;

    public int dkprGetIdEnvio() { return dkprIdEnvio; }
    public void dkprSetIdEnvio(int dkprIdEnvio) { this.dkprIdEnvio = dkprIdEnvio; }

    public String dkprGetTipo() { return dkprTipo; }
    public void dkprSetTipo(String dkprTipo) { this.dkprTipo = dkprTipo; }

    public String dkprGetSerie() { return dkprSerie; }
    public void dkprSetSerie(String dkprSerie) { this.dkprSerie = dkprSerie; }

    public int dkprGetNumero() { return dkprNumero; }
    public void dkprSetNumero(int dkprNumero) { this.dkprNumero = dkprNumero; }

    public LocalDateTime dkprGetFechaEmision() { return dkprFechaEmision; }
    public void dkprSetFechaEmision(LocalDateTime dkprFechaEmision) { this.dkprFechaEmision = dkprFechaEmision; }

    public BigDecimal dkprGetSubtotal() { return dkprSubtotal; }
    public void dkprSetSubtotal(BigDecimal dkprSubtotal) { this.dkprSubtotal = dkprSubtotal; }

    public BigDecimal dkprGetIgv() { return dkprIgv; }
    public void dkprSetIgv(BigDecimal dkprIgv) { this.dkprIgv = dkprIgv; }

    public BigDecimal dkprGetTotal() { return dkprTotal; }
    public void dkprSetTotal(BigDecimal dkprTotal) { this.dkprTotal = dkprTotal; }

    public String dkprGetMetodoPago() { return dkprMetodoPago; }
    public void dkprSetMetodoPago(String dkprMetodoPago) { this.dkprMetodoPago = dkprMetodoPago; }

    public String dkprGetNumeroTexto() {
        return dkprSerie + "-" + String.format("%08d", dkprNumero);
    }

    public String dkprGetFechaTexto() {
        return dkprFechaEmision == null ? "" : dkprFechaEmision.format(FORMATO);
    }
}
