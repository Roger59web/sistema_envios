package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.model.DkprConductor;
import com.grupo.envios.model.DkprManifiesto;
import com.grupo.envios.model.DkprVehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DkprManifiestoDAO {

    public int dkprInsertar(Connection dkprCn, int dkprIdRuta, int dkprIdEmpleado, DkprVehiculo dkprVehiculo, DkprConductor dkprConductor,
                        String dkprObservacion) throws SQLException {
        int dkprId;
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(
                "SELECT nextval(pg_get_serial_sequence('manifiesto', 'id_manifiesto'))");
             ResultSet dkprRs = dkprPs.executeQuery()) {
            dkprRs.next();
            dkprId = dkprRs.getInt(1);
        }
        String dkprSql = "INSERT INTO manifiesto (id_manifiesto, codigo, id_ruta, id_empleado, placa, conductor, observacion, "
                + "id_vehiculo, id_conductor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprId);
            dkprPs.setString(2, dkprCodigo(dkprId));
            dkprPs.setInt(3, dkprIdRuta);
            dkprPs.setInt(4, dkprIdEmpleado);
            dkprPs.setString(5, dkprVehiculo.dkprGetPlaca());
            dkprPs.setString(6, dkprConductor.dkprGetTextoManifiesto());
            dkprPs.setString(7, dkprObservacion);
            dkprPs.setInt(8, dkprVehiculo.dkprGetIdVehiculo());
            dkprPs.setInt(9, dkprConductor.dkprGetIdConductor());
            dkprPs.executeUpdate();
        }
        return dkprId;
    }

    public static String dkprCodigo(int dkprIdManifiesto) {
        return String.format("MAN-%06d", dkprIdManifiesto);
    }

    private static final String SELECT_BASE = """
            SELECT m.id_manifiesto, m.codigo, m.id_ruta, ru.id_agencia_origen, ru.id_agencia_destino,
                   ao.nombre AS origen, ad.nombre AS destino, m.fecha_salida, m.placa, m.conductor, m.observacion,
                   em.nombres || ' ' || em.apellidos AS empleado,
                   (SELECT COUNT(*) FROM envio e
                    WHERE e.id_manifiesto = m.id_manifiesto AND e.id_estado_actual = 2) AS pendientes
            FROM manifiesto m
            JOIN ruta ru ON ru.id_ruta = m.id_ruta
            JOIN agencia ao ON ao.id_agencia = ru.id_agencia_origen
            JOIN agencia ad ON ad.id_agencia = ru.id_agencia_destino
            JOIN empleado em ON em.id_empleado = m.id_empleado
            """;

    public DkprManifiesto dkprBuscar(int dkprIdManifiesto) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(SELECT_BASE + " WHERE m.id_manifiesto = ?")) {
            dkprPs.setInt(1, dkprIdManifiesto);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? dkprMapear(dkprRs) : null;
            }
        }
    }

    public DkprManifiesto dkprBuscarPorCodigo(String dkprCodigo) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(SELECT_BASE + " WHERE m.codigo = ?")) {
            dkprPs.setString(1, dkprCodigo);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? dkprMapear(dkprRs) : null;
            }
        }
    }

    public List<DkprManifiesto> dkprListarEnCaminoHacia(int dkprIdAgencia) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE ru.id_agencia_destino = ? AND EXISTS (SELECT 1 FROM envio e "
                + "WHERE e.id_manifiesto = m.id_manifiesto AND e.id_estado_actual = 2) ORDER BY m.fecha_salida";
        List<DkprManifiesto> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdAgencia);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(dkprMapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    private DkprManifiesto dkprMapear(ResultSet dkprRs) throws SQLException {
        DkprManifiesto dkprM = new DkprManifiesto();
        dkprM.dkprSetIdManifiesto(dkprRs.getInt("id_manifiesto"));
        dkprM.dkprSetCodigo(dkprRs.getString("codigo"));
        dkprM.dkprSetIdRuta(dkprRs.getInt("id_ruta"));
        dkprM.dkprSetIdAgenciaOrigen(dkprRs.getInt("id_agencia_origen"));
        dkprM.dkprSetIdAgenciaDestino(dkprRs.getInt("id_agencia_destino"));
        dkprM.dkprSetOrigen(dkprRs.getString("origen"));
        dkprM.dkprSetDestino(dkprRs.getString("destino"));
        dkprM.dkprSetFechaSalida(dkprRs.getTimestamp("fecha_salida").toLocalDateTime());
        dkprM.dkprSetPlaca(dkprRs.getString("placa"));
        dkprM.dkprSetConductor(dkprRs.getString("conductor"));
        dkprM.dkprSetObservacion(dkprRs.getString("observacion"));
        dkprM.dkprSetEmpleado(dkprRs.getString("empleado"));
        dkprM.dkprSetPendientes(dkprRs.getInt("pendientes"));
        return dkprM;
    }
}
