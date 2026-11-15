package com.grupo.envios.model;

public class DkprRol {

    private int dkprIdRol;
    private String dkprNombre;

    public DkprRol(int dkprIdRol, String dkprNombre) {
        this.dkprIdRol = dkprIdRol;
        this.dkprNombre = dkprNombre;
    }

    public int dkprGetIdRol() { return dkprIdRol; }
    public String dkprGetNombre() { return dkprNombre; }

    @Override
    public String toString() {
        return dkprNombre;
    }
}
