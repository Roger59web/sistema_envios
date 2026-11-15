package com.grupo.envios.service;

import com.grupo.envios.dao.DkprConfiguracionDAO;
import com.grupo.envios.dao.DkprEnvioDAO;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.util.DkprEstados;
import com.grupo.envios.util.DkprNegocioException;
import com.grupo.envios.util.DkprRoles;

import java.sql.SQLException;
import java.util.List;

public class DkprEstancadoService {

    public static final String CLAVE_DIAS = "dias_estancado";
    public static final int DIAS_POR_DEFECTO = 3;
    public static final int DIAS_MAXIMO = 90;

    private final DkprConfiguracionDAO dkprConfiguracionDAO = new DkprConfiguracionDAO();
    private final DkprEnvioDAO dkprEnvioDAO = new DkprEnvioDAO();

    public int dkprDiasEstancado() throws SQLException {
        String dkprValor = dkprConfiguracionDAO.dkprObtener(CLAVE_DIAS);
        try {
            return dkprValor == null ? DIAS_POR_DEFECTO : Integer.parseInt(dkprValor.trim());
        } catch (NumberFormatException dkprE) {
            return DIAS_POR_DEFECTO;
        }
    }

    public boolean dkprPuedeConfigurar(DkprEmpleado dkprEmpleado) {
        return dkprEmpleado != null && DkprRoles.ADMINISTRADOR.equals(dkprEmpleado.dkprGetRol());
    }

    public void dkprGuardarDias(int dkprDias, DkprEmpleado dkprEmpleado) throws SQLException {
        if (!dkprPuedeConfigurar(dkprEmpleado)) {
            throw new DkprNegocioException("Solo el administrador puede cambiar los días.");
        }
        if (dkprDias < 1 || dkprDias > DIAS_MAXIMO) {
            throw new DkprNegocioException("Los días deben estar entre 1 y " + DIAS_MAXIMO + ".");
        }
        dkprConfiguracionDAO.dkprGuardar(CLAVE_DIAS, String.valueOf(dkprDias));
    }

    public List<DkprEnvio> dkprListar(DkprEmpleado dkprEmpleado, int dkprDias) throws SQLException {
        int dkprIdAgencia = dkprPuedeConfigurar(dkprEmpleado) ? 0 : dkprEmpleado.dkprGetIdAgencia();
        return dkprEnvioDAO.dkprListarEstancados(dkprDias, dkprIdAgencia);
    }

    public int dkprContar(DkprEmpleado dkprEmpleado) throws SQLException {
        return dkprListar(dkprEmpleado, dkprDiasEstancado()).size();
    }

    public static String dkprQueFalta(DkprEnvio dkprE) {
        return switch (dkprE.dkprGetIdEstadoActual()) {
            case DkprEstados.REGISTRADO -> "Despachar desde " + dkprE.dkprGetOrigen();
            case DkprEstados.EN_TRANSITO -> "Confirmar llegada en " + dkprE.dkprGetDestino() + " o ubicar el vehículo"
                    + (dkprE.dkprGetPlaca() == null ? "" : " " + dkprE.dkprGetPlaca())
                    + (dkprE.dkprGetConductor() == null ? "" : " - " + dkprE.dkprGetConductor());
            case DkprEstados.EN_AGENCIA_DESTINO -> "Avisar al destinatario para que recoja en " + dkprE.dkprGetDestino();
            default -> "";
        };
    }
}
