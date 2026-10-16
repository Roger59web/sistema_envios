package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.Agencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AgenciaDAO {

    public List<Agencia> listar(boolean soloActivas) throws SQLException {
        String sql = "SELECT id_agencia, nombre, ciudad, direccion, telefono, activo FROM agencia"
                + (soloActivas ? " WHERE activo = TRUE" : "")
                + " ORDER BY nombre";
        List<Agencia> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Agencia a = new Agencia();
                a.setIdAgencia(rs.getInt("id_agencia"));
                a.setNombre(rs.getString("nombre"));
                a.setCiudad(rs.getString("ciudad"));
                a.setDireccion(rs.getString("direccion"));
                a.setTelefono(rs.getString("telefono"));
                a.setActivo(rs.getBoolean("activo"));
                lista.add(a);
            }
        }
        return lista;
    }

    public void insertar(Agencia a) throws SQLException {
        String sql = "INSERT INTO agencia (nombre, ciudad, direccion, telefono, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, a.getNombre());
            ps.setString(2, a.getCiudad());
            ps.setString(3, a.getDireccion());
            ps.setString(4, a.getTelefono());
            ps.setBoolean(5, a.isActivo());
            ps.executeUpdate();
        }
    }

    public void actualizar(Agencia a) throws SQLException {
        String sql = "UPDATE agencia SET nombre = ?, ciudad = ?, direccion = ?, telefono = ?, activo = ? "
                + "WHERE id_agencia = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, a.getNombre());
            ps.setString(2, a.getCiudad());
            ps.setString(3, a.getDireccion());
            ps.setString(4, a.getTelefono());
            ps.setBoolean(5, a.isActivo());
            ps.setInt(6, a.getIdAgencia());
            ps.executeUpdate();
        }
    }
}
