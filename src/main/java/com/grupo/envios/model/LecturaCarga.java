package com.grupo.envios.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Una lectura del escáner dentro de un control de carga, con su resultado. */
public class LecturaCarga {

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

    public LecturaCarga() {
    }

    public LecturaCarga(String dkprCodigoLeido, Integer dkprIdEnvio, Integer dkprNroBulto, String dkprResultado,
                        String dkprDetalle) {
        this.dkprCodigoLeido = dkprCodigoLeido;
        this.dkprIdEnvio = dkprIdEnvio;
        this.dkprNroBulto = dkprNroBulto;
        this.dkprResultado = dkprResultado;
        this.dkprDetalle = dkprDetalle;
        this.dkprFecha = LocalDateTime.now();
    }

    public String getCodigoLeido() { return dkprCodigoLeido; }
    public void setCodigoLeido(String dkprCodigoLeido) { this.dkprCodigoLeido = dkprCodigoLeido; }

    public Integer getIdEnvio() { return dkprIdEnvio; }
    public void setIdEnvio(Integer dkprIdEnvio) { this.dkprIdEnvio = dkprIdEnvio; }

    public Integer getNroBulto() { return dkprNroBulto; }
    public void setNroBulto(Integer dkprNroBulto) { this.dkprNroBulto = dkprNroBulto; }

    public String getResultado() { return dkprResultado; }
    public void setResultado(String dkprResultado) { this.dkprResultado = dkprResultado; }

    public String getDetalle() { return dkprDetalle; }
    public void setDetalle(String dkprDetalle) { this.dkprDetalle = dkprDetalle; }

    public LocalDateTime getFecha() { return dkprFecha; }
    public void setFecha(LocalDateTime dkprFecha) { this.dkprFecha = dkprFecha; }

    public boolean esOk() {
        return OK.equals(dkprResultado);
    }

    public String getHoraTexto() {
        return dkprFecha == null ? "" : dkprFecha.format(FORMATO_HORA);
    }

    /** Resultado en palabras para la tabla de lecturas. */
    public String getResultadoTexto() {
        return switch (dkprResultado) {
            case OK -> "OK";
            case DUPLICADO -> "Duplicado";
            case OTRO_DESTINO -> "Otro destino";
            case NO_EXISTE -> "No existe";
            default -> "No corresponde";
        };
    }
}
