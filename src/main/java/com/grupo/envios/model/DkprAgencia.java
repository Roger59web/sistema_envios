package com.grupo.envios.model;

public class DkprAgencia {

    private int dkprIdAgencia;
    private String dkprNombre;
    private String dkprCiudad;
    private String dkprDireccion;
    private String dkprTelefono;
    private boolean dkprActivo = true;

    public int dkprGetIdAgencia() { return dkprIdAgencia; }
    public void dkprSetIdAgencia(int dkprIdAgencia) { this.dkprIdAgencia = dkprIdAgencia; }

    public String dkprGetNombre() { return dkprNombre; }
    public void dkprSetNombre(String dkprNombre) { this.dkprNombre = dkprNombre; }

    public String dkprGetCiudad() { return dkprCiudad; }
    public void dkprSetCiudad(String dkprCiudad) { this.dkprCiudad = dkprCiudad; }

    public String dkprGetDireccion() { return dkprDireccion; }
    public void dkprSetDireccion(String dkprDireccion) { this.dkprDireccion = dkprDireccion; }

    public String dkprGetTelefono() { return dkprTelefono; }
    public void dkprSetTelefono(String dkprTelefono) { this.dkprTelefono = dkprTelefono; }

    public boolean dkprIsActivo() { return dkprActivo; }
    public void dkprSetActivo(boolean dkprActivo) { this.dkprActivo = dkprActivo; }

    public String dkprGetEstadoTexto() {
        return dkprActivo ? "Activa" : "Inactiva";
    }

    @Override
    public String toString() {
        return dkprNombre + " (" + dkprCiudad + ")";
    }
}
