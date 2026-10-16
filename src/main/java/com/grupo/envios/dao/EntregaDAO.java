package com.grupo.envios.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EntregaDAO {

    public void insertar(Connection cn, int idEnvio, int idEmpleado, String nombreReceptor,
                         String dniReceptor, String observacion) throws SQLException {
        String sql = "INSERT INTO entrega (id_envio, id_empleado, nombre_receptor, dni_receptor, observacion) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idEnvio);
            ps.setInt(2, idEmpleado);
            ps.setString(3, nombreReceptor);
            ps.setString(4, dniReceptor);
            ps.setString(5, observacion);
            ps.executeUpdate();
        }
    }
}
