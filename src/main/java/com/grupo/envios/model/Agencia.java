package com.grupo.envios.model;

public class Agencia {

    private int dkprIdAgencia;
    private String dkprNombre;
    private String dkprCiudad;
    private String dkprDireccion;
    private String dkprTelefono;
    private boolean dkprActivo = true;

    public int getIdAgencia() { return dkprIdAgencia; }
    public void setIdAgencia(int dkprIdAgencia) { this.dkprIdAgencia = dkprIdAgencia; }

    public String getNombre() { return dkprNombre; }
    public void setNombre(String dkprNombre) { this.dkprNombre = dkprNombre; }

    public String getCiudad() { return dkprCiudad; }
    public void setCiudad(String dkprCiudad) { this.dkprCiudad = dkprCiudad; }

    public String getDireccion() { return dkprDireccion; }
    public void setDireccion(String dkprDireccion) { this.dkprDireccion = dkprDireccion; }

    public String getTelefono() { return dkprTelefono; }
    public void setTelefono(String dkprTelefono) { this.dkprTelefono = dkprTelefono; }

    public boolean isActivo() { return dkprActivo; }
    public void setActivo(boolean dkprActivo) { this.dkprActivo = dkprActivo; }

    public String getEstadoTexto() {
        return dkprActivo ? "Activa" : "Inactiva";
    }

    @Override
    public String toString() {
        return dkprNombre + " (" + dkprCiudad + ")";
    }
}
