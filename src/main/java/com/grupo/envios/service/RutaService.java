package com.grupo.envios.service;

import com.grupo.envios.dao.RutaDAO;
import com.grupo.envios.model.Ruta;
import com.grupo.envios.util.NegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

public class RutaService {

    private static final BigDecimal MAXIMO_PRECIO = new BigDecimal("99999.99");
    private static final BigDecimal MAXIMO_PESO = new BigDecimal("9999.99");

    private final RutaDAO dkprDao = new RutaDAO();

    public List<Ruta> listar() throws SQLException {
        return dkprDao.listar();
    }

    public List<Ruta> listarPorOrigen(int dkprIdAgenciaOrigen) throws SQLException {
        return dkprDao.listarPorOrigen(dkprIdAgenciaOrigen);
    }

    public void guardar(Ruta dkprR) throws SQLException {
        if (dkprR.getIdAgenciaOrigen() <= 0 || dkprR.getIdAgenciaDestino() <= 0) {
            throw new NegocioException("Selecciona la agencia de origen y la de destino.");
        }
        if (dkprR.getIdAgenciaOrigen() == dkprR.getIdAgenciaDestino()) {
            throw new NegocioException("El origen y el destino deben ser agencias distintas.");
        }
        if (dkprR.getDiasEstimados() < 1 || dkprR.getDiasEstimados() > 30) {
            throw new NegocioException("Los días estimados deben estar entre 1 y 30.");
        }
        dkprR.setPrecioBase(validarMonto(dkprR.getPrecioBase(), "Precio base", MAXIMO_PRECIO, true));
        dkprR.setPrecioKgExtra(validarMonto(dkprR.getPrecioKgExtra(), "Precio por kg extra", MAXIMO_PRECIO, true));
        dkprR.setPesoMaxKg(validarMonto(dkprR.getPesoMaxKg(), "Peso máximo", MAXIMO_PESO, false));

        try {
            if (dkprR.getIdRuta() == 0) {
                dkprDao.insertar(dkprR);
            } else {
                dkprDao.actualizar(dkprR);
            }
        } catch (SQLException dkprE) {
            if ("23505".equals(dkprE.getSQLState())) {
                throw new NegocioException("Ya existe una ruta entre esas dos agencias.");
            }
            throw dkprE;
        }
    }

    private BigDecimal validarMonto(BigDecimal dkprValor, String dkprCampo, BigDecimal dkprMaximo, boolean dkprAdmiteCero) {
        if (dkprValor == null) {
            throw new NegocioException("El campo \"" + dkprCampo + "\" es obligatorio.");
        }
        boolean dkprInvalido = dkprAdmiteCero ? dkprValor.signum() < 0 : dkprValor.signum() <= 0;
        if (dkprInvalido) {
            throw new NegocioException("El campo \"" + dkprCampo + "\" debe ser "
                    + (dkprAdmiteCero ? "cero o mayor." : "mayor que cero."));
        }
        if (dkprValor.compareTo(dkprMaximo) > 0) {
            throw new NegocioException("El campo \"" + dkprCampo + "\" no puede superar " + dkprMaximo + ".");
        }
        return dkprValor.setScale(2, RoundingMode.HALF_UP);
    }
}
