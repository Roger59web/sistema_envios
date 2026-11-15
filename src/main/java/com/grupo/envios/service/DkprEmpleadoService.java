package com.grupo.envios.service;

import com.grupo.envios.dao.DkprEmpleadoDAO;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprRol;
import com.grupo.envios.util.DkprNegocioException;
import com.grupo.envios.util.DkprSesion;
import com.grupo.envios.util.DkprValidaciones;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

public class DkprEmpleadoService {

    private final DkprEmpleadoDAO dkprDao = new DkprEmpleadoDAO();

    public List<DkprEmpleado> dkprListar() throws SQLException {
        return dkprDao.dkprListar();
    }

    public List<DkprRol> dkprListarRoles() throws SQLException {
        return dkprDao.dkprListarRoles();
    }

    public void dkprGuardar(DkprEmpleado dkprE, String dkprClavePlano) throws SQLException {
        if (dkprE.dkprGetIdAgencia() <= 0) {
            throw new DkprNegocioException("Selecciona la agencia del empleado.");
        }
        if (dkprE.dkprGetIdRol() <= 0) {
            throw new DkprNegocioException("Selecciona el rol del empleado.");
        }
        DkprValidaciones.dkprValidarDni(dkprE.dkprGetDni(), "El DNI");
        dkprE.dkprSetDni(dkprE.dkprGetDni().trim());
        dkprE.dkprSetNombres(DkprValidaciones.dkprRequerido(dkprE.dkprGetNombres(), "Nombres", 80));
        dkprE.dkprSetApellidos(DkprValidaciones.dkprRequerido(dkprE.dkprGetApellidos(), "Apellidos", 80));
        dkprE.dkprSetUsuario(DkprValidaciones.dkprRequerido(dkprE.dkprGetUsuario(), "Usuario", 30));
        if (!dkprE.dkprGetUsuario().matches("[A-Za-z0-9._-]{3,30}")) {
            throw new DkprNegocioException("El usuario debe tener entre 3 y 30 caracteres: letras, números, punto, guion o guion bajo.");
        }

        boolean dkprEsNuevo = dkprE.dkprGetIdEmpleado() == 0;
        if (dkprEsNuevo && DkprValidaciones.dkprVacio(dkprClavePlano)) {
            throw new DkprNegocioException("Ingresa una contraseña para el nuevo empleado.");
        }
        if (!DkprValidaciones.dkprVacio(dkprClavePlano)) {
            if (dkprClavePlano.length() < 6) {
                throw new DkprNegocioException("La contraseña debe tener al menos 6 caracteres.");
            }
            dkprE.dkprSetClaveHash(BCrypt.hashpw(dkprClavePlano, BCrypt.gensalt(10)));
        } else {
            dkprE.dkprSetClaveHash(null);
        }

        if (!dkprEsNuevo && !dkprE.dkprIsActivo() && DkprSesion.dkprGetActual() != null
                && DkprSesion.dkprGetActual().dkprGetIdEmpleado() == dkprE.dkprGetIdEmpleado()) {
            throw new DkprNegocioException("No puedes desactivar tu propio usuario.");
        }

        try {
            if (dkprEsNuevo) {
                dkprDao.dkprInsertar(dkprE);
            } else {
                dkprDao.dkprActualizar(dkprE);
            }
        } catch (SQLException dkprEx) {
            if ("23505".equals(dkprEx.getSQLState())) {
                throw new DkprNegocioException("Ya existe un empleado con ese DNI o ese usuario.");
            }
            throw dkprEx;
        }
    }
}
