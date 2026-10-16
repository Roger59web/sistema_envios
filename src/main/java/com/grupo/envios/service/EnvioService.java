package com.grupo.envios.service;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.dao.ComprobanteDAO;
import com.grupo.envios.dao.EntregaDAO;
import com.grupo.envios.dao.EnvioDAO;
import com.grupo.envios.dao.HistorialDAO;
import com.grupo.envios.model.Cliente;
import com.grupo.envios.model.Comprobante;
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
import java.util.Set;

/** Reglas de negocio de los envíos: tarifa, registro, cambios de estado y entrega. */
public class EnvioService {

    private static final BigDecimal FACTOR_IGV = new BigDecimal("1.18");
    private static final Set<String> TIPOS_COMPROBANTE = Set.of("BOLETA", "FACTURA");
    private static final Set<String> METODOS_PAGO = Set.of("EFECTIVO", "TARJETA", "YAPE", "PLIN");

    private final EnvioDAO envioDAO = new EnvioDAO();
    private final HistorialDAO historialDAO = new HistorialDAO();
    private final ComprobanteDAO comprobanteDAO = new ComprobanteDAO();
    private final EntregaDAO entregaDAO = new EntregaDAO();
    private final ClienteService clienteService = new ClienteService();

    // ------------------------------------------------------------------ tarifa

    /**
     * Costo = precio base (hasta 1 kg) + precio por cada kg adicional (se redondea hacia arriba).
     * Ejemplo con base 15.00 y extra 1.50: 3.2 kg → 15.00 + 3 × 1.50 = 19.50.
     */
    public BigDecimal calcularCosto(Ruta ruta, BigDecimal pesoKg) {
        if (ruta == null) {
            throw new NegocioException("Selecciona la ruta de destino.");
        }
        if (ruta.getPrecioBase() == null || ruta.getPrecioKgExtra() == null) {
            throw new NegocioException("La ruta seleccionada no tiene una tarifa vigente.");
        }
        if (pesoKg == null || pesoKg.signum() <= 0) {
            throw new NegocioException("El peso debe ser mayor que 0.");
        }
        if (ruta.getPesoMaxKg() != null && pesoKg.compareTo(ruta.getPesoMaxKg()) > 0) {
            throw new NegocioException("El peso máximo para esta ruta es " + ruta.getPesoMaxKg() + " kg.");
        }
        BigDecimal kgExtra = pesoKg.subtract(BigDecimal.ONE).max(BigDecimal.ZERO).setScale(0, RoundingMode.CEILING);
        return ruta.getPrecioBase().add(ruta.getPrecioKgExtra().multiply(kgExtra)).setScale(2, RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------------- registro

    /** Registra el envío, su primer movimiento y el comprobante en una sola transacción. */
    public ResultadoRegistro registrar(SolicitudEnvio s, Empleado empleado) throws SQLException {
        if (!tieneRol(empleado, Roles.ADMINISTRADOR, Roles.CAJERO)) {
            throw new NegocioException("Tu rol no permite registrar envíos.");
        }
        if (s.ruta() == null) {
            throw new NegocioException("Selecciona la ruta de destino.");
        }

        Validaciones.validarDocumento(s.remitenteTipo(), s.remitenteDocumento());
        Validaciones.validarDocumento(s.destinatarioTipo(), s.destinatarioDocumento());
        if (s.remitenteTipo().equals(s.destinatarioTipo())
                && s.remitenteDocumento().trim().equals(s.destinatarioDocumento().trim())) {
            throw new NegocioException("El remitente y el destinatario no pueden ser la misma persona.");
        }

        String descripcion = Validaciones.requerido(s.descripcion(), "Descripción", 200);
        String observaciones = Validaciones.opcional(s.observaciones(), "Observaciones", 200);
        if (s.cantidadBultos() < 1 || s.cantidadBultos() > 999) {
            throw new NegocioException("La cantidad de bultos debe estar entre 1 y 999.");
        }
        BigDecimal valor = s.valorDeclarado() == null ? BigDecimal.ZERO : s.valorDeclarado();
        if (valor.signum() < 0 || valor.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new NegocioException("El valor declarado no es válido.");
        }
        if (s.tipoComprobante() == null || !TIPOS_COMPROBANTE.contains(s.tipoComprobante())) {
            throw new NegocioException("Selecciona el tipo de comprobante.");
        }
        if (s.metodoPago() == null || !METODOS_PAGO.contains(s.metodoPago())) {
            throw new NegocioException("Selecciona el método de pago.");
        }
        if ("FACTURA".equals(s.tipoComprobante()) && !"RUC".equals(s.remitenteTipo())) {
            throw new NegocioException("Para emitir factura, el remitente debe tener RUC.");
        }

        BigDecimal costo = calcularCosto(s.ruta(), s.pesoKg());

        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                Cliente remitente = clienteService.obtenerOCrear(cn, s.remitenteTipo(), s.remitenteDocumento(),
                        s.remitenteNombre(), s.remitenteTelefono(), "remitente");
                Cliente destinatario = clienteService.obtenerOCrear(cn, s.destinatarioTipo(),
                        s.destinatarioDocumento(), s.destinatarioNombre(), s.destinatarioTelefono(), "destinatario");

                int idEnvio = envioDAO.siguienteId(cn);
                String codigo = String.format("ENV-%06d", idEnvio);

                Envio envio = new Envio();
                envio.setIdEnvio(idEnvio);
                envio.setCodigoSeguimiento(codigo);
                envio.setIdRuta(s.ruta().getIdRuta());
                envio.setIdRemitente(remitente.getIdCliente());
                envio.setIdDestinatario(destinatario.getIdCliente());
                envio.setIdEmpleadoRegistro(empleado.getIdEmpleado());
                envio.setIdEstadoActual(Estados.REGISTRADO);
                envio.setDescripcion(descripcion);
                envio.setCantidadBultos(s.cantidadBultos());
                envio.setPesoKg(s.pesoKg().setScale(2, RoundingMode.HALF_UP));
                envio.setValorDeclarado(valor.setScale(2, RoundingMode.HALF_UP));
                envio.setCostoEnvio(costo);
                envio.setObservaciones(observaciones);
                envioDAO.insertar(cn, envio);

                historialDAO.insertar(cn, idEnvio, Estados.REGISTRADO, empleado.getIdAgencia(),
                        empleado.getIdEmpleado(), "Envío registrado en ventanilla");

                // El costo ya incluye IGV (18 %): se separa subtotal e IGV.
                BigDecimal subtotal = costo.divide(FACTOR_IGV, 2, RoundingMode.HALF_UP);
                Comprobante comprobante = new Comprobante();
                comprobante.setIdEnvio(idEnvio);
                comprobante.setTipo(s.tipoComprobante());
                comprobante.setSerie("BOLETA".equals(s.tipoComprobante()) ? "B001" : "F001");
                comprobante.setSubtotal(subtotal);
                comprobante.setIgv(costo.subtract(subtotal));
                comprobante.setTotal(costo);
                comprobante.setMetodoPago(s.metodoPago());
                comprobanteDAO.insertar(cn, comprobante);

                cn.commit();
                return new ResultadoRegistro(codigo, comprobante.getNumeroTexto(), costo);
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    // ------------------------------------------------------------- seguimiento

    public Envio buscar(String codigo) throws SQLException {
        String c = Validaciones.requerido(codigo, "Código de seguimiento", 15).toUpperCase();
        Envio envio = envioDAO.buscarPorCodigo(c);
        if (envio == null) {
            throw new NegocioException("No se encontró ningún envío con el código " + c + ".");
        }
        return envio;
    }

    public List<HistorialEnvio> historial(int idEnvio) throws SQLException {
        return historialDAO.listarPorEnvio(idEnvio);
    }

    /** Estados a los que se puede pasar desde el estado actual (la entrega se hace en su módulo). */
    public List<Integer> siguientesEstados(int estadoActual) {
        return switch (estadoActual) {
            case Estados.REGISTRADO -> List.of(Estados.EN_TRANSITO, Estados.ANULADO);
            case Estados.EN_TRANSITO -> List.of(Estados.EN_AGENCIA_DESTINO);
            default -> List.of();
        };
    }

    public boolean puedeCambiarEstado(Empleado empleado) {
        return tieneRol(empleado, Roles.ADMINISTRADOR, Roles.ALMACEN, Roles.CAJERO);
    }

    /** Estados a los que este empleado puede pasar el envío según su rol y su agencia. */
    public List<Integer> estadosDisponibles(Envio envio, Empleado empleado) {
        List<Integer> disponibles = new ArrayList<>();
        for (int estado : siguientesEstados(envio.getIdEstadoActual())) {
            if (puedeCambiarA(estado, envio, empleado)) {
                disponibles.add(estado);
            }
        }
        return disponibles;
    }

    /**
     * Despachar y confirmar llegada: almacén (o administrador) en la agencia correspondiente.
     * Anular: cajero (o administrador) de la agencia de origen. El administrador opera en cualquier agencia.
     */
    private boolean puedeCambiarA(int nuevoEstado, Envio envio, Empleado empleado) {
        boolean admin = esAdmin(empleado);
        if (nuevoEstado == Estados.ANULADO) {
            return tieneRol(empleado, Roles.ADMINISTRADOR, Roles.CAJERO)
                    && (admin || empleado.getIdAgencia() == envio.getIdAgenciaOrigen());
        }
        if (nuevoEstado == Estados.EN_TRANSITO) {
            return tieneRol(empleado, Roles.ADMINISTRADOR, Roles.ALMACEN)
                    && (admin || empleado.getIdAgencia() == envio.getIdAgenciaOrigen());
        }
        if (nuevoEstado == Estados.EN_AGENCIA_DESTINO) {
            return tieneRol(empleado, Roles.ADMINISTRADOR, Roles.ALMACEN)
                    && (admin || empleado.getIdAgencia() == envio.getIdAgenciaDestino());
        }
        return false;
    }

    private String mensajeSinPermiso(int nuevoEstado, Envio envio) {
        if (nuevoEstado == Estados.ANULADO) {
            return "Solo el cajero de la agencia de origen (" + envio.getOrigen() + ") o el administrador puede anular el envío.";
        }
        if (nuevoEstado == Estados.EN_TRANSITO) {
            return "Solo el personal de almacén de la agencia de origen (" + envio.getOrigen() + ") puede despachar el envío.";
        }
        return "Solo el personal de almacén de la agencia destino (" + envio.getDestino() + ") puede confirmar la llegada del envío.";
    }

    public boolean puedeEntregar(Empleado empleado) {
        return tieneRol(empleado, Roles.ADMINISTRADOR, Roles.CAJERO);
    }

    public void cambiarEstado(Envio envio, int nuevoEstado, String observacion, Empleado empleado)
            throws SQLException {
        Envio actual = buscar(envio.getCodigoSeguimiento());
        if (!siguientesEstados(actual.getIdEstadoActual()).contains(nuevoEstado)) {
            throw new NegocioException("No se puede pasar de " + actual.getEstado()
                    + " a " + Estados.nombre(nuevoEstado) + ".");
        }
        if (!puedeCambiarA(nuevoEstado, actual, empleado)) {
            throw new NegocioException(mensajeSinPermiso(nuevoEstado, actual));
        }
        String motivo = Validaciones.opcional(observacion, "Observación", 200);
        if (nuevoEstado == Estados.ANULADO && motivo == null) {
            throw new NegocioException("Indica el motivo de la anulación en la observación.");
        }

        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                envioDAO.actualizarEstado(cn, actual.getIdEnvio(), nuevoEstado);
                historialDAO.insertar(cn, actual.getIdEnvio(), nuevoEstado, empleado.getIdAgencia(),
                        empleado.getIdEmpleado(), motivo);
                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    // ----------------------------------------------------------------- entrega

    /** Registra quién recoge la encomienda y pasa el envío a ENTREGADO. */
    public void registrarEntrega(String codigo, String nombreReceptor, String dniReceptor,
                                 String observacion, Empleado empleado) throws SQLException {
        if (!puedeEntregar(empleado)) {
            throw new NegocioException("Tu rol no permite registrar entregas.");
        }
        Envio actual = buscar(codigo);
        if (actual.getIdEstadoActual() != Estados.EN_AGENCIA_DESTINO) {
            throw new NegocioException("Solo se pueden entregar envíos en estado EN AGENCIA DESTINO. "
                    + "Estado actual: " + actual.getEstado() + ".");
        }
        if (!esAdmin(empleado) && empleado.getIdAgencia() != actual.getIdAgenciaDestino()) {
            throw new NegocioException("Este envío se entrega en la agencia " + actual.getDestino() + ".");
        }

        String nombre = Validaciones.requerido(nombreReceptor, "Nombre de quien recoge", 150);
        Validaciones.validarDni(dniReceptor, "El DNI de quien recoge");
        String dni = dniReceptor.trim();
        String obs = Validaciones.opcional(observacion, "Observación", 200);

        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                entregaDAO.insertar(cn, actual.getIdEnvio(), empleado.getIdEmpleado(), nombre, dni, obs);
                envioDAO.actualizarEstado(cn, actual.getIdEnvio(), Estados.ENTREGADO);
                historialDAO.insertar(cn, actual.getIdEnvio(), Estados.ENTREGADO, empleado.getIdAgencia(),
                        empleado.getIdEmpleado(), "Entregado a " + nombre + " (DNI " + dni + ")");
                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    // ----------------------------------------------------------------- ayudas

    private boolean esAdmin(Empleado e) {
        return e != null && Roles.ADMINISTRADOR.equals(e.getRol());
    }

    private boolean tieneRol(Empleado e, String... roles) {
        if (e == null) {
            return false;
        }
        for (String rol : roles) {
            if (rol.equals(e.getRol())) {
                return true;
            }
        }
        return false;
    }
}
