package com.grupo.envios.service;

import com.grupo.envios.dao.DkprVehiculoDAO;
import com.grupo.envios.model.DkprConductor;
import com.grupo.envios.model.DkprVehiculo;
import com.grupo.envios.util.DkprNegocioException;
import com.grupo.envios.util.DkprValidaciones;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class DkprVehiculoService {

    private static final BigDecimal CAPACIDAD_MAXIMA = new BigDecimal("99999");

    private final DkprVehiculoDAO dkprDao = new DkprVehiculoDAO();

    public List<DkprVehiculo> dkprListarVehiculos(boolean dkprSoloActivos) throws SQLException {
        return dkprDao.dkprListarVehiculos(dkprSoloActivos);
    }

    public List<DkprConductor> dkprListarConductores(boolean dkprSoloActivos) throws SQLException {
        return dkprDao.dkprListarConductores(dkprSoloActivos);
    }

    public static String dkprNormalizarPlaca(String dkprPlaca) {
        String dkprP = DkprValidaciones.dkprRequerido(dkprPlaca, "Placa", 10).toUpperCase(Locale.ROOT).replaceAll("[\\s-]", "");
        if (!dkprP.matches("[A-Z0-9]{6}")) {
            throw new DkprNegocioException("La placa debe tener 6 letras o números, por ejemplo ABC-123.");
        }
        return dkprP.substring(0, 3) + "-" + dkprP.substring(3);
    }

    public void dkprGuardarVehiculo(DkprVehiculo dkprV) throws SQLException {
        dkprV.dkprSetPlaca(dkprNormalizarPlaca(dkprV.dkprGetPlaca()));
        dkprV.dkprSetDescripcion(DkprValidaciones.dkprOpcional(dkprV.dkprGetDescripcion(), "Descripción", 100));
        BigDecimal dkprCapacidad = dkprV.dkprGetCapacidadKg();
        if (dkprCapacidad != null && (dkprCapacidad.signum() <= 0 || dkprCapacidad.compareTo(CAPACIDAD_MAXIMA) > 0)) {
            throw new DkprNegocioException("La capacidad debe ser mayor que 0 y como máximo " + CAPACIDAD_MAXIMA + " kg.");
        }
        dkprDao.dkprGuardarVehiculo(dkprV);
    }

    public void dkprGuardarConductor(DkprConductor dkprC) throws SQLException {
        DkprValidaciones.dkprValidarDni(dkprC.dkprGetDni(), "El DNI del conductor");
        dkprC.dkprSetDni(dkprC.dkprGetDni().trim());
        dkprC.dkprSetNombreCompleto(DkprValidaciones.dkprRequerido(dkprC.dkprGetNombreCompleto(), "Nombre completo", 80));
        String dkprLicencia = DkprValidaciones.dkprRequerido(dkprC.dkprGetLicencia(), "Licencia", 12).toUpperCase(Locale.ROOT);
        if (!dkprLicencia.matches("[A-Z0-9]{6,12}")) {
            throw new DkprNegocioException("La licencia (brevete) debe tener entre 6 y 12 letras o números, por ejemplo Q12345678.");
        }
        dkprC.dkprSetLicencia(dkprLicencia);
        String dkprTelefono = DkprValidaciones.dkprOpcional(dkprC.dkprGetTelefono(), "Teléfono", 15);
        DkprValidaciones.dkprValidarTelefono(dkprTelefono);
        dkprC.dkprSetTelefono(dkprTelefono);
        dkprDao.dkprGuardarConductor(dkprC);
    }

    public DkprVehiculo dkprVehiculoParaDespacho(Integer dkprIdVehiculo) throws SQLException {
        if (dkprIdVehiculo == null) {
            throw new DkprNegocioException("Elige el vehículo que lleva la carga (se registran en Vehículos y conductores).");
        }
        DkprVehiculo dkprV = dkprDao.dkprBuscarVehiculo(dkprIdVehiculo);
        if (dkprV == null || !dkprV.dkprIsActivo()) {
            throw new DkprNegocioException("El vehículo elegido no existe o está inactivo.");
        }
        return dkprV;
    }

    public DkprConductor dkprConductorParaDespacho(Integer dkprIdConductor) throws SQLException {
        if (dkprIdConductor == null) {
            throw new DkprNegocioException("Elige el conductor (se registran en Vehículos y conductores).");
        }
        DkprConductor dkprC = dkprDao.dkprBuscarConductor(dkprIdConductor);
        if (dkprC == null || !dkprC.dkprIsActivo()) {
            throw new DkprNegocioException("El conductor elegido no existe o está inactivo.");
        }
        return dkprC;
    }
}
