package com.grupo.envios.service;

import com.grupo.envios.dao.EnvioDAO;
import com.grupo.envios.model.Envio;
import com.grupo.envios.model.ResumenFila;
import com.grupo.envios.util.NegocioException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReporteService {

    private final EnvioDAO dao = new EnvioDAO();

    public List<Envio> detalle(LocalDate desde, LocalDate hasta) throws SQLException {
        validar(desde, hasta);
        return dao.listarPorFechas(desde, hasta);
    }

    public List<ResumenFila> porAgencia(LocalDate desde, LocalDate hasta) throws SQLException {
        validar(desde, hasta);
        return dao.resumenPorAgencia(desde, hasta);
    }

    public List<ResumenFila> porEstado(LocalDate desde, LocalDate hasta) throws SQLException {
        validar(desde, hasta);
        return dao.resumenPorEstado(desde, hasta);
    }

    private void validar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new NegocioException("Selecciona la fecha inicial y la final.");
        }
        if (desde.isAfter(hasta)) {
            throw new NegocioException("La fecha inicial no puede ser posterior a la final.");
        }
    }
}
