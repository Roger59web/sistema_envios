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
    public int siguienteId(Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT nextval(pg_get_serial_sequence('envio', 'id_envio'))");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public void insertar(Connection cn, Envio e) throws SQLException {
        String sql = "INSERT INTO envio (id_envio, codigo_seguimiento, id_ruta, id_remitente, id_destinatario, "
                + "id_empleado_registro, id_estado_actual, descripcion, cantidad_bultos, peso_kg, "
                + "valor_declarado, costo_envio, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, e.getIdEnvio());
            ps.setString(2, e.getCodigoSeguimiento());
            ps.setInt(3, e.getIdRuta());
            ps.setInt(4, e.getIdRemitente());
            ps.setInt(5, e.getIdDestinatario());
            ps.setInt(6, e.getIdEmpleadoRegistro());
            ps.setInt(7, e.getIdEstadoActual());
            ps.setString(8, e.getDescripcion());
            ps.setInt(9, e.getCantidadBultos());
            ps.setBigDecimal(10, e.getPesoKg());
            ps.setBigDecimal(11, e.getValorDeclarado());
            ps.setBigDecimal(12, e.getCostoEnvio());
            ps.setString(13, e.getObservaciones());
            ps.executeUpdate();
        }
    }

    /**
     * Cambia el estado solo si el envío sigue en el estado esperado (evita que dos empleados
     * muevan el mismo paquete a la vez). Devuelve false si otro ya lo cambió.
     */
    public boolean actualizarEstadoSi(Connection cn, int idEnvio, int estadoEsperado, int nuevoEstado,
                                      Integer idManifiesto) throws SQLException {
        String sql = "UPDATE envio SET id_estado_actual = ?, id_manifiesto = COALESCE(?, id_manifiesto) "
                + "WHERE id_envio = ? AND id_estado_actual = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado);
            ps.setObject(2, idManifiesto);
            ps.setInt(3, idEnvio);
            ps.setInt(4, estadoEsperado);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Envíos en un estado que pertenecen a una agencia, del más antiguo al más reciente
     * (lo que lleva más tiempo esperando se atiende primero).
     *
     * @param comoOrigen true: la agencia es el origen (por despachar); false: es el destino
     */
    public List<Envio> listarPorEstadoYAgencia(int idEstado, int idAgencia, boolean comoOrigen) throws SQLException {
        String sql = SELECT_BASE + " WHERE e.id_estado_actual = ? AND "
                + (comoOrigen ? "ru.id_agencia_origen" : "ru.id_agencia_destino") + " = ? "
                + "ORDER BY e.fecha_registro, e.id_envio";
        List<Envio> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEstado);
            ps.setInt(2, idAgencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Devuelve el envío con ese código, o null si no existe. */
    public Envio buscarPorCodigo(String codigo) throws SQLException {
        String sql = SELECT_BASE + " WHERE e.codigo_seguimiento = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Envíos registrados entre dos fechas (ambas incluidas), del más reciente al más antiguo. */
    public List<Envio> listarPorFechas(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = SELECT_BASE + " WHERE e.fecha_registro >= ? AND e.fecha_registro < ? "
                + "ORDER BY e.fecha_registro DESC, e.id_envio DESC";
        List<Envio> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Cantidad de envíos e ingresos por agencia de origen (los anulados no suman ingresos). */
    public List<ResumenFila> resumenPorAgencia(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
                SELECT ao.nombre AS etiqueta, COUNT(*) AS cantidad,
                       COALESCE(SUM(CASE WHEN e.id_estado_actual <> 5 THEN e.costo_envio ELSE 0 END), 0) AS monto
                FROM envio e
                JOIN ruta ru ON ru.id_ruta = e.id_ruta
                JOIN agencia ao ON ao.id_agencia = ru.id_agencia_origen
                WHERE e.fecha_registro >= ? AND e.fecha_registro < ?
                GROUP BY ao.nombre
                ORDER BY ao.nombre
                """;
        return resumen(sql, desde, hasta);
    }

    /** Cantidad de envíos y monto por estado actual. */
    public List<ResumenFila> resumenPorEstado(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
                SELECT es.nombre AS etiqueta, COUNT(*) AS cantidad, COALESCE(SUM(e.costo_envio), 0) AS monto
                FROM envio e
                JOIN estado_envio es ON es.id_estado = e.id_estado_actual
                WHERE e.fecha_registro >= ? AND e.fecha_registro < ?
                GROUP BY es.nombre, es.orden
                ORDER BY es.orden
                """;
        return resumen(sql, desde, hasta);
    }

    private List<ResumenFila> resumen(String sql, LocalDate desde, LocalDate hasta) throws SQLException {
        List<ResumenFila> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResumenFila(rs.getString("etiqueta"), rs.getInt("cantidad"),
                            rs.getBigDecimal("monto")));
                }
            }
        }
        return lista;
    }

    private Envio mapear(ResultSet rs) throws SQLException {
        Envio e = new Envio();
        e.setIdEnvio(rs.getInt("id_envio"));
        e.setCodigoSeguimiento(rs.getString("codigo_seguimiento"));
        e.setIdRuta(rs.getInt("id_ruta"));
        e.setIdAgenciaOrigen(rs.getInt("id_agencia_origen"));
        e.setIdAgenciaDestino(rs.getInt("id_agencia_destino"));
        e.setOrigen(rs.getString("origen"));
        e.setDestino(rs.getString("destino"));
        e.setOrigenDireccion(rs.getString("origen_direccion"));
        e.setOrigenTelefono(rs.getString("origen_telefono"));
        e.setIdRemitente(rs.getInt("id_remitente"));
        e.setRemitente(rs.getString("remitente"));
        e.setRemitenteDocumento(rs.getString("remitente_doc"));
        e.setRemitenteTelefono(rs.getString("remitente_tel"));
        e.setIdDestinatario(rs.getInt("id_destinatario"));
        e.setDestinatario(rs.getString("destinatario"));
        e.setDestinatarioDocumento(rs.getString("destinatario_doc"));
        e.setDestinatarioTelefono(rs.getString("destinatario_tel"));
        e.setIdEmpleadoRegistro(rs.getInt("id_empleado_registro"));
        e.setEmpleadoRegistro(rs.getString("empleado_registro"));
        e.setIdEstadoActual(rs.getInt("id_estado_actual"));
        e.setEstado(rs.getString("estado"));
        e.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        e.setDescripcion(rs.getString("descripcion"));
        e.setCantidadBultos(rs.getInt("cantidad_bultos"));
        e.setPesoKg(rs.getBigDecimal("peso_kg"));
        e.setValorDeclarado(rs.getBigDecimal("valor_declarado"));
        e.setCostoEnvio(rs.getBigDecimal("costo_envio"));
        e.setObservaciones(rs.getString("observaciones"));
        e.setDiasEstimados(rs.getInt("dias_estimados"));
        e.setManifiesto(rs.getString("manifiesto"));
        Timestamp ultimo = rs.getTimestamp("fecha_ultimo_mov");
        e.setFechaUltimoMovimiento(ultimo == null ? null : ultimo.toLocalDateTime());
        return e;
    }
}
