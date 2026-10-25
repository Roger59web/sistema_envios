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
    public Empleado buscarPorUsuario(String dkprUsuario) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.usuario = ? AND e.activo = TRUE";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprUsuario);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? mapear(dkprRs) : null;
            }
        }
    }

    public List<Empleado> listar() throws SQLException {
        String dkprSql = SELECT_BASE + " ORDER BY e.apellidos, e.nombres";
        List<Empleado> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql);
             ResultSet dkprRs = dkprPs.executeQuery()) {
            while (dkprRs.next()) {
                dkprLista.add(mapear(dkprRs));
            }
        }
        return dkprLista;
    }

    public List<Rol> listarRoles() throws SQLException {
        List<Rol> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement("SELECT id_rol, nombre FROM rol ORDER BY id_rol");
             ResultSet dkprRs = dkprPs.executeQuery()) {
            while (dkprRs.next()) {
                dkprLista.add(new Rol(dkprRs.getInt("id_rol"), dkprRs.getString("nombre")));
            }
        }
        return dkprLista;
    }

    public void insertar(Empleado dkprE) throws SQLException {
        String dkprSql = "INSERT INTO empleado (id_agencia, id_rol, dni, nombres, apellidos, usuario, clave_hash, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprE.getIdAgencia());
            dkprPs.setInt(2, dkprE.getIdRol());
            dkprPs.setString(3, dkprE.getDni());
            dkprPs.setString(4, dkprE.getNombres());
            dkprPs.setString(5, dkprE.getApellidos());
            dkprPs.setString(6, dkprE.getUsuario());
            dkprPs.setString(7, dkprE.getClaveHash());
            dkprPs.setBoolean(8, dkprE.isActivo());
            dkprPs.executeUpdate();
        }
    }

    /** Si claveHash es null, la contraseña actual no se modifica. */
    public void actualizar(Empleado dkprE) throws SQLException {
        boolean dkprCambiaClave = dkprE.getClaveHash() != null;
        String dkprSql = "UPDATE empleado SET id_agencia = ?, id_rol = ?, dni = ?, nombres = ?, apellidos = ?, "
                + "usuario = ?, activo = ?" + (dkprCambiaClave ? ", clave_hash = ?" : "")
                + " WHERE id_empleado = ?";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            int dkprI = 1;
            dkprPs.setInt(dkprI++, dkprE.getIdAgencia());
            dkprPs.setInt(dkprI++, dkprE.getIdRol());
            dkprPs.setString(dkprI++, dkprE.getDni());
            dkprPs.setString(dkprI++, dkprE.getNombres());
            dkprPs.setString(dkprI++, dkprE.getApellidos());
            dkprPs.setString(dkprI++, dkprE.getUsuario());
            dkprPs.setBoolean(dkprI++, dkprE.isActivo());
            if (dkprCambiaClave) {
                dkprPs.setString(dkprI++, dkprE.getClaveHash());
            }
            dkprPs.setInt(dkprI, dkprE.getIdEmpleado());
            dkprPs.executeUpdate();
        }
    }

    private Empleado mapear(ResultSet dkprRs) throws SQLException {
        Empleado dkprE = new Empleado();
        dkprE.setIdEmpleado(dkprRs.getInt("id_empleado"));
        dkprE.setIdAgencia(dkprRs.getInt("id_agencia"));
        dkprE.setAgencia(dkprRs.getString("agencia"));
        dkprE.setIdRol(dkprRs.getInt("id_rol"));
        dkprE.setRol(dkprRs.getString("rol"));
        dkprE.setDni(dkprRs.getString("dni"));
        dkprE.setNombres(dkprRs.getString("nombres"));
        dkprE.setApellidos(dkprRs.getString("apellidos"));
        dkprE.setUsuario(dkprRs.getString("usuario"));
        dkprE.setClaveHash(dkprRs.getString("clave_hash"));
        dkprE.setActivo(dkprRs.getBoolean("activo"));
        return dkprE;
    }
}
