package com.grupo.envios.service;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.dao.ControlCargaDAO;
import com.grupo.envios.dao.EnvioDAO;
import com.grupo.envios.dao.ManifiestoDAO;
import com.grupo.envios.model.ControlCarga;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.model.LecturaCarga;
import com.grupo.envios.model.Manifiesto;
import com.grupo.envios.util.CodigosBarras;
import com.grupo.envios.util.CodigosBarras.CodigoBulto;
import com.grupo.envios.util.Estados;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Validaciones;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ControlCargaService {

    private final ControlCargaDAO dkprControlDAO = new ControlCargaDAO();
    private final EnvioDAO dkprEnvioDAO = new EnvioDAO();
    private final ManifiestoDAO dkprManifiestoDAO = new ManifiestoDAO();
    private final EnvioService dkprEnvioService = new EnvioService();

    public record ResultadoCierre(String manifiesto, Integer idManifiesto, List<Envio> movidos, List<String> faltantes) {
    }

    public boolean puedeOperar(Empleado dkprEmpleado, int dkprIdAgencia) {
        return dkprEnvioService.puedeOperarAlmacen(dkprEmpleado, dkprIdAgencia);
    }

    public List<Envio> porEtiquetar(int dkprIdAgencia) throws SQLException {
        return dkprEnvioService.porDespachar(dkprIdAgencia);
    }

    public Envio buscarEnvio(String dkprTexto) throws SQLException {
        CodigoBulto dkprC = CodigosBarras.leer(dkprTexto);
        if (dkprC == null) {
            throw new NegocioException("Escribe o escanea un código de envío (ej: ENV-000015 o 15).");
        }
        Envio dkprE = dkprEnvioDAO.buscarPorId(dkprC.idEnvio());
        if (dkprE == null) {
            throw new NegocioException("No existe ningún envío " + codigoEnvio(dkprC.idEnvio()) + ".");
        }
        return dkprE;
    }

    public ControlCarga iniciarDespacho(int dkprIdAgencia, int dkprIdRuta, Empleado dkprEmpleado) throws SQLException {
        validarPermiso(dkprEmpleado, dkprIdAgencia);
        ControlCarga dkprAbierto = dkprControlDAO.buscarDespachoAbierto(dkprIdAgencia, dkprIdRuta);
        if (dkprAbierto != null) {
            return dkprAbierto;
        }
        try {
            return dkprControlDAO.buscar(dkprControlDAO.insertar(ControlCarga.DESPACHO, dkprIdAgencia, dkprIdRuta, null,
                    dkprEmpleado.getIdEmpleado()));
        } catch (SQLException dkprE) {
            if ("23505".equals(dkprE.getSQLState())) {
                return dkprControlDAO.buscarDespachoAbierto(dkprIdAgencia, dkprIdRuta);
            }
            throw dkprE;
        }
    }

    public List<Manifiesto> manifiestosPorRecibir(int dkprIdAgencia) throws SQLException {
        return dkprManifiestoDAO.listarEnCaminoHacia(dkprIdAgencia);
    }

    public ControlCarga iniciarRecepcion(int dkprIdAgencia, Manifiesto dkprManifiesto, Empleado dkprEmpleado)
            throws SQLException {
        validarPermiso(dkprEmpleado, dkprIdAgencia);
        if (dkprManifiesto.getIdAgenciaDestino() != dkprIdAgencia) {
            throw new NegocioException("El manifiesto " + dkprManifiesto.getCodigo() + " va a " + dkprManifiesto.getDestino() + ".");
        }
        ControlCarga dkprAbierta = dkprControlDAO.buscarRecepcionAbierta(dkprManifiesto.getIdManifiesto());
        if (dkprAbierta != null) {
            return dkprAbierta;
        }
        try {
            return dkprControlDAO.buscar(dkprControlDAO.insertar(ControlCarga.RECEPCION, dkprIdAgencia,
                    dkprManifiesto.getIdRuta(), dkprManifiesto.getIdManifiesto(), dkprEmpleado.getIdEmpleado()));
        } catch (SQLException dkprE) {
            if ("23505".equals(dkprE.getSQLState())) {
                return dkprControlDAO.buscarRecepcionAbierta(dkprManifiesto.getIdManifiesto());
            }
            throw dkprE;
        }
    }

    public List<Envio> esperados(ControlCarga dkprC) throws SQLException {
        if (dkprC.esDespacho()) {
            return dkprEnvioService.porDespachar(dkprC.getIdAgencia()).stream()
                    .filter(dkprE -> dkprE.getIdRuta() == dkprC.getIdRuta())
                    .toList();
        }
        return dkprEnvioDAO.listarPorManifiesto(dkprC.getIdManifiesto()).stream()
                .filter(dkprE -> dkprE.getIdEstadoActual() == Estados.EN_TRANSITO)
                .toList();
    }

    public List<LecturaCarga> lecturas(ControlCarga dkprC) throws SQLException {
        return dkprControlDAO.listarLecturas(dkprC.getIdControl());
    }

    public LecturaCarga escanear(ControlCarga dkprC, String dkprTexto, Empleado dkprEmpleado) throws SQLException {
        String dkprLeido = dkprTexto == null ? "" : dkprTexto.trim();
        if (dkprLeido.isEmpty()) {
            return null;
        }
        ControlCarga dkprActual = dkprControlDAO.buscar(dkprC.getIdControl());
        if (dkprActual == null || !"ABIERTO".equals(dkprActual.getEstado())) {
            throw new NegocioException("Esta sesión ya fue cerrada por otra persona. Vuelve a iniciarla.");
        }

        CodigoBulto dkprCodigo = CodigosBarras.leer(dkprLeido);
        LecturaCarga dkprL = dkprCodigo == null
                ? new LecturaCarga(dkprLeido, null, null, LecturaCarga.NO_EXISTE, "Código no reconocido: no es una etiqueta de envío.")
                : evaluar(dkprC, dkprLeido, dkprCodigo, dkprEnvioDAO.buscarPorId(dkprCodigo.idEnvio()));

        try {
            dkprControlDAO.insertarLectura(dkprC.getIdControl(), dkprL, dkprEmpleado.getIdEmpleado());
        } catch (SQLException dkprE) {
            if (!"23505".equals(dkprE.getSQLState())) {
                throw dkprE;
            }
            dkprL.setResultado(LecturaCarga.DUPLICADO);
            dkprL.setDetalle(etiquetaDe(dkprL) + " ya fue escaneado en esta sesión. No lo cuentes dos veces.");
            dkprControlDAO.insertarLectura(dkprC.getIdControl(), dkprL, dkprEmpleado.getIdEmpleado());
        }
        return dkprL;
    }

    private LecturaCarga evaluar(ControlCarga dkprC, String dkprLeido, CodigoBulto dkprCodigo, Envio dkprE) {
        if (dkprE == null) {
            return new LecturaCarga(dkprLeido, null, dkprCodigo.nroBulto(), LecturaCarga.NO_EXISTE,
                    "No existe ningún envío " + codigoEnvio(dkprCodigo.idEnvio()) + ".");
        }
        int dkprId = dkprE.getIdEnvio();
        Integer dkprN = dkprCodigo.nroBulto();
        if (dkprN == null) {
            if (dkprE.getCantidadBultos() > 1) {
                return new LecturaCarga(dkprLeido, dkprId, null, LecturaCarga.NO_CORRESPONDE, dkprE.getCodigoSeguimiento()
                        + " tiene " + dkprE.getCantidadBultos() + " bultos: escanea la etiqueta de cada bulto, no el ticket.");
            }
            dkprN = 1;
        }
        if (dkprN < 1 || dkprN > dkprE.getCantidadBultos()) {
            return new LecturaCarga(dkprLeido, dkprId, dkprN, LecturaCarga.NO_EXISTE, dkprE.getCodigoSeguimiento()
                    + " tiene " + dkprE.getCantidadBultos() + " bulto(s): no existe el bulto " + dkprN + ".");
        }
        String dkprEtiqueta = CodigosBarras.codigoBulto(dkprE, dkprN);

        if (dkprC.esDespacho()) {
            if (dkprE.getIdAgenciaOrigen() != dkprC.getIdAgencia()) {
                return new LecturaCarga(dkprLeido, dkprId, dkprN, LecturaCarga.NO_CORRESPONDE,
                        dkprEtiqueta + " se registró en " + dkprE.getOrigen() + ", no en esta agencia.");
            }
            if (dkprE.getIdEstadoActual() != Estados.REGISTRADO) {
                return new LecturaCarga(dkprLeido, dkprId, dkprN, LecturaCarga.NO_CORRESPONDE,
                        dkprEtiqueta + " está " + dkprE.getEstado() + ": no se puede despachar.");
            }
            if (dkprE.getIdRuta() != dkprC.getIdRuta()) {
                return new LecturaCarga(dkprLeido, dkprId, dkprN, LecturaCarga.OTRO_DESTINO, "¡NO CARGAR! "
                        + dkprEtiqueta + " va a " + dkprE.getDestino() + "; este vehículo va a " + dkprC.getDestino() + ".");
            }
        } else {
            if (dkprE.getIdAgenciaDestino() != dkprC.getIdAgencia()) {
                return new LecturaCarga(dkprLeido, dkprId, dkprN, LecturaCarga.OTRO_DESTINO, "¡NO DESCARGAR! "
                        + dkprEtiqueta + " va a " + dkprE.getDestino() + ", no a esta agencia.");
            }
            if (!dkprC.getManifiesto().equals(dkprE.getManifiesto())) {
                return new LecturaCarga(dkprLeido, dkprId, dkprN, LecturaCarga.NO_CORRESPONDE, dkprEtiqueta
                        + " no viene en " + dkprC.getManifiesto()
                        + (dkprE.getManifiesto() == null ? " (no tiene manifiesto)." : " (salió en " + dkprE.getManifiesto() + ")."));
            }
            if (dkprE.getIdEstadoActual() != Estados.EN_TRANSITO) {
                return new LecturaCarga(dkprLeido, dkprId, dkprN, LecturaCarga.NO_CORRESPONDE,
                        dkprEtiqueta + " ya está " + dkprE.getEstado() + ".");
            }
        }
        return new LecturaCarga(dkprLeido, dkprId, dkprN, LecturaCarga.OK, dkprEtiqueta + "  ·  bulto " + dkprN + " de "
                + dkprE.getCantidadBultos() + "  ·  " + dkprE.getDestinatario());
    }

    public static Map<Integer, Set<Integer>> bultosLeidos(List<LecturaCarga> dkprLecturas) {
        Map<Integer, Set<Integer>> dkprLeidos = new HashMap<>();
        for (LecturaCarga dkprL : dkprLecturas) {
            if (dkprL.esOk()) {
                dkprLeidos.computeIfAbsent(dkprL.getIdEnvio(), dkprK -> new TreeSet<>()).add(dkprL.getNroBulto());
            }
        }
        return dkprLeidos;
    }

    public static boolean completo(Envio dkprE, Map<Integer, Set<Integer>> dkprLeidos) {
        return dkprLeidos.getOrDefault(dkprE.getIdEnvio(), Set.of()).size() >= dkprE.getCantidadBultos();
    }

    public static List<String> faltantes(List<Envio> dkprEsperados, Map<Integer, Set<Integer>> dkprLeidos) {
        List<String> dkprLista = new ArrayList<>();
        for (Envio dkprE : dkprEsperados) {
            Set<Integer> dkprLeidosEnvio = dkprLeidos.getOrDefault(dkprE.getIdEnvio(), Set.of());
            if (dkprLeidosEnvio.size() >= dkprE.getCantidadBultos()) {
                continue;
            }
            String dkprQuien = dkprE.getCodigoSeguimiento() + " (" + dkprE.getDestinatario() + "): ";
            if (dkprLeidosEnvio.isEmpty()) {
                dkprLista.add(dkprQuien + "no se escaneó ningún bulto (" + dkprE.getCantidadBultos() + ")");
            } else {
                String dkprNumeros = IntStream.rangeClosed(1, dkprE.getCantidadBultos())
                        .filter(dkprN -> !dkprLeidosEnvio.contains(dkprN))
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(", "));
                dkprLista.add(dkprQuien + "faltan bultos " + dkprNumeros + " de " + dkprE.getCantidadBultos());
            }
        }
        return dkprLista;
    }

    public ResultadoCierre cerrarDespacho(ControlCarga dkprC, Integer dkprIdVehiculo, Integer dkprIdConductor,
                                          String dkprObservacion, Empleado dkprEmpleado) throws SQLException {
        validarPermiso(dkprEmpleado, dkprC.getIdAgencia());
        List<Envio> dkprEsperados = esperados(dkprC);
        Map<Integer, Set<Integer>> dkprLeidos = bultosLeidos(lecturas(dkprC));
        List<Envio> dkprCompletos = dkprEsperados.stream().filter(dkprE -> completo(dkprE, dkprLeidos)).toList();
        if (dkprCompletos.isEmpty()) {
            throw new NegocioException("Ningún envío tiene todos sus bultos escaneados. "
                    + "Escanea los bultos o cancela la carga.");
        }
        String dkprObs = Validaciones.opcional(dkprObservacion, "Observación", 200);

        try (Connection dkprCn = Conexion.obtener()) {
            dkprCn.setAutoCommit(false);
            try {
                int dkprIdManifiesto = dkprEnvioService.despacharEn(dkprCn, dkprCompletos, dkprIdVehiculo, dkprIdConductor, dkprObs,
                        dkprEmpleado);
                if (!dkprControlDAO.cerrar(dkprCn, dkprC.getIdControl(), "CERRADO", dkprIdManifiesto, dkprObs)) {
                    throw new NegocioException("Esta carga ya fue cerrada por otra persona.");
                }
                dkprCn.commit();
                return new ResultadoCierre(ManifiestoDAO.codigo(dkprIdManifiesto), dkprIdManifiesto, dkprCompletos,
                        faltantes(dkprEsperados, dkprLeidos));
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    public ResultadoCierre cerrarRecepcion(ControlCarga dkprC, String dkprObservacion, Empleado dkprEmpleado)
            throws SQLException {
        validarPermiso(dkprEmpleado, dkprC.getIdAgencia());
        List<Envio> dkprEsperados = esperados(dkprC);
        Map<Integer, Set<Integer>> dkprLeidos = bultosLeidos(lecturas(dkprC));
        List<Envio> dkprCompletos = dkprEsperados.stream().filter(dkprE -> completo(dkprE, dkprLeidos)).toList();
        if (dkprCompletos.isEmpty()) {
            throw new NegocioException("Ningún envío llegó con todos sus bultos. Si el vehículo aún no termina "
                    + "de descargar, deja la recepción abierta; si no llegó, cancélala.");
        }
        String dkprObs = Validaciones.opcional(dkprObservacion, "Observación", 200);

        try (Connection dkprCn = Conexion.obtener()) {
            dkprCn.setAutoCommit(false);
            try {
                dkprEnvioService.recibirEn(dkprCn, dkprCompletos, dkprObs, dkprEmpleado);
                if (!dkprControlDAO.cerrar(dkprCn, dkprC.getIdControl(), "CERRADO", null, dkprObs)) {
                    throw new NegocioException("Esta recepción ya fue cerrada por otra persona.");
                }
                dkprCn.commit();
                return new ResultadoCierre(dkprC.getManifiesto(), dkprC.getIdManifiesto(), dkprCompletos,
                        faltantes(dkprEsperados, dkprLeidos));
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    public void cancelar(ControlCarga dkprC, Empleado dkprEmpleado) throws SQLException {
        validarPermiso(dkprEmpleado, dkprC.getIdAgencia());
        try (Connection dkprCn = Conexion.obtener()) {
            dkprControlDAO.cerrar(dkprCn, dkprC.getIdControl(), "CANCELADO", null, "Cancelada por " + dkprEmpleado.getNombreCompleto());
        }
    }

    public Manifiesto buscarManifiesto(int dkprIdManifiesto) throws SQLException {
        return dkprManifiestoDAO.buscar(dkprIdManifiesto);
    }

    public Manifiesto buscarManifiesto(String dkprTexto) throws SQLException {
        String dkprT = Validaciones.requerido(dkprTexto, "Código de manifiesto", 15).toUpperCase();
        if (dkprT.matches("\\d{1,6}")) {
            dkprT = ManifiestoDAO.codigo(Integer.parseInt(dkprT));
        }
        Manifiesto dkprM = dkprManifiestoDAO.buscarPorCodigo(dkprT);
        if (dkprM == null) {
            throw new NegocioException("No existe el manifiesto " + dkprT + ".");
        }
        return dkprM;
    }

    public List<Envio> enviosDeManifiesto(int dkprIdManifiesto) throws SQLException {
        return dkprEnvioDAO.listarPorManifiesto(dkprIdManifiesto);
    }

    private void validarPermiso(Empleado dkprEmpleado, int dkprIdAgencia) {
        if (!puedeOperar(dkprEmpleado, dkprIdAgencia)) {
            throw new NegocioException("Solo el personal de almacén de la agencia (o el administrador) puede controlar la carga.");
        }
    }

    private static String codigoEnvio(int dkprIdEnvio) {
        return String.format("ENV-%06d", dkprIdEnvio);
    }

    private static String etiquetaDe(LecturaCarga dkprL) {
        return codigoEnvio(dkprL.getIdEnvio()) + "-" + String.format("%02d", dkprL.getNroBulto());
    }
}
