package com.grupo.envios.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comprobante {

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

    public int getIdEnvio() { return dkprIdEnvio; }
    public void setIdEnvio(int dkprIdEnvio) { this.dkprIdEnvio = dkprIdEnvio; }

    public String getTipo() { return dkprTipo; }
    public void setTipo(String dkprTipo) { this.dkprTipo = dkprTipo; }

    public String getSerie() { return dkprSerie; }
    public void setSerie(String dkprSerie) { this.dkprSerie = dkprSerie; }

    public int getNumero() { return dkprNumero; }
    public void setNumero(int dkprNumero) { this.dkprNumero = dkprNumero; }

    public LocalDateTime getFechaEmision() { return dkprFechaEmision; }
    public void setFechaEmision(LocalDateTime dkprFechaEmision) { this.dkprFechaEmision = dkprFechaEmision; }

    public BigDecimal getSubtotal() { return dkprSubtotal; }
    public void setSubtotal(BigDecimal dkprSubtotal) { this.dkprSubtotal = dkprSubtotal; }

    public BigDecimal getIgv() { return dkprIgv; }
    public void setIgv(BigDecimal dkprIgv) { this.dkprIgv = dkprIgv; }

    public BigDecimal getTotal() { return dkprTotal; }
    public void setTotal(BigDecimal dkprTotal) { this.dkprTotal = dkprTotal; }

    public String getMetodoPago() { return dkprMetodoPago; }
    public void setMetodoPago(String dkprMetodoPago) { this.dkprMetodoPago = dkprMetodoPago; }

    /** Ejemplo: B001-00000012 */
    public String getNumeroTexto() {
        return dkprSerie + "-" + String.format("%08d", dkprNumero);
    }

    public String getFechaTexto() {
        return dkprFechaEmision == null ? "" : dkprFechaEmision.format(FORMATO);
    }
}
