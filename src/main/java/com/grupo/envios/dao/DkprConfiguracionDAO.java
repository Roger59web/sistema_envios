package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DkprConfiguracionDAO {

    public String dkprObtener(String dkprClave) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement("SELECT valor FROM configuracion WHERE clave = ?")) {
            dkprPs.setString(1, dkprClave);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? dkprRs.getString(1) : null;
            }
        }
    }

    public void dkprGuardar(String dkprClave, String dkprValor) throws SQLException {
        String dkprSql = "INSERT INTO configuracion (clave, valor) VALUES (?, ?) "
                + "ON CONFLICT (clave) DO UPDATE SET valor = EXCLUDED.valor";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprClave);
            dkprPs.setString(2, dkprValor);
            dkprPs.executeUpdate();
        }
    }
}
