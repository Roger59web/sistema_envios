package com.grupo.envios.model;

public class DkprCliente {

    private int dkprIdCliente;
    private String dkprTipoDocumento;
    private String dkprNumeroDocumento;
    private String dkprNombreCompleto;
    private String dkprTelefono;
    private String dkprEmail;
    private String dkprDireccion;

    public int dkprGetIdCliente() { return dkprIdCliente; }
    public void dkprSetIdCliente(int dkprIdCliente) { this.dkprIdCliente = dkprIdCliente; }

    public String dkprGetTipoDocumento() { return dkprTipoDocumento; }
    public void dkprSetTipoDocumento(String dkprTipoDocumento) { this.dkprTipoDocumento = dkprTipoDocumento; }

    public String dkprGetNumeroDocumento() { return dkprNumeroDocumento; }
    public void dkprSetNumeroDocumento(String dkprNumeroDocumento) { this.dkprNumeroDocumento = dkprNumeroDocumento; }

    public String dkprGetNombreCompleto() { return dkprNombreCompleto; }
    public void dkprSetNombreCompleto(String dkprNombreCompleto) { this.dkprNombreCompleto = dkprNombreCompleto; }

    public String dkprGetTelefono() { return dkprTelefono; }
    public void dkprSetTelefono(String dkprTelefono) { this.dkprTelefono = dkprTelefono; }

    public String dkprGetEmail() { return dkprEmail; }
    public void dkprSetEmail(String dkprEmail) { this.dkprEmail = dkprEmail; }

    public String dkprGetDireccion() { return dkprDireccion; }
    public void dkprSetDireccion(String dkprDireccion) { this.dkprDireccion = dkprDireccion; }

    @Override
    public String toString() {
        return dkprNombreCompleto;
    }
}
