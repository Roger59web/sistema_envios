package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private static final String COLUMNAS =
            "id_cliente, tipo_documento, numero_documento, nombre_completo, telefono, email, direccion";

    /** Lista clientes cuyo documento o nombre contenga el filtro (vacío = todos, máximo 200). */
    public List<Cliente> listar(String filtro) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM cliente "
                + "WHERE numero_documento ILIKE ? OR nombre_completo ILIKE ? "
                + "ORDER BY nombre_completo LIMIT 200";
        String patron = "%" + filtro + "%";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Cliente buscarPorDocumento(String tipo, String numero) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            return buscarPorDocumento(cn, tipo, numero);
        }
    }

    /** Versión para usar dentro de una transacción. Devuelve null si no existe. */
    public Cliente buscarPorDocumento(Connection cn, String tipo, String numero) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM cliente WHERE tipo_documento = ? AND numero_documento = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setString(2, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public void insertar(Cliente c) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            insertar(cn, c);
        }
    }

    /** Versión para usar dentro de una transacción. Devuelve el id generado. */
    public int insertar(Connection cn, Cliente c) throws SQLException {
        String sql = "INSERT INTO cliente (tipo_documento, numero_documento, nombre_completo, telefono, email, direccion) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_cliente";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getTipoDocumento());
            ps.setString(2, c.getNumeroDocumento());
            ps.setString(3, c.getNombreCompleto());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getDireccion());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizar(Cliente c) throws SQLException {
        String sql = "UPDATE cliente SET tipo_documento = ?, numero_documento = ?, nombre_completo = ?, "
                + "telefono = ?, email = ?, direccion = ? WHERE id_cliente = ?";
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getTipoDocumento());
            ps.setString(2, c.getNumeroDocumento());
            ps.setString(3, c.getNombreCompleto());
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getDireccion());
            ps.setInt(7, c.getIdCliente());
            ps.executeUpdate();
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setTipoDocumento(rs.getString("tipo_documento"));
        c.setNumeroDocumento(rs.getString("numero_documento"));
        c.setNombreCompleto(rs.getString("nombre_completo"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setDireccion(rs.getString("direccion"));
        return c;
    }
}
