package com.grupo.envios.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CajaTurno {

    public static final List<String> METODOS = List.of("EFECTIVO", "TARJETA", "YAPE", "PLIN");
    public static final String ABIERTO = "ABIERTO";
    public static final String CERRADO = "CERRADO";

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int dkprIdTurno;
    private int dkprIdEmpleado;
    private String dkprEmpleado;
    private int dkprIdAgencia;
    private String dkprAgencia;
    private LocalDateTime dkprFechaApertura;
    private LocalDateTime dkprFechaCierre;
    private BigDecimal dkprMontoInicial = BigDecimal.ZERO;
    private String dkprEstado;
    private String dkprObservacion;
    private final Map<String, BigDecimal> dkprEsperado = new LinkedHashMap<>();
    private final Map<String, BigDecimal> dkprContado = new LinkedHashMap<>();
    private final Map<String, Integer> dkprCantidad = new LinkedHashMap<>();
    private int dkprAnuladosCantidad;
    private BigDecimal dkprAnuladosMonto = BigDecimal.ZERO;

    public int getIdTurno() { return dkprIdTurno; }
    public void setIdTurno(int dkprIdTurno) { this.dkprIdTurno = dkprIdTurno; }

    public int getIdEmpleado() { return dkprIdEmpleado; }
    public void setIdEmpleado(int dkprIdEmpleado) { this.dkprIdEmpleado = dkprIdEmpleado; }

    public String getEmpleado() { return dkprEmpleado; }
    public void setEmpleado(String dkprEmpleado) { this.dkprEmpleado = dkprEmpleado; }

    public int getIdAgencia() { return dkprIdAgencia; }
    public void setIdAgencia(int dkprIdAgencia) { this.dkprIdAgencia = dkprIdAgencia; }

    public String getAgencia() { return dkprAgencia; }
    public void setAgencia(String dkprAgencia) { this.dkprAgencia = dkprAgencia; }

    public LocalDateTime getFechaApertura() { return dkprFechaApertura; }
    public void setFechaApertura(LocalDateTime dkprFechaApertura) { this.dkprFechaApertura = dkprFechaApertura; }

    public LocalDateTime getFechaCierre() { return dkprFechaCierre; }
    public void setFechaCierre(LocalDateTime dkprFechaCierre) { this.dkprFechaCierre = dkprFechaCierre; }

    public BigDecimal getMontoInicial() { return dkprMontoInicial; }
    public void setMontoInicial(BigDecimal dkprMontoInicial) { this.dkprMontoInicial = dkprMontoInicial; }

    public String getEstado() { return dkprEstado; }
    public void setEstado(String dkprEstado) { this.dkprEstado = dkprEstado; }

    public String getObservacion() { return dkprObservacion; }
    public void setObservacion(String dkprObservacion) { this.dkprObservacion = dkprObservacion; }

    public int getAnuladosCantidad() { return dkprAnuladosCantidad; }
    public void setAnuladosCantidad(int dkprAnuladosCantidad) { this.dkprAnuladosCantidad = dkprAnuladosCantidad; }

    public BigDecimal getAnuladosMonto() { return dkprAnuladosMonto; }
    public void setAnuladosMonto(BigDecimal dkprAnuladosMonto) { this.dkprAnuladosMonto = dkprAnuladosMonto; }

    public boolean isAbierto() {
        return ABIERTO.equals(dkprEstado);
    }

    public BigDecimal getEsperado(String dkprMetodo) {
        return dkprEsperado.getOrDefault(dkprMetodo, BigDecimal.ZERO);
    }

    public void setEsperado(String dkprMetodo, BigDecimal dkprMonto) {
        dkprEsperado.put(dkprMetodo, dkprMonto == null ? BigDecimal.ZERO : dkprMonto);
    }

    public BigDecimal getContado(String dkprMetodo) {
        return dkprContado.get(dkprMetodo);
    }

    public void setContado(String dkprMetodo, BigDecimal dkprMonto) {
        if (dkprMonto == null) {
            dkprContado.remove(dkprMetodo);
        } else {
            dkprContado.put(dkprMetodo, dkprMonto);
        }
    }

    public int getCantidad(String dkprMetodo) {
        return dkprCantidad.getOrDefault(dkprMetodo, 0);
    }

    public void setCantidad(String dkprMetodo, int dkprN) {
        dkprCantidad.put(dkprMetodo, dkprN);
    }

    public BigDecimal getDiferencia(String dkprMetodo) {
        BigDecimal dkprC = getContado(dkprMetodo);
        return dkprC == null ? null : dkprC.subtract(getEsperado(dkprMetodo));
    }

    public BigDecimal getTotalEsperado() {
        return METODOS.stream().map(this::getEsperado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalContado() {
        BigDecimal dkprTotal = BigDecimal.ZERO;
        for (String dkprM : METODOS) {
            if (getContado(dkprM) == null) {
                return null;
            }
            dkprTotal = dkprTotal.add(getContado(dkprM));
        }
        return dkprTotal;
    }

    public BigDecimal getDiferenciaTotal() {
        BigDecimal dkprContadoTotal = getTotalContado();
        return dkprContadoTotal == null ? null : dkprContadoTotal.subtract(getTotalEsperado());
    }

    public int getCantidadTotal() {
        return METODOS.stream().mapToInt(this::getCantidad).sum();
    }

    public String getFechaAperturaTexto() {
        return dkprFechaApertura == null ? "" : dkprFechaApertura.format(FORMATO);
    }

    public String getFechaCierreTexto() {
        return dkprFechaCierre == null ? "" : dkprFechaCierre.format(FORMATO);
    }

    public static String resultado(BigDecimal dkprDiferencia) {
        if (dkprDiferencia == null) {
            return "Sin contar";
        }
        int dkprSigno = dkprDiferencia.signum();
        if (dkprSigno == 0) {
            return "Cuadra";
        }
        String dkprMonto = "S/ " + dkprDiferencia.abs().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
        return dkprSigno < 0 ? "Faltante " + dkprMonto : "Sobrante " + dkprMonto;
    }
}
