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
    public List<Ruta> listarPorOrigen(int idAgenciaOrigen) throws SQLException {
        String sql = SELECT_BASE
                + " WHERE r.id_agencia_origen = ? AND r.activo = TRUE AND ao.activo = TRUE AND ad.activo = TRUE"
                + " AND t.id_tarifa IS NOT NULL ORDER BY ad.nombre";
        return consultar(sql, idAgenciaOrigen);
    }

    public void insertar(Ruta r) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                int idRuta;
                String sql = "INSERT INTO ruta (id_agencia_origen, id_agencia_destino, dias_estimados, activo) "
                        + "VALUES (?, ?, ?, ?) RETURNING id_ruta";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, r.getIdAgenciaOrigen());
                    ps.setInt(2, r.getIdAgenciaDestino());
                    ps.setInt(3, r.getDiasEstimados());
                    ps.setBoolean(4, r.isActivo());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        idRuta = rs.getInt(1);
                    }
                }
                insertarTarifa(cn, idRuta, r);
                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    public void actualizar(Ruta r) throws SQLException {
        try (Connection cn = Conexion.obtener()) {
            cn.setAutoCommit(false);
            try {
                String sql = "UPDATE ruta SET dias_estimados = ?, activo = ? WHERE id_ruta = ?";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setInt(1, r.getDiasEstimados());
                    ps.setBoolean(2, r.isActivo());
                    ps.setInt(3, r.getIdRuta());
                    ps.executeUpdate();
                }
                if (r.getIdTarifa() > 0) {
                    String sqlTarifa = "UPDATE tarifa SET precio_base = ?, precio_kg_extra = ?, peso_max_kg = ? "
                            + "WHERE id_tarifa = ?";
                    try (PreparedStatement ps = cn.prepareStatement(sqlTarifa)) {
                        ps.setBigDecimal(1, r.getPrecioBase());
                        ps.setBigDecimal(2, r.getPrecioKgExtra());
                        ps.setBigDecimal(3, r.getPesoMaxKg());
                        ps.setInt(4, r.getIdTarifa());
                        ps.executeUpdate();
                    }
                } else {
                    insertarTarifa(cn, r.getIdRuta(), r);
                }
                cn.commit();
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            }
        }
    }

    private void insertarTarifa(Connection cn, int idRuta, Ruta r) throws SQLException {
        String sql = "INSERT INTO tarifa (id_ruta, precio_base, precio_kg_extra, peso_max_kg) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRuta);
            ps.setBigDecimal(2, r.getPrecioBase());
            ps.setBigDecimal(3, r.getPrecioKgExtra());
            ps.setBigDecimal(4, r.getPesoMaxKg());
            ps.executeUpdate();
        }
    }

    private List<Ruta> consultar(String sql, Integer parametro) throws SQLException {
        List<Ruta> lista = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setInt(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ruta r = new Ruta();
                    r.setIdRuta(rs.getInt("id_ruta"));
                    r.setIdAgenciaOrigen(rs.getInt("id_agencia_origen"));
                    r.setIdAgenciaDestino(rs.getInt("id_agencia_destino"));
                    r.setOrigen(rs.getString("origen"));
                    r.setDestino(rs.getString("destino"));
                    r.setDiasEstimados(rs.getInt("dias_estimados"));
                    r.setActivo(rs.getBoolean("activo"));
                    r.setIdTarifa(rs.getInt("id_tarifa"));
                    r.setPrecioBase(rs.getBigDecimal("precio_base"));
                    r.setPrecioKgExtra(rs.getBigDecimal("precio_kg_extra"));
                    r.setPesoMaxKg(rs.getBigDecimal("peso_max_kg"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }
}
