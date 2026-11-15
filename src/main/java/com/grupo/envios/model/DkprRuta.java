package com.grupo.envios.model;

import java.math.BigDecimal;


public class DkprRuta {

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

    public int dkprGetIdRuta() { return dkprIdRuta; }
    public void dkprSetIdRuta(int dkprIdRuta) { this.dkprIdRuta = dkprIdRuta; }

    public int dkprGetIdAgenciaOrigen() { return dkprIdAgenciaOrigen; }
    public void dkprSetIdAgenciaOrigen(int dkprIdAgenciaOrigen) { this.dkprIdAgenciaOrigen = dkprIdAgenciaOrigen; }

    public int dkprGetIdAgenciaDestino() { return dkprIdAgenciaDestino; }
    public void dkprSetIdAgenciaDestino(int dkprIdAgenciaDestino) { this.dkprIdAgenciaDestino = dkprIdAgenciaDestino; }

    public String dkprGetOrigen() { return dkprOrigen; }
    public void dkprSetOrigen(String dkprOrigen) { this.dkprOrigen = dkprOrigen; }

    public String dkprGetDestino() { return dkprDestino; }
    public void dkprSetDestino(String dkprDestino) { this.dkprDestino = dkprDestino; }

    public int dkprGetDiasEstimados() { return dkprDiasEstimados; }
    public void dkprSetDiasEstimados(int dkprDiasEstimados) { this.dkprDiasEstimados = dkprDiasEstimados; }

    public boolean dkprIsActivo() { return dkprActivo; }
    public void dkprSetActivo(boolean dkprActivo) { this.dkprActivo = dkprActivo; }

    public int dkprGetIdTarifa() { return dkprIdTarifa; }
    public void dkprSetIdTarifa(int dkprIdTarifa) { this.dkprIdTarifa = dkprIdTarifa; }

    public BigDecimal dkprGetPrecioBase() { return dkprPrecioBase; }
    public void dkprSetPrecioBase(BigDecimal dkprPrecioBase) { this.dkprPrecioBase = dkprPrecioBase; }

    public BigDecimal dkprGetPrecioKgExtra() { return dkprPrecioKgExtra; }
    public void dkprSetPrecioKgExtra(BigDecimal dkprPrecioKgExtra) { this.dkprPrecioKgExtra = dkprPrecioKgExtra; }

    public BigDecimal dkprGetPesoMaxKg() { return dkprPesoMaxKg; }
    public void dkprSetPesoMaxKg(BigDecimal dkprPesoMaxKg) { this.dkprPesoMaxKg = dkprPesoMaxKg; }

    public String dkprGetEstadoTexto() {
        return dkprActivo ? "Activa" : "Inactiva";
    }

    @Override
    public String toString() {
        return dkprOrigen + " → " + dkprDestino + " (" + dkprDiasEstimados + (dkprDiasEstimados == 1 ? " día)" : " días)");
    }
}
