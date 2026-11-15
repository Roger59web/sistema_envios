package com.grupo.envios.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DkprManifiesto {

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
    private int dkprPendientes;

    public int dkprGetIdManifiesto() { return dkprIdManifiesto; }
    public void dkprSetIdManifiesto(int dkprIdManifiesto) { this.dkprIdManifiesto = dkprIdManifiesto; }

    public String dkprGetCodigo() { return dkprCodigo; }
    public void dkprSetCodigo(String dkprCodigo) { this.dkprCodigo = dkprCodigo; }

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

    public LocalDateTime dkprGetFechaSalida() { return dkprFechaSalida; }
    public void dkprSetFechaSalida(LocalDateTime dkprFechaSalida) { this.dkprFechaSalida = dkprFechaSalida; }

    public String dkprGetPlaca() { return dkprPlaca; }
    public void dkprSetPlaca(String dkprPlaca) { this.dkprPlaca = dkprPlaca; }

    public String dkprGetConductor() { return dkprConductor; }
    public void dkprSetConductor(String dkprConductor) { this.dkprConductor = dkprConductor; }

    public String dkprGetObservacion() { return dkprObservacion; }
    public void dkprSetObservacion(String dkprObservacion) { this.dkprObservacion = dkprObservacion; }

    public String dkprGetEmpleado() { return dkprEmpleado; }
    public void dkprSetEmpleado(String dkprEmpleado) { this.dkprEmpleado = dkprEmpleado; }

    public int dkprGetPendientes() { return dkprPendientes; }
    public void dkprSetPendientes(int dkprPendientes) { this.dkprPendientes = dkprPendientes; }

    public String dkprGetFechaSalidaTexto() {
        return dkprFechaSalida == null ? "" : dkprFechaSalida.format(FORMATO);
    }

    @Override
    public String toString() {
        return dkprCodigo + "  ·  " + (dkprPlaca == null ? "sin placa" : dkprPlaca)
                + (dkprConductor == null ? "" : "  ·  " + dkprConductor)
                + "  ·  desde " + dkprOrigen + ", salió " + dkprGetFechaSalidaTexto()
                + "  ·  " + dkprPendientes + " envío(s) en camino";
    }
}
