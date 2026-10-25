package com.grupo.envios.service;

import com.grupo.envios.dao.VehiculoDAO;
import com.grupo.envios.model.Conductor;
import com.grupo.envios.model.Vehiculo;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Validaciones;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/** Registro de vehículos y conductores, y su validación al despachar. */
public class VehiculoService {

    private static final BigDecimal CAPACIDAD_MAXIMA = new BigDecimal("99999");

    private final VehiculoDAO dkprDao = new VehiculoDAO();

    public List<Vehiculo> listarVehiculos(boolean dkprSoloActivos) throws SQLException {
        return dkprDao.listarVehiculos(dkprSoloActivos);
    }

    public List<Conductor> listarConductores(boolean dkprSoloActivos) throws SQLException {
        return dkprDao.listarConductores(dkprSoloActivos);
    }

    /** Placa peruana normalizada a ABC-123 (acepta abc123, ABC 123, A1B-234...). */
    public static String normalizarPlaca(String dkprPlaca) {
        String dkprP = Validaciones.requerido(dkprPlaca, "Placa", 10).toUpperCase(Locale.ROOT).replaceAll("[\\s-]", "");
        if (!dkprP.matches("[A-Z0-9]{6}")) {
            throw new NegocioException("La placa debe tener 6 letras o números, por ejemplo ABC-123.");
        }
        return dkprP.substring(0, 3) + "-" + dkprP.substring(3);
    }

    public void guardarVehiculo(Vehiculo dkprV) throws SQLException {
        dkprV.setPlaca(normalizarPlaca(dkprV.getPlaca()));
        dkprV.setDescripcion(Validaciones.opcional(dkprV.getDescripcion(), "Descripción", 100));
        BigDecimal dkprCapacidad = dkprV.getCapacidadKg();
        if (dkprCapacidad != null && (dkprCapacidad.signum() <= 0 || dkprCapacidad.compareTo(CAPACIDAD_MAXIMA) > 0)) {
            throw new NegocioException("La capacidad debe ser mayor que 0 y como máximo " + CAPACIDAD_MAXIMA + " kg.");
        }
        dkprDao.guardarVehiculo(dkprV);
    }

    public void guardarConductor(Conductor dkprC) throws SQLException {
        Validaciones.validarDni(dkprC.getDni(), "El DNI del conductor");
        dkprC.setDni(dkprC.getDni().trim());
        dkprC.setNombreCompleto(Validaciones.requerido(dkprC.getNombreCompleto(), "Nombre completo", 80));
        String dkprLicencia = Validaciones.requerido(dkprC.getLicencia(), "Licencia", 12).toUpperCase(Locale.ROOT);
        if (!dkprLicencia.matches("[A-Z0-9]{6,12}")) {
            throw new NegocioException("La licencia (brevete) debe tener entre 6 y 12 letras o números, por ejemplo Q12345678.");
        }
        dkprC.setLicencia(dkprLicencia);
        String dkprTelefono = Validaciones.opcional(dkprC.getTelefono(), "Teléfono", 15);
        Validaciones.validarTelefono(dkprTelefono);
        dkprC.setTelefono(dkprTelefono);
        dkprDao.guardarConductor(dkprC);
    }

    /** Vehículo elegido para un despacho: debe existir y estar activo. */
    public Vehiculo vehiculoParaDespacho(Integer dkprIdVehiculo) throws SQLException {
        if (dkprIdVehiculo == null) {
            throw new NegocioException("Elige el vehículo que lleva la carga (se registran en Vehículos y conductores).");
        }
        Vehiculo dkprV = dkprDao.buscarVehiculo(dkprIdVehiculo);
        if (dkprV == null || !dkprV.isActivo()) {
            throw new NegocioException("El vehículo elegido no existe o está inactivo.");
        }
        return dkprV;
    }

    /** Conductor elegido para un despacho: debe existir y estar activo. */
    public Conductor conductorParaDespacho(Integer dkprIdConductor) throws SQLException {
        if (dkprIdConductor == null) {
            throw new NegocioException("Elige el conductor (se registran en Vehículos y conductores).");
        }
        Conductor dkprC = dkprDao.buscarConductor(dkprIdConductor);
        if (dkprC == null || !dkprC.isActivo()) {
            throw new NegocioException("El conductor elegido no existe o está inactivo.");
        }
        return dkprC;
    }
}
