package com.grupo.envios.service;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.dao.ComprobanteDAO;
import com.grupo.envios.dao.EntregaDAO;
import com.grupo.envios.dao.EnvioDAO;
import com.grupo.envios.dao.HistorialDAO;
import com.grupo.envios.dao.ManifiestoDAO;
import com.grupo.envios.model.Cliente;
import com.grupo.envios.model.Comprobante;
import com.grupo.envios.model.DetalleEnvio;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.model.HistorialEnvio;
import com.grupo.envios.model.ResultadoRegistro;
import com.grupo.envios.model.Ruta;
import com.grupo.envios.model.SolicitudEnvio;
import com.grupo.envios.util.Estados;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Roles;
import com.grupo.envios.util.Validaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Reglas de negocio de los envíos: tarifa, registro, cambios de estado, despacho en lote y entrega. */
public class EnvioService {

    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");
    private static final Set<String> TIPOS_COMPROBANTE = Set.of("BOLETA", "FACTURA");
    private static final Set<String> METODOS_PAGO = Set.of("EFECTIVO", "TARJETA", "YAPE", "PLIN");

    private final EnvioDAO dkprEnvioDAO = new EnvioDAO();
    private final HistorialDAO dkprHistorialDAO = new HistorialDAO();
    private final ComprobanteDAO dkprComprobanteDAO = new ComprobanteDAO();
    private final EntregaDAO dkprEntregaDAO = new EntregaDAO();
    private final ManifiestoDAO dkprManifiestoDAO = new ManifiestoDAO();
    private final ClienteService dkprClienteService = new ClienteService();

    // ------------------------------------------------------------------ tarifa

    /**
     * Costo = precio base (hasta 1 kg) + precio por cada kg adicional (se redondea hacia arriba).
     * Ejemplo con base 15.00 y extra 1.50: 3.2 kg → 15.00 + 3 × 1.50 = 19.50.
     */
    public BigDecimal calcularCosto(Ruta dkprRuta, BigDecimal dkprPesoKg) {
        if (dkprRuta == null) {
            throw new NegocioException("Selecciona la ruta de destino.");
        }
        if (dkprRuta.getPrecioBase() == null || dkprRuta.getPrecioKgExtra() == null) {
            throw new NegocioException("La ruta seleccionada no tiene una tarifa vigente.");
        }
        if (dkprPesoKg == null || dkprPesoKg.signum() <= 0) {
            throw new NegocioException("El peso debe ser mayor que 0.");
        }
        if (dkprRuta.getPesoMaxKg() != null && dkprPesoKg.compareTo(dkprRuta.getPesoMaxKg()) > 0) {
            throw new NegocioException("El peso máximo para esta ruta es " + dkprRuta.getPesoMaxKg() + " kg.");
        }
        BigDecimal dkprKgExtra = dkprPesoKg.subtract(BigDecimal.ONE).max(BigDecimal.ZERO).setScale(0, RoundingMode.CEILING);
        return dkprRuta.getPrecioBase().add(dkprRuta.getPrecioKgExtra().multiply(dkprKgExtra)).setScale(2, RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------------- registro

    /** Registra el envío, su primer movimiento y el comprobante en una sola transacción. */
    public ResultadoRegistro registrar(SolicitudEnvio dkprS, Empleado dkprEmpleado) throws SQLException {
        if (!tieneRol(dkprEmpleado, Roles.ADMINISTRADOR, Roles.CAJERO)) {
            throw new NegocioException("Tu rol no permite registrar envíos.");
        }
        if (dkprS.ruta() == null) {
            throw new NegocioException("Selecciona la ruta de destino.");
        }

        Validaciones.validarDocumento(dkprS.remitenteTipo(), dkprS.remitenteDocumento());
        Validaciones.validarDocumento(dkprS.destinatarioTipo(), dkprS.destinatarioDocumento());
        if (dkprS.remitenteTipo().equals(dkprS.destinatarioTipo())
                && dkprS.remitenteDocumento().trim().equals(dkprS.destinatarioDocumento().trim())) {
            throw new NegocioException("El remitente y el destinatario no pueden ser la misma persona.");
        }

        String dkprDescripcion = Validaciones.requerido(dkprS.descripcion(), "Descripción", 200);
        String dkprObservaciones = Validaciones.opcional(dkprS.observaciones(), "Observaciones", 200);
        if (dkprS.cantidadBultos() < 1 || dkprS.cantidadBultos() > 999) {
            throw new NegocioException("La cantidad de bultos debe estar entre 1 y 999.");
        }
        BigDecimal dkprValor = dkprS.valorDeclarado() == null ? BigDecimal.ZERO : dkprS.valorDeclarado();
        if (dkprValor.signum() < 0 || dkprValor.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new NegocioException("El valor declarado no es válido.");
        }
        if (dkprS.tipoComprobante() == null || !TIPOS_COMPROBANTE.contains(dkprS.tipoComprobante())) {
            throw new NegocioException("Selecciona el tipo de comprobante.");
        }
        if (dkprS.metodoPago() == null || !METODOS_PAGO.contains(dkprS.metodoPago())) {
            throw new NegocioException("Selecciona el método de pago.");
        }
        if ("FACTURA".equals(dkprS.tipoComprobante()) && !"RUC".equals(dkprS.remitenteTipo())) {
            throw new NegocioException("Para emitir factura, el remitente debe tener RUC.");
        }

        BigDecimal dkprCosto = calcularCosto(dkprS.ruta(), dkprS.pesoKg());

        try (Connection dkprCn = Conexion.obtener()) {
            dkprCn.setAutoCommit(false);
            try {
                Cliente dkprRemitente = dkprClienteService.obtenerOCrear(dkprCn, dkprS.remitenteTipo(), dkprS.remitenteDocumento(),
                        dkprS.remitenteNombre(), dkprS.remitenteTelefono(), "remitente");
                Cliente dkprDestinatario = dkprClienteService.obtenerOCrear(dkprCn, dkprS.destinatarioTipo(),
                        dkprS.destinatarioDocumento(), dkprS.destinatarioNombre(), dkprS.destinatarioTelefono(), "destinatario");

                int dkprIdEnvio = dkprEnvioDAO.siguienteId(dkprCn);
                String dkprCodigo = String.format("ENV-%06d", dkprIdEnvio);

                Envio dkprEnvio = new Envio();
                dkprEnvio.setIdEnvio(dkprIdEnvio);
                dkprEnvio.setCodigoSeguimiento(dkprCodigo);
                dkprEnvio.setIdRuta(dkprS.ruta().getIdRuta());
                dkprEnvio.setIdRemitente(dkprRemitente.getIdCliente());
                dkprEnvio.setIdDestinatario(dkprDestinatario.getIdCliente());
                dkprEnvio.setIdEmpleadoRegistro(dkprEmpleado.getIdEmpleado());
                dkprEnvio.setIdEstadoActual(Estados.REGISTRADO);
                dkprEnvio.setDescripcion(dkprDescripcion);
                dkprEnvio.setCantidadBultos(dkprS.cantidadBultos());
                dkprEnvio.setPesoKg(dkprS.pesoKg().setScale(2, RoundingMode.HALF_UP));
                dkprEnvio.setValorDeclarado(dkprValor.setScale(2, RoundingMode.HALF_UP));
                dkprEnvio.setCostoEnvio(dkprCosto);
                dkprEnvio.setObservaciones(dkprObservaciones);
                dkprEnvioDAO.insertar(dkprCn, dkprEnvio);

                dkprHistorialDAO.insertar(dkprCn, dkprIdEnvio, Estados.REGISTRADO, dkprEmpleado.getIdAgencia(),
                        dkprEmpleado.getIdEmpleado(), "Envío registrado en ventanilla");

                // El costo ya incluye IGV (18 %): se separa subtotal e IGV.
                BigDecimal dkprSubtotal = dkprCosto.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
                Comprobante dkprComprobante = new Comprobante();
                dkprComprobante.setIdEnvio(dkprIdEnvio);
                dkprComprobante.setTipo(dkprS.tipoComprobante());
                dkprComprobante.setSerie("BOLETA".equals(dkprS.tipoComprobante()) ? "B001" : "F001");
                dkprComprobante.setSubtotal(dkprSubtotal);
                dkprComprobante.setIgv(dkprCosto.subtract(dkprSubtotal));
                dkprComprobante.setTotal(dkprCosto);
                dkprComprobante.setMetodoPago(dkprS.metodoPago());
                dkprComprobanteDAO.insertar(dkprCn, dkprComprobante);

                dkprCn.commit();
                return new ResultadoRegistro(dkprCodigo, dkprComprobante.getNumeroTexto(), dkprCosto);
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    // ------------------------------------------------------------- seguimiento

    public Envio buscar(String dkprCodigo) throws SQLException {
        String dkprC = Validaciones.requerido(dkprCodigo, "Código de seguimiento", 15).toUpperCase();
        Envio dkprEnvio = dkprEnvioDAO.buscarPorCodigo(dkprC);
        if (dkprEnvio == null) {
            throw new NegocioException("No se encontró ningún envío con el código " + dkprC + ".");
        }
        return dkprEnvio;
    }

    public List<HistorialEnvio> historial(int dkprIdEnvio) throws SQLException {
        return dkprHistorialDAO.listarPorEnvio(dkprIdEnvio);
    }

    /** Envío con su comprobante e historial: lo necesario para imprimir el ticket o el PDF. */
    public DetalleEnvio detalle(String dkprCodigo) throws SQLException {
        Envio dkprEnvio = buscar(dkprCodigo);
        return new DetalleEnvio(dkprEnvio, dkprComprobanteDAO.buscarPorEnvio(dkprEnvio.getIdEnvio()),
                dkprHistorialDAO.listarPorEnvio(dkprEnvio.getIdEnvio()));
    }

    /** Estados a los que se puede pasar desde el estado actual (la entrega se hace en su módulo). */
    public List<Integer> siguientesEstados(int dkprEstadoActual) {
        return switch (dkprEstadoActual) {
            case Estados.REGISTRADO -> List.of(Estados.EN_TRANSITO, Estados.ANULADO);
            case Estados.EN_TRANSITO -> List.of(Estados.EN_AGENCIA_DESTINO);
            default -> List.of();
        };
    }

    public boolean puedeCambiarEstado(Empleado dkprEmpleado) {
        return tieneRol(dkprEmpleado, Roles.ADMINISTRADOR, Roles.ALMACEN, Roles.CAJERO);
    }

    /** Estados a los que este empleado puede pasar el envío según su rol y su agencia. */
    public List<Integer> estadosDisponibles(Envio dkprEnvio, Empleado dkprEmpleado) {
        List<Integer> dkprDisponibles = new ArrayList<>();
        for (int dkprEstado : siguientesEstados(dkprEnvio.getIdEstadoActual())) {
            if (puedeCambiarA(dkprEstado, dkprEnvio, dkprEmpleado)) {
                dkprDisponibles.add(dkprEstado);
            }
        }
        return dkprDisponibles;
    }

    /**
     * Despachar y confirmar llegada: almacén (o administrador) en la agencia correspondiente.
     * Anular: cajero (o administrador) de la agencia de origen. El administrador opera en cualquier agencia.
     */
    private boolean puedeCambiarA(int dkprNuevoEstado, Envio dkprEnvio, Empleado dkprEmpleado) {
        boolean dkprAdmin = esAdmin(dkprEmpleado);
        if (dkprNuevoEstado == Estados.ANULADO) {
            return tieneRol(dkprEmpleado, Roles.ADMINISTRADOR, Roles.CAJERO)
                    && (dkprAdmin || dkprEmpleado.getIdAgencia() == dkprEnvio.getIdAgenciaOrigen());
        }
        if (dkprNuevoEstado == Estados.EN_TRANSITO) {
            return tieneRol(dkprEmpleado, Roles.ADMINISTRADOR, Roles.ALMACEN)
                    && (dkprAdmin || dkprEmpleado.getIdAgencia() == dkprEnvio.getIdAgenciaOrigen());
        }
        if (dkprNuevoEstado == Estados.EN_AGENCIA_DESTINO) {
            return tieneRol(dkprEmpleado, Roles.ADMINISTRADOR, Roles.ALMACEN)
                    && (dkprAdmin || dkprEmpleado.getIdAgencia() == dkprEnvio.getIdAgenciaDestino());
        }
        return false;
    }

    private String mensajeSinPermiso(int dkprNuevoEstado, Envio dkprEnvio) {
        if (dkprNuevoEstado == Estados.ANULADO) {
            return "Solo el cajero de la agencia de origen (" + dkprEnvio.getOrigen() + ") o el administrador puede anular el envío.";
        }
        if (dkprNuevoEstado == Estados.EN_TRANSITO) {
            return "Solo el personal de almacén de la agencia de origen (" + dkprEnvio.getOrigen() + ") puede despachar el envío.";
        }
        return "Solo el personal de almacén de la agencia destino (" + dkprEnvio.getDestino() + ") puede confirmar la llegada del envío.";
    }

    public boolean puedeEntregar(Empleado dkprEmpleado) {
        return tieneRol(dkprEmpleado, Roles.ADMINISTRADOR, Roles.CAJERO);
    }

    public void cambiarEstado(Envio dkprEnvio, int dkprNuevoEstado, String dkprObservacion, Empleado dkprEmpleado)
            throws SQLException {
        Envio dkprActual = buscar(dkprEnvio.getCodigoSeguimiento());
        if (!siguientesEstados(dkprActual.getIdEstadoActual()).contains(dkprNuevoEstado)) {
            throw new NegocioException("No se puede pasar de " + dkprActual.getEstado()
                    + " a " + Estados.nombre(dkprNuevoEstado) + ".");
        }
        if (!puedeCambiarA(dkprNuevoEstado, dkprActual, dkprEmpleado)) {
            throw new NegocioException(mensajeSinPermiso(dkprNuevoEstado, dkprActual));
        }
        String dkprMotivo = Validaciones.opcional(dkprObservacion, "Observación", 200);
        if (dkprNuevoEstado == Estados.ANULADO && dkprMotivo == null) {
            throw new NegocioException("Indica el motivo de la anulación en la observación.");
        }

        try (Connection dkprCn = Conexion.obtener()) {
            dkprCn.setAutoCommit(false);
            try {
                moverEstado(dkprCn, dkprActual, dkprNuevoEstado);
                dkprHistorialDAO.insertar(dkprCn, dkprActual.getIdEnvio(), dkprNuevoEstado, agenciaDelMovimiento(dkprNuevoEstado, dkprActual),
                        dkprEmpleado.getIdEmpleado(), dkprMotivo);
                dkprCn.commit();
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    /**
     * Aviso para quien consulta un envío que no le toca mover: qué sigue y qué agencia lo hace.
     * Devuelve "" si el empleado puede hacer el siguiente paso o si el envío ya terminó su flujo.
     */
    public String avisoSoloConsulta(Envio dkprEnvio, Empleado dkprEmpleado) {
        return switch (dkprEnvio.getIdEstadoActual()) {
            case Estados.REGISTRADO -> puedeCambiarA(Estados.EN_TRANSITO, dkprEnvio, dkprEmpleado) ? ""
                    : "Solo consulta. El despacho lo realiza el personal de almacén de " + dkprEnvio.getOrigen() + ".";
            case Estados.EN_TRANSITO -> puedeCambiarA(Estados.EN_AGENCIA_DESTINO, dkprEnvio, dkprEmpleado) ? ""
                    : "Solo consulta. La llegada la confirma el personal de almacén de " + dkprEnvio.getDestino() + ".";
            case Estados.EN_AGENCIA_DESTINO -> puedeEntregarEn(dkprEmpleado, dkprEnvio.getIdAgenciaDestino())
                    ? "Listo para entregar: regístralo en Entregas o en la Bandeja de agencia."
                    : "Solo consulta. La entrega la registra el cajero de " + dkprEnvio.getDestino() + ".";
            default -> "";
        };
    }

    // ------------------------------------------------------- bandeja de agencia

    /** Envíos registrados en la agencia que esperan salir. */
    public List<Envio> porDespachar(int dkprIdAgencia) throws SQLException {
        return dkprEnvioDAO.listarPorEstadoYAgencia(Estados.REGISTRADO, dkprIdAgencia, true);
    }

    /** Envíos que vienen en camino hacia la agencia. */
    public List<Envio> porRecibir(int dkprIdAgencia) throws SQLException {
        return dkprEnvioDAO.listarPorEstadoYAgencia(Estados.EN_TRANSITO, dkprIdAgencia, false);
    }

    /** Envíos que ya están en la agencia esperando que el destinatario los recoja. */
    public List<Envio> porEntregar(int dkprIdAgencia) throws SQLException {
        return dkprEnvioDAO.listarPorEstadoYAgencia(Estados.EN_AGENCIA_DESTINO, dkprIdAgencia, false);
    }

    public boolean puedeOperarAlmacen(Empleado dkprEmpleado, int dkprIdAgencia) {
        return tieneRol(dkprEmpleado, Roles.ADMINISTRADOR, Roles.ALMACEN)
                && (esAdmin(dkprEmpleado) || dkprEmpleado.getIdAgencia() == dkprIdAgencia);
    }

    public boolean puedeEntregarEn(Empleado dkprEmpleado, int dkprIdAgencia) {
        return puedeEntregar(dkprEmpleado) && (esAdmin(dkprEmpleado) || dkprEmpleado.getIdAgencia() == dkprIdAgencia);
    }

    /**
     * Despacha varios envíos juntos: crea un manifiesto de carga (vehículo y conductor) y pasa
     * todos a EN TRANSITO. Todos deben ir por la misma ruta. Devuelve el código del manifiesto.
     */
    public String despachar(List<Envio> dkprEnvios, String dkprPlaca, String dkprConductor, String dkprObservacion,
                            Empleado dkprEmpleado) throws SQLException {
        try (Connection dkprCn = Conexion.obtener()) {
            dkprCn.setAutoCommit(false);
            try {
                int dkprIdManifiesto = despacharEn(dkprCn, dkprEnvios, dkprPlaca, dkprConductor, dkprObservacion, dkprEmpleado);
                dkprCn.commit();
                return ManifiestoDAO.codigo(dkprIdManifiesto);
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    /**
     * Igual que {@link #despachar}, pero dentro de una transacción que maneja quien llama
     * (el control de carga cierra su sesión en la misma transacción). Devuelve el id del manifiesto.
     */
    public int despacharEn(Connection dkprCn, List<Envio> dkprEnvios, String dkprPlaca, String dkprConductor,
                           String dkprObservacion, Empleado dkprEmpleado) throws SQLException {
        if (dkprEnvios == null || dkprEnvios.isEmpty()) {
            throw new NegocioException("Marca al menos un envío para despachar.");
        }
        Envio dkprPrimero = dkprEnvios.get(0);
        if (!puedeCambiarA(Estados.EN_TRANSITO, dkprPrimero, dkprEmpleado)) {
            throw new NegocioException(mensajeSinPermiso(Estados.EN_TRANSITO, dkprPrimero));
        }
        for (Envio dkprE : dkprEnvios) {
            if (dkprE.getIdRuta() != dkprPrimero.getIdRuta()) {
                throw new NegocioException("Un manifiesto agrupa envíos de un mismo destino: "
                        + dkprPrimero.getCodigoSeguimiento() + " va a " + dkprPrimero.getDestino() + " y "
                        + dkprE.getCodigoSeguimiento() + " a " + dkprE.getDestino() + ". Filtra por destino.");
            }
        }
        String dkprPlacaOk = Validaciones.opcional(dkprPlaca, "Placa", 10);
        if (dkprPlacaOk != null) {
            dkprPlacaOk = dkprPlacaOk.toUpperCase(Locale.ROOT);
        }
        String dkprConductorOk = Validaciones.opcional(dkprConductor, "Conductor", 100);
        String dkprObs = Validaciones.opcional(dkprObservacion, "Observación", 200);

        int dkprIdManifiesto = dkprManifiestoDAO.insertar(dkprCn, dkprPrimero.getIdRuta(), dkprEmpleado.getIdEmpleado(),
                dkprPlacaOk, dkprConductorOk, dkprObs);
        String dkprCodigo = ManifiestoDAO.codigo(dkprIdManifiesto);
        String dkprDetalle = "Despachado en " + dkprCodigo + (dkprPlacaOk == null ? "" : " (placa " + dkprPlacaOk + ")");
        for (Envio dkprE : dkprEnvios) {
            if (!dkprEnvioDAO.actualizarEstadoSi(dkprCn, dkprE.getIdEnvio(), Estados.REGISTRADO,
                    Estados.EN_TRANSITO, dkprIdManifiesto)) {
                throw new NegocioException(dkprE.getCodigoSeguimiento()
                        + " ya no está pendiente de despacho. Actualiza la bandeja.");
            }
            dkprHistorialDAO.insertar(dkprCn, dkprE.getIdEnvio(), Estados.EN_TRANSITO, dkprE.getIdAgenciaOrigen(),
                    dkprEmpleado.getIdEmpleado(), dkprDetalle);
        }
        return dkprIdManifiesto;
    }

    /**
     * Confirma la llegada de varios envíos a la agencia destino. Solo se marcan los paquetes que
     * físicamente llegaron: los que falten siguen EN TRANSITO y aparecen como atrasados.
     */
    public void recibir(List<Envio> dkprEnvios, String dkprObservacion, Empleado dkprEmpleado) throws SQLException {
        try (Connection dkprCn = Conexion.obtener()) {
            dkprCn.setAutoCommit(false);
            try {
                recibirEn(dkprCn, dkprEnvios, dkprObservacion, dkprEmpleado);
                dkprCn.commit();
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    /** Igual que {@link #recibir}, pero dentro de una transacción que maneja quien llama. */
    public void recibirEn(Connection dkprCn, List<Envio> dkprEnvios, String dkprObservacion, Empleado dkprEmpleado)
            throws SQLException {
        if (dkprEnvios == null || dkprEnvios.isEmpty()) {
            throw new NegocioException("Marca al menos un envío que haya llegado.");
        }
        for (Envio dkprE : dkprEnvios) {
            if (!puedeCambiarA(Estados.EN_AGENCIA_DESTINO, dkprE, dkprEmpleado)) {
                throw new NegocioException(mensajeSinPermiso(Estados.EN_AGENCIA_DESTINO, dkprE));
            }
        }
        String dkprObs = Validaciones.opcional(dkprObservacion, "Observación", 200);

        for (Envio dkprE : dkprEnvios) {
            if (!dkprEnvioDAO.actualizarEstadoSi(dkprCn, dkprE.getIdEnvio(), Estados.EN_TRANSITO,
                    Estados.EN_AGENCIA_DESTINO, null)) {
                throw new NegocioException(dkprE.getCodigoSeguimiento()
                        + " ya no está en tránsito. Actualiza la bandeja.");
            }
            String dkprDetalle = dkprObs != null ? dkprObs : "Llegó a agencia destino"
                    + (dkprE.getManifiesto() == null ? "" : " (" + dkprE.getManifiesto() + ")");
            dkprHistorialDAO.insertar(dkprCn, dkprE.getIdEnvio(), Estados.EN_AGENCIA_DESTINO, dkprE.getIdAgenciaDestino(),
                    dkprEmpleado.getIdEmpleado(), dkprDetalle);
        }
    }

    // ----------------------------------------------------------------- entrega

    /** Registra quién recoge la encomienda y pasa el envío a ENTREGADO. */
    public void registrarEntrega(String dkprCodigo, String dkprNombreReceptor, String dkprDniReceptor,
                                 String dkprObservacion, Empleado dkprEmpleado) throws SQLException {
        if (!puedeEntregar(dkprEmpleado)) {
            throw new NegocioException("Tu rol no permite registrar entregas.");
        }
        Envio dkprActual = buscar(dkprCodigo);
        if (dkprActual.getIdEstadoActual() != Estados.EN_AGENCIA_DESTINO) {
            throw new NegocioException("Solo se pueden entregar envíos en estado EN AGENCIA DESTINO. "
                    + "Estado actual: " + dkprActual.getEstado() + ".");
        }
        if (!esAdmin(dkprEmpleado) && dkprEmpleado.getIdAgencia() != dkprActual.getIdAgenciaDestino()) {
            throw new NegocioException("Este envío se entrega en la agencia " + dkprActual.getDestino() + ".");
        }

        String dkprNombre = Validaciones.requerido(dkprNombreReceptor, "Nombre de quien recoge", 150);
        Validaciones.validarDni(dkprDniReceptor, "El DNI de quien recoge");
        String dkprDni = dkprDniReceptor.trim();
        String dkprObs = Validaciones.opcional(dkprObservacion, "Observación", 200);
        boolean dkprTercero = !dkprDni.equals(dkprActual.getDestinatarioDocumento());
        if (dkprTercero && dkprObs == null) {
            throw new NegocioException("Quien recoge no es el destinatario registrado. Indica en la observación "
                    + "su vínculo (familiar, trabajador de la empresa, carta poder...).");
        }

        try (Connection dkprCn = Conexion.obtener()) {
            dkprCn.setAutoCommit(false);
            try {
                moverEstado(dkprCn, dkprActual, Estados.ENTREGADO);
                dkprEntregaDAO.insertar(dkprCn, dkprActual.getIdEnvio(), dkprEmpleado.getIdEmpleado(), dkprNombre, dkprDni, dkprObs);
                dkprHistorialDAO.insertar(dkprCn, dkprActual.getIdEnvio(), Estados.ENTREGADO, dkprActual.getIdAgenciaDestino(),
                        dkprEmpleado.getIdEmpleado(), "Entregado a " + dkprNombre + " (DNI " + dkprDni + ")"
                                + (dkprTercero ? " - recoge un tercero: " + dkprObs : ""));
                dkprCn.commit();
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    // ----------------------------------------------------------------- ayudas

    /** Cambia el estado solo si nadie lo cambió antes (si no, avisa en lugar de pisar el otro cambio). */
    private void moverEstado(Connection dkprCn, Envio dkprEnvio, int dkprNuevoEstado) throws SQLException {
        if (!dkprEnvioDAO.actualizarEstadoSi(dkprCn, dkprEnvio.getIdEnvio(), dkprEnvio.getIdEstadoActual(), dkprNuevoEstado, null)) {
            throw new NegocioException("El envío " + dkprEnvio.getCodigoSeguimiento()
                    + " acaba de cambiar de estado. Vuelve a buscarlo.");
        }
    }

    /** Agencia donde ocurre físicamente el movimiento (el administrador puede operar desde otra). */
    private int agenciaDelMovimiento(int dkprNuevoEstado, Envio dkprEnvio) {
        return dkprNuevoEstado == Estados.EN_AGENCIA_DESTINO || dkprNuevoEstado == Estados.ENTREGADO
                ? dkprEnvio.getIdAgenciaDestino() : dkprEnvio.getIdAgenciaOrigen();
    }

    private boolean esAdmin(Empleado dkprE) {
        return dkprE != null && Roles.ADMINISTRADOR.equals(dkprE.getRol());
    }

    private boolean tieneRol(Empleado dkprE, String... dkprRoles) {
        if (dkprE == null) {
            return false;
        }
        for (String dkprRol : dkprRoles) {
            if (dkprRol.equals(dkprE.getRol())) {
                return true;
            }
        }
        return false;
    }
}
