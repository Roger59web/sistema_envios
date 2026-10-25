package com.grupo.envios.service;

import com.grupo.envios.dao.AgenciaDAO;
import com.grupo.envios.model.Agencia;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Validaciones;

import java.sql.SQLException;
import java.util.List;

public class AgenciaService {

    private final AgenciaDAO dkprDao = new AgenciaDAO();

    public List<Agencia> listar(boolean dkprSoloActivas) throws SQLException {
        return dkprDao.listar(dkprSoloActivas);
    }

    public void guardar(Agencia dkprA) throws SQLException {
        dkprA.setNombre(Validaciones.requerido(dkprA.getNombre(), "Nombre", 80));
        dkprA.setCiudad(Validaciones.requerido(dkprA.getCiudad(), "Ciudad", 60));
        dkprA.setDireccion(Validaciones.requerido(dkprA.getDireccion(), "Dirección", 150));
        String dkprTelefono = Validaciones.opcional(dkprA.getTelefono(), "Teléfono", 15);
        Validaciones.validarTelefono(dkprTelefono);
        dkprA.setTelefono(dkprTelefono);

        if (dkprA.getIdAgencia() == 0) {
            dkprDao.insertar(dkprA);
        } else {
            dkprDao.actualizar(dkprA);
        }
    }
}
