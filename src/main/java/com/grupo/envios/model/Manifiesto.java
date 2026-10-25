package com.grupo.envios.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Manifiesto de carga: lo que lleva un vehículo hacia una agencia destino. */
public class Manifiesto {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int dkprIdManifiesto;
    private String dkprCodigo;
    private int dkprIdRuta;
    private int dkprIdAgenciaOrigen;
    private int dkprIdAgenciaDestino;
    private String dkprOrigen;
    private String dkprDestino;
    private LocalDateTime dkprFechaSalida;
    private String dkprPlaca;
    private String dkprConductor;
    private String dkprObservacion;
    private String dkprEmpleado;
    /** Envíos del manifiesto que siguen EN TRANSITO (solo se llena al listar los que vienen en camino). */
    private int dkprPendientes;

    public int getIdManifiesto() { return dkprIdManifiesto; }
    public void setIdManifiesto(int dkprIdManifiesto) { this.dkprIdManifiesto = dkprIdManifiesto; }

    public String getCodigo() { return dkprCodigo; }
    public void setCodigo(String dkprCodigo) { this.dkprCodigo = dkprCodigo; }

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

    public LocalDateTime getFechaSalida() { return dkprFechaSalida; }
    public void setFechaSalida(LocalDateTime dkprFechaSalida) { this.dkprFechaSalida = dkprFechaSalida; }

    public String getPlaca() { return dkprPlaca; }
    public void setPlaca(String dkprPlaca) { this.dkprPlaca = dkprPlaca; }

    public String getConductor() { return dkprConductor; }
    public void setConductor(String dkprConductor) { this.dkprConductor = dkprConductor; }

    public String getObservacion() { return dkprObservacion; }
    public void setObservacion(String dkprObservacion) { this.dkprObservacion = dkprObservacion; }

    public String getEmpleado() { return dkprEmpleado; }
    public void setEmpleado(String dkprEmpleado) { this.dkprEmpleado = dkprEmpleado; }

    public int getPendientes() { return dkprPendientes; }
    public void setPendientes(int dkprPendientes) { this.dkprPendientes = dkprPendientes; }

    public String getFechaSalidaTexto() {
        return dkprFechaSalida == null ? "" : dkprFechaSalida.format(FORMATO);
    }

    @Override
    public String toString() {
        return dkprCodigo + "  ·  " + (dkprPlaca == null ? "sin placa" : dkprPlaca)
                + (dkprConductor == null ? "" : "  ·  " + dkprConductor)
                + "  ·  desde " + dkprOrigen + ", salió " + getFechaSalidaTexto()
                + "  ·  " + dkprPendientes + " envío(s) en camino";
    }
}
