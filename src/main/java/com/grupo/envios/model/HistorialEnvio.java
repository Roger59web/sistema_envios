package com.grupo.envios.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistorialEnvio {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private LocalDateTime dkprFecha;
    private String dkprEstado;
    private String dkprAgencia;
    private String dkprEmpleado;
    private String dkprObservacion;

    public LocalDateTime getFecha() { return dkprFecha; }
    public void setFecha(LocalDateTime dkprFecha) { this.dkprFecha = dkprFecha; }

    public String getEstado() { return dkprEstado; }
    public void setEstado(String dkprEstado) { this.dkprEstado = dkprEstado; }

    public String getAgencia() { return dkprAgencia; }
    public void setAgencia(String dkprAgencia) { this.dkprAgencia = dkprAgencia; }

    public String getEmpleado() { return dkprEmpleado; }
    public void setEmpleado(String dkprEmpleado) { this.dkprEmpleado = dkprEmpleado; }

    public String getObservacion() { return dkprObservacion; }
    public void setObservacion(String dkprObservacion) { this.dkprObservacion = dkprObservacion; }

    public String getFechaTexto() {
        return dkprFecha == null ? "" : dkprFecha.format(FORMATO);
    }
}
