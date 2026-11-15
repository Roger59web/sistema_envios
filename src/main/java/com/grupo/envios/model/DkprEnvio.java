package com.grupo.envios.model;

import com.grupo.envios.util.DkprEstados;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class DkprEnvio {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
    private String dkprPlaca;
    private String dkprConductor;

    public int dkprGetIdEnvio() { return dkprIdEnvio; }
    public void dkprSetIdEnvio(int dkprIdEnvio) { this.dkprIdEnvio = dkprIdEnvio; }

    public String dkprGetCodigoSeguimiento() { return dkprCodigoSeguimiento; }
    public void dkprSetCodigoSeguimiento(String dkprCodigoSeguimiento) { this.dkprCodigoSeguimiento = dkprCodigoSeguimiento; }

    public int dkprGetIdRuta() { return dkprIdRuta; }
    public void dkprSetIdRuta(int dkprIdRuta) { this.dkprIdRuta = dkprIdRuta; }

    public int dkprGetIdAgenciaOrigen() { return dkprIdAgenciaOrigen; }
    public void dkprSetIdAgenciaOrigen(int dkprIdAgenciaOrigen) { this.dkprIdAgenciaOrigen = dkprIdAgenciaOrigen; }

    public int dkprGetIdAgenciaDestino() { return dkprIdAgenciaDestino; }
    public void dkprSetIdAgenciaDestino(int dkprIdAgenciaDestino) { this.dkprIdAgenciaDestino = dkprIdAgenciaDestino; }

    public String dkprGetOrigen() { return dkprOrigen; }
    public void dkprSetOrigen(String dkprOrigen) { this.dkprOrigen = dkprOrigen; }

    public String dkprGetDestino() { return dkprDestino; }
    public void dkprSetDestino(String dkprDestino) { this.dkprDestino = dkprDestino; }

    public String dkprGetOrigenDireccion() { return dkprOrigenDireccion; }
    public void dkprSetOrigenDireccion(String dkprOrigenDireccion) { this.dkprOrigenDireccion = dkprOrigenDireccion; }

    public String dkprGetOrigenTelefono() { return dkprOrigenTelefono; }
    public void dkprSetOrigenTelefono(String dkprOrigenTelefono) { this.dkprOrigenTelefono = dkprOrigenTelefono; }

    public int dkprGetIdRemitente() { return dkprIdRemitente; }
    public void dkprSetIdRemitente(int dkprIdRemitente) { this.dkprIdRemitente = dkprIdRemitente; }

    public String dkprGetRemitente() { return dkprRemitente; }
    public void dkprSetRemitente(String dkprRemitente) { this.dkprRemitente = dkprRemitente; }

    public String dkprGetRemitenteDocumento() { return dkprRemitenteDocumento; }
    public void dkprSetRemitenteDocumento(String dkprRemitenteDocumento) { this.dkprRemitenteDocumento = dkprRemitenteDocumento; }

    public String dkprGetRemitenteTelefono() { return dkprRemitenteTelefono; }
    public void dkprSetRemitenteTelefono(String dkprRemitenteTelefono) { this.dkprRemitenteTelefono = dkprRemitenteTelefono; }

    public int dkprGetIdDestinatario() { return dkprIdDestinatario; }
    public void dkprSetIdDestinatario(int dkprIdDestinatario) { this.dkprIdDestinatario = dkprIdDestinatario; }

    public String dkprGetDestinatario() { return dkprDestinatario; }
    public void dkprSetDestinatario(String dkprDestinatario) { this.dkprDestinatario = dkprDestinatario; }

    public String dkprGetDestinatarioDocumento() { return dkprDestinatarioDocumento; }
    public void dkprSetDestinatarioDocumento(String dkprDestinatarioDocumento) { this.dkprDestinatarioDocumento = dkprDestinatarioDocumento; }

    public String dkprGetDestinatarioTelefono() { return dkprDestinatarioTelefono; }
    public void dkprSetDestinatarioTelefono(String dkprDestinatarioTelefono) { this.dkprDestinatarioTelefono = dkprDestinatarioTelefono; }

    public int dkprGetIdEmpleadoRegistro() { return dkprIdEmpleadoRegistro; }
    public void dkprSetIdEmpleadoRegistro(int dkprIdEmpleadoRegistro) { this.dkprIdEmpleadoRegistro = dkprIdEmpleadoRegistro; }

    public String dkprGetEmpleadoRegistro() { return dkprEmpleadoRegistro; }
    public void dkprSetEmpleadoRegistro(String dkprEmpleadoRegistro) { this.dkprEmpleadoRegistro = dkprEmpleadoRegistro; }

    public int dkprGetIdEstadoActual() { return dkprIdEstadoActual; }
    public void dkprSetIdEstadoActual(int dkprIdEstadoActual) { this.dkprIdEstadoActual = dkprIdEstadoActual; }

    public String dkprGetEstado() { return dkprEstado; }
    public void dkprSetEstado(String dkprEstado) { this.dkprEstado = dkprEstado; }

    public LocalDateTime dkprGetFechaRegistro() { return dkprFechaRegistro; }
    public void dkprSetFechaRegistro(LocalDateTime dkprFechaRegistro) { this.dkprFechaRegistro = dkprFechaRegistro; }

    public String dkprGetDescripcion() { return dkprDescripcion; }
    public void dkprSetDescripcion(String dkprDescripcion) { this.dkprDescripcion = dkprDescripcion; }

    public int dkprGetCantidadBultos() { return dkprCantidadBultos; }
    public void dkprSetCantidadBultos(int dkprCantidadBultos) { this.dkprCantidadBultos = dkprCantidadBultos; }

    public BigDecimal dkprGetPesoKg() { return dkprPesoKg; }
    public void dkprSetPesoKg(BigDecimal dkprPesoKg) { this.dkprPesoKg = dkprPesoKg; }

    public BigDecimal dkprGetValorDeclarado() { return dkprValorDeclarado; }
    public void dkprSetValorDeclarado(BigDecimal dkprValorDeclarado) { this.dkprValorDeclarado = dkprValorDeclarado; }

    public BigDecimal dkprGetCostoEnvio() { return dkprCostoEnvio; }
    public void dkprSetCostoEnvio(BigDecimal dkprCostoEnvio) { this.dkprCostoEnvio = dkprCostoEnvio; }

    public String dkprGetObservaciones() { return dkprObservaciones; }
    public void dkprSetObservaciones(String dkprObservaciones) { this.dkprObservaciones = dkprObservaciones; }

    public int dkprGetDiasEstimados() { return dkprDiasEstimados; }
    public void dkprSetDiasEstimados(int dkprDiasEstimados) { this.dkprDiasEstimados = dkprDiasEstimados; }

    public LocalDateTime dkprGetFechaUltimoMovimiento() { return dkprFechaUltimoMovimiento; }
    public void dkprSetFechaUltimoMovimiento(LocalDateTime dkprFechaUltimoMovimiento) { this.dkprFechaUltimoMovimiento = dkprFechaUltimoMovimiento; }

    public String dkprGetManifiesto() { return dkprManifiesto; }
    public void dkprSetManifiesto(String dkprManifiesto) { this.dkprManifiesto = dkprManifiesto; }

    public String dkprGetPlaca() { return dkprPlaca; }
    public void dkprSetPlaca(String dkprPlaca) { this.dkprPlaca = dkprPlaca; }

    public String dkprGetConductor() { return dkprConductor; }
    public void dkprSetConductor(String dkprConductor) { this.dkprConductor = dkprConductor; }

    public String dkprGetVehiculoTexto() {
        if (dkprManifiesto == null) {
            return "-";
        }
        return (dkprPlaca == null || dkprPlaca.isBlank() ? "Sin placa" : dkprPlaca) + " (" + dkprManifiesto + ")";
    }

    public String dkprGetRuta() {
        return dkprOrigen + " → " + dkprDestino;
    }

    public String dkprGetFechaTexto() {
        return dkprFechaRegistro == null ? "" : dkprFechaRegistro.format(FORMATO);
    }

    public String dkprGetUltimoMovimientoTexto() {
        return dkprFechaUltimoMovimiento == null ? "" : dkprFechaUltimoMovimiento.format(FORMATO);
    }

    public LocalDate dkprGetFechaEstimada() {
        return dkprFechaRegistro == null ? null : dkprFechaRegistro.toLocalDate().plusDays(dkprDiasEstimados);
    }

    public String dkprGetFechaEstimadaTexto() {
        LocalDate dkprF = dkprGetFechaEstimada();
        return dkprF == null ? "" : dkprF.format(FORMATO_DIA);
    }

    public long dkprGetDiasAtraso() {
        LocalDate dkprF = dkprGetFechaEstimada();
        if (dkprF == null || (dkprIdEstadoActual != DkprEstados.REGISTRADO && dkprIdEstadoActual != DkprEstados.EN_TRANSITO)) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(dkprF, LocalDate.now()));
    }

    public long dkprGetDiasEnEstado() {
        LocalDateTime dkprDesde = dkprFechaUltimoMovimiento != null ? dkprFechaUltimoMovimiento : dkprFechaRegistro;
        return dkprDesde == null ? 0 : ChronoUnit.DAYS.between(dkprDesde.toLocalDate(), LocalDate.now());
    }

    public String dkprGetAlerta() {
        long dkprAtraso = dkprGetDiasAtraso();
        if (dkprAtraso > 0) {
            return "ATRASADO " + dkprAtraso + (dkprAtraso == 1 ? " día" : " días");
        }
        if (dkprIdEstadoActual == DkprEstados.EN_AGENCIA_DESTINO && dkprGetDiasEnEstado() > DIAS_ALMACEN_GRATIS) {
            return "Almacenaje: " + dkprGetDiasEnEstado() + " días sin recoger";
        }
        return "";
    }
}
