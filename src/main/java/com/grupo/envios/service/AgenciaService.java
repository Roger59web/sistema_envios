package com.grupo.envios.service;

import com.grupo.envios.dao.AgenciaDAO;
import com.grupo.envios.model.Agencia;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Validaciones;

import java.sql.SQLException;
import java.util.List;

public class AgenciaService {

    private final AgenciaDAO dao = new AgenciaDAO();

    public List<Agencia> listar(boolean soloActivas) throws SQLException {
        return dao.listar(soloActivas);
    }

    public void guardar(Agencia a) throws SQLException {
        a.setNombre(Validaciones.requerido(a.getNombre(), "Nombre", 80));
        a.setCiudad(Validaciones.requerido(a.getCiudad(), "Ciudad", 60));
        a.setDireccion(Validaciones.requerido(a.getDireccion(), "Dirección", 150));
        String telefono = Validaciones.opcional(a.getTelefono(), "Teléfono", 15);
        Validaciones.validarTelefono(telefono);
        a.setTelefono(telefono);

        if (a.getIdAgencia() == 0) {
            dao.insertar(a);
        } else {
            dao.actualizar(a);
        }
    }
}
