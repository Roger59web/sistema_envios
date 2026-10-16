package com.grupo.envios.model;

public class Rol {

    private int idRol;
    private String nombre;

    public Rol(int idRol, String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
    }

    public int getIdRol() { return idRol; }
    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return nombre;
    }
}
