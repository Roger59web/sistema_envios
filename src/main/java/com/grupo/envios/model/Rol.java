package com.grupo.envios.model;

public class Rol {

    private int dkprIdRol;
    private String dkprNombre;

    public Rol(int dkprIdRol, String dkprNombre) {
        this.dkprIdRol = dkprIdRol;
        this.dkprNombre = dkprNombre;
    }

    public int getIdRol() { return dkprIdRol; }
    public String getNombre() { return dkprNombre; }

    @Override
    public String toString() {
        return dkprNombre;
    }
}
