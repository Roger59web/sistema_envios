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

    public void insertar(Connection dkprCn, int dkprIdEnvio, int dkprIdEstado, int dkprIdAgencia, int dkprIdEmpleado,
                         String dkprObservacion) throws SQLException {
        String dkprSql = "INSERT INTO historial_envio (id_envio, id_estado, id_agencia, id_empleado, observacion) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEnvio);
            dkprPs.setInt(2, dkprIdEstado);
            dkprPs.setInt(3, dkprIdAgencia);
            dkprPs.setInt(4, dkprIdEmpleado);
            dkprPs.setString(5, dkprObservacion);
            dkprPs.executeUpdate();
        }
    }

    public List<HistorialEnvio> listarPorEnvio(int dkprIdEnvio) throws SQLException {
        String dkprSql = """
                SELECT h.fecha, es.nombre AS estado, ag.nombre AS agencia,
                       em.nombres || ' ' || em.apellidos AS empleado, h.observacion
                FROM historial_envio h
                JOIN estado_envio es ON es.id_estado = h.id_estado
                JOIN agencia ag ON ag.id_agencia = h.id_agencia
                JOIN empleado em ON em.id_empleado = h.id_empleado
                WHERE h.id_envio = ?
                ORDER BY h.fecha, h.id_historial
                """;
        List<HistorialEnvio> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEnvio);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    HistorialEnvio dkprH = new HistorialEnvio();
                    dkprH.setFecha(dkprRs.getTimestamp("fecha").toLocalDateTime());
                    dkprH.setEstado(dkprRs.getString("estado"));
                    dkprH.setAgencia(dkprRs.getString("agencia"));
                    dkprH.setEmpleado(dkprRs.getString("empleado"));
                    dkprH.setObservacion(dkprRs.getString("observacion"));
                    dkprLista.add(dkprH);
                }
            }
        }
        return dkprLista;
    }
}
