package com.grupo.envios.model;

import java.time.LocalDateTime;

public class DkprControlCarga {

    public static final String DESPACHO = "DESPACHO";
    public static final String RECEPCION = "RECEPCION";

    private int dkprIdControl;
    private String dkprTipo;
    private int dkprIdAgencia;
    private int dkprIdRuta;
    private Integer dkprIdManifiesto;
    private String dkprManifiesto;
    private String dkprOrigen;
    private String dkprDestino;
    private String dkprEmpleado;
    private LocalDateTime dkprFechaInicio;
    private String dkprEstado;

    public int dkprGetIdControl() { return dkprIdControl; }
    public void dkprSetIdControl(int dkprIdControl) { this.dkprIdControl = dkprIdControl; }

    public String dkprGetTipo() { return dkprTipo; }
    public void dkprSetTipo(String dkprTipo) { this.dkprTipo = dkprTipo; }

    public int dkprGetIdAgencia() { return dkprIdAgencia; }
    public void dkprSetIdAgencia(int dkprIdAgencia) { this.dkprIdAgencia = dkprIdAgencia; }

    public int dkprGetIdRuta() { return dkprIdRuta; }
    public void dkprSetIdRuta(int dkprIdRuta) { this.dkprIdRuta = dkprIdRuta; }

    public Integer dkprGetIdManifiesto() { return dkprIdManifiesto; }
    public void dkprSetIdManifiesto(Integer dkprIdManifiesto) { this.dkprIdManifiesto = dkprIdManifiesto; }

    public String dkprGetManifiesto() { return dkprManifiesto; }
    public void dkprSetManifiesto(String dkprManifiesto) { this.dkprManifiesto = dkprManifiesto; }

    public String dkprGetOrigen() { return dkprOrigen; }
    public void dkprSetOrigen(String dkprOrigen) { this.dkprOrigen = dkprOrigen; }

    public String dkprGetDestino() { return dkprDestino; }
    public void dkprSetDestino(String dkprDestino) { this.dkprDestino = dkprDestino; }

    public String dkprGetEmpleado() { return dkprEmpleado; }
    public void dkprSetEmpleado(String dkprEmpleado) { this.dkprEmpleado = dkprEmpleado; }

    public LocalDateTime dkprGetFechaInicio() { return dkprFechaInicio; }
    public void dkprSetFechaInicio(LocalDateTime dkprFechaInicio) { this.dkprFechaInicio = dkprFechaInicio; }

    public String dkprGetEstado() { return dkprEstado; }
    public void dkprSetEstado(String dkprEstado) { this.dkprEstado = dkprEstado; }

    public boolean dkprEsDespacho() {
        return DESPACHO.equals(dkprTipo);
    }
}
