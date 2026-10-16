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

    private final EmpleadoDAO dao = new EmpleadoDAO();

    public List<Empleado> listar() throws SQLException {
        return dao.listar();
    }

    public List<Rol> listarRoles() throws SQLException {
        return dao.listarRoles();
    }

    /**
     * @param clavePlano contraseña escrita en pantalla. Obligatoria al crear; al editar,
     *                   si está vacía se conserva la contraseña actual.
     */
    public void guardar(Empleado e, String clavePlano) throws SQLException {
        if (e.getIdAgencia() <= 0) {
            throw new NegocioException("Selecciona la agencia del empleado.");
        }
        if (e.getIdRol() <= 0) {
            throw new NegocioException("Selecciona el rol del empleado.");
        }
        Validaciones.validarDni(e.getDni(), "El DNI");
        e.setDni(e.getDni().trim());
        e.setNombres(Validaciones.requerido(e.getNombres(), "Nombres", 80));
        e.setApellidos(Validaciones.requerido(e.getApellidos(), "Apellidos", 80));
        e.setUsuario(Validaciones.requerido(e.getUsuario(), "Usuario", 30));
        if (!e.getUsuario().matches("[A-Za-z0-9._-]{3,30}")) {
            throw new NegocioException("El usuario debe tener entre 3 y 30 caracteres: letras, números, punto, guion o guion bajo.");
        }

        boolean esNuevo = e.getIdEmpleado() == 0;
        if (esNuevo && Validaciones.vacio(clavePlano)) {
            throw new NegocioException("Ingresa una contraseña para el nuevo empleado.");
        }
        if (!Validaciones.vacio(clavePlano)) {
            if (clavePlano.length() < 6) {
                throw new NegocioException("La contraseña debe tener al menos 6 caracteres.");
            }
            e.setClaveHash(BCrypt.hashpw(clavePlano, BCrypt.gensalt(10)));
        } else {
            e.setClaveHash(null);
        }

        if (!esNuevo && !e.isActivo() && Sesion.getActual() != null
                && Sesion.getActual().getIdEmpleado() == e.getIdEmpleado()) {
            throw new NegocioException("No puedes desactivar tu propio usuario.");
        }

        try {
            if (esNuevo) {
                dao.insertar(e);
            } else {
                dao.actualizar(e);
            }
        } catch (SQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                throw new NegocioException("Ya existe un empleado con ese DNI o ese usuario.");
            }
            throw ex;
        }
    }
}
