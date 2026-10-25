package com.grupo.envios.model;

public class Empleado {

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

    public int getIdEmpleado() { return dkprIdEmpleado; }
    public void setIdEmpleado(int dkprIdEmpleado) { this.dkprIdEmpleado = dkprIdEmpleado; }

    public int getIdAgencia() { return dkprIdAgencia; }
    public void setIdAgencia(int dkprIdAgencia) { this.dkprIdAgencia = dkprIdAgencia; }

    public String getAgencia() { return dkprAgencia; }
    public void setAgencia(String dkprAgencia) { this.dkprAgencia = dkprAgencia; }

    public int getIdRol() { return dkprIdRol; }
    public void setIdRol(int dkprIdRol) { this.dkprIdRol = dkprIdRol; }

    public String getRol() { return dkprRol; }
    public void setRol(String dkprRol) { this.dkprRol = dkprRol; }

    public String getDni() { return dkprDni; }
    public void setDni(String dkprDni) { this.dkprDni = dkprDni; }

    public String getNombres() { return dkprNombres; }
    public void setNombres(String dkprNombres) { this.dkprNombres = dkprNombres; }

    public String getApellidos() { return dkprApellidos; }
    public void setApellidos(String dkprApellidos) { this.dkprApellidos = dkprApellidos; }

    public String getUsuario() { return dkprUsuario; }
    public void setUsuario(String dkprUsuario) { this.dkprUsuario = dkprUsuario; }

    public String getClaveHash() { return dkprClaveHash; }
    public void setClaveHash(String dkprClaveHash) { this.dkprClaveHash = dkprClaveHash; }

    public boolean isActivo() { return dkprActivo; }
    public void setActivo(boolean dkprActivo) { this.dkprActivo = dkprActivo; }

    public String getNombreCompleto() {
        return dkprNombres + " " + dkprApellidos;
    }

    public String getEstadoTexto() {
        return dkprActivo ? "Activo" : "Inactivo";
    }
}
