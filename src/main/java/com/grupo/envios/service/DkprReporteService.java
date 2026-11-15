package com.grupo.envios.service;

import com.grupo.envios.dao.DkprEnvioDAO;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.model.DkprResumenFila;
import com.grupo.envios.util.DkprNegocioException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DkprReporteService {

    private final DkprEnvioDAO dkprDao = new DkprEnvioDAO();

    public List<DkprEnvio> dkprDetalle(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        dkprValidar(dkprDesde, dkprHasta);
        return dkprDao.dkprListarPorFechas(dkprDesde, dkprHasta);
    }

    public List<DkprResumenFila> dkprPorAgencia(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        dkprValidar(dkprDesde, dkprHasta);
        return dkprDao.dkprResumenPorAgencia(dkprDesde, dkprHasta);
    }

    public List<DkprResumenFila> dkprPorEstado(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        dkprValidar(dkprDesde, dkprHasta);
        return dkprDao.dkprResumenPorEstado(dkprDesde, dkprHasta);
    }

    private void dkprValidar(LocalDate dkprDesde, LocalDate dkprHasta) {
        if (dkprDesde == null || dkprHasta == null) {
            throw new DkprNegocioException("Selecciona la fecha inicial y la final.");
        }
        if (dkprDesde.isAfter(dkprHasta)) {
            throw new DkprNegocioException("La fecha inicial no puede ser posterior a la final.");
        }
    }
}
