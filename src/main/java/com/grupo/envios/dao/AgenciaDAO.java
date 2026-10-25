package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.Agencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AgenciaDAO {

    public List<Agencia> listar(boolean dkprSoloActivas) throws SQLException {
        String dkprSql = "SELECT id_agencia, nombre, ciudad, direccion, telefono, activo FROM agencia"
                + (dkprSoloActivas ? " WHERE activo = TRUE" : "")
                + " ORDER BY nombre";
        List<Agencia> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql);
             ResultSet dkprRs = dkprPs.executeQuery()) {
            while (dkprRs.next()) {
                Agencia dkprA = new Agencia();
                dkprA.setIdAgencia(dkprRs.getInt("id_agencia"));
                dkprA.setNombre(dkprRs.getString("nombre"));
                dkprA.setCiudad(dkprRs.getString("ciudad"));
                dkprA.setDireccion(dkprRs.getString("direccion"));
                dkprA.setTelefono(dkprRs.getString("telefono"));
                dkprA.setActivo(dkprRs.getBoolean("activo"));
                dkprLista.add(dkprA);
            }
        }
        return dkprLista;
    }

    public void insertar(Agencia dkprA) throws SQLException {
        String dkprSql = "INSERT INTO agencia (nombre, ciudad, direccion, telefono, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprA.getNombre());
            dkprPs.setString(2, dkprA.getCiudad());
            dkprPs.setString(3, dkprA.getDireccion());
            dkprPs.setString(4, dkprA.getTelefono());
            dkprPs.setBoolean(5, dkprA.isActivo());
            dkprPs.executeUpdate();
        }
    }

    public void actualizar(Agencia dkprA) throws SQLException {
        String dkprSql = "UPDATE agencia SET nombre = ?, ciudad = ?, direccion = ?, telefono = ?, activo = ? "
                + "WHERE id_agencia = ?";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprA.getNombre());
            dkprPs.setString(2, dkprA.getCiudad());
            dkprPs.setString(3, dkprA.getDireccion());
            dkprPs.setString(4, dkprA.getTelefono());
            dkprPs.setBoolean(5, dkprA.isActivo());
            dkprPs.setInt(6, dkprA.getIdAgencia());
            dkprPs.executeUpdate();
        }
    }
}
