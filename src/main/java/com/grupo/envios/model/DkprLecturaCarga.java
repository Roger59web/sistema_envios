package com.grupo.envios.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DkprLecturaCarga {

    public static final String OK = "OK";
    public static final String DUPLICADO = "DUPLICADO";
    public static final String OTRO_DESTINO = "OTRO_DESTINO";
    public static final String NO_EXISTE = "NO_EXISTE";
    public static final String NO_CORRESPONDE = "NO_CORRESPONDE";

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String dkprCodigoLeido;
    private Integer dkprIdEnvio;
    private Integer dkprNroBulto;
    private String dkprResultado;
    private String dkprDetalle;
    private LocalDateTime dkprFecha;

    public DkprLecturaCarga() {
    }

    public DkprLecturaCarga(String dkprCodigoLeido, Integer dkprIdEnvio, Integer dkprNroBulto, String dkprResultado,
                        String dkprDetalle) {
        this.dkprCodigoLeido = dkprCodigoLeido;
        this.dkprIdEnvio = dkprIdEnvio;
        this.dkprNroBulto = dkprNroBulto;
        this.dkprResultado = dkprResultado;
        this.dkprDetalle = dkprDetalle;
        this.dkprFecha = LocalDateTime.now();
    }

    public String dkprGetCodigoLeido() { return dkprCodigoLeido; }
    public void dkprSetCodigoLeido(String dkprCodigoLeido) { this.dkprCodigoLeido = dkprCodigoLeido; }

    public Integer dkprGetIdEnvio() { return dkprIdEnvio; }
    public void dkprSetIdEnvio(Integer dkprIdEnvio) { this.dkprIdEnvio = dkprIdEnvio; }

    public Integer dkprGetNroBulto() { return dkprNroBulto; }
    public void dkprSetNroBulto(Integer dkprNroBulto) { this.dkprNroBulto = dkprNroBulto; }

    public String dkprGetResultado() { return dkprResultado; }
    public void dkprSetResultado(String dkprResultado) { this.dkprResultado = dkprResultado; }

    public String dkprGetDetalle() { return dkprDetalle; }
    public void dkprSetDetalle(String dkprDetalle) { this.dkprDetalle = dkprDetalle; }

    public LocalDateTime dkprGetFecha() { return dkprFecha; }
    public void dkprSetFecha(LocalDateTime dkprFecha) { this.dkprFecha = dkprFecha; }

    public boolean dkprEsOk() {
        return OK.equals(dkprResultado);
    }

    public String dkprGetHoraTexto() {
        return dkprFecha == null ? "" : dkprFecha.format(FORMATO_HORA);
    }

    public String dkprGetResultadoTexto() {
        return switch (dkprResultado) {
            case OK -> "OK";
            case DUPLICADO -> "Duplicado";
            case OTRO_DESTINO -> "Otro destino";
            case NO_EXISTE -> "No existe";
            default -> "No corresponde";
        };
    }
}
