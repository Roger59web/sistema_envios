package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.Empleado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EmpleadoDAO {

    private static final String SQL_POR_USUARIO = """
            SELECT e.id_empleado, e.id_agencia, a.nombre AS agencia, r.nombre AS rol,
                   e.nombres, e.apellidos, e.usuario, e.clave_hash
            FROM empleado e
            JOIN rol r     ON r.id_rol = e.id_rol
            JOIN agencia a ON a.id_agencia = e.id_agencia
            WHERE e.usuario = ? AND e.activo = TRUE
            """;

    /** Devuelve el empleado activo con ese usuario, o null si no existe. */
    public Empleado buscarPorUsuario(String usuario) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_POR_USUARIO)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Empleado e = new Empleado();
                e.setIdEmpleado(rs.getInt("id_empleado"));
                e.setIdAgencia(rs.getInt("id_agencia"));
                e.setAgencia(rs.getString("agencia"));
                e.setRol(rs.getString("rol"));
                e.setNombres(rs.getString("nombres"));
                e.setApellidos(rs.getString("apellidos"));
                e.setUsuario(rs.getString("usuario"));
                e.setClaveHash(rs.getString("clave_hash"));
                return e;
            }
        }
    }
}
