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

    public List<Cliente> listar(String dkprFiltro) throws SQLException {
        String dkprSql = "SELECT " + COLUMNAS + " FROM cliente "
                + "WHERE numero_documento ILIKE ? OR nombre_completo ILIKE ? "
                + "ORDER BY nombre_completo LIMIT 200";
        String dkprPatron = "%" + dkprFiltro + "%";
        List<Cliente> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprPatron);
            dkprPs.setString(2, dkprPatron);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(mapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    public Cliente buscarPorDocumento(String dkprTipo, String dkprNumero) throws SQLException {
        try (Connection dkprCn = Conexion.obtener()) {
            return buscarPorDocumento(dkprCn, dkprTipo, dkprNumero);
        }
    }

    public Cliente buscarPorDocumento(Connection dkprCn, String dkprTipo, String dkprNumero) throws SQLException {
        String dkprSql = "SELECT " + COLUMNAS + " FROM cliente WHERE tipo_documento = ? AND numero_documento = ?";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprTipo);
            dkprPs.setString(2, dkprNumero);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? mapear(dkprRs) : null;
            }
        }
    }

    public void insertar(Cliente dkprC) throws SQLException {
        try (Connection dkprCn = Conexion.obtener()) {
            insertar(dkprCn, dkprC);
        }
    }

    public int insertar(Connection dkprCn, Cliente dkprC) throws SQLException {
        String dkprSql = "INSERT INTO cliente (tipo_documento, numero_documento, nombre_completo, telefono, email, direccion) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_cliente";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprC.getTipoDocumento());
            dkprPs.setString(2, dkprC.getNumeroDocumento());
            dkprPs.setString(3, dkprC.getNombreCompleto());
            dkprPs.setString(4, dkprC.getTelefono());
            dkprPs.setString(5, dkprC.getEmail());
            dkprPs.setString(6, dkprC.getDireccion());
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                dkprRs.next();
                return dkprRs.getInt(1);
            }
        }
    }

    public void actualizar(Cliente dkprC) throws SQLException {
        String dkprSql = "UPDATE cliente SET tipo_documento = ?, numero_documento = ?, nombre_completo = ?, "
                + "telefono = ?, email = ?, direccion = ? WHERE id_cliente = ?";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprC.getTipoDocumento());
            dkprPs.setString(2, dkprC.getNumeroDocumento());
            dkprPs.setString(3, dkprC.getNombreCompleto());
            dkprPs.setString(4, dkprC.getTelefono());
            dkprPs.setString(5, dkprC.getEmail());
            dkprPs.setString(6, dkprC.getDireccion());
            dkprPs.setInt(7, dkprC.getIdCliente());
            dkprPs.executeUpdate();
        }
    }

    private Cliente mapear(ResultSet dkprRs) throws SQLException {
        Cliente dkprC = new Cliente();
        dkprC.setIdCliente(dkprRs.getInt("id_cliente"));
        dkprC.setTipoDocumento(dkprRs.getString("tipo_documento"));
        dkprC.setNumeroDocumento(dkprRs.getString("numero_documento"));
        dkprC.setNombreCompleto(dkprRs.getString("nombre_completo"));
        dkprC.setTelefono(dkprRs.getString("telefono"));
        dkprC.setEmail(dkprRs.getString("email"));
        dkprC.setDireccion(dkprRs.getString("direccion"));
        return dkprC;
    }
}
