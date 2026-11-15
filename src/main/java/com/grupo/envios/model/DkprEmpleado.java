package com.grupo.envios.model;

public class DkprEmpleado {

    private int dkprIdEmpleado;
    private int dkprIdAgencia;
    private String dkprAgencia;
    private int dkprIdRol;
    private String dkprRol;
    private String dkprDni;
    private String dkprNombres;
    private String dkprApellidos;
    private String dkprUsuario;
    private String dkprClaveHash;
    private boolean dkprActivo = true;

    public int dkprGetIdEmpleado() { return dkprIdEmpleado; }
    public void dkprSetIdEmpleado(int dkprIdEmpleado) { this.dkprIdEmpleado = dkprIdEmpleado; }

    public int dkprGetIdAgencia() { return dkprIdAgencia; }
    public void dkprSetIdAgencia(int dkprIdAgencia) { this.dkprIdAgencia = dkprIdAgencia; }

    public String dkprGetAgencia() { return dkprAgencia; }
    public void dkprSetAgencia(String dkprAgencia) { this.dkprAgencia = dkprAgencia; }

    public int dkprGetIdRol() { return dkprIdRol; }
    public void dkprSetIdRol(int dkprIdRol) { this.dkprIdRol = dkprIdRol; }

    public String dkprGetRol() { return dkprRol; }
    public void dkprSetRol(String dkprRol) { this.dkprRol = dkprRol; }

    public String dkprGetDni() { return dkprDni; }
    public void dkprSetDni(String dkprDni) { this.dkprDni = dkprDni; }

    public String dkprGetNombres() { return dkprNombres; }
    public void dkprSetNombres(String dkprNombres) { this.dkprNombres = dkprNombres; }

    public String dkprGetApellidos() { return dkprApellidos; }
    public void dkprSetApellidos(String dkprApellidos) { this.dkprApellidos = dkprApellidos; }

    public String dkprGetUsuario() { return dkprUsuario; }
    public void dkprSetUsuario(String dkprUsuario) { this.dkprUsuario = dkprUsuario; }

    public String dkprGetClaveHash() { return dkprClaveHash; }
    public void dkprSetClaveHash(String dkprClaveHash) { this.dkprClaveHash = dkprClaveHash; }

    public boolean dkprIsActivo() { return dkprActivo; }
    public void dkprSetActivo(boolean dkprActivo) { this.dkprActivo = dkprActivo; }

    public String dkprGetNombreCompleto() {
        return dkprNombres + " " + dkprApellidos;
    }

    public String dkprGetEstadoTexto() {
        return dkprActivo ? "Activo" : "Inactivo";
    }
}
