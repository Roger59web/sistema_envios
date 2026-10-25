package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.Conductor;
import com.grupo.envios.model.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Vehículos y conductores (tablas vehiculo y conductor). */
public class VehiculoDAO {

    // ---------------------------------------------------------------- vehículos

    public List<Vehiculo> listarVehiculos(boolean dkprSoloActivos) throws SQLException {
        String dkprSql = "SELECT * FROM vehiculo" + (dkprSoloActivos ? " WHERE activo" : "") + " ORDER BY placa";
        List<Vehiculo> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql);
             ResultSet dkprRs = dkprPs.executeQuery()) {
            while (dkprRs.next()) {
                dkprLista.add(mapearVehiculo(dkprRs));
            }
        }
        return dkprLista;
    }

    /** Devuelve el vehículo con ese id, o null si no existe. */
    public Vehiculo buscarVehiculo(int dkprId) throws SQLException {
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement("SELECT * FROM vehiculo WHERE id_vehiculo = ?")) {
            dkprPs.setInt(1, dkprId);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? mapearVehiculo(dkprRs) : null;
            }
        }
    }

    public void guardarVehiculo(Vehiculo dkprV) throws SQLException {
        String dkprSql = dkprV.getIdVehiculo() == 0
                ? "INSERT INTO vehiculo (placa, descripcion, capacidad_kg, activo) VALUES (?, ?, ?, ?)"
                : "UPDATE vehiculo SET placa = ?, descripcion = ?, capacidad_kg = ?, activo = ? WHERE id_vehiculo = ?";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprV.getPlaca());
            dkprPs.setString(2, dkprV.getDescripcion());
            dkprPs.setBigDecimal(3, dkprV.getCapacidadKg());
            dkprPs.setBoolean(4, dkprV.isActivo());
            if (dkprV.getIdVehiculo() != 0) {
                dkprPs.setInt(5, dkprV.getIdVehiculo());
            }
            dkprPs.executeUpdate();
        }
    }

    private Vehiculo mapearVehiculo(ResultSet dkprRs) throws SQLException {
        Vehiculo dkprV = new Vehiculo();
        dkprV.setIdVehiculo(dkprRs.getInt("id_vehiculo"));
        dkprV.setPlaca(dkprRs.getString("placa"));
        dkprV.setDescripcion(dkprRs.getString("descripcion"));
        dkprV.setCapacidadKg(dkprRs.getBigDecimal("capacidad_kg"));
        dkprV.setActivo(dkprRs.getBoolean("activo"));
        return dkprV;
    }

    // --------------------------------------------------------------- conductores

    public List<Conductor> listarConductores(boolean dkprSoloActivos) throws SQLException {
        String dkprSql = "SELECT * FROM conductor" + (dkprSoloActivos ? " WHERE activo" : "") + " ORDER BY nombre_completo";
        List<Conductor> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql);
             ResultSet dkprRs = dkprPs.executeQuery()) {
            while (dkprRs.next()) {
                dkprLista.add(mapearConductor(dkprRs));
            }
        }
        return dkprLista;
    }

    /** Devuelve el conductor con ese id, o null si no existe. */
    public Conductor buscarConductor(int dkprId) throws SQLException {
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement("SELECT * FROM conductor WHERE id_conductor = ?")) {
            dkprPs.setInt(1, dkprId);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? mapearConductor(dkprRs) : null;
            }
        }
    }

    public void guardarConductor(Conductor dkprC) throws SQLException {
        String dkprSql = dkprC.getIdConductor() == 0
                ? "INSERT INTO conductor (dni, nombre_completo, licencia, telefono, activo) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE conductor SET dni = ?, nombre_completo = ?, licencia = ?, telefono = ?, activo = ? WHERE id_conductor = ?";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprC.getDni());
            dkprPs.setString(2, dkprC.getNombreCompleto());
            dkprPs.setString(3, dkprC.getLicencia());
            dkprPs.setString(4, dkprC.getTelefono());
            dkprPs.setBoolean(5, dkprC.isActivo());
            if (dkprC.getIdConductor() != 0) {
                dkprPs.setInt(6, dkprC.getIdConductor());
            }
            dkprPs.executeUpdate();
        }
    }

    private Conductor mapearConductor(ResultSet dkprRs) throws SQLException {
        Conductor dkprC = new Conductor();
        dkprC.setIdConductor(dkprRs.getInt("id_conductor"));
        dkprC.setDni(dkprRs.getString("dni"));
        dkprC.setNombreCompleto(dkprRs.getString("nombre_completo"));
        dkprC.setLicencia(dkprRs.getString("licencia"));
        dkprC.setTelefono(dkprRs.getString("telefono"));
        dkprC.setActivo(dkprRs.getBoolean("activo"));
        return dkprC;
    }
}
