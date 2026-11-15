package com.grupo.envios.model;

public class DkprConductor {

    private int dkprIdConductor;
    private String dkprDni;
    private String dkprNombreCompleto;
    private String dkprLicencia;
    private String dkprTelefono;
    private boolean dkprActivo = true;

    public int dkprGetIdConductor() { return dkprIdConductor; }
    public void dkprSetIdConductor(int dkprIdConductor) { this.dkprIdConductor = dkprIdConductor; }

    public String dkprGetDni() { return dkprDni; }
    public void dkprSetDni(String dkprDni) { this.dkprDni = dkprDni; }

    public String dkprGetNombreCompleto() { return dkprNombreCompleto; }
    public void dkprSetNombreCompleto(String dkprNombreCompleto) { this.dkprNombreCompleto = dkprNombreCompleto; }

    public String dkprGetLicencia() { return dkprLicencia; }
    public void dkprSetLicencia(String dkprLicencia) { this.dkprLicencia = dkprLicencia; }

    public String dkprGetTelefono() { return dkprTelefono; }
    public void dkprSetTelefono(String dkprTelefono) { this.dkprTelefono = dkprTelefono; }

    public boolean dkprIsActivo() { return dkprActivo; }
    public void dkprSetActivo(boolean dkprActivo) { this.dkprActivo = dkprActivo; }

    public String dkprGetEstadoTexto() {
        return dkprActivo ? "Activo" : "Inactivo";
    }

    public String dkprGetTextoManifiesto() {
        return dkprNombreCompleto + " (DNI " + dkprDni + ")";
    }

    @Override
    public String toString() {
        return dkprNombreCompleto + "  ·  DNI " + dkprDni;
    }
}
