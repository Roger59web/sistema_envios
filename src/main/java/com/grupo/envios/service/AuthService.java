package com.grupo.envios.service;

import com.grupo.envios.dao.EmpleadoDAO;
import com.grupo.envios.model.Empleado;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {

    private final EmpleadoDAO dkprEmpleadoDAO = new EmpleadoDAO();

    public Empleado login(String dkprUsuario, String dkprClave) throws SQLException {
        Empleado dkprEmpleado = dkprEmpleadoDAO.buscarPorUsuario(dkprUsuario);
        if (dkprEmpleado == null) {
            return null;
        }
        return BCrypt.checkpw(dkprClave, dkprEmpleado.getClaveHash()) ? dkprEmpleado : null;
    }
}
