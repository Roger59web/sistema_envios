package com.grupo.envios.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}
