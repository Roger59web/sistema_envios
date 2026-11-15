package com.grupo.envios.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DkprHistorialEnvio {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private LocalDateTime dkprFecha;
    private String dkprEstado;
    private String dkprAgencia;
    private String dkprEmpleado;
    private String dkprObservacion;

    public LocalDateTime dkprGetFecha() { return dkprFecha; }
    public void dkprSetFecha(LocalDateTime dkprFecha) { this.dkprFecha = dkprFecha; }

    public String dkprGetEstado() { return dkprEstado; }
    public void dkprSetEstado(String dkprEstado) { this.dkprEstado = dkprEstado; }

    public String dkprGetAgencia() { return dkprAgencia; }
    public void dkprSetAgencia(String dkprAgencia) { this.dkprAgencia = dkprAgencia; }

    public String dkprGetEmpleado() { return dkprEmpleado; }
    public void dkprSetEmpleado(String dkprEmpleado) { this.dkprEmpleado = dkprEmpleado; }

    public String dkprGetObservacion() { return dkprObservacion; }
    public void dkprSetObservacion(String dkprObservacion) { this.dkprObservacion = dkprObservacion; }

    public String dkprGetFechaTexto() {
        return dkprFecha == null ? "" : dkprFecha.format(FORMATO);
    }
}
