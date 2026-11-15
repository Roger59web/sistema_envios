package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.model.DkprCliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DkprClienteDAO {

    private static final String COLUMNAS =
            "id_cliente, tipo_documento, numero_documento, nombre_completo, telefono, email, direccion";

    public List<DkprCliente> dkprListar(String dkprFiltro) throws SQLException {
        String dkprSql = "SELECT " + COLUMNAS + " FROM cliente "
                + "WHERE numero_documento ILIKE ? OR nombre_completo ILIKE ? "
                + "ORDER BY nombre_completo LIMIT 200";
        String dkprPatron = "%" + dkprFiltro + "%";
        List<DkprCliente> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprPatron);
            dkprPs.setString(2, dkprPatron);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(dkprMapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    public DkprCliente dkprBuscarPorDocumento(String dkprTipo, String dkprNumero) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            return dkprBuscarPorDocumento(dkprCn, dkprTipo, dkprNumero);
        }
    }

    public DkprCliente dkprBuscarPorDocumento(Connection dkprCn, String dkprTipo, String dkprNumero) throws SQLException {
        String dkprSql = "SELECT " + COLUMNAS + " FROM cliente WHERE tipo_documento = ? AND numero_documento = ?";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprTipo);
            dkprPs.setString(2, dkprNumero);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? dkprMapear(dkprRs) : null;
            }
        }
    }

    public void dkprInsertar(DkprCliente dkprC) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprInsertar(dkprCn, dkprC);
        }
    }

    public int dkprInsertar(Connection dkprCn, DkprCliente dkprC) throws SQLException {
        String dkprSql = "INSERT INTO cliente (tipo_documento, numero_documento, nombre_completo, telefono, email, direccion) "
                + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_cliente";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprC.dkprGetTipoDocumento());
            dkprPs.setString(2, dkprC.dkprGetNumeroDocumento());
            dkprPs.setString(3, dkprC.dkprGetNombreCompleto());
            dkprPs.setString(4, dkprC.dkprGetTelefono());
            dkprPs.setString(5, dkprC.dkprGetEmail());
            dkprPs.setString(6, dkprC.dkprGetDireccion());
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                dkprRs.next();
                return dkprRs.getInt(1);
            }
        }
    }

    public void dkprActualizar(DkprCliente dkprC) throws SQLException {
        String dkprSql = "UPDATE cliente SET tipo_documento = ?, numero_documento = ?, nombre_completo = ?, "
                + "telefono = ?, email = ?, direccion = ? WHERE id_cliente = ?";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprC.dkprGetTipoDocumento());
            dkprPs.setString(2, dkprC.dkprGetNumeroDocumento());
            dkprPs.setString(3, dkprC.dkprGetNombreCompleto());
            dkprPs.setString(4, dkprC.dkprGetTelefono());
            dkprPs.setString(5, dkprC.dkprGetEmail());
            dkprPs.setString(6, dkprC.dkprGetDireccion());
            dkprPs.setInt(7, dkprC.dkprGetIdCliente());
            dkprPs.executeUpdate();
        }
    }

    private DkprCliente dkprMapear(ResultSet dkprRs) throws SQLException {
        DkprCliente dkprC = new DkprCliente();
        dkprC.dkprSetIdCliente(dkprRs.getInt("id_cliente"));
        dkprC.dkprSetTipoDocumento(dkprRs.getString("tipo_documento"));
        dkprC.dkprSetNumeroDocumento(dkprRs.getString("numero_documento"));
        dkprC.dkprSetNombreCompleto(dkprRs.getString("nombre_completo"));
        dkprC.dkprSetTelefono(dkprRs.getString("telefono"));
        dkprC.dkprSetEmail(dkprRs.getString("email"));
        dkprC.dkprSetDireccion(dkprRs.getString("direccion"));
        return dkprC;
    }
}
