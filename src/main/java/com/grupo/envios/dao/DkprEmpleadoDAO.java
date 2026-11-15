package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprRol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DkprEmpleadoDAO {

    private static final String SELECT_BASE = """
            SELECT e.id_empleado, e.id_agencia, a.nombre AS agencia, e.id_rol, r.nombre AS rol,
                   e.dni, e.nombres, e.apellidos, e.usuario, e.clave_hash, e.activo
            FROM empleado e
            JOIN rol r     ON r.id_rol = e.id_rol
            JOIN agencia a ON a.id_agencia = e.id_agencia
            """;

    public DkprEmpleado dkprBuscarPorUsuario(String dkprUsuario) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.usuario = ? AND e.activo = TRUE";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprUsuario);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? dkprMapear(dkprRs) : null;
            }
        }
    }

    public List<DkprEmpleado> dkprListar() throws SQLException {
        String dkprSql = SELECT_BASE + " ORDER BY e.apellidos, e.nombres";
        List<DkprEmpleado> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql);
             ResultSet dkprRs = dkprPs.executeQuery()) {
            while (dkprRs.next()) {
                dkprLista.add(dkprMapear(dkprRs));
            }
        }
        return dkprLista;
    }

    public List<DkprRol> dkprListarRoles() throws SQLException {
        List<DkprRol> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement("SELECT id_rol, nombre FROM rol ORDER BY id_rol");
             ResultSet dkprRs = dkprPs.executeQuery()) {
            while (dkprRs.next()) {
                dkprLista.add(new DkprRol(dkprRs.getInt("id_rol"), dkprRs.getString("nombre")));
            }
        }
        return dkprLista;
    }

    public void dkprInsertar(DkprEmpleado dkprE) throws SQLException {
        String dkprSql = "INSERT INTO empleado (id_agencia, id_rol, dni, nombres, apellidos, usuario, clave_hash, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprE.dkprGetIdAgencia());
            dkprPs.setInt(2, dkprE.dkprGetIdRol());
            dkprPs.setString(3, dkprE.dkprGetDni());
            dkprPs.setString(4, dkprE.dkprGetNombres());
            dkprPs.setString(5, dkprE.dkprGetApellidos());
            dkprPs.setString(6, dkprE.dkprGetUsuario());
            dkprPs.setString(7, dkprE.dkprGetClaveHash());
            dkprPs.setBoolean(8, dkprE.dkprIsActivo());
            dkprPs.executeUpdate();
        }
    }

    public void dkprActualizar(DkprEmpleado dkprE) throws SQLException {
        boolean dkprCambiaClave = dkprE.dkprGetClaveHash() != null;
        String dkprSql = "UPDATE empleado SET id_agencia = ?, id_rol = ?, dni = ?, nombres = ?, apellidos = ?, "
                + "usuario = ?, activo = ?" + (dkprCambiaClave ? ", clave_hash = ?" : "")
                + " WHERE id_empleado = ?";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            int dkprI = 1;
            dkprPs.setInt(dkprI++, dkprE.dkprGetIdAgencia());
            dkprPs.setInt(dkprI++, dkprE.dkprGetIdRol());
            dkprPs.setString(dkprI++, dkprE.dkprGetDni());
            dkprPs.setString(dkprI++, dkprE.dkprGetNombres());
            dkprPs.setString(dkprI++, dkprE.dkprGetApellidos());
            dkprPs.setString(dkprI++, dkprE.dkprGetUsuario());
            dkprPs.setBoolean(dkprI++, dkprE.dkprIsActivo());
            if (dkprCambiaClave) {
                dkprPs.setString(dkprI++, dkprE.dkprGetClaveHash());
            }
            dkprPs.setInt(dkprI, dkprE.dkprGetIdEmpleado());
            dkprPs.executeUpdate();
        }
    }

    private DkprEmpleado dkprMapear(ResultSet dkprRs) throws SQLException {
        DkprEmpleado dkprE = new DkprEmpleado();
        dkprE.dkprSetIdEmpleado(dkprRs.getInt("id_empleado"));
        dkprE.dkprSetIdAgencia(dkprRs.getInt("id_agencia"));
        dkprE.dkprSetAgencia(dkprRs.getString("agencia"));
        dkprE.dkprSetIdRol(dkprRs.getInt("id_rol"));
        dkprE.dkprSetRol(dkprRs.getString("rol"));
        dkprE.dkprSetDni(dkprRs.getString("dni"));
        dkprE.dkprSetNombres(dkprRs.getString("nombres"));
        dkprE.dkprSetApellidos(dkprRs.getString("apellidos"));
        dkprE.dkprSetUsuario(dkprRs.getString("usuario"));
        dkprE.dkprSetClaveHash(dkprRs.getString("clave_hash"));
        dkprE.dkprSetActivo(dkprRs.getBoolean("activo"));
        return dkprE;
    }
}
