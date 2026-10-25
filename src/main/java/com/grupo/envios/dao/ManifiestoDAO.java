package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.Manifiesto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManifiestoDAO {

    /** Crea el manifiesto y devuelve su id. El código se arma con el id: MAN-000001. */
    public int insertar(Connection dkprCn, int dkprIdRuta, int dkprIdEmpleado, String dkprPlaca, String dkprConductor,
                        String dkprObservacion) throws SQLException {
        int dkprId;
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(
                "SELECT nextval(pg_get_serial_sequence('manifiesto', 'id_manifiesto'))");
             ResultSet dkprRs = dkprPs.executeQuery()) {
            dkprRs.next();
            dkprId = dkprRs.getInt(1);
        }
        String dkprSql = "INSERT INTO manifiesto (id_manifiesto, codigo, id_ruta, id_empleado, placa, conductor, observacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprId);
            dkprPs.setString(2, codigo(dkprId));
            dkprPs.setInt(3, dkprIdRuta);
            dkprPs.setInt(4, dkprIdEmpleado);
            dkprPs.setString(5, dkprPlaca);
            dkprPs.setString(6, dkprConductor);
            dkprPs.setString(7, dkprObservacion);
            dkprPs.executeUpdate();
        }
        return dkprId;
    }

    public static String codigo(int dkprIdManifiesto) {
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

    /** Devuelve el manifiesto con ese id, o null si no existe. */
    public Manifiesto buscar(int dkprIdManifiesto) throws SQLException {
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(SELECT_BASE + " WHERE m.id_manifiesto = ?")) {
            dkprPs.setInt(1, dkprIdManifiesto);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? mapear(dkprRs) : null;
            }
        }
    }

    /** Devuelve el manifiesto con ese código (MAN-000001), o null si no existe. */
    public Manifiesto buscarPorCodigo(String dkprCodigo) throws SQLException {
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(SELECT_BASE + " WHERE m.codigo = ?")) {
            dkprPs.setString(1, dkprCodigo);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? mapear(dkprRs) : null;
            }
        }
    }

    /** Manifiestos que vienen hacia la agencia con al menos un envío todavía EN TRANSITO. */
    public List<Manifiesto> listarEnCaminoHacia(int dkprIdAgencia) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE ru.id_agencia_destino = ? AND EXISTS (SELECT 1 FROM envio e "
                + "WHERE e.id_manifiesto = m.id_manifiesto AND e.id_estado_actual = 2) ORDER BY m.fecha_salida";
        List<Manifiesto> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdAgencia);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(mapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    private Manifiesto mapear(ResultSet dkprRs) throws SQLException {
        Manifiesto dkprM = new Manifiesto();
        dkprM.setIdManifiesto(dkprRs.getInt("id_manifiesto"));
        dkprM.setCodigo(dkprRs.getString("codigo"));
        dkprM.setIdRuta(dkprRs.getInt("id_ruta"));
        dkprM.setIdAgenciaOrigen(dkprRs.getInt("id_agencia_origen"));
        dkprM.setIdAgenciaDestino(dkprRs.getInt("id_agencia_destino"));
        dkprM.setOrigen(dkprRs.getString("origen"));
        dkprM.setDestino(dkprRs.getString("destino"));
        dkprM.setFechaSalida(dkprRs.getTimestamp("fecha_salida").toLocalDateTime());
        dkprM.setPlaca(dkprRs.getString("placa"));
        dkprM.setConductor(dkprRs.getString("conductor"));
        dkprM.setObservacion(dkprRs.getString("observacion"));
        dkprM.setEmpleado(dkprRs.getString("empleado"));
        dkprM.setPendientes(dkprRs.getInt("pendientes"));
        return dkprM;
    }
}
