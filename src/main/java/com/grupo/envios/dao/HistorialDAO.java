package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.HistorialEnvio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HistorialDAO {

    public void insertar(Connection cn, int idEnvio, int idEstado, int idAgencia, int idEmpleado,
                         String observacion) throws SQLException {
        String sql = "INSERT INTO historial_envio (id_envio, id_estado, id_agencia, id_empleado, observacion) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEnvio);
            ps.setInt(2, idEstado);
            ps.setInt(3, idAgencia);
            ps.setInt(4, idEmpleado);
            ps.setString(5, observacion);
            ps.executeUpdate();
        }
    }

    public List<HistorialEnvio> listarPorEnvio(int idEnvio) throws SQLException {
        String sql = """
                SELECT h.fecha, es.nombre AS estado, ag.nombre AS agencia,
                       em.nombres || ' ' || em.apellidos AS empleado, h.observacion
                FROM historial_envio h
                JOIN estado_envio es ON es.id_estado = h.id_estado
                JOIN agencia ag ON ag.id_agencia = h.id_agencia
                JOIN empleado em ON em.id_empleado = h.id_empleado
                WHERE h.id_envio = ?
                ORDER BY h.fecha, h.id_historial
                """;
        List<HistorialEnvio> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEnvio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialEnvio h = new HistorialEnvio();
                    h.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
                    h.setEstado(rs.getString("estado"));
                    h.setAgencia(rs.getString("agencia"));
                    h.setEmpleado(rs.getString("empleado"));
                    h.setObservacion(rs.getString("observacion"));
                    lista.add(h);
                }
            }
        }
        return lista;
    }
}
