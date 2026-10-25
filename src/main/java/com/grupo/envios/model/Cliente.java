package com.grupo.envios.model;

public class Cliente {

    private int dkprIdCliente;
    private String dkprTipoDocumento;
    private String dkprNumeroDocumento;
    private String dkprNombreCompleto;
    private String dkprTelefono;
    private String dkprEmail;
    private String dkprDireccion;

    public int getIdCliente() { return dkprIdCliente; }
    public void setIdCliente(int dkprIdCliente) { this.dkprIdCliente = dkprIdCliente; }

    public String getTipoDocumento() { return dkprTipoDocumento; }
    public void setTipoDocumento(String dkprTipoDocumento) { this.dkprTipoDocumento = dkprTipoDocumento; }

    public String getNumeroDocumento() { return dkprNumeroDocumento; }
    public void setNumeroDocumento(String dkprNumeroDocumento) { this.dkprNumeroDocumento = dkprNumeroDocumento; }

    public String getNombreCompleto() { return dkprNombreCompleto; }
    public void setNombreCompleto(String dkprNombreCompleto) { this.dkprNombreCompleto = dkprNombreCompleto; }

    public String getTelefono() { return dkprTelefono; }
    public void setTelefono(String dkprTelefono) { this.dkprTelefono = dkprTelefono; }

    public String getEmail() { return dkprEmail; }
    public void setEmail(String dkprEmail) { this.dkprEmail = dkprEmail; }

    public String getDireccion() { return dkprDireccion; }
    public void setDireccion(String dkprDireccion) { this.dkprDireccion = dkprDireccion; }

    @Override
    public String toString() {
        return dkprNombreCompleto;
    }
}
