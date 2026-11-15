package com.grupo.envios.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DkprCajaTurno {

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

    public int dkprGetIdTurno() { return dkprIdTurno; }
    public void dkprSetIdTurno(int dkprIdTurno) { this.dkprIdTurno = dkprIdTurno; }

    public int dkprGetIdEmpleado() { return dkprIdEmpleado; }
    public void dkprSetIdEmpleado(int dkprIdEmpleado) { this.dkprIdEmpleado = dkprIdEmpleado; }

    public String dkprGetEmpleado() { return dkprEmpleado; }
    public void dkprSetEmpleado(String dkprEmpleado) { this.dkprEmpleado = dkprEmpleado; }

    public int dkprGetIdAgencia() { return dkprIdAgencia; }
    public void dkprSetIdAgencia(int dkprIdAgencia) { this.dkprIdAgencia = dkprIdAgencia; }

    public String dkprGetAgencia() { return dkprAgencia; }
    public void dkprSetAgencia(String dkprAgencia) { this.dkprAgencia = dkprAgencia; }

    public LocalDateTime dkprGetFechaApertura() { return dkprFechaApertura; }
    public void dkprSetFechaApertura(LocalDateTime dkprFechaApertura) { this.dkprFechaApertura = dkprFechaApertura; }

    public LocalDateTime dkprGetFechaCierre() { return dkprFechaCierre; }
    public void dkprSetFechaCierre(LocalDateTime dkprFechaCierre) { this.dkprFechaCierre = dkprFechaCierre; }

    public BigDecimal dkprGetMontoInicial() { return dkprMontoInicial; }
    public void dkprSetMontoInicial(BigDecimal dkprMontoInicial) { this.dkprMontoInicial = dkprMontoInicial; }

    public String dkprGetEstado() { return dkprEstado; }
    public void dkprSetEstado(String dkprEstado) { this.dkprEstado = dkprEstado; }

    public String dkprGetObservacion() { return dkprObservacion; }
    public void dkprSetObservacion(String dkprObservacion) { this.dkprObservacion = dkprObservacion; }

    public int dkprGetAnuladosCantidad() { return dkprAnuladosCantidad; }
    public void dkprSetAnuladosCantidad(int dkprAnuladosCantidad) { this.dkprAnuladosCantidad = dkprAnuladosCantidad; }

    public BigDecimal dkprGetAnuladosMonto() { return dkprAnuladosMonto; }
    public void dkprSetAnuladosMonto(BigDecimal dkprAnuladosMonto) { this.dkprAnuladosMonto = dkprAnuladosMonto; }

    public boolean dkprIsAbierto() {
        return ABIERTO.equals(dkprEstado);
    }

    public BigDecimal dkprGetEsperado(String dkprMetodo) {
        return dkprEsperado.getOrDefault(dkprMetodo, BigDecimal.ZERO);
    }

    public void dkprSetEsperado(String dkprMetodo, BigDecimal dkprMonto) {
        dkprEsperado.put(dkprMetodo, dkprMonto == null ? BigDecimal.ZERO : dkprMonto);
    }

    public BigDecimal dkprGetContado(String dkprMetodo) {
        return dkprContado.get(dkprMetodo);
    }

    public void dkprSetContado(String dkprMetodo, BigDecimal dkprMonto) {
        if (dkprMonto == null) {
            dkprContado.remove(dkprMetodo);
        } else {
            dkprContado.put(dkprMetodo, dkprMonto);
        }
    }

    public int dkprGetCantidad(String dkprMetodo) {
        return dkprCantidad.getOrDefault(dkprMetodo, 0);
    }

    public void dkprSetCantidad(String dkprMetodo, int dkprN) {
        dkprCantidad.put(dkprMetodo, dkprN);
    }

    public BigDecimal dkprGetDiferencia(String dkprMetodo) {
        BigDecimal dkprC = dkprGetContado(dkprMetodo);
        return dkprC == null ? null : dkprC.subtract(dkprGetEsperado(dkprMetodo));
    }

    public BigDecimal dkprGetTotalEsperado() {
        return METODOS.stream().map(this::dkprGetEsperado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal dkprGetTotalContado() {
        BigDecimal dkprTotal = BigDecimal.ZERO;
        for (String dkprM : METODOS) {
            if (dkprGetContado(dkprM) == null) {
                return null;
            }
            dkprTotal = dkprTotal.add(dkprGetContado(dkprM));
        }
        return dkprTotal;
    }

    public BigDecimal dkprGetDiferenciaTotal() {
        BigDecimal dkprContadoTotal = dkprGetTotalContado();
        return dkprContadoTotal == null ? null : dkprContadoTotal.subtract(dkprGetTotalEsperado());
    }

    public int dkprGetCantidadTotal() {
        return METODOS.stream().mapToInt(this::dkprGetCantidad).sum();
    }

    public String dkprGetFechaAperturaTexto() {
        return dkprFechaApertura == null ? "" : dkprFechaApertura.format(FORMATO);
    }

    public String dkprGetFechaCierreTexto() {
        return dkprFechaCierre == null ? "" : dkprFechaCierre.format(FORMATO);
    }

    public static String dkprResultado(BigDecimal dkprDiferencia) {
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
