package com.grupo.envios.service;

import com.grupo.envios.dao.DkprAgenciaDAO;
import com.grupo.envios.model.DkprAgencia;
import com.grupo.envios.util.DkprNegocioException;
import com.grupo.envios.util.DkprValidaciones;

import java.sql.SQLException;
import java.util.List;

public class DkprAgenciaService {

    private final DkprAgenciaDAO dkprDao = new DkprAgenciaDAO();

    public List<DkprAgencia> dkprListar(boolean dkprSoloActivas) throws SQLException {
        return dkprDao.dkprListar(dkprSoloActivas);
    }

    public void dkprGuardar(DkprAgencia dkprA) throws SQLException {
        dkprA.dkprSetNombre(DkprValidaciones.dkprRequerido(dkprA.dkprGetNombre(), "Nombre", 80));
        dkprA.dkprSetCiudad(DkprValidaciones.dkprRequerido(dkprA.dkprGetCiudad(), "Ciudad", 60));
        dkprA.dkprSetDireccion(DkprValidaciones.dkprRequerido(dkprA.dkprGetDireccion(), "Dirección", 150));
        String dkprTelefono = DkprValidaciones.dkprOpcional(dkprA.dkprGetTelefono(), "Teléfono", 15);
        DkprValidaciones.dkprValidarTelefono(dkprTelefono);
        dkprA.dkprSetTelefono(dkprTelefono);

        if (dkprA.dkprGetIdAgencia() == 0) {
            dkprDao.dkprInsertar(dkprA);
        } else {
            dkprDao.dkprActualizar(dkprA);
        }
    }
}
