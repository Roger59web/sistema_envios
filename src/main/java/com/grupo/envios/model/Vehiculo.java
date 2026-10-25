package com.grupo.envios.model;

import java.math.BigDecimal;

/** Vehículo de la empresa que traslada la carga entre agencias. */
public class Vehiculo {

    private int dkprIdVehiculo;
    private String dkprPlaca;
    private String dkprDescripcion;
    private BigDecimal dkprCapacidadKg;
    private boolean dkprActivo = true;

    public int getIdVehiculo() { return dkprIdVehiculo; }
    public void setIdVehiculo(int dkprIdVehiculo) { this.dkprIdVehiculo = dkprIdVehiculo; }

    public String getPlaca() { return dkprPlaca; }
    public void setPlaca(String dkprPlaca) { this.dkprPlaca = dkprPlaca; }

    public String getDescripcion() { return dkprDescripcion; }
    public void setDescripcion(String dkprDescripcion) { this.dkprDescripcion = dkprDescripcion; }

    public BigDecimal getCapacidadKg() { return dkprCapacidadKg; }
    public void setCapacidadKg(BigDecimal dkprCapacidadKg) { this.dkprCapacidadKg = dkprCapacidadKg; }

    public boolean isActivo() { return dkprActivo; }
    public void setActivo(boolean dkprActivo) { this.dkprActivo = dkprActivo; }

    public String getEstadoTexto() {
        return dkprActivo ? "Activo" : "Inactivo";
    }

    public String getCapacidadTexto() {
        return dkprCapacidadKg == null ? "-" : dkprCapacidadKg.stripTrailingZeros().toPlainString() + " kg";
    }

    @Override
    public String toString() {
        return dkprPlaca + (dkprDescripcion == null || dkprDescripcion.isBlank() ? "" : "  ·  " + dkprDescripcion);
    }
}
