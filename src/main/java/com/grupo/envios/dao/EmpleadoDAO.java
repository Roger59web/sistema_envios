package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    private static final String SELECT_BASE = """
            SELECT e.id_empleado, e.id_agencia, a.nombre AS agencia, e.id_rol, r.nombre AS rol,
                   e.dni, e.nombres, e.apellidos, e.usuario, e.clave_hash, e.activo
            FROM empleado e
            JOIN rol r     ON r.id_rol = e.id_rol
            JOIN agencia a ON a.id_agencia = e.id_agencia
            """;

    /** Devuelve el empleado activo con ese usuario, o null si no existe. */
    public Empleado buscarPorUsuario(String usuario) throws SQLException {
        String sql = SELECT_BASE + " WHERE e.usuario = ? AND e.activo = TRUE";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Empleado> listar() throws SQLException {
        String sql = SELECT_BASE + " ORDER BY e.apellidos, e.nombres";
        List<Empleado> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Rol> listarRoles() throws SQLException {
        List<Rol> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement("SELECT id_rol, nombre FROM rol ORDER BY id_rol");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Rol(rs.getInt("id_rol"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    public void insertar(Empleado e) throws SQLException {
        String sql = "INSERT INTO empleado (id_agencia, id_rol, dni, nombres, apellidos, usuario, clave_hash, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, e.getIdAgencia());
            ps.setInt(2, e.getIdRol());
            ps.setString(3, e.getDni());
            ps.setString(4, e.getNombres());
            ps.setString(5, e.getApellidos());
            ps.setString(6, e.getUsuario());
            ps.setString(7, e.getClaveHash());
            ps.setBoolean(8, e.isActivo());
            ps.executeUpdate();
        }
    }

    /** Si claveHash es null, la contraseña actual no se modifica. */
    public void actualizar(Empleado e) throws SQLException {
        boolean cambiaClave = e.getClaveHash() != null;
        String sql = "UPDATE empleado SET id_agencia = ?, id_rol = ?, dni = ?, nombres = ?, apellidos = ?, "
                + "usuario = ?, activo = ?" + (cambiaClave ? ", clave_hash = ?" : "")
                + " WHERE id_empleado = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            ps.setInt(i++, e.getIdAgencia());
            ps.setInt(i++, e.getIdRol());
            ps.setString(i++, e.getDni());
            ps.setString(i++, e.getNombres());
            ps.setString(i++, e.getApellidos());
            ps.setString(i++, e.getUsuario());
            ps.setBoolean(i++, e.isActivo());
            if (cambiaClave) {
                ps.setString(i++, e.getClaveHash());
            }
            ps.setInt(i, e.getIdEmpleado());
            ps.executeUpdate();
        }
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setIdEmpleado(rs.getInt("id_empleado"));
        e.setIdAgencia(rs.getInt("id_agencia"));
        e.setAgencia(rs.getString("agencia"));
        e.setIdRol(rs.getInt("id_rol"));
        e.setRol(rs.getString("rol"));
        e.setDni(rs.getString("dni"));
        e.setNombres(rs.getString("nombres"));
        e.setApellidos(rs.getString("apellidos"));
        e.setUsuario(rs.getString("usuario"));
        e.setClaveHash(rs.getString("clave_hash"));
        e.setActivo(rs.getBoolean("activo"));
        return e;
    }
}
