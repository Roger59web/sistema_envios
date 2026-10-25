package com.grupo.envios.model;

import com.grupo.envios.util.Estados;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class Envio {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Días que la agencia guarda una encomienda sin cobrar almacenaje (luego se avisa). */
    public static final int DIAS_ALMACEN_GRATIS = 7;

    private int dkprIdEnvio;
    private String dkprCodigoSeguimiento;
    private int dkprIdRuta;
    private int dkprIdAgenciaOrigen;
    private int dkprIdAgenciaDestino;
    private String dkprOrigen;
    private String dkprDestino;
    private String dkprOrigenDireccion;
    private String dkprOrigenTelefono;
    private int dkprIdRemitente;
    private String dkprRemitente;
    private String dkprRemitenteDocumento;
    private String dkprRemitenteTelefono;
    private int dkprIdDestinatario;
    private String dkprDestinatario;
    private String dkprDestinatarioDocumento;
    private String dkprDestinatarioTelefono;
    private int dkprIdEmpleadoRegistro;
    private String dkprEmpleadoRegistro;
    private int dkprIdEstadoActual;
    private String dkprEstado;
    private LocalDateTime dkprFechaRegistro;
    private String dkprDescripcion;
    private int dkprCantidadBultos = 1;
    private BigDecimal dkprPesoKg;
    private BigDecimal dkprValorDeclarado = BigDecimal.ZERO;
    private BigDecimal dkprCostoEnvio;
    private String dkprObservaciones;
    private int dkprDiasEstimados;
    private LocalDateTime dkprFechaUltimoMovimiento;
    private String dkprManifiesto;

    public int getIdEnvio() { return dkprIdEnvio; }
    public void setIdEnvio(int dkprIdEnvio) { this.dkprIdEnvio = dkprIdEnvio; }

    public String getCodigoSeguimiento() { return dkprCodigoSeguimiento; }
    public void setCodigoSeguimiento(String dkprCodigoSeguimiento) { this.dkprCodigoSeguimiento = dkprCodigoSeguimiento; }

    public int getIdRuta() { return dkprIdRuta; }
    public void setIdRuta(int dkprIdRuta) { this.dkprIdRuta = dkprIdRuta; }

    public int getIdAgenciaOrigen() { return dkprIdAgenciaOrigen; }
    public void setIdAgenciaOrigen(int dkprIdAgenciaOrigen) { this.dkprIdAgenciaOrigen = dkprIdAgenciaOrigen; }

    public int getIdAgenciaDestino() { return dkprIdAgenciaDestino; }
    public void setIdAgenciaDestino(int dkprIdAgenciaDestino) { this.dkprIdAgenciaDestino = dkprIdAgenciaDestino; }

    public String getOrigen() { return dkprOrigen; }
    public void setOrigen(String dkprOrigen) { this.dkprOrigen = dkprOrigen; }

    public String getDestino() { return dkprDestino; }
    public void setDestino(String dkprDestino) { this.dkprDestino = dkprDestino; }

    public String getOrigenDireccion() { return dkprOrigenDireccion; }
    public void setOrigenDireccion(String dkprOrigenDireccion) { this.dkprOrigenDireccion = dkprOrigenDireccion; }

    public String getOrigenTelefono() { return dkprOrigenTelefono; }
    public void setOrigenTelefono(String dkprOrigenTelefono) { this.dkprOrigenTelefono = dkprOrigenTelefono; }

    public int getIdRemitente() { return dkprIdRemitente; }
    public void setIdRemitente(int dkprIdRemitente) { this.dkprIdRemitente = dkprIdRemitente; }

    public String getRemitente() { return dkprRemitente; }
    public void setRemitente(String dkprRemitente) { this.dkprRemitente = dkprRemitente; }

    public String getRemitenteDocumento() { return dkprRemitenteDocumento; }
    public void setRemitenteDocumento(String dkprRemitenteDocumento) { this.dkprRemitenteDocumento = dkprRemitenteDocumento; }

    public String getRemitenteTelefono() { return dkprRemitenteTelefono; }
    public void setRemitenteTelefono(String dkprRemitenteTelefono) { this.dkprRemitenteTelefono = dkprRemitenteTelefono; }

    public int getIdDestinatario() { return dkprIdDestinatario; }
    public void setIdDestinatario(int dkprIdDestinatario) { this.dkprIdDestinatario = dkprIdDestinatario; }

    public String getDestinatario() { return dkprDestinatario; }
    public void setDestinatario(String dkprDestinatario) { this.dkprDestinatario = dkprDestinatario; }

    public String getDestinatarioDocumento() { return dkprDestinatarioDocumento; }
    public void setDestinatarioDocumento(String dkprDestinatarioDocumento) { this.dkprDestinatarioDocumento = dkprDestinatarioDocumento; }

    public String getDestinatarioTelefono() { return dkprDestinatarioTelefono; }
    public void setDestinatarioTelefono(String dkprDestinatarioTelefono) { this.dkprDestinatarioTelefono = dkprDestinatarioTelefono; }

    public int getIdEmpleadoRegistro() { return dkprIdEmpleadoRegistro; }
    public void setIdEmpleadoRegistro(int dkprIdEmpleadoRegistro) { this.dkprIdEmpleadoRegistro = dkprIdEmpleadoRegistro; }

    public String getEmpleadoRegistro() { return dkprEmpleadoRegistro; }
    public void setEmpleadoRegistro(String dkprEmpleadoRegistro) { this.dkprEmpleadoRegistro = dkprEmpleadoRegistro; }

    public int getIdEstadoActual() { return dkprIdEstadoActual; }
    public void setIdEstadoActual(int dkprIdEstadoActual) { this.dkprIdEstadoActual = dkprIdEstadoActual; }

    public String getEstado() { return dkprEstado; }
    public void setEstado(String dkprEstado) { this.dkprEstado = dkprEstado; }

    public LocalDateTime getFechaRegistro() { return dkprFechaRegistro; }
    public void setFechaRegistro(LocalDateTime dkprFechaRegistro) { this.dkprFechaRegistro = dkprFechaRegistro; }

    public String getDescripcion() { return dkprDescripcion; }
    public void setDescripcion(String dkprDescripcion) { this.dkprDescripcion = dkprDescripcion; }

    public int getCantidadBultos() { return dkprCantidadBultos; }
    public void setCantidadBultos(int dkprCantidadBultos) { this.dkprCantidadBultos = dkprCantidadBultos; }

    public BigDecimal getPesoKg() { return dkprPesoKg; }
    public void setPesoKg(BigDecimal dkprPesoKg) { this.dkprPesoKg = dkprPesoKg; }

    public BigDecimal getValorDeclarado() { return dkprValorDeclarado; }
    public void setValorDeclarado(BigDecimal dkprValorDeclarado) { this.dkprValorDeclarado = dkprValorDeclarado; }

    public BigDecimal getCostoEnvio() { return dkprCostoEnvio; }
    public void setCostoEnvio(BigDecimal dkprCostoEnvio) { this.dkprCostoEnvio = dkprCostoEnvio; }

    public String getObservaciones() { return dkprObservaciones; }
    public void setObservaciones(String dkprObservaciones) { this.dkprObservaciones = dkprObservaciones; }

    public int getDiasEstimados() { return dkprDiasEstimados; }
    public void setDiasEstimados(int dkprDiasEstimados) { this.dkprDiasEstimados = dkprDiasEstimados; }

    public LocalDateTime getFechaUltimoMovimiento() { return dkprFechaUltimoMovimiento; }
    public void setFechaUltimoMovimiento(LocalDateTime dkprFechaUltimoMovimiento) { this.dkprFechaUltimoMovimiento = dkprFechaUltimoMovimiento; }

    public String getManifiesto() { return dkprManifiesto; }
    public void setManifiesto(String dkprManifiesto) { this.dkprManifiesto = dkprManifiesto; }

    /** Texto "Origen → Destino" para mostrar en tablas. */
    public String getRuta() {
        return dkprOrigen + " → " + dkprDestino;
    }

    public String getFechaTexto() {
        return dkprFechaRegistro == null ? "" : dkprFechaRegistro.format(FORMATO);
    }

    public String getUltimoMovimientoTexto() {
        return dkprFechaUltimoMovimiento == null ? "" : dkprFechaUltimoMovimiento.format(FORMATO);
    }

    // ------------------------------------------------- datos calculados (automáticos)

    /** Fecha de llegada prometida al cliente: registro + días estimados de la ruta. */
    public LocalDate getFechaEstimada() {
        return dkprFechaRegistro == null ? null : dkprFechaRegistro.toLocalDate().plusDays(dkprDiasEstimados);
    }

    public String getFechaEstimadaTexto() {
        LocalDate dkprF = getFechaEstimada();
        return dkprF == null ? "" : dkprF.format(FORMATO_DIA);
    }

    /** Días de retraso respecto a la llegada estimada (0 si va a tiempo o ya llegó a destino). */
    public long getDiasAtraso() {
        LocalDate dkprF = getFechaEstimada();
        if (dkprF == null || (dkprIdEstadoActual != Estados.REGISTRADO && dkprIdEstadoActual != Estados.EN_TRANSITO)) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(dkprF, LocalDate.now()));
    }

    /** Días que el envío lleva en su estado actual (desde el último movimiento). */
    public long getDiasEnEstado() {
        LocalDateTime dkprDesde = dkprFechaUltimoMovimiento != null ? dkprFechaUltimoMovimiento : dkprFechaRegistro;
        return dkprDesde == null ? 0 : ChronoUnit.DAYS.between(dkprDesde.toLocalDate(), LocalDate.now());
    }

    /** Aviso para las bandejas: atraso en el camino o demasiados días esperando en la agencia destino. */
    public String getAlerta() {
        long dkprAtraso = getDiasAtraso();
        if (dkprAtraso > 0) {
            return "ATRASADO " + dkprAtraso + (dkprAtraso == 1 ? " día" : " días");
        }
        if (dkprIdEstadoActual == Estados.EN_AGENCIA_DESTINO && getDiasEnEstado() > DIAS_ALMACEN_GRATIS) {
            return "Almacenaje: " + getDiasEnEstado() + " días sin recoger";
        }
        return "";
    }
}
