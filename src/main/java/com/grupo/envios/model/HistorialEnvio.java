package com.grupo.envios.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistorialEnvio {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private LocalDateTime fecha;
    private String estado;
    private String agencia;
    private String empleado;
    private String observacion;

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }

    public String getEmpleado() { return empleado; }
    public void setEmpleado(String empleado) { this.empleado = empleado; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATO);
    }
}
