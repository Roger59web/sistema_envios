package com.grupo.envios.service;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.dao.DkprControlCargaDAO;
import com.grupo.envios.dao.DkprEnvioDAO;
import com.grupo.envios.dao.DkprManifiestoDAO;
import com.grupo.envios.model.DkprControlCarga;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.model.DkprLecturaCarga;
import com.grupo.envios.model.DkprManifiesto;
import com.grupo.envios.util.DkprCodigosBarras;
import com.grupo.envios.util.DkprCodigosBarras.DkprCodigoBulto;
import com.grupo.envios.util.DkprEstados;
import com.grupo.envios.util.DkprNegocioException;
import com.grupo.envios.util.DkprValidaciones;

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

public class DkprControlCargaService {

    private final DkprControlCargaDAO dkprControlDAO = new DkprControlCargaDAO();
    private final DkprEnvioDAO dkprEnvioDAO = new DkprEnvioDAO();
    private final DkprManifiestoDAO dkprManifiestoDAO = new DkprManifiestoDAO();
    private final DkprEnvioService dkprEnvioService = new DkprEnvioService();

    public record DkprResultadoCierre(String dkprManifiesto, Integer dkprIdManifiesto, List<DkprEnvio> dkprMovidos, List<String> dkprFaltantes) {
    }

    public boolean dkprPuedeOperar(DkprEmpleado dkprEmpleado, int dkprIdAgencia) {
        return dkprEnvioService.dkprPuedeOperarAlmacen(dkprEmpleado, dkprIdAgencia);
    }

    public List<DkprEnvio> dkprPorEtiquetar(int dkprIdAgencia) throws SQLException {
        return dkprEnvioService.dkprPorDespachar(dkprIdAgencia);
    }

    public DkprEnvio dkprBuscarEnvio(String dkprTexto) throws SQLException {
        DkprCodigoBulto dkprC = DkprCodigosBarras.dkprLeer(dkprTexto);
        if (dkprC == null) {
            throw new DkprNegocioException("Escribe o escanea un código de envío (ej: ENV-000015 o 15).");
        }
        DkprEnvio dkprE = dkprEnvioDAO.dkprBuscarPorId(dkprC.dkprIdEnvio());
        if (dkprE == null) {
            throw new DkprNegocioException("No existe ningún envío " + dkprCodigoEnvio(dkprC.dkprIdEnvio()) + ".");
        }
        return dkprE;
    }

    public DkprControlCarga dkprIniciarDespacho(int dkprIdAgencia, int dkprIdRuta, DkprEmpleado dkprEmpleado) throws SQLException {
        dkprValidarPermiso(dkprEmpleado, dkprIdAgencia);
        DkprControlCarga dkprAbierto = dkprControlDAO.dkprBuscarDespachoAbierto(dkprIdAgencia, dkprIdRuta);
        if (dkprAbierto != null) {
            return dkprAbierto;
        }
        try {
            return dkprControlDAO.dkprBuscar(dkprControlDAO.dkprInsertar(DkprControlCarga.DESPACHO, dkprIdAgencia, dkprIdRuta, null,
                    dkprEmpleado.dkprGetIdEmpleado()));
        } catch (SQLException dkprE) {
            if ("23505".equals(dkprE.getSQLState())) {
                return dkprControlDAO.dkprBuscarDespachoAbierto(dkprIdAgencia, dkprIdRuta);
            }
            throw dkprE;
        }
    }

    public List<DkprManifiesto> dkprManifiestosPorRecibir(int dkprIdAgencia) throws SQLException {
        return dkprManifiestoDAO.dkprListarEnCaminoHacia(dkprIdAgencia);
    }

    public DkprControlCarga dkprIniciarRecepcion(int dkprIdAgencia, DkprManifiesto dkprManifiesto, DkprEmpleado dkprEmpleado)
            throws SQLException {
        dkprValidarPermiso(dkprEmpleado, dkprIdAgencia);
        if (dkprManifiesto.dkprGetIdAgenciaDestino() != dkprIdAgencia) {
            throw new DkprNegocioException("El manifiesto " + dkprManifiesto.dkprGetCodigo() + " va a " + dkprManifiesto.dkprGetDestino() + ".");
        }
        DkprControlCarga dkprAbierta = dkprControlDAO.dkprBuscarRecepcionAbierta(dkprManifiesto.dkprGetIdManifiesto());
        if (dkprAbierta != null) {
            return dkprAbierta;
        }
        try {
            return dkprControlDAO.dkprBuscar(dkprControlDAO.dkprInsertar(DkprControlCarga.RECEPCION, dkprIdAgencia,
                    dkprManifiesto.dkprGetIdRuta(), dkprManifiesto.dkprGetIdManifiesto(), dkprEmpleado.dkprGetIdEmpleado()));
        } catch (SQLException dkprE) {
            if ("23505".equals(dkprE.getSQLState())) {
                return dkprControlDAO.dkprBuscarRecepcionAbierta(dkprManifiesto.dkprGetIdManifiesto());
            }
            throw dkprE;
        }
    }

    public List<DkprEnvio> dkprEsperados(DkprControlCarga dkprC) throws SQLException {
        if (dkprC.dkprEsDespacho()) {
            return dkprEnvioService.dkprPorDespachar(dkprC.dkprGetIdAgencia()).stream()
                    .filter(dkprE -> dkprE.dkprGetIdRuta() == dkprC.dkprGetIdRuta())
                    .toList();
        }
        return dkprEnvioDAO.dkprListarPorManifiesto(dkprC.dkprGetIdManifiesto()).stream()
                .filter(dkprE -> dkprE.dkprGetIdEstadoActual() == DkprEstados.EN_TRANSITO)
                .toList();
    }

    public List<DkprLecturaCarga> dkprLecturas(DkprControlCarga dkprC) throws SQLException {
        return dkprControlDAO.dkprListarLecturas(dkprC.dkprGetIdControl());
    }

    public DkprLecturaCarga dkprEscanear(DkprControlCarga dkprC, String dkprTexto, DkprEmpleado dkprEmpleado) throws SQLException {
        String dkprLeido = dkprTexto == null ? "" : dkprTexto.trim();
        if (dkprLeido.isEmpty()) {
            return null;
        }
        DkprControlCarga dkprActual = dkprControlDAO.dkprBuscar(dkprC.dkprGetIdControl());
        if (dkprActual == null || !"ABIERTO".equals(dkprActual.dkprGetEstado())) {
            throw new DkprNegocioException("Esta sesión ya fue cerrada por otra persona. Vuelve a iniciarla.");
        }

        DkprCodigoBulto dkprCodigo = DkprCodigosBarras.dkprLeer(dkprLeido);
        DkprLecturaCarga dkprL = dkprCodigo == null
                ? new DkprLecturaCarga(dkprLeido, null, null, DkprLecturaCarga.NO_EXISTE, "Código no reconocido: no es una etiqueta de envío.")
                : dkprEvaluar(dkprC, dkprLeido, dkprCodigo, dkprEnvioDAO.dkprBuscarPorId(dkprCodigo.dkprIdEnvio()));

        try {
            dkprControlDAO.dkprInsertarLectura(dkprC.dkprGetIdControl(), dkprL, dkprEmpleado.dkprGetIdEmpleado());
        } catch (SQLException dkprE) {
            if (!"23505".equals(dkprE.getSQLState())) {
                throw dkprE;
            }
            dkprL.dkprSetResultado(DkprLecturaCarga.DUPLICADO);
            dkprL.dkprSetDetalle(dkprEtiquetaDe(dkprL) + " ya fue escaneado en esta sesión. No lo cuentes dos veces.");
            dkprControlDAO.dkprInsertarLectura(dkprC.dkprGetIdControl(), dkprL, dkprEmpleado.dkprGetIdEmpleado());
        }
        return dkprL;
    }

    private DkprLecturaCarga dkprEvaluar(DkprControlCarga dkprC, String dkprLeido, DkprCodigoBulto dkprCodigo, DkprEnvio dkprE) {
        if (dkprE == null) {
            return new DkprLecturaCarga(dkprLeido, null, dkprCodigo.dkprNroBulto(), DkprLecturaCarga.NO_EXISTE,
                    "No existe ningún envío " + dkprCodigoEnvio(dkprCodigo.dkprIdEnvio()) + ".");
        }
        int dkprId = dkprE.dkprGetIdEnvio();
        Integer dkprN = dkprCodigo.dkprNroBulto();
        if (dkprN == null) {
            if (dkprE.dkprGetCantidadBultos() > 1) {
                return new DkprLecturaCarga(dkprLeido, dkprId, null, DkprLecturaCarga.NO_CORRESPONDE, dkprE.dkprGetCodigoSeguimiento()
                        + " tiene " + dkprE.dkprGetCantidadBultos() + " bultos: escanea la etiqueta de cada bulto, no el ticket.");
            }
            dkprN = 1;
        }
        if (dkprN < 1 || dkprN > dkprE.dkprGetCantidadBultos()) {
            return new DkprLecturaCarga(dkprLeido, dkprId, dkprN, DkprLecturaCarga.NO_EXISTE, dkprE.dkprGetCodigoSeguimiento()
                    + " tiene " + dkprE.dkprGetCantidadBultos() + " bulto(s): no existe el bulto " + dkprN + ".");
        }
        String dkprEtiqueta = DkprCodigosBarras.dkprCodigoBulto(dkprE, dkprN);

        if (dkprC.dkprEsDespacho()) {
            if (dkprE.dkprGetIdAgenciaOrigen() != dkprC.dkprGetIdAgencia()) {
                return new DkprLecturaCarga(dkprLeido, dkprId, dkprN, DkprLecturaCarga.NO_CORRESPONDE,
                        dkprEtiqueta + " se registró en " + dkprE.dkprGetOrigen() + ", no en esta agencia.");
            }
            if (dkprE.dkprGetIdEstadoActual() != DkprEstados.REGISTRADO) {
                return new DkprLecturaCarga(dkprLeido, dkprId, dkprN, DkprLecturaCarga.NO_CORRESPONDE,
                        dkprEtiqueta + " está " + dkprE.dkprGetEstado() + ": no se puede despachar.");
            }
            if (dkprE.dkprGetIdRuta() != dkprC.dkprGetIdRuta()) {
                return new DkprLecturaCarga(dkprLeido, dkprId, dkprN, DkprLecturaCarga.OTRO_DESTINO, "¡NO CARGAR! "
                        + dkprEtiqueta + " va a " + dkprE.dkprGetDestino() + "; este vehículo va a " + dkprC.dkprGetDestino() + ".");
            }
        } else {
            if (dkprE.dkprGetIdAgenciaDestino() != dkprC.dkprGetIdAgencia()) {
                return new DkprLecturaCarga(dkprLeido, dkprId, dkprN, DkprLecturaCarga.OTRO_DESTINO, "¡NO DESCARGAR! "
                        + dkprEtiqueta + " va a " + dkprE.dkprGetDestino() + ", no a esta agencia.");
            }
            if (!dkprC.dkprGetManifiesto().equals(dkprE.dkprGetManifiesto())) {
                return new DkprLecturaCarga(dkprLeido, dkprId, dkprN, DkprLecturaCarga.NO_CORRESPONDE, dkprEtiqueta
                        + " no viene en " + dkprC.dkprGetManifiesto()
                        + (dkprE.dkprGetManifiesto() == null ? " (no tiene manifiesto)." : " (salió en " + dkprE.dkprGetManifiesto() + ")."));
            }
            if (dkprE.dkprGetIdEstadoActual() != DkprEstados.EN_TRANSITO) {
                return new DkprLecturaCarga(dkprLeido, dkprId, dkprN, DkprLecturaCarga.NO_CORRESPONDE,
                        dkprEtiqueta + " ya está " + dkprE.dkprGetEstado() + ".");
            }
        }
        return new DkprLecturaCarga(dkprLeido, dkprId, dkprN, DkprLecturaCarga.OK, dkprEtiqueta + "  ·  bulto " + dkprN + " de "
                + dkprE.dkprGetCantidadBultos() + "  ·  " + dkprE.dkprGetDestinatario());
    }

    public static Map<Integer, Set<Integer>> dkprBultosLeidos(List<DkprLecturaCarga> dkprLecturas) {
        Map<Integer, Set<Integer>> dkprLeidos = new HashMap<>();
        for (DkprLecturaCarga dkprL : dkprLecturas) {
            if (dkprL.dkprEsOk()) {
                dkprLeidos.computeIfAbsent(dkprL.dkprGetIdEnvio(), dkprK -> new TreeSet<>()).add(dkprL.dkprGetNroBulto());
            }
        }
        return dkprLeidos;
    }

    public static boolean dkprCompleto(DkprEnvio dkprE, Map<Integer, Set<Integer>> dkprLeidos) {
        return dkprLeidos.getOrDefault(dkprE.dkprGetIdEnvio(), Set.of()).size() >= dkprE.dkprGetCantidadBultos();
    }

    public static List<String> dkprFaltantes(List<DkprEnvio> dkprEsperados, Map<Integer, Set<Integer>> dkprLeidos) {
        List<String> dkprLista = new ArrayList<>();
        for (DkprEnvio dkprE : dkprEsperados) {
            Set<Integer> dkprLeidosEnvio = dkprLeidos.getOrDefault(dkprE.dkprGetIdEnvio(), Set.of());
            if (dkprLeidosEnvio.size() >= dkprE.dkprGetCantidadBultos()) {
                continue;
            }
            String dkprQuien = dkprE.dkprGetCodigoSeguimiento() + " (" + dkprE.dkprGetDestinatario() + "): ";
            if (dkprLeidosEnvio.isEmpty()) {
                dkprLista.add(dkprQuien + "no se escaneó ningún bulto (" + dkprE.dkprGetCantidadBultos() + ")");
            } else {
                String dkprNumeros = IntStream.rangeClosed(1, dkprE.dkprGetCantidadBultos())
                        .filter(dkprN -> !dkprLeidosEnvio.contains(dkprN))
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(", "));
                dkprLista.add(dkprQuien + "faltan bultos " + dkprNumeros + " de " + dkprE.dkprGetCantidadBultos());
            }
        }
        return dkprLista;
    }

    public DkprResultadoCierre dkprCerrarDespacho(DkprControlCarga dkprC, Integer dkprIdVehiculo, Integer dkprIdConductor,
                                          String dkprObservacion, DkprEmpleado dkprEmpleado) throws SQLException {
        dkprValidarPermiso(dkprEmpleado, dkprC.dkprGetIdAgencia());
        List<DkprEnvio> dkprEsperados = dkprEsperados(dkprC);
        Map<Integer, Set<Integer>> dkprLeidos = dkprBultosLeidos(dkprLecturas(dkprC));
        List<DkprEnvio> dkprCompletos = dkprEsperados.stream().filter(dkprE -> dkprCompleto(dkprE, dkprLeidos)).toList();
        if (dkprCompletos.isEmpty()) {
            throw new DkprNegocioException("Ningún envío tiene todos sus bultos escaneados. "
                    + "Escanea los bultos o cancela la carga.");
        }
        String dkprObs = DkprValidaciones.dkprOpcional(dkprObservacion, "Observación", 200);

        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprCn.setAutoCommit(false);
            try {
                int dkprIdManifiesto = dkprEnvioService.dkprDespacharEn(dkprCn, dkprCompletos, dkprIdVehiculo, dkprIdConductor, dkprObs,
                        dkprEmpleado);
                if (!dkprControlDAO.dkprCerrar(dkprCn, dkprC.dkprGetIdControl(), "CERRADO", dkprIdManifiesto, dkprObs)) {
                    throw new DkprNegocioException("Esta carga ya fue cerrada por otra persona.");
                }
                dkprCn.commit();
                return new DkprResultadoCierre(DkprManifiestoDAO.dkprCodigo(dkprIdManifiesto), dkprIdManifiesto, dkprCompletos,
                        dkprFaltantes(dkprEsperados, dkprLeidos));
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    public DkprResultadoCierre dkprCerrarRecepcion(DkprControlCarga dkprC, String dkprObservacion, DkprEmpleado dkprEmpleado)
            throws SQLException {
        dkprValidarPermiso(dkprEmpleado, dkprC.dkprGetIdAgencia());
        List<DkprEnvio> dkprEsperados = dkprEsperados(dkprC);
        Map<Integer, Set<Integer>> dkprLeidos = dkprBultosLeidos(dkprLecturas(dkprC));
        List<DkprEnvio> dkprCompletos = dkprEsperados.stream().filter(dkprE -> dkprCompleto(dkprE, dkprLeidos)).toList();
        if (dkprCompletos.isEmpty()) {
            throw new DkprNegocioException("Ningún envío llegó con todos sus bultos. Si el vehículo aún no termina "
                    + "de descargar, deja la recepción abierta; si no llegó, cancélala.");
        }
        String dkprObs = DkprValidaciones.dkprOpcional(dkprObservacion, "Observación", 200);

        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprCn.setAutoCommit(false);
            try {
                dkprEnvioService.dkprRecibirEn(dkprCn, dkprCompletos, dkprObs, dkprEmpleado);
                if (!dkprControlDAO.dkprCerrar(dkprCn, dkprC.dkprGetIdControl(), "CERRADO", null, dkprObs)) {
                    throw new DkprNegocioException("Esta recepción ya fue cerrada por otra persona.");
                }
                dkprCn.commit();
                return new DkprResultadoCierre(dkprC.dkprGetManifiesto(), dkprC.dkprGetIdManifiesto(), dkprCompletos,
                        dkprFaltantes(dkprEsperados, dkprLeidos));
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    public void dkprCancelar(DkprControlCarga dkprC, DkprEmpleado dkprEmpleado) throws SQLException {
        dkprValidarPermiso(dkprEmpleado, dkprC.dkprGetIdAgencia());
        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprControlDAO.dkprCerrar(dkprCn, dkprC.dkprGetIdControl(), "CANCELADO", null, "Cancelada por " + dkprEmpleado.dkprGetNombreCompleto());
        }
    }

    public DkprManifiesto dkprBuscarManifiesto(int dkprIdManifiesto) throws SQLException {
        return dkprManifiestoDAO.dkprBuscar(dkprIdManifiesto);
    }

    public DkprManifiesto dkprBuscarManifiesto(String dkprTexto) throws SQLException {
        String dkprT = DkprValidaciones.dkprRequerido(dkprTexto, "Código de manifiesto", 15).toUpperCase();
        if (dkprT.matches("\\d{1,6}")) {
            dkprT = DkprManifiestoDAO.dkprCodigo(Integer.parseInt(dkprT));
        }
        DkprManifiesto dkprM = dkprManifiestoDAO.dkprBuscarPorCodigo(dkprT);
        if (dkprM == null) {
            throw new DkprNegocioException("No existe el manifiesto " + dkprT + ".");
        }
        return dkprM;
    }

    public List<DkprEnvio> dkprEnviosDeManifiesto(int dkprIdManifiesto) throws SQLException {
        return dkprEnvioDAO.dkprListarPorManifiesto(dkprIdManifiesto);
    }

    private void dkprValidarPermiso(DkprEmpleado dkprEmpleado, int dkprIdAgencia) {
        if (!dkprPuedeOperar(dkprEmpleado, dkprIdAgencia)) {
            throw new DkprNegocioException("Solo el personal de almacén de la agencia (o el administrador) puede controlar la carga.");
        }
    }

    private static String dkprCodigoEnvio(int dkprIdEnvio) {
        return String.format("ENV-%06d", dkprIdEnvio);
    }

    private static String dkprEtiquetaDe(DkprLecturaCarga dkprL) {
        return dkprCodigoEnvio(dkprL.dkprGetIdEnvio()) + "-" + String.format("%02d", dkprL.dkprGetNroBulto());
    }
}
