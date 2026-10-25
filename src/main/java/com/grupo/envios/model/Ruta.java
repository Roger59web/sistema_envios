package com.grupo.envios.model;

import java.math.BigDecimal;


public class Ruta {

    private int dkprIdRuta;
    private int dkprIdAgenciaOrigen;
    private int dkprIdAgenciaDestino;
    private String dkprOrigen;
    private String dkprDestino;
    private int dkprDiasEstimados = 1;
    private boolean dkprActivo = true;

    private int dkprIdTarifa;
    private BigDecimal dkprPrecioBase;
    private BigDecimal dkprPrecioKgExtra;
    private BigDecimal dkprPesoMaxKg;

    public int getIdRuta() { return dkprIdRuta; }
    public void setIdRuta(int dkprIdRuta) { this.dkprIdRuta = dkprIdRuta; }

    public int getIdAgenciaOrigen() { return dkprIdAgenciaOrigen; }
    public void setIdAgenciaOrigen(int dkprIdAgenciaOrigen) { this.dkprIdAgenciaOrigen = dkprIdAgenciaOrigen; }

    public int getIdAgenciaDestino() { return dkprIdAgenciaDestino; }
    public void setIdAgenciaDestino(int dkprIdAgenciaDestino) { this.dkprIdAgenciaDestino = dkprIdAgenciaDestino; }

    public String getOrigen() { return dkprOrigen; }
    public void setOrigen(String dkprOrigen) { this.dkprOrigen = dkprOrigen; }

    public String getDestino() { return dkprDestino; }
    public void setDestino(String dkprDestino) { this.dkprDestino = dkprDestino; }

    public int getDiasEstimados() { return dkprDiasEstimados; }
    public void setDiasEstimados(int dkprDiasEstimados) { this.dkprDiasEstimados = dkprDiasEstimados; }

    public boolean isActivo() { return dkprActivo; }
    public void setActivo(boolean dkprActivo) { this.dkprActivo = dkprActivo; }

    public int getIdTarifa() { return dkprIdTarifa; }
    public void setIdTarifa(int dkprIdTarifa) { this.dkprIdTarifa = dkprIdTarifa; }

    public BigDecimal getPrecioBase() { return dkprPrecioBase; }
    public void setPrecioBase(BigDecimal dkprPrecioBase) { this.dkprPrecioBase = dkprPrecioBase; }

    public BigDecimal getPrecioKgExtra() { return dkprPrecioKgExtra; }
    public void setPrecioKgExtra(BigDecimal dkprPrecioKgExtra) { this.dkprPrecioKgExtra = dkprPrecioKgExtra; }

    public BigDecimal getPesoMaxKg() { return dkprPesoMaxKg; }
    public void setPesoMaxKg(BigDecimal dkprPesoMaxKg) { this.dkprPesoMaxKg = dkprPesoMaxKg; }

    public String getEstadoTexto() {
        return dkprActivo ? "Activa" : "Inactiva";
    }

    @Override
    public String toString() {
        return dkprOrigen + " → " + dkprDestino + " (" + dkprDiasEstimados + (dkprDiasEstimados == 1 ? " día)" : " días)");
    }
}
