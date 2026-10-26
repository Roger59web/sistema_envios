package com.grupo.envios.model;

import java.time.LocalDateTime;

public class ControlCarga {

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

    public int getIdControl() { return dkprIdControl; }
    public void setIdControl(int dkprIdControl) { this.dkprIdControl = dkprIdControl; }

    public String getTipo() { return dkprTipo; }
    public void setTipo(String dkprTipo) { this.dkprTipo = dkprTipo; }

    public int getIdAgencia() { return dkprIdAgencia; }
    public void setIdAgencia(int dkprIdAgencia) { this.dkprIdAgencia = dkprIdAgencia; }

    public int getIdRuta() { return dkprIdRuta; }
    public void setIdRuta(int dkprIdRuta) { this.dkprIdRuta = dkprIdRuta; }

    public Integer getIdManifiesto() { return dkprIdManifiesto; }
    public void setIdManifiesto(Integer dkprIdManifiesto) { this.dkprIdManifiesto = dkprIdManifiesto; }

    public String getManifiesto() { return dkprManifiesto; }
    public void setManifiesto(String dkprManifiesto) { this.dkprManifiesto = dkprManifiesto; }

    public String getOrigen() { return dkprOrigen; }
    public void setOrigen(String dkprOrigen) { this.dkprOrigen = dkprOrigen; }

    public String getDestino() { return dkprDestino; }
    public void setDestino(String dkprDestino) { this.dkprDestino = dkprDestino; }

    public String getEmpleado() { return dkprEmpleado; }
    public void setEmpleado(String dkprEmpleado) { this.dkprEmpleado = dkprEmpleado; }

    public LocalDateTime getFechaInicio() { return dkprFechaInicio; }
    public void setFechaInicio(LocalDateTime dkprFechaInicio) { this.dkprFechaInicio = dkprFechaInicio; }

    public String getEstado() { return dkprEstado; }
    public void setEstado(String dkprEstado) { this.dkprEstado = dkprEstado; }

    public boolean esDespacho() {
        return DESPACHO.equals(dkprTipo);
    }
}
