package com.grupo.envios.model;

import java.math.BigDecimal;

/** Una ruta (origen → destino) junto con su tarifa vigente. */
public class Ruta {

    private int idRuta;
    private int idAgenciaOrigen;
    private int idAgenciaDestino;
    private String origen;
    private String destino;
    private int diasEstimados = 1;
    private boolean activo = true;

    private int idTarifa;
    private BigDecimal precioBase;
    private BigDecimal precioKgExtra;
    private BigDecimal pesoMaxKg;

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public int getIdAgenciaOrigen() { return idAgenciaOrigen; }
    public void setIdAgenciaOrigen(int idAgenciaOrigen) { this.idAgenciaOrigen = idAgenciaOrigen; }

    public int getIdAgenciaDestino() { return idAgenciaDestino; }
    public void setIdAgenciaDestino(int idAgenciaDestino) { this.idAgenciaDestino = idAgenciaDestino; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public int getDiasEstimados() { return diasEstimados; }
    public void setDiasEstimados(int diasEstimados) { this.diasEstimados = diasEstimados; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getIdTarifa() { return idTarifa; }
    public void setIdTarifa(int idTarifa) { this.idTarifa = idTarifa; }

    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }

    public BigDecimal getPrecioKgExtra() { return precioKgExtra; }
    public void setPrecioKgExtra(BigDecimal precioKgExtra) { this.precioKgExtra = precioKgExtra; }

    public BigDecimal getPesoMaxKg() { return pesoMaxKg; }
    public void setPesoMaxKg(BigDecimal pesoMaxKg) { this.pesoMaxKg = pesoMaxKg; }

    public String getEstadoTexto() {
        return activo ? "Activa" : "Inactiva";
    }

    @Override
    public String toString() {
        return origen + " → " + destino + " (" + diasEstimados + (diasEstimados == 1 ? " día)" : " días)");
    }
}
