package com.grupo.envios.service;

import com.grupo.envios.dao.ConfiguracionDAO;
import com.grupo.envios.dao.EnvioDAO;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.util.Estados;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Roles;

import java.sql.SQLException;
import java.util.List;

/**
 * Paquetes estancados: envíos sin terminar que llevan más de X días sin ningún movimiento
 * en su historial. X se configura (tabla configuracion, clave dias_estancado).
 */
public class EstancadoService {

    public static final String CLAVE_DIAS = "dias_estancado";
    public static final int DIAS_POR_DEFECTO = 3;
    public static final int DIAS_MAXIMO = 90;

    private final ConfiguracionDAO dkprConfiguracionDAO = new ConfiguracionDAO();
    private final EnvioDAO dkprEnvioDAO = new EnvioDAO();

    public int diasEstancado() throws SQLException {
        String dkprValor = dkprConfiguracionDAO.obtener(CLAVE_DIAS);
        try {
            return dkprValor == null ? DIAS_POR_DEFECTO : Integer.parseInt(dkprValor.trim());
        } catch (NumberFormatException dkprE) {
            return DIAS_POR_DEFECTO;
        }
    }

    public boolean puedeConfigurar(Empleado dkprEmpleado) {
        return dkprEmpleado != null && Roles.ADMINISTRADOR.equals(dkprEmpleado.getRol());
    }

    public void guardarDias(int dkprDias, Empleado dkprEmpleado) throws SQLException {
        if (!puedeConfigurar(dkprEmpleado)) {
            throw new NegocioException("Solo el administrador puede cambiar los días.");
        }
        if (dkprDias < 1 || dkprDias > DIAS_MAXIMO) {
            throw new NegocioException("Los días deben estar entre 1 y " + DIAS_MAXIMO + ".");
        }
        dkprConfiguracionDAO.guardar(CLAVE_DIAS, String.valueOf(dkprDias));
    }

    /** El administrador ve todas las agencias; el resto, los envíos en los que participa su agencia. */
    public List<Envio> listar(Empleado dkprEmpleado, int dkprDias) throws SQLException {
        int dkprIdAgencia = puedeConfigurar(dkprEmpleado) ? 0 : dkprEmpleado.getIdAgencia();
        return dkprEnvioDAO.listarEstancados(dkprDias, dkprIdAgencia);
    }

    public int contar(Empleado dkprEmpleado) throws SQLException {
        return listar(dkprEmpleado, diasEstancado()).size();
    }

    /** Qué hay que hacer para que el envío vuelva a moverse, y quién lo hace. */
    public static String queFalta(Envio dkprE) {
        return switch (dkprE.getIdEstadoActual()) {
            case Estados.REGISTRADO -> "Despachar desde " + dkprE.getOrigen();
            case Estados.EN_TRANSITO -> "Confirmar llegada en " + dkprE.getDestino() + " o ubicar el vehículo"
                    + (dkprE.getPlaca() == null ? "" : " " + dkprE.getPlaca())
                    + (dkprE.getConductor() == null ? "" : " - " + dkprE.getConductor());
            case Estados.EN_AGENCIA_DESTINO -> "Avisar al destinatario para que recoja en " + dkprE.getDestino();
            default -> "";
        };
    }
}
