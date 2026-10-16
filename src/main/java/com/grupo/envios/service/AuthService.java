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
        if (empleado == null) return null;

        // IMPRIMIR INFORMACIÓN DE DIAGNÓSTICO
        System.out.println("--- DIAGNÓSTICO DE CLAVE ---");
        System.out.println("Clave recibida: [" + clave + "]");
        System.out.println("Longitud clave recibida: " + clave.length());

        // Hash generado en vivo con la clave ingresada
        String hashGeneradoEnvivo = BCrypt.hashpw(clave, BCrypt.gensalt());
        System.out.println("Nuevo hash generado para tu BD: " + hashGeneradoEnvivo);

        boolean match = BCrypt.checkpw(clave, empleado.getClaveHash());
        System.out.println("Coincidencia actual: " + match);
        System.out.println("-----------------------------");

        return match ? empleado : null;
    }
}