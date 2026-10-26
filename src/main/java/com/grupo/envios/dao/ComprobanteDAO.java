package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.Comprobante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ComprobanteDAO {

    public void insertar(Connection dkprCn, Comprobante dkprC) throws SQLException {
        String dkprSqlNumero = "SELECT COALESCE(MAX(numero), 0) + 1 FROM comprobante WHERE tipo = ? AND serie = ?";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSqlNumero)) {
            dkprPs.setString(1, dkprC.getTipo());
            dkprPs.setString(2, dkprC.getSerie());
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                dkprRs.next();
                dkprC.setNumero(dkprRs.getInt(1));
            }
        }

        String dkprSql = "INSERT INTO comprobante (id_envio, tipo, serie, numero, subtotal, igv, total, metodo_pago) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprC.getIdEnvio());
            dkprPs.setString(2, dkprC.getTipo());
            dkprPs.setString(3, dkprC.getSerie());
            dkprPs.setInt(4, dkprC.getNumero());
            dkprPs.setBigDecimal(5, dkprC.getSubtotal());
            dkprPs.setBigDecimal(6, dkprC.getIgv());
            dkprPs.setBigDecimal(7, dkprC.getTotal());
            dkprPs.setString(8, dkprC.getMetodoPago());
            dkprPs.executeUpdate();
        }
    }

    public Comprobante buscarPorEnvio(int dkprIdEnvio) throws SQLException {
        String dkprSql = "SELECT id_envio, tipo, serie, numero, fecha_emision, subtotal, igv, total, metodo_pago "
                + "FROM comprobante WHERE id_envio = ?";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEnvio);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                if (!dkprRs.next()) {
                    return null;
                }
                Comprobante dkprC = new Comprobante();
                dkprC.setIdEnvio(dkprRs.getInt("id_envio"));
                dkprC.setTipo(dkprRs.getString("tipo"));
                dkprC.setSerie(dkprRs.getString("serie"));
                dkprC.setNumero(dkprRs.getInt("numero"));
                dkprC.setFechaEmision(dkprRs.getTimestamp("fecha_emision").toLocalDateTime());
                dkprC.setSubtotal(dkprRs.getBigDecimal("subtotal"));
                dkprC.setIgv(dkprRs.getBigDecimal("igv"));
                dkprC.setTotal(dkprRs.getBigDecimal("total"));
                dkprC.setMetodoPago(dkprRs.getString("metodo_pago"));
                return dkprC;
            }
        }
    }
}
