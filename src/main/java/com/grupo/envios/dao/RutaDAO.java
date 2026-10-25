package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.Ruta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Rutas con su tarifa vigente (una tarifa activa por ruta). */
public class RutaDAO {

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

    public List<Ruta> listar() throws SQLException {
        return consultar(SELECT_BASE + " ORDER BY ao.nombre, ad.nombre", null);
    }

    /** Rutas activas con tarifa que salen de una agencia (para registrar envíos). */
    public List<Ruta> listarPorOrigen(int dkprIdAgenciaOrigen) throws SQLException {
        String dkprSql = SELECT_BASE
                + " WHERE r.id_agencia_origen = ? AND r.activo = TRUE AND ao.activo = TRUE AND ad.activo = TRUE"
                + " AND t.id_tarifa IS NOT NULL ORDER BY ad.nombre";
        return consultar(dkprSql, dkprIdAgenciaOrigen);
    }

    public void insertar(Ruta dkprR) throws SQLException {
        try (Connection dkprCn = Conexion.obtener()) {
            dkprCn.setAutoCommit(false);
            try {
                int dkprIdRuta;
                String dkprSql = "INSERT INTO ruta (id_agencia_origen, id_agencia_destino, dias_estimados, activo) "
                        + "VALUES (?, ?, ?, ?) RETURNING id_ruta";
                try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
                    dkprPs.setInt(1, dkprR.getIdAgenciaOrigen());
                    dkprPs.setInt(2, dkprR.getIdAgenciaDestino());
                    dkprPs.setInt(3, dkprR.getDiasEstimados());
                    dkprPs.setBoolean(4, dkprR.isActivo());
                    try (ResultSet dkprRs = dkprPs.executeQuery()) {
                        dkprRs.next();
                        dkprIdRuta = dkprRs.getInt(1);
                    }
                }
                insertarTarifa(dkprCn, dkprIdRuta, dkprR);
                dkprCn.commit();
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    public void actualizar(Ruta dkprR) throws SQLException {
        try (Connection dkprCn = Conexion.obtener()) {
            dkprCn.setAutoCommit(false);
            try {
                String dkprSql = "UPDATE ruta SET dias_estimados = ?, activo = ? WHERE id_ruta = ?";
                try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
                    dkprPs.setInt(1, dkprR.getDiasEstimados());
                    dkprPs.setBoolean(2, dkprR.isActivo());
                    dkprPs.setInt(3, dkprR.getIdRuta());
                    dkprPs.executeUpdate();
                }
                if (dkprR.getIdTarifa() > 0) {
                    String dkprSqlTarifa = "UPDATE tarifa SET precio_base = ?, precio_kg_extra = ?, peso_max_kg = ? "
                            + "WHERE id_tarifa = ?";
                    try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSqlTarifa)) {
                        dkprPs.setBigDecimal(1, dkprR.getPrecioBase());
                        dkprPs.setBigDecimal(2, dkprR.getPrecioKgExtra());
                        dkprPs.setBigDecimal(3, dkprR.getPesoMaxKg());
                        dkprPs.setInt(4, dkprR.getIdTarifa());
                        dkprPs.executeUpdate();
                    }
                } else {
                    insertarTarifa(dkprCn, dkprR.getIdRuta(), dkprR);
                }
                dkprCn.commit();
            } catch (SQLException | RuntimeException dkprE) {
                dkprCn.rollback();
                throw dkprE;
            }
        }
    }

    private void insertarTarifa(Connection dkprCn, int dkprIdRuta, Ruta dkprR) throws SQLException {
        String dkprSql = "INSERT INTO tarifa (id_ruta, precio_base, precio_kg_extra, peso_max_kg) VALUES (?, ?, ?, ?)";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdRuta);
            dkprPs.setBigDecimal(2, dkprR.getPrecioBase());
            dkprPs.setBigDecimal(3, dkprR.getPrecioKgExtra());
            dkprPs.setBigDecimal(4, dkprR.getPesoMaxKg());
            dkprPs.executeUpdate();
        }
    }

    private List<Ruta> consultar(String dkprSql, Integer dkprParametro) throws SQLException {
        List<Ruta> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            if (dkprParametro != null) {
                dkprPs.setInt(1, dkprParametro);
            }
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    Ruta dkprR = new Ruta();
                    dkprR.setIdRuta(dkprRs.getInt("id_ruta"));
                    dkprR.setIdAgenciaOrigen(dkprRs.getInt("id_agencia_origen"));
                    dkprR.setIdAgenciaDestino(dkprRs.getInt("id_agencia_destino"));
                    dkprR.setOrigen(dkprRs.getString("origen"));
                    dkprR.setDestino(dkprRs.getString("destino"));
                    dkprR.setDiasEstimados(dkprRs.getInt("dias_estimados"));
                    dkprR.setActivo(dkprRs.getBoolean("activo"));
                    dkprR.setIdTarifa(dkprRs.getInt("id_tarifa"));
                    dkprR.setPrecioBase(dkprRs.getBigDecimal("precio_base"));
                    dkprR.setPrecioKgExtra(dkprRs.getBigDecimal("precio_kg_extra"));
                    dkprR.setPesoMaxKg(dkprRs.getBigDecimal("peso_max_kg"));
                    dkprLista.add(dkprR);
                }
            }
        }
        return dkprLista;
    }
}
