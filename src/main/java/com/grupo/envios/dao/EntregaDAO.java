package com.grupo.envios.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EntregaDAO {

    public void insertar(Connection dkprCn, int dkprIdEnvio, int dkprIdEmpleado, String dkprNombreReceptor,
                         String dkprDniReceptor, String dkprObservacion) throws SQLException {
        String dkprSql = "INSERT INTO entrega (id_envio, id_empleado, nombre_receptor, dni_receptor, observacion) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEnvio);
            dkprPs.setInt(2, dkprIdEmpleado);
            dkprPs.setString(3, dkprNombreReceptor);
            dkprPs.setString(4, dkprDniReceptor);
            dkprPs.setString(5, dkprObservacion);
            dkprPs.executeUpdate();
        }
    }
}
