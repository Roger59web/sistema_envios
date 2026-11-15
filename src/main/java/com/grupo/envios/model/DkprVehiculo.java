package com.grupo.envios.model;

import java.math.BigDecimal;

public class DkprVehiculo {

    private int dkprIdVehiculo;
    private String dkprPlaca;
    private String dkprDescripcion;
    private BigDecimal dkprCapacidadKg;
    private boolean dkprActivo = true;

    public int dkprGetIdVehiculo() { return dkprIdVehiculo; }
    public void dkprSetIdVehiculo(int dkprIdVehiculo) { this.dkprIdVehiculo = dkprIdVehiculo; }

    public String dkprGetPlaca() { return dkprPlaca; }
    public void dkprSetPlaca(String dkprPlaca) { this.dkprPlaca = dkprPlaca; }

    public String dkprGetDescripcion() { return dkprDescripcion; }
    public void dkprSetDescripcion(String dkprDescripcion) { this.dkprDescripcion = dkprDescripcion; }

    public BigDecimal dkprGetCapacidadKg() { return dkprCapacidadKg; }
    public void dkprSetCapacidadKg(BigDecimal dkprCapacidadKg) { this.dkprCapacidadKg = dkprCapacidadKg; }

    public boolean dkprIsActivo() { return dkprActivo; }
    public void dkprSetActivo(boolean dkprActivo) { this.dkprActivo = dkprActivo; }

    public String dkprGetEstadoTexto() {
        return dkprActivo ? "Activo" : "Inactivo";
    }

    public String dkprGetCapacidadTexto() {
        return dkprCapacidadKg == null ? "-" : dkprCapacidadKg.stripTrailingZeros().toPlainString() + " kg";
    }

    @Override
    public String toString() {
        return dkprPlaca + (dkprDescripcion == null || dkprDescripcion.isBlank() ? "" : "  ·  " + dkprDescripcion);
    }
}
