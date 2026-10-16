package com.grupo.envios.dao;

import com.grupo.envios.model.Comprobante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ComprobanteDAO {

    /** Inserta el comprobante asignándole el siguiente número de su serie. */
    public void insertar(Connection cn, Comprobante c) throws SQLException {
        String sqlNumero = "SELECT COALESCE(MAX(numero), 0) + 1 FROM comprobante WHERE tipo = ? AND serie = ?";
        try (PreparedStatement ps = cn.prepareStatement(sqlNumero)) {
            ps.setString(1, c.getTipo());
            ps.setString(2, c.getSerie());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                c.setNumero(rs.getInt(1));
            }
        }

        String sql = "INSERT INTO comprobante (id_envio, tipo, serie, numero, subtotal, igv, total, metodo_pago) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, c.getIdEnvio());
            ps.setString(2, c.getTipo());
            ps.setString(3, c.getSerie());
            ps.setInt(4, c.getNumero());
            ps.setBigDecimal(5, c.getSubtotal());
            ps.setBigDecimal(6, c.getIgv());
            ps.setBigDecimal(7, c.getTotal());
            ps.setString(8, c.getMetodoPago());
            ps.executeUpdate();
        }
    }
}
