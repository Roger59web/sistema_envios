package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.model.DkprComprobante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DkprComprobanteDAO {

    public void dkprInsertar(Connection dkprCn, DkprComprobante dkprC) throws SQLException {
        String dkprSqlNumero = "SELECT COALESCE(MAX(numero), 0) + 1 FROM comprobante WHERE tipo = ? AND serie = ?";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSqlNumero)) {
            dkprPs.setString(1, dkprC.dkprGetTipo());
            dkprPs.setString(2, dkprC.dkprGetSerie());
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                dkprRs.next();
                dkprC.dkprSetNumero(dkprRs.getInt(1));
            }
        }

        String dkprSql = "INSERT INTO comprobante (id_envio, tipo, serie, numero, subtotal, igv, total, metodo_pago) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprC.dkprGetIdEnvio());
            dkprPs.setString(2, dkprC.dkprGetTipo());
            dkprPs.setString(3, dkprC.dkprGetSerie());
            dkprPs.setInt(4, dkprC.dkprGetNumero());
            dkprPs.setBigDecimal(5, dkprC.dkprGetSubtotal());
            dkprPs.setBigDecimal(6, dkprC.dkprGetIgv());
            dkprPs.setBigDecimal(7, dkprC.dkprGetTotal());
            dkprPs.setString(8, dkprC.dkprGetMetodoPago());
            dkprPs.executeUpdate();
        }
    }

    public DkprComprobante dkprBuscarPorEnvio(int dkprIdEnvio) throws SQLException {
        String dkprSql = "SELECT id_envio, tipo, serie, numero, fecha_emision, subtotal, igv, total, metodo_pago "
                + "FROM comprobante WHERE id_envio = ?";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEnvio);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                if (!dkprRs.next()) {
                    return null;
                }
                DkprComprobante dkprC = new DkprComprobante();
                dkprC.dkprSetIdEnvio(dkprRs.getInt("id_envio"));
                dkprC.dkprSetTipo(dkprRs.getString("tipo"));
                dkprC.dkprSetSerie(dkprRs.getString("serie"));
                dkprC.dkprSetNumero(dkprRs.getInt("numero"));
                dkprC.dkprSetFechaEmision(dkprRs.getTimestamp("fecha_emision").toLocalDateTime());
                dkprC.dkprSetSubtotal(dkprRs.getBigDecimal("subtotal"));
                dkprC.dkprSetIgv(dkprRs.getBigDecimal("igv"));
                dkprC.dkprSetTotal(dkprRs.getBigDecimal("total"));
                dkprC.dkprSetMetodoPago(dkprRs.getString("metodo_pago"));
                return dkprC;
            }
        }
    }
}
