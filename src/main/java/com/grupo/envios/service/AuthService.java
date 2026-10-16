package com.grupo.envios.service;

import com.grupo.envios.dao.EmpleadoDAO;
import com.grupo.envios.model.Empleado;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    /** Devuelve el empleado si las credenciales son correctas, o null si no lo son. */
    public Empleado login(String usuario, String clave) throws SQLException {
        Empleado empleado = empleadoDAO.buscarPorUsuario(usuario);
        if (empleado == null) {
            return null;
        }
        return BCrypt.checkpw(clave, empleado.getClaveHash()) ? empleado : null;
    }
}
