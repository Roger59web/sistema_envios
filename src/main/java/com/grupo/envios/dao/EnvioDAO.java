package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.Envio;
import com.grupo.envios.model.ResumenFila;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EnvioDAO {

    private static final String SELECT_BASE = """
            SELECT e.id_envio, e.codigo_seguimiento, e.id_ruta,
                   ru.id_agencia_origen, ru.id_agencia_destino,
                   ao.nombre AS origen, ad.nombre AS destino,
                   ao.direccion AS origen_direccion, ao.telefono AS origen_telefono,
                   e.id_remitente, rem.nombre_completo AS remitente, rem.numero_documento AS remitente_doc,
                   rem.telefono AS remitente_tel,
                   e.id_destinatario, des.nombre_completo AS destinatario, des.numero_documento AS destinatario_doc,
                   des.telefono AS destinatario_tel,
                   e.id_empleado_registro, em.nombres || ' ' || em.apellidos AS empleado_registro,
                   e.id_estado_actual, es.nombre AS estado, e.fecha_registro,
                   e.descripcion, e.cantidad_bultos, e.peso_kg, e.valor_declarado, e.costo_envio, e.observaciones,
                   ru.dias_estimados, man.codigo AS manifiesto,
                   (SELECT MAX(h.fecha) FROM historial_envio h WHERE h.id_envio = e.id_envio) AS fecha_ultimo_mov
            FROM envio e
            JOIN ruta ru ON ru.id_ruta = e.id_ruta
            JOIN agencia ao ON ao.id_agencia = ru.id_agencia_origen
            JOIN agencia ad ON ad.id_agencia = ru.id_agencia_destino
            JOIN cliente rem ON rem.id_cliente = e.id_remitente
            JOIN cliente des ON des.id_cliente = e.id_destinatario
            JOIN estado_envio es ON es.id_estado = e.id_estado_actual
            JOIN empleado em ON em.id_empleado = e.id_empleado_registro
            LEFT JOIN manifiesto man ON man.id_manifiesto = e.id_manifiesto
            """;

    /** Reserva el siguiente id de envío (sirve para armar el código de seguimiento). */
    public int siguienteId(Connection dkprCn) throws SQLException {
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(
                "SELECT nextval(pg_get_serial_sequence('envio', 'id_envio'))");
             ResultSet dkprRs = dkprPs.executeQuery()) {
            dkprRs.next();
            return dkprRs.getInt(1);
        }
    }

    public void insertar(Connection dkprCn, Envio dkprE) throws SQLException {
        String dkprSql = "INSERT INTO envio (id_envio, codigo_seguimiento, id_ruta, id_remitente, id_destinatario, "
                + "id_empleado_registro, id_estado_actual, descripcion, cantidad_bultos, peso_kg, "
                + "valor_declarado, costo_envio, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprE.getIdEnvio());
            dkprPs.setString(2, dkprE.getCodigoSeguimiento());
            dkprPs.setInt(3, dkprE.getIdRuta());
            dkprPs.setInt(4, dkprE.getIdRemitente());
            dkprPs.setInt(5, dkprE.getIdDestinatario());
            dkprPs.setInt(6, dkprE.getIdEmpleadoRegistro());
            dkprPs.setInt(7, dkprE.getIdEstadoActual());
            dkprPs.setString(8, dkprE.getDescripcion());
            dkprPs.setInt(9, dkprE.getCantidadBultos());
            dkprPs.setBigDecimal(10, dkprE.getPesoKg());
            dkprPs.setBigDecimal(11, dkprE.getValorDeclarado());
            dkprPs.setBigDecimal(12, dkprE.getCostoEnvio());
            dkprPs.setString(13, dkprE.getObservaciones());
            dkprPs.executeUpdate();
        }
    }

    /**
     * Cambia el estado solo si el envío sigue en el estado esperado (evita que dos empleados
     * muevan el mismo paquete a la vez). Devuelve false si otro ya lo cambió.
     */
    public boolean actualizarEstadoSi(Connection dkprCn, int dkprIdEnvio, int dkprEstadoEsperado, int dkprNuevoEstado,
                                      Integer dkprIdManifiesto) throws SQLException {
        String dkprSql = "UPDATE envio SET id_estado_actual = ?, id_manifiesto = COALESCE(?, id_manifiesto) "
                + "WHERE id_envio = ? AND id_estado_actual = ?";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprNuevoEstado);
            dkprPs.setObject(2, dkprIdManifiesto);
            dkprPs.setInt(3, dkprIdEnvio);
            dkprPs.setInt(4, dkprEstadoEsperado);
            return dkprPs.executeUpdate() == 1;
        }
    }

    /**
     * Envíos en un estado que pertenecen a una agencia, del más antiguo al más reciente
     * (lo que lleva más tiempo esperando se atiende primero).
     *
     * @param comoOrigen true: la agencia es el origen (por despachar); false: es el destino
     */
    public List<Envio> listarPorEstadoYAgencia(int dkprIdEstado, int dkprIdAgencia, boolean dkprComoOrigen) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.id_estado_actual = ? AND "
                + (dkprComoOrigen ? "ru.id_agencia_origen" : "ru.id_agencia_destino") + " = ? "
                + "ORDER BY e.fecha_registro, e.id_envio";
        List<Envio> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEstado);
            dkprPs.setInt(2, dkprIdAgencia);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(mapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    /** Devuelve el envío con ese código, o null si no existe. */
    public Envio buscarPorCodigo(String dkprCodigo) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.codigo_seguimiento = ?";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprCodigo);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? mapear(dkprRs) : null;
            }
        }
    }

    /** Devuelve el envío con ese id, o null si no existe. */
    public Envio buscarPorId(int dkprIdEnvio) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.id_envio = ?";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEnvio);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? mapear(dkprRs) : null;
            }
        }
    }

    /** Envíos que salieron con un manifiesto (en cualquier estado), en el orden en que se registraron. */
    public List<Envio> listarPorManifiesto(int dkprIdManifiesto) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.id_manifiesto = ? ORDER BY e.id_envio";
        List<Envio> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdManifiesto);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(mapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    /**
     * Envíos sin terminar (registrado, en tránsito o en agencia destino) cuyo último movimiento fue
     * hace más de {@code dias} días, del más antiguo al más reciente.
     *
     * @param idAgencia 0 = todas; si no, los envíos en los que la agencia tiene que actuar o esperar
     *                  (origen mientras no sale, origen o destino en camino, destino al llegar)
     */
    public List<Envio> listarEstancados(int dkprDias, int dkprIdAgencia) throws SQLException {
        String dkprSql = "SELECT * FROM (" + SELECT_BASE + """
                 WHERE e.id_estado_actual IN (1, 2, 3)) x
                WHERE COALESCE(x.fecha_ultimo_mov, x.fecha_registro)::date < CURRENT_DATE - ?
                  AND (? = 0
                       OR (x.id_estado_actual = 1 AND x.id_agencia_origen = ?)
                       OR (x.id_estado_actual = 2 AND ? IN (x.id_agencia_origen, x.id_agencia_destino))
                       OR (x.id_estado_actual = 3 AND x.id_agencia_destino = ?))
                ORDER BY COALESCE(x.fecha_ultimo_mov, x.fecha_registro), x.id_envio
                """;
        List<Envio> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprDias);
            for (int dkprI = 2; dkprI <= 5; dkprI++) {
                dkprPs.setInt(dkprI, dkprIdAgencia);
            }
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(mapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    /** Envíos registrados entre dos fechas (ambas incluidas), del más reciente al más antiguo. */
    public List<Envio> listarPorFechas(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.fecha_registro >= ? AND e.fecha_registro < ? "
                + "ORDER BY e.fecha_registro DESC, e.id_envio DESC";
        List<Envio> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setTimestamp(1, Timestamp.valueOf(dkprDesde.atStartOfDay()));
            dkprPs.setTimestamp(2, Timestamp.valueOf(dkprHasta.plusDays(1).atStartOfDay()));
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(mapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    /** Cantidad de envíos e ingresos por agencia de origen (los anulados no suman ingresos). */
    public List<ResumenFila> resumenPorAgencia(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        String dkprSql = """
                SELECT ao.nombre AS etiqueta, COUNT(*) AS cantidad,
                       COALESCE(SUM(CASE WHEN e.id_estado_actual <> 5 THEN e.costo_envio ELSE 0 END), 0) AS monto
                FROM envio e
                JOIN ruta ru ON ru.id_ruta = e.id_ruta
                JOIN agencia ao ON ao.id_agencia = ru.id_agencia_origen
                WHERE e.fecha_registro >= ? AND e.fecha_registro < ?
                GROUP BY ao.nombre
                ORDER BY ao.nombre
                """;
        return resumen(dkprSql, dkprDesde, dkprHasta);
    }

    /** Cantidad de envíos y monto por estado actual. */
    public List<ResumenFila> resumenPorEstado(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        String dkprSql = """
                SELECT es.nombre AS etiqueta, COUNT(*) AS cantidad, COALESCE(SUM(e.costo_envio), 0) AS monto
                FROM envio e
                JOIN estado_envio es ON es.id_estado = e.id_estado_actual
                WHERE e.fecha_registro >= ? AND e.fecha_registro < ?
                GROUP BY es.nombre, es.orden
                ORDER BY es.orden
                """;
        return resumen(dkprSql, dkprDesde, dkprHasta);
    }

    private List<ResumenFila> resumen(String dkprSql, LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        List<ResumenFila> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setTimestamp(1, Timestamp.valueOf(dkprDesde.atStartOfDay()));
            dkprPs.setTimestamp(2, Timestamp.valueOf(dkprHasta.plusDays(1).atStartOfDay()));
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(new ResumenFila(dkprRs.getString("etiqueta"), dkprRs.getInt("cantidad"),
                            dkprRs.getBigDecimal("monto")));
                }
            }
        }
        return dkprLista;
    }

    private Envio mapear(ResultSet dkprRs) throws SQLException {
        Envio dkprE = new Envio();
        dkprE.setIdEnvio(dkprRs.getInt("id_envio"));
        dkprE.setCodigoSeguimiento(dkprRs.getString("codigo_seguimiento"));
        dkprE.setIdRuta(dkprRs.getInt("id_ruta"));
        dkprE.setIdAgenciaOrigen(dkprRs.getInt("id_agencia_origen"));
        dkprE.setIdAgenciaDestino(dkprRs.getInt("id_agencia_destino"));
        dkprE.setOrigen(dkprRs.getString("origen"));
        dkprE.setDestino(dkprRs.getString("destino"));
        dkprE.setOrigenDireccion(dkprRs.getString("origen_direccion"));
        dkprE.setOrigenTelefono(dkprRs.getString("origen_telefono"));
        dkprE.setIdRemitente(dkprRs.getInt("id_remitente"));
        dkprE.setRemitente(dkprRs.getString("remitente"));
        dkprE.setRemitenteDocumento(dkprRs.getString("remitente_doc"));
        dkprE.setRemitenteTelefono(dkprRs.getString("remitente_tel"));
        dkprE.setIdDestinatario(dkprRs.getInt("id_destinatario"));
        dkprE.setDestinatario(dkprRs.getString("destinatario"));
        dkprE.setDestinatarioDocumento(dkprRs.getString("destinatario_doc"));
        dkprE.setDestinatarioTelefono(dkprRs.getString("destinatario_tel"));
        dkprE.setIdEmpleadoRegistro(dkprRs.getInt("id_empleado_registro"));
        dkprE.setEmpleadoRegistro(dkprRs.getString("empleado_registro"));
        dkprE.setIdEstadoActual(dkprRs.getInt("id_estado_actual"));
        dkprE.setEstado(dkprRs.getString("estado"));
        dkprE.setFechaRegistro(dkprRs.getTimestamp("fecha_registro").toLocalDateTime());
        dkprE.setDescripcion(dkprRs.getString("descripcion"));
        dkprE.setCantidadBultos(dkprRs.getInt("cantidad_bultos"));
        dkprE.setPesoKg(dkprRs.getBigDecimal("peso_kg"));
        dkprE.setValorDeclarado(dkprRs.getBigDecimal("valor_declarado"));
        dkprE.setCostoEnvio(dkprRs.getBigDecimal("costo_envio"));
        dkprE.setObservaciones(dkprRs.getString("observaciones"));
        dkprE.setDiasEstimados(dkprRs.getInt("dias_estimados"));
        dkprE.setManifiesto(dkprRs.getString("manifiesto"));
        Timestamp dkprUltimo = dkprRs.getTimestamp("fecha_ultimo_mov");
        dkprE.setFechaUltimoMovimiento(dkprUltimo == null ? null : dkprUltimo.toLocalDateTime());
        return dkprE;
    }
}
