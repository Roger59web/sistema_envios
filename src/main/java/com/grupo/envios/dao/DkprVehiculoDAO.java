package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.model.DkprConductor;
import com.grupo.envios.model.DkprVehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DkprVehiculoDAO {

    public List<DkprVehiculo> dkprListarVehiculos(boolean dkprSoloActivos) throws SQLException {
        String dkprSql = "SELECT * FROM vehiculo" + (dkprSoloActivos ? " WHERE activo" : "") + " ORDER BY placa";
        List<DkprVehiculo> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql);
             ResultSet dkprRs = dkprPs.executeQuery()) {
            while (dkprRs.next()) {
                dkprLista.add(dkprMapearVehiculo(dkprRs));
            }
        }
        return dkprLista;
    }

    public DkprVehiculo dkprBuscarVehiculo(int dkprId) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement("SELECT * FROM vehiculo WHERE id_vehiculo = ?")) {
            dkprPs.setInt(1, dkprId);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? dkprMapearVehiculo(dkprRs) : null;
            }
        }
    }

    public void dkprGuardarVehiculo(DkprVehiculo dkprV) throws SQLException {
        String dkprSql = dkprV.dkprGetIdVehiculo() == 0
                ? "INSERT INTO vehiculo (placa, descripcion, capacidad_kg, activo) VALUES (?, ?, ?, ?)"
                : "UPDATE vehiculo SET placa = ?, descripcion = ?, capacidad_kg = ?, activo = ? WHERE id_vehiculo = ?";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprV.dkprGetPlaca());
            dkprPs.setString(2, dkprV.dkprGetDescripcion());
            dkprPs.setBigDecimal(3, dkprV.dkprGetCapacidadKg());
            dkprPs.setBoolean(4, dkprV.dkprIsActivo());
            if (dkprV.dkprGetIdVehiculo() != 0) {
                dkprPs.setInt(5, dkprV.dkprGetIdVehiculo());
            }
            dkprPs.executeUpdate();
        }
    }

    private DkprVehiculo dkprMapearVehiculo(ResultSet dkprRs) throws SQLException {
        DkprVehiculo dkprV = new DkprVehiculo();
        dkprV.dkprSetIdVehiculo(dkprRs.getInt("id_vehiculo"));
        dkprV.dkprSetPlaca(dkprRs.getString("placa"));
        dkprV.dkprSetDescripcion(dkprRs.getString("descripcion"));
        dkprV.dkprSetCapacidadKg(dkprRs.getBigDecimal("capacidad_kg"));
        dkprV.dkprSetActivo(dkprRs.getBoolean("activo"));
        return dkprV;
    }

    public List<DkprConductor> dkprListarConductores(boolean dkprSoloActivos) throws SQLException {
        String dkprSql = "SELECT * FROM conductor" + (dkprSoloActivos ? " WHERE activo" : "") + " ORDER BY nombre_completo";
        List<DkprConductor> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql);
             ResultSet dkprRs = dkprPs.executeQuery()) {
            while (dkprRs.next()) {
                dkprLista.add(dkprMapearConductor(dkprRs));
            }
        }
        return dkprLista;
    }

    public DkprConductor dkprBuscarConductor(int dkprId) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement("SELECT * FROM conductor WHERE id_conductor = ?")) {
            dkprPs.setInt(1, dkprId);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? dkprMapearConductor(dkprRs) : null;
            }
        }
    }

    public void dkprGuardarConductor(DkprConductor dkprC) throws SQLException {
        String dkprSql = dkprC.dkprGetIdConductor() == 0
                ? "INSERT INTO conductor (dni, nombre_completo, licencia, telefono, activo) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE conductor SET dni = ?, nombre_completo = ?, licencia = ?, telefono = ?, activo = ? WHERE id_conductor = ?";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprC.dkprGetDni());
            dkprPs.setString(2, dkprC.dkprGetNombreCompleto());
            dkprPs.setString(3, dkprC.dkprGetLicencia());
            dkprPs.setString(4, dkprC.dkprGetTelefono());
            dkprPs.setBoolean(5, dkprC.dkprIsActivo());
            if (dkprC.dkprGetIdConductor() != 0) {
                dkprPs.setInt(6, dkprC.dkprGetIdConductor());
            }
            dkprPs.executeUpdate();
        }
    }

    private DkprConductor dkprMapearConductor(ResultSet dkprRs) throws SQLException {
        DkprConductor dkprC = new DkprConductor();
        dkprC.dkprSetIdConductor(dkprRs.getInt("id_conductor"));
        dkprC.dkprSetDni(dkprRs.getString("dni"));
        dkprC.dkprSetNombreCompleto(dkprRs.getString("nombre_completo"));
        dkprC.dkprSetLicencia(dkprRs.getString("licencia"));
        dkprC.dkprSetTelefono(dkprRs.getString("telefono"));
        dkprC.dkprSetActivo(dkprRs.getBoolean("activo"));
        return dkprC;
    }
}
