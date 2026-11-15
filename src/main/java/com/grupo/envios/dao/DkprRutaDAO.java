package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.model.DkprRuta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DkprRutaDAO {

    private static final String SELECT_BASE = """
            SELECT r.id_ruta, r.id_agencia_origen, r.id_agencia_destino,
                   ao.nombre AS origen, ad.nombre AS destino,
                   r.dias_estimados, r.activo,
                   t.id_tarifa, t.precio_base, t.precio_kg_extra, t.peso_max_kg
            FROM ruta r
            JOIN agencia ao ON ao.id_agencia = r.id_agencia_origen
            JOIN agencia ad ON ad.id_agencia = r.id_agencia_destino
            LEFT JOIN tarifa t ON t.id_ruta = r.id_ruta AND t.activo = TRUE
            """;

    public List<DkprRuta> dkprListar() throws SQLException {
        return dkprConsultar(SELECT_BASE + " ORDER BY ao.nombre, ad.nombre", null);
    }

    public List<DkprRuta> dkprListarPorOrigen(int dkprIdAgenciaOrigen) throws SQLException {
        String dkprSql = SELECT_BASE
                + " WHERE r.id_agencia_origen = ? AND r.activo = TRUE AND ao.activo = TRUE AND ad.activo = TRUE"
                + " AND t.id_tarifa IS NOT NULL ORDER BY ad.nombre";
        return dkprConsultar(dkprSql, dkprIdAgenciaOrigen);
    }

    public void dkprInsertar(DkprRuta dkprR) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprCn.setAutoCommit(false);
            try {
                int dkprIdRuta;
                String dkprSql = "INSERT INTO ruta (id_agencia_origen, id_agencia_destino, dias_estimados, activo) "
                        + "VALUES (?, ?, ?, ?) RETURNING id_ruta";
                try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
                    dkprPs.setInt(1, dkprR.dkprGetIdAgenciaOrigen());
                    dkprPs.setInt(2, dkprR.dkprGetIdAgenciaDestino());
                    dkprPs.setInt(3, dkprR.dkprGetDiasEstimados());
                    dkprPs.setBoolean(4, dkprR.dkprIsActivo());
                    try (ResultSet dkprRs = dkprPs.executeQuery()) {
                        dkprRs.next();
                        dkprIdRuta = dkprRs.getInt(1);
                    }
                }
                dkprInsertarTarifa(dkprCn, dkprIdRuta, dkprR);
                dkprCn.commit();
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    public void dkprActualizar(DkprRuta dkprR) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener()) {
            dkprCn.setAutoCommit(false);
            try {
                String dkprSql = "UPDATE ruta SET dias_estimados = ?, activo = ? WHERE id_ruta = ?";
                try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
                    dkprPs.setInt(1, dkprR.dkprGetDiasEstimados());
                    dkprPs.setBoolean(2, dkprR.dkprIsActivo());
                    dkprPs.setInt(3, dkprR.dkprGetIdRuta());
                    dkprPs.executeUpdate();
                }
                if (dkprR.dkprGetIdTarifa() > 0) {
                    String dkprSqlTarifa = "UPDATE tarifa SET precio_base = ?, precio_kg_extra = ?, peso_max_kg = ? "
                            + "WHERE id_tarifa = ?";
                    try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSqlTarifa)) {
                        dkprPs.setBigDecimal(1, dkprR.dkprGetPrecioBase());
                        dkprPs.setBigDecimal(2, dkprR.dkprGetPrecioKgExtra());
                        dkprPs.setBigDecimal(3, dkprR.dkprGetPesoMaxKg());
                        dkprPs.setInt(4, dkprR.dkprGetIdTarifa());
                        dkprPs.executeUpdate();
                    }
                } else {
                    dkprInsertarTarifa(dkprCn, dkprR.dkprGetIdRuta(), dkprR);
                }
                dkprCn.commit();
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    private void dkprInsertarTarifa(Connection dkprCn, int dkprIdRuta, DkprRuta dkprR) throws SQLException {
        String dkprSql = "INSERT INTO tarifa (id_ruta, precio_base, precio_kg_extra, peso_max_kg) VALUES (?, ?, ?, ?)";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdRuta);
            dkprPs.setBigDecimal(2, dkprR.dkprGetPrecioBase());
            dkprPs.setBigDecimal(3, dkprR.dkprGetPrecioKgExtra());
            dkprPs.setBigDecimal(4, dkprR.dkprGetPesoMaxKg());
            dkprPs.executeUpdate();
        }
    }

    private List<DkprRuta> dkprConsultar(String dkprSql, Integer dkprParametro) throws SQLException {
        List<DkprRuta> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            if (dkprParametro != null) {
                dkprPs.setInt(1, dkprParametro);
            }
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    DkprRuta dkprR = new DkprRuta();
                    dkprR.dkprSetIdRuta(dkprRs.getInt("id_ruta"));
                    dkprR.dkprSetIdAgenciaOrigen(dkprRs.getInt("id_agencia_origen"));
                    dkprR.dkprSetIdAgenciaDestino(dkprRs.getInt("id_agencia_destino"));
                    dkprR.dkprSetOrigen(dkprRs.getString("origen"));
                    dkprR.dkprSetDestino(dkprRs.getString("destino"));
                    dkprR.dkprSetDiasEstimados(dkprRs.getInt("dias_estimados"));
                    dkprR.dkprSetActivo(dkprRs.getBoolean("activo"));
                    dkprR.dkprSetIdTarifa(dkprRs.getInt("id_tarifa"));
                    dkprR.dkprSetPrecioBase(dkprRs.getBigDecimal("precio_base"));
                    dkprR.dkprSetPrecioKgExtra(dkprRs.getBigDecimal("precio_kg_extra"));
                    dkprR.dkprSetPesoMaxKg(dkprRs.getBigDecimal("peso_max_kg"));
                    dkprLista.add(dkprR);
                }
            }
        }
        return dkprLista;
    }
}
