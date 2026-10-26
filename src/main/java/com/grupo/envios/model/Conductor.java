package com.grupo.envios.model;

public class Conductor {

    private int dkprIdConductor;
    private String dkprDni;
    private String dkprNombreCompleto;
    private String dkprLicencia;
    private String dkprTelefono;
    private boolean dkprActivo = true;

    public int getIdConductor() { return dkprIdConductor; }
    public void setIdConductor(int dkprIdConductor) { this.dkprIdConductor = dkprIdConductor; }

    public String getDni() { return dkprDni; }
    public void setDni(String dkprDni) { this.dkprDni = dkprDni; }

    public String getNombreCompleto() { return dkprNombreCompleto; }
    public void setNombreCompleto(String dkprNombreCompleto) { this.dkprNombreCompleto = dkprNombreCompleto; }

    public String getLicencia() { return dkprLicencia; }
    public void setLicencia(String dkprLicencia) { this.dkprLicencia = dkprLicencia; }

    public String getTelefono() { return dkprTelefono; }
    public void setTelefono(String dkprTelefono) { this.dkprTelefono = dkprTelefono; }

    public boolean isActivo() { return dkprActivo; }
    public void setActivo(boolean dkprActivo) { this.dkprActivo = dkprActivo; }

    public String getEstadoTexto() {
        return dkprActivo ? "Activo" : "Inactivo";
    }

    public String getTextoManifiesto() {
        return dkprNombreCompleto + " (DNI " + dkprDni + ")";
    }

    @Override
    public String toString() {
        return dkprNombreCompleto + "  ·  DNI " + dkprDni;
    }
}
