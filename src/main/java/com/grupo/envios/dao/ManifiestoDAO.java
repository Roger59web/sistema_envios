package com.grupo.envios.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManifiestoDAO {

    /** Crea el manifiesto y devuelve su id. El código se arma con el id: MAN-000001. */
    public int insertar(Connection cn, int idRuta, int idEmpleado, String placa, String conductor,
                        String observacion) throws SQLException {
        int id;
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT nextval(pg_get_serial_sequence('manifiesto', 'id_manifiesto'))");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            id = rs.getInt(1);
        }
        String sql = "INSERT INTO manifiesto (id_manifiesto, codigo, id_ruta, id_empleado, placa, conductor, observacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, codigo(id));
            ps.setInt(3, idRuta);
            ps.setInt(4, idEmpleado);
            ps.setString(5, placa);
            ps.setString(6, conductor);
            ps.setString(7, observacion);
            ps.executeUpdate();
        }
        return id;
    }

    public static String codigo(int idManifiesto) {
        return String.format("MAN-%06d", idManifiesto);
    }
}
