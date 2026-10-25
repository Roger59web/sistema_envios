package com.grupo.envios.service;

import com.grupo.envios.dao.EmpleadoDAO;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Rol;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Sesion;
import com.grupo.envios.util.Validaciones;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;

public class EmpleadoService {

    private final EmpleadoDAO dkprDao = new EmpleadoDAO();

    public List<Empleado> listar() throws SQLException {
        return dkprDao.listar();
    }

    public List<Rol> listarRoles() throws SQLException {
        return dkprDao.listarRoles();
    }

    /**
     * @param clavePlano contraseña escrita en pantalla. Obligatoria al crear; al editar,
     *                   si está vacía se conserva la contraseña actual.
     */
    public void guardar(Empleado dkprE, String dkprClavePlano) throws SQLException {
        if (dkprE.getIdAgencia() <= 0) {
            throw new NegocioException("Selecciona la agencia del empleado.");
        }
        if (dkprE.getIdRol() <= 0) {
            throw new NegocioException("Selecciona el rol del empleado.");
        }
        Validaciones.validarDni(dkprE.getDni(), "El DNI");
        dkprE.setDni(dkprE.getDni().trim());
        dkprE.setNombres(Validaciones.requerido(dkprE.getNombres(), "Nombres", 80));
        dkprE.setApellidos(Validaciones.requerido(dkprE.getApellidos(), "Apellidos", 80));
        dkprE.setUsuario(Validaciones.requerido(dkprE.getUsuario(), "Usuario", 30));
        if (!dkprE.getUsuario().matches("[A-Za-z0-9._-]{3,30}")) {
            throw new NegocioException("El usuario debe tener entre 3 y 30 caracteres: letras, números, punto, guion o guion bajo.");
        }

        boolean dkprEsNuevo = dkprE.getIdEmpleado() == 0;
        if (dkprEsNuevo && Validaciones.vacio(dkprClavePlano)) {
            throw new NegocioException("Ingresa una contraseña para el nuevo empleado.");
        }
        if (!Validaciones.vacio(dkprClavePlano)) {
            if (dkprClavePlano.length() < 6) {
                throw new NegocioException("La contraseña debe tener al menos 6 caracteres.");
            }
            dkprE.setClaveHash(BCrypt.hashpw(dkprClavePlano, BCrypt.gensalt(10)));
        } else {
            dkprE.setClaveHash(null);
        }

        if (!dkprEsNuevo && !dkprE.isActivo() && Sesion.getActual() != null
                && Sesion.getActual().getIdEmpleado() == dkprE.getIdEmpleado()) {
            throw new NegocioException("No puedes desactivar tu propio usuario.");
        }

        try {
            if (dkprEsNuevo) {
                dkprDao.insertar(dkprE);
            } else {
                dkprDao.actualizar(dkprE);
            }
        } catch (SQLException dkprEx) {
            if ("23505".equals(dkprEx.getSQLState())) {
                throw new NegocioException("Ya existe un empleado con ese DNI o ese usuario.");
            }
            throw dkprEx;
        }
    }
}
