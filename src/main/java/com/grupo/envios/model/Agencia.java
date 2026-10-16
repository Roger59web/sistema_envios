package com.grupo.envios.model;

public class Agencia {

    private int idAgencia;
    private String nombre;
    private String ciudad;
    private String direccion;
    private String telefono;
    private boolean activo = true;

    public int getIdAgencia() { return idAgencia; }
    public void setIdAgencia(int idAgencia) { this.idAgencia = idAgencia; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getEstadoTexto() {
        return activo ? "Activa" : "Inactiva";
    }

    @Override
    public String toString() {
        return nombre + " (" + ciudad + ")";
    }
}
