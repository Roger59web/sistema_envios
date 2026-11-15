package com.grupo.envios.service;

import com.grupo.envios.dao.DkprEmpleadoDAO;
import com.grupo.envios.model.DkprEmpleado;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class DkprAuthService {

    private final DkprEmpleadoDAO dkprEmpleadoDAO = new DkprEmpleadoDAO();

    public DkprEmpleado dkprLogin(String dkprUsuario, String dkprClave) throws SQLException {
        DkprEmpleado dkprEmpleado = dkprEmpleadoDAO.dkprBuscarPorUsuario(dkprUsuario);
        if (dkprEmpleado == null) {
            return null;
        }
        return BCrypt.checkpw(dkprClave, dkprEmpleado.dkprGetClaveHash()) ? dkprEmpleado : null;
    }
}
