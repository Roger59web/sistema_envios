package com.grupo.envios.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Envio {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int idEnvio;
    private String codigoSeguimiento;
    private int idRuta;
    private int idAgenciaOrigen;
    private int idAgenciaDestino;
    private String origen;
    private String destino;
    private String origenDireccion;
    private String origenTelefono;
    private int idRemitente;
    private String remitente;
    private String remitenteDocumento;
    private String remitenteTelefono;
    private int idDestinatario;
    private String destinatario;
    private String destinatarioDocumento;
    private String destinatarioTelefono;
    private int idEmpleadoRegistro;
    private String empleadoRegistro;
    private int idEstadoActual;
    private String estado;
    private LocalDateTime fechaRegistro;
    private String descripcion;
    private int cantidadBultos = 1;
    private BigDecimal pesoKg;
    private BigDecimal valorDeclarado = BigDecimal.ZERO;
    private BigDecimal costoEnvio;
    private String observaciones;

    public int getIdEnvio() { return idEnvio; }
    public void setIdEnvio(int idEnvio) { this.idEnvio = idEnvio; }

    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }

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

    public String getOrigenDireccion() { return origenDireccion; }
    public void setOrigenDireccion(String origenDireccion) { this.origenDireccion = origenDireccion; }

    public String getOrigenTelefono() { return origenTelefono; }
    public void setOrigenTelefono(String origenTelefono) { this.origenTelefono = origenTelefono; }

    public int getIdRemitente() { return idRemitente; }
    public void setIdRemitente(int idRemitente) { this.idRemitente = idRemitente; }

    public String getRemitente() { return remitente; }
    public void setRemitente(String remitente) { this.remitente = remitente; }

    public String getRemitenteDocumento() { return remitenteDocumento; }
    public void setRemitenteDocumento(String remitenteDocumento) { this.remitenteDocumento = remitenteDocumento; }

    public String getRemitenteTelefono() { return remitenteTelefono; }
    public void setRemitenteTelefono(String remitenteTelefono) { this.remitenteTelefono = remitenteTelefono; }

    public int getIdDestinatario() { return idDestinatario; }
    public void setIdDestinatario(int idDestinatario) { this.idDestinatario = idDestinatario; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getDestinatarioDocumento() { return destinatarioDocumento; }
    public void setDestinatarioDocumento(String destinatarioDocumento) { this.destinatarioDocumento = destinatarioDocumento; }

    public String getDestinatarioTelefono() { return destinatarioTelefono; }
    public void setDestinatarioTelefono(String destinatarioTelefono) { this.destinatarioTelefono = destinatarioTelefono; }

    public int getIdEmpleadoRegistro() { return idEmpleadoRegistro; }
    public void setIdEmpleadoRegistro(int idEmpleadoRegistro) { this.idEmpleadoRegistro = idEmpleadoRegistro; }

    public String getEmpleadoRegistro() { return empleadoRegistro; }
    public void setEmpleadoRegistro(String empleadoRegistro) { this.empleadoRegistro = empleadoRegistro; }

    public int getIdEstadoActual() { return idEstadoActual; }
    public void setIdEstadoActual(int idEstadoActual) { this.idEstadoActual = idEstadoActual; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getCantidadBultos() { return cantidadBultos; }
    public void setCantidadBultos(int cantidadBultos) { this.cantidadBultos = cantidadBultos; }

    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }

    public BigDecimal getValorDeclarado() { return valorDeclarado; }
    public void setValorDeclarado(BigDecimal valorDeclarado) { this.valorDeclarado = valorDeclarado; }

    public BigDecimal getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(BigDecimal costoEnvio) { this.costoEnvio = costoEnvio; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    /** Texto "Origen → Destino" para mostrar en tablas. */
    public String getRuta() {
        return origen + " → " + destino;
    }

    public String getFechaTexto() {
        return fechaRegistro == null ? "" : fechaRegistro.format(FORMATO);
    }
}
