package com.grupo.envios.service;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.dao.DkprComprobanteDAO;
import com.grupo.envios.dao.DkprEntregaDAO;
import com.grupo.envios.dao.DkprEnvioDAO;
import com.grupo.envios.dao.DkprHistorialDAO;
import com.grupo.envios.dao.DkprManifiestoDAO;
import com.grupo.envios.model.DkprCliente;
import com.grupo.envios.model.DkprComprobante;
import com.grupo.envios.model.DkprConductor;
import com.grupo.envios.model.DkprVehiculo;
import com.grupo.envios.model.DkprDetalleEnvio;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.model.DkprHistorialEnvio;
import com.grupo.envios.model.DkprResultadoRegistro;
import com.grupo.envios.model.DkprRuta;
import com.grupo.envios.model.DkprSolicitudEnvio;
import com.grupo.envios.util.DkprCodigosBarras;
import com.grupo.envios.util.DkprEstados;
import com.grupo.envios.util.DkprNegocioException;
import com.grupo.envios.util.DkprRoles;
import com.grupo.envios.util.DkprValidaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DkprEnvioService {

    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");
    private static final Set<String> TIPOS_COMPROBANTE = Set.of("BOLETA", "FACTURA");
    private static final Set<String> METODOS_PAGO = Set.of("EFECTIVO", "TARJETA", "YAPE", "PLIN");

    private final DkprEnvioDAO dkprEnvioDAO = new DkprEnvioDAO();
    private final DkprHistorialDAO dkprHistorialDAO = new DkprHistorialDAO();
    private final DkprComprobanteDAO dkprComprobanteDAO = new DkprComprobanteDAO();
    private final DkprEntregaDAO dkprEntregaDAO = new DkprEntregaDAO();
    private final DkprManifiestoDAO dkprManifiestoDAO = new DkprManifiestoDAO();
    private final DkprClienteService dkprClienteService = new DkprClienteService();
    private final DkprVehiculoService dkprVehiculoService = new DkprVehiculoService();

    public BigDecimal dkprCalcularCosto(DkprRuta dkprRuta, int dkprBultos, BigDecimal dkprPesoKg) {
        if (dkprRuta == null) {
            throw new DkprNegocioException("Selecciona la ruta de destino.");
        }
        if (dkprRuta.dkprGetPrecioBase() == null || dkprRuta.dkprGetPrecioKgExtra() == null) {
            throw new DkprNegocioException("La ruta seleccionada no tiene una tarifa vigente.");
        }
        if (dkprBultos < 1 || dkprBultos > 999) {
            throw new DkprNegocioException("La cantidad de bultos debe estar entre 1 y 999.");
        }
        if (dkprPesoKg == null || dkprPesoKg.signum() <= 0) {
            throw new DkprNegocioException("El peso debe ser mayor que 0.");
        }
        BigDecimal dkprCantidad = BigDecimal.valueOf(dkprBultos);
        if (dkprRuta.dkprGetPesoMaxKg() != null && dkprPesoKg.compareTo(dkprRuta.dkprGetPesoMaxKg().multiply(dkprCantidad)) > 0) {
            throw new DkprNegocioException("El peso máximo para esta ruta es " + dkprRuta.dkprGetPesoMaxKg() + " kg por bulto.");
        }
        // Cada bulto paga el precio base (que incluye 1 kg); se cobra aparte cada kg que pase de eso.
        BigDecimal dkprKgExtra = dkprPesoKg.subtract(dkprCantidad).max(BigDecimal.ZERO).setScale(0, RoundingMode.CEILING);
        return dkprRuta.dkprGetPrecioBase().multiply(dkprCantidad)
                .add(dkprRuta.dkprGetPrecioKgExtra().multiply(dkprKgExtra)).setScale(2, RoundingMode.HALF_UP);
    }

    public DkprResultadoRegistro dkprRegistrar(DkprSolicitudEnvio dkprS, DkprEmpleado dkprEmpleado) throws SQLException {
        if (!dkprTieneRol(dkprEmpleado, DkprRoles.ADMINISTRADOR, DkprRoles.CAJERO)) {
            throw new DkprNegocioException("Tu rol no permite registrar envíos.");
        }
        if (dkprS.dkprRuta() == null) {
            throw new DkprNegocioException("Selecciona la ruta de destino.");
        }

        DkprValidaciones.dkprValidarDocumento(dkprS.dkprRemitenteTipo(), dkprS.dkprRemitenteDocumento());
        DkprValidaciones.dkprValidarDocumento(dkprS.dkprDestinatarioTipo(), dkprS.dkprDestinatarioDocumento());
        if (dkprS.dkprRemitenteTipo().equals(dkprS.dkprDestinatarioTipo())
                && dkprS.dkprRemitenteDocumento().trim().equals(dkprS.dkprDestinatarioDocumento().trim())) {
            throw new DkprNegocioException("El remitente y el destinatario no pueden ser la misma persona.");
        }

        String dkprDescripcion = DkprValidaciones.dkprRequerido(dkprS.dkprDescripcion(), "Descripción", 200);
        String dkprObservaciones = DkprValidaciones.dkprOpcional(dkprS.dkprObservaciones(), "Observaciones", 200);
        if (dkprS.dkprCantidadBultos() < 1 || dkprS.dkprCantidadBultos() > 999) {
            throw new DkprNegocioException("La cantidad de bultos debe estar entre 1 y 999.");
        }
        BigDecimal dkprValor = dkprS.dkprValorDeclarado() == null ? BigDecimal.ZERO : dkprS.dkprValorDeclarado();
        if (dkprValor.signum() < 0 || dkprValor.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new DkprNegocioException("El valor declarado no es válido.");
        }
        if (dkprS.dkprTipoComprobante() == null || !TIPOS_COMPROBANTE.contains(dkprS.dkprTipoComprobante())) {
            throw new DkprNegocioException("Selecciona el tipo de comprobante.");
        }
        if (dkprS.dkprMetodoPago() == null || !METODOS_PAGO.contains(dkprS.dkprMetodoPago())) {
            throw new DkprNegocioException("Selecciona el método de pago.");
        }
        if ("FACTURA".equals(dkprS.dkprTipoComprobante()) && !"RUC".equals(dkprS.dkprRemitenteTipo())) {
            throw new DkprNegocioException("Para emitir factura, el remitente debe tener RUC.");
        }

        BigDecimal dkprCosto = dkprCalcularCosto(dkprS.dkprRuta(), dkprS.dkprCantidadBultos(), dkprS.dkprPesoKg());

        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprCn.setAutoCommit(false);
            try {
                DkprCliente dkprRemitente = dkprClienteService.dkprObtenerOCrear(dkprCn, dkprS.dkprRemitenteTipo(), dkprS.dkprRemitenteDocumento(),
                        dkprS.dkprRemitenteNombre(), dkprS.dkprRemitenteTelefono(), "remitente");
                DkprCliente dkprDestinatario = dkprClienteService.dkprObtenerOCrear(dkprCn, dkprS.dkprDestinatarioTipo(),
                        dkprS.dkprDestinatarioDocumento(), dkprS.dkprDestinatarioNombre(), dkprS.dkprDestinatarioTelefono(), "destinatario");

                int dkprIdEnvio = dkprEnvioDAO.dkprSiguienteId(dkprCn);
                String dkprCodigo = String.format("ENV-%06d", dkprIdEnvio);

                DkprEnvio dkprEnvio = new DkprEnvio();
                dkprEnvio.dkprSetIdEnvio(dkprIdEnvio);
                dkprEnvio.dkprSetCodigoSeguimiento(dkprCodigo);
                dkprEnvio.dkprSetIdRuta(dkprS.dkprRuta().dkprGetIdRuta());
                dkprEnvio.dkprSetIdRemitente(dkprRemitente.dkprGetIdCliente());
                dkprEnvio.dkprSetIdDestinatario(dkprDestinatario.dkprGetIdCliente());
                dkprEnvio.dkprSetIdEmpleadoRegistro(dkprEmpleado.dkprGetIdEmpleado());
                dkprEnvio.dkprSetIdEstadoActual(DkprEstados.REGISTRADO);
                dkprEnvio.dkprSetDescripcion(dkprDescripcion);
                dkprEnvio.dkprSetCantidadBultos(dkprS.dkprCantidadBultos());
                dkprEnvio.dkprSetPesoKg(dkprS.dkprPesoKg().setScale(2, RoundingMode.HALF_UP));
                dkprEnvio.dkprSetValorDeclarado(dkprValor.setScale(2, RoundingMode.HALF_UP));
                dkprEnvio.dkprSetCostoEnvio(dkprCosto);
                dkprEnvio.dkprSetObservaciones(dkprObservaciones);
                dkprEnvioDAO.dkprInsertar(dkprCn, dkprEnvio);

                dkprHistorialDAO.dkprInsertar(dkprCn, dkprIdEnvio, DkprEstados.REGISTRADO, dkprEmpleado.dkprGetIdAgencia(),
                        dkprEmpleado.dkprGetIdEmpleado(), "Envío registrado en ventanilla");

                BigDecimal dkprSubtotal = dkprCosto.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
                DkprComprobante dkprComprobante = new DkprComprobante();
                dkprComprobante.dkprSetIdEnvio(dkprIdEnvio);
                dkprComprobante.dkprSetTipo(dkprS.dkprTipoComprobante());
                dkprComprobante.dkprSetSerie("BOLETA".equals(dkprS.dkprTipoComprobante()) ? "B001" : "F001");
                dkprComprobante.dkprSetSubtotal(dkprSubtotal);
                dkprComprobante.dkprSetIgv(dkprCosto.subtract(dkprSubtotal));
                dkprComprobante.dkprSetTotal(dkprCosto);
                dkprComprobante.dkprSetMetodoPago(dkprS.dkprMetodoPago());
                dkprComprobanteDAO.dkprInsertar(dkprCn, dkprComprobante);

                dkprCn.commit();
                return new DkprResultadoRegistro(dkprCodigo, dkprComprobante.dkprGetNumeroTexto(), dkprCosto);
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    public DkprEnvio dkprBuscar(String dkprCodigo) throws SQLException {
        String dkprC = DkprCodigosBarras.dkprNormalizarEnvio(DkprValidaciones.dkprRequerido(dkprCodigo, "Código de seguimiento", 60));
        DkprEnvio dkprEnvio = dkprEnvioDAO.dkprBuscarPorCodigo(dkprC);
        if (dkprEnvio == null) {
            throw new DkprNegocioException("No se encontró ningún envío con el código " + dkprC + ".");
        }
        return dkprEnvio;
    }

    public List<DkprHistorialEnvio> dkprHistorial(int dkprIdEnvio) throws SQLException {
        return dkprHistorialDAO.dkprListarPorEnvio(dkprIdEnvio);
    }

    public DkprDetalleEnvio dkprDetalle(String dkprCodigo) throws SQLException {
        DkprEnvio dkprEnvio = dkprBuscar(dkprCodigo);
        return new DkprDetalleEnvio(dkprEnvio, dkprComprobanteDAO.dkprBuscarPorEnvio(dkprEnvio.dkprGetIdEnvio()),
                dkprHistorialDAO.dkprListarPorEnvio(dkprEnvio.dkprGetIdEnvio()));
    }

    public List<Integer> dkprSiguientesEstados(int dkprEstadoActual) {
        return switch (dkprEstadoActual) {
            case DkprEstados.REGISTRADO -> List.of(DkprEstados.EN_TRANSITO, DkprEstados.ANULADO);
            case DkprEstados.EN_TRANSITO -> List.of(DkprEstados.EN_AGENCIA_DESTINO);
            default -> List.of();
        };
    }

    public boolean dkprPuedeCambiarEstado(DkprEmpleado dkprEmpleado) {
        return dkprTieneRol(dkprEmpleado, DkprRoles.ADMINISTRADOR, DkprRoles.ALMACEN, DkprRoles.CAJERO);
    }

    public List<Integer> dkprEstadosDisponibles(DkprEnvio dkprEnvio, DkprEmpleado dkprEmpleado) {
        List<Integer> dkprDisponibles = new ArrayList<>();
        for (int dkprEstado : dkprSiguientesEstados(dkprEnvio.dkprGetIdEstadoActual())) {
            if (dkprPuedeCambiarA(dkprEstado, dkprEnvio, dkprEmpleado)) {
                dkprDisponibles.add(dkprEstado);
            }
        }
        return dkprDisponibles;
    }

    private boolean dkprPuedeCambiarA(int dkprNuevoEstado, DkprEnvio dkprEnvio, DkprEmpleado dkprEmpleado) {
        boolean dkprAdmin = dkprEsAdmin(dkprEmpleado);
        if (dkprNuevoEstado == DkprEstados.ANULADO) {
            return dkprTieneRol(dkprEmpleado, DkprRoles.ADMINISTRADOR, DkprRoles.CAJERO)
                    && (dkprAdmin || dkprEmpleado.dkprGetIdAgencia() == dkprEnvio.dkprGetIdAgenciaOrigen());
        }
        if (dkprNuevoEstado == DkprEstados.EN_TRANSITO) {
            return dkprTieneRol(dkprEmpleado, DkprRoles.ADMINISTRADOR, DkprRoles.ALMACEN)
                    && (dkprAdmin || dkprEmpleado.dkprGetIdAgencia() == dkprEnvio.dkprGetIdAgenciaOrigen());
        }
        if (dkprNuevoEstado == DkprEstados.EN_AGENCIA_DESTINO) {
            return dkprTieneRol(dkprEmpleado, DkprRoles.ADMINISTRADOR, DkprRoles.ALMACEN)
                    && (dkprAdmin || dkprEmpleado.dkprGetIdAgencia() == dkprEnvio.dkprGetIdAgenciaDestino());
        }
        return false;
    }

    private String dkprMensajeSinPermiso(int dkprNuevoEstado, DkprEnvio dkprEnvio) {
        if (dkprNuevoEstado == DkprEstados.ANULADO) {
            return "Solo el cajero de la agencia de origen (" + dkprEnvio.dkprGetOrigen() + ") o el administrador puede anular el envío.";
        }
        if (dkprNuevoEstado == DkprEstados.EN_TRANSITO) {
            return "Solo el personal de almacén de la agencia de origen (" + dkprEnvio.dkprGetOrigen() + ") puede despachar el envío.";
        }
        return "Solo el personal de almacén de la agencia destino (" + dkprEnvio.dkprGetDestino() + ") puede confirmar la llegada del envío.";
    }

    public boolean dkprPuedeEntregar(DkprEmpleado dkprEmpleado) {
        return dkprTieneRol(dkprEmpleado, DkprRoles.ADMINISTRADOR, DkprRoles.CAJERO);
    }

    public void dkprCambiarEstado(DkprEnvio dkprEnvio, int dkprNuevoEstado, String dkprObservacion, DkprEmpleado dkprEmpleado)
            throws SQLException {
        DkprEnvio dkprActual = dkprBuscar(dkprEnvio.dkprGetCodigoSeguimiento());
        if (!dkprSiguientesEstados(dkprActual.dkprGetIdEstadoActual()).contains(dkprNuevoEstado)) {
            throw new DkprNegocioException("No se puede pasar de " + dkprActual.dkprGetEstado()
                    + " a " + DkprEstados.dkprNombre(dkprNuevoEstado) + ".");
        }
        if (!dkprPuedeCambiarA(dkprNuevoEstado, dkprActual, dkprEmpleado)) {
            throw new DkprNegocioException(dkprMensajeSinPermiso(dkprNuevoEstado, dkprActual));
        }
        String dkprMotivo = DkprValidaciones.dkprOpcional(dkprObservacion, "Observación", 200);
        if (dkprNuevoEstado == DkprEstados.ANULADO && dkprMotivo == null) {
            throw new DkprNegocioException("Indica el motivo de la anulación en la observación.");
        }

        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprCn.setAutoCommit(false);
            try {
                dkprMoverEstado(dkprCn, dkprActual, dkprNuevoEstado);
                dkprHistorialDAO.dkprInsertar(dkprCn, dkprActual.dkprGetIdEnvio(), dkprNuevoEstado, dkprAgenciaDelMovimiento(dkprNuevoEstado, dkprActual),
                        dkprEmpleado.dkprGetIdEmpleado(), dkprMotivo);
                dkprCn.commit();
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    public String dkprAvisoSoloConsulta(DkprEnvio dkprEnvio, DkprEmpleado dkprEmpleado) {
        return switch (dkprEnvio.dkprGetIdEstadoActual()) {
            case DkprEstados.REGISTRADO -> dkprPuedeCambiarA(DkprEstados.EN_TRANSITO, dkprEnvio, dkprEmpleado) ? ""
                    : "Solo consulta. El despacho lo realiza el personal de almacén de " + dkprEnvio.dkprGetOrigen() + ".";
            case DkprEstados.EN_TRANSITO -> dkprPuedeCambiarA(DkprEstados.EN_AGENCIA_DESTINO, dkprEnvio, dkprEmpleado) ? ""
                    : "Solo consulta. La llegada la confirma el personal de almacén de " + dkprEnvio.dkprGetDestino() + ".";
            case DkprEstados.EN_AGENCIA_DESTINO -> dkprPuedeEntregarEn(dkprEmpleado, dkprEnvio.dkprGetIdAgenciaDestino())
                    ? "Listo para entregar: regístralo en Entregas o en la Bandeja de agencia."
                    : "Solo consulta. La entrega la registra el cajero de " + dkprEnvio.dkprGetDestino() + ".";
            default -> "";
        };
    }

    public List<DkprEnvio> dkprPorDespachar(int dkprIdAgencia) throws SQLException {
        return dkprEnvioDAO.dkprListarPorEstadoYAgencia(DkprEstados.REGISTRADO, dkprIdAgencia, true);
    }

    public List<DkprEnvio> dkprPorRecibir(int dkprIdAgencia) throws SQLException {
        return dkprEnvioDAO.dkprListarPorEstadoYAgencia(DkprEstados.EN_TRANSITO, dkprIdAgencia, false);
    }

    public List<DkprEnvio> dkprPorEntregar(int dkprIdAgencia) throws SQLException {
        return dkprEnvioDAO.dkprListarPorEstadoYAgencia(DkprEstados.EN_AGENCIA_DESTINO, dkprIdAgencia, false);
    }

    public boolean dkprPuedeOperarAlmacen(DkprEmpleado dkprEmpleado, int dkprIdAgencia) {
        return dkprTieneRol(dkprEmpleado, DkprRoles.ADMINISTRADOR, DkprRoles.ALMACEN)
                && (dkprEsAdmin(dkprEmpleado) || dkprEmpleado.dkprGetIdAgencia() == dkprIdAgencia);
    }

    public boolean dkprPuedeEntregarEn(DkprEmpleado dkprEmpleado, int dkprIdAgencia) {
        return dkprPuedeEntregar(dkprEmpleado) && (dkprEsAdmin(dkprEmpleado) || dkprEmpleado.dkprGetIdAgencia() == dkprIdAgencia);
    }

    public String dkprDespachar(List<DkprEnvio> dkprEnvios, Integer dkprIdVehiculo, Integer dkprIdConductor, String dkprObservacion,
                            DkprEmpleado dkprEmpleado) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprCn.setAutoCommit(false);
            try {
                int dkprIdManifiesto = dkprDespacharEn(dkprCn, dkprEnvios, dkprIdVehiculo, dkprIdConductor, dkprObservacion, dkprEmpleado);
                dkprCn.commit();
                return DkprManifiestoDAO.dkprCodigo(dkprIdManifiesto);
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    public int dkprDespacharEn(Connection dkprCn, List<DkprEnvio> dkprEnvios, Integer dkprIdVehiculo, Integer dkprIdConductor,
                           String dkprObservacion, DkprEmpleado dkprEmpleado) throws SQLException {
        if (dkprEnvios == null || dkprEnvios.isEmpty()) {
            throw new DkprNegocioException("Marca al menos un envío para despachar.");
        }
        DkprEnvio dkprPrimero = dkprEnvios.get(0);
        if (!dkprPuedeCambiarA(DkprEstados.EN_TRANSITO, dkprPrimero, dkprEmpleado)) {
            throw new DkprNegocioException(dkprMensajeSinPermiso(DkprEstados.EN_TRANSITO, dkprPrimero));
        }
        for (DkprEnvio dkprE : dkprEnvios) {
            if (dkprE.dkprGetIdRuta() != dkprPrimero.dkprGetIdRuta()) {
                throw new DkprNegocioException("Un manifiesto agrupa envíos de un mismo destino: "
                        + dkprPrimero.dkprGetCodigoSeguimiento() + " va a " + dkprPrimero.dkprGetDestino() + " y "
                        + dkprE.dkprGetCodigoSeguimiento() + " a " + dkprE.dkprGetDestino() + ". Filtra por destino.");
            }
        }
        DkprVehiculo dkprVehiculo = dkprVehiculoService.dkprVehiculoParaDespacho(dkprIdVehiculo);
        DkprConductor dkprConductor = dkprVehiculoService.dkprConductorParaDespacho(dkprIdConductor);
        String dkprObs = DkprValidaciones.dkprOpcional(dkprObservacion, "Observación", 200);

        int dkprIdManifiesto = dkprManifiestoDAO.dkprInsertar(dkprCn, dkprPrimero.dkprGetIdRuta(), dkprEmpleado.dkprGetIdEmpleado(),
                dkprVehiculo, dkprConductor, dkprObs);
        String dkprCodigo = DkprManifiestoDAO.dkprCodigo(dkprIdManifiesto);
        String dkprDetalle = "Despachado en " + dkprCodigo + " (placa " + dkprVehiculo.dkprGetPlaca()
                + ", conductor " + dkprConductor.dkprGetNombreCompleto() + ")";
        for (DkprEnvio dkprE : dkprEnvios) {
            if (!dkprEnvioDAO.dkprActualizarEstadoSi(dkprCn, dkprE.dkprGetIdEnvio(), DkprEstados.REGISTRADO,
                    DkprEstados.EN_TRANSITO, dkprIdManifiesto)) {
                throw new DkprNegocioException(dkprE.dkprGetCodigoSeguimiento()
                        + " ya no está pendiente de despacho. Actualiza la bandeja.");
            }
            dkprHistorialDAO.dkprInsertar(dkprCn, dkprE.dkprGetIdEnvio(), DkprEstados.EN_TRANSITO, dkprE.dkprGetIdAgenciaOrigen(),
                    dkprEmpleado.dkprGetIdEmpleado(), dkprDetalle);
        }
        return dkprIdManifiesto;
    }

    public void dkprRecibir(List<DkprEnvio> dkprEnvios, String dkprObservacion, DkprEmpleado dkprEmpleado) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprCn.setAutoCommit(false);
            try {
                dkprRecibirEn(dkprCn, dkprEnvios, dkprObservacion, dkprEmpleado);
                dkprCn.commit();
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    public void dkprRecibirEn(Connection dkprCn, List<DkprEnvio> dkprEnvios, String dkprObservacion, DkprEmpleado dkprEmpleado)
            throws SQLException {
        if (dkprEnvios == null || dkprEnvios.isEmpty()) {
            throw new DkprNegocioException("Marca al menos un envío que haya llegado.");
        }
        for (DkprEnvio dkprE : dkprEnvios) {
            if (!dkprPuedeCambiarA(DkprEstados.EN_AGENCIA_DESTINO, dkprE, dkprEmpleado)) {
                throw new DkprNegocioException(dkprMensajeSinPermiso(DkprEstados.EN_AGENCIA_DESTINO, dkprE));
            }
        }
        String dkprObs = DkprValidaciones.dkprOpcional(dkprObservacion, "Observación", 200);

        for (DkprEnvio dkprE : dkprEnvios) {
            if (!dkprEnvioDAO.dkprActualizarEstadoSi(dkprCn, dkprE.dkprGetIdEnvio(), DkprEstados.EN_TRANSITO,
                    DkprEstados.EN_AGENCIA_DESTINO, null)) {
                throw new DkprNegocioException(dkprE.dkprGetCodigoSeguimiento()
                        + " ya no está en tránsito. Actualiza la bandeja.");
            }
            String dkprDetalle = dkprObs != null ? dkprObs : "Llegó a agencia destino"
                    + (dkprE.dkprGetManifiesto() == null ? "" : " (" + dkprE.dkprGetManifiesto() + ")");
            dkprHistorialDAO.dkprInsertar(dkprCn, dkprE.dkprGetIdEnvio(), DkprEstados.EN_AGENCIA_DESTINO, dkprE.dkprGetIdAgenciaDestino(),
                    dkprEmpleado.dkprGetIdEmpleado(), dkprDetalle);
        }
    }

    public void dkprRegistrarEntrega(String dkprCodigo, String dkprNombreReceptor, String dkprDniReceptor,
                                 String dkprObservacion, DkprEmpleado dkprEmpleado) throws SQLException {
        if (!dkprPuedeEntregar(dkprEmpleado)) {
            throw new DkprNegocioException("Tu rol no permite registrar entregas.");
        }
        DkprEnvio dkprActual = dkprBuscar(dkprCodigo);
        if (dkprActual.dkprGetIdEstadoActual() != DkprEstados.EN_AGENCIA_DESTINO) {
            throw new DkprNegocioException("Solo se pueden entregar envíos en estado EN AGENCIA DESTINO. "
                    + "Estado actual: " + dkprActual.dkprGetEstado() + ".");
        }
        if (!dkprEsAdmin(dkprEmpleado) && dkprEmpleado.dkprGetIdAgencia() != dkprActual.dkprGetIdAgenciaDestino()) {
            throw new DkprNegocioException("Este envío se entrega en la agencia " + dkprActual.dkprGetDestino() + ".");
        }

        String dkprNombre = DkprValidaciones.dkprRequerido(dkprNombreReceptor, "Nombre de quien recoge", 150);
        DkprValidaciones.dkprValidarDni(dkprDniReceptor, "El DNI de quien recoge");
        String dkprDni = dkprDniReceptor.trim();
        String dkprObs = DkprValidaciones.dkprOpcional(dkprObservacion, "Observación", 200);
        boolean dkprTercero = !dkprDni.equals(dkprActual.dkprGetDestinatarioDocumento());
        if (dkprTercero && dkprObs == null) {
            throw new DkprNegocioException("Quien recoge no es el destinatario registrado. Indica en la observación "
                    + "su vínculo (familiar, trabajador de la empresa, carta poder...).");
        }

        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprCn.setAutoCommit(false);
            try {
                dkprMoverEstado(dkprCn, dkprActual, DkprEstados.ENTREGADO);
                dkprEntregaDAO.dkprInsertar(dkprCn, dkprActual.dkprGetIdEnvio(), dkprEmpleado.dkprGetIdEmpleado(), dkprNombre, dkprDni, dkprObs);
                dkprHistorialDAO.dkprInsertar(dkprCn, dkprActual.dkprGetIdEnvio(), DkprEstados.ENTREGADO, dkprActual.dkprGetIdAgenciaDestino(),
                        dkprEmpleado.dkprGetIdEmpleado(), "Entregado a " + dkprNombre + " (DNI " + dkprDni + ")"
                                + (dkprTercero ? " - recoge un tercero: " + dkprObs : ""));
                dkprCn.commit();
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    private void dkprMoverEstado(Connection dkprCn, DkprEnvio dkprEnvio, int dkprNuevoEstado) throws SQLException {
        if (!dkprEnvioDAO.dkprActualizarEstadoSi(dkprCn, dkprEnvio.dkprGetIdEnvio(), dkprEnvio.dkprGetIdEstadoActual(), dkprNuevoEstado, null)) {
            throw new DkprNegocioException("El envío " + dkprEnvio.dkprGetCodigoSeguimiento()
                    + " acaba de cambiar de estado. Vuelve a buscarlo.");
        }
    }

    private int dkprAgenciaDelMovimiento(int dkprNuevoEstado, DkprEnvio dkprEnvio) {
        return dkprNuevoEstado == DkprEstados.EN_AGENCIA_DESTINO || dkprNuevoEstado == DkprEstados.ENTREGADO
                ? dkprEnvio.dkprGetIdAgenciaDestino() : dkprEnvio.dkprGetIdAgenciaOrigen();
    }

    private boolean dkprEsAdmin(DkprEmpleado dkprE) {
        return dkprE != null && DkprRoles.ADMINISTRADOR.equals(dkprE.dkprGetRol());
    }

    private boolean dkprTieneRol(DkprEmpleado dkprE, String... dkprRoles) {
        if (dkprE == null) {
            return false;
        }
        for (String dkprRol : dkprRoles) {
            if (dkprRol.equals(dkprE.dkprGetRol())) {
                return true;
            }
        }
        return false;
    }
}
