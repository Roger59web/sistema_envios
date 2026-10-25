package com.grupo.envios.service;

import com.grupo.envios.dao.EnvioDAO;
import com.grupo.envios.model.Envio;
import com.grupo.envios.model.ResumenFila;
import com.grupo.envios.util.NegocioException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReporteService {

    private final EnvioDAO dkprDao = new EnvioDAO();

    public List<Envio> detalle(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        validar(dkprDesde, dkprHasta);
        return dkprDao.listarPorFechas(dkprDesde, dkprHasta);
    }

    public List<ResumenFila> porAgencia(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        validar(dkprDesde, dkprHasta);
        return dkprDao.resumenPorAgencia(dkprDesde, dkprHasta);
    }

    public List<ResumenFila> porEstado(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        validar(dkprDesde, dkprHasta);
        return dkprDao.resumenPorEstado(dkprDesde, dkprHasta);
    }

    private void validar(LocalDate dkprDesde, LocalDate dkprHasta) {
        if (dkprDesde == null || dkprHasta == null) {
            throw new NegocioException("Selecciona la fecha inicial y la final.");
        }
        if (dkprDesde.isAfter(dkprHasta)) {
            throw new NegocioException("La fecha inicial no puede ser posterior a la final.");
        }
    }
}
