package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.model.DkprAgencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DkprAgenciaDAO {

    public List<DkprAgencia> dkprListar(boolean dkprSoloActivas) throws SQLException {
        String dkprSql = "SELECT id_agencia, nombre, ciudad, direccion, telefono, activo FROM agencia"
                + (dkprSoloActivas ? " WHERE activo = TRUE" : "")
                + " ORDER BY nombre";
        List<DkprAgencia> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql);
             ResultSet dkprRs = dkprPs.executeQuery()) {
            while (dkprRs.next()) {
                DkprAgencia dkprA = new DkprAgencia();
                dkprA.dkprSetIdAgencia(dkprRs.getInt("id_agencia"));
                dkprA.dkprSetNombre(dkprRs.getString("nombre"));
                dkprA.dkprSetCiudad(dkprRs.getString("ciudad"));
                dkprA.dkprSetDireccion(dkprRs.getString("direccion"));
                dkprA.dkprSetTelefono(dkprRs.getString("telefono"));
                dkprA.dkprSetActivo(dkprRs.getBoolean("activo"));
                dkprLista.add(dkprA);
            }
        }
        return dkprLista;
    }

    public void dkprInsertar(DkprAgencia dkprA) throws SQLException {
        String dkprSql = "INSERT INTO agencia (nombre, ciudad, direccion, telefono, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprA.dkprGetNombre());
            dkprPs.setString(2, dkprA.dkprGetCiudad());
            dkprPs.setString(3, dkprA.dkprGetDireccion());
            dkprPs.setString(4, dkprA.dkprGetTelefono());
            dkprPs.setBoolean(5, dkprA.dkprIsActivo());
            dkprPs.executeUpdate();
        }
    }

    public void dkprActualizar(DkprAgencia dkprA) throws SQLException {
        String dkprSql = "UPDATE agencia SET nombre = ?, ciudad = ?, direccion = ?, telefono = ?, activo = ? "
                + "WHERE id_agencia = ?";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprA.dkprGetNombre());
            dkprPs.setString(2, dkprA.dkprGetCiudad());
            dkprPs.setString(3, dkprA.dkprGetDireccion());
            dkprPs.setString(4, dkprA.dkprGetTelefono());
            dkprPs.setBoolean(5, dkprA.dkprIsActivo());
            dkprPs.setInt(6, dkprA.dkprGetIdAgencia());
            dkprPs.executeUpdate();
        }
    }
}
