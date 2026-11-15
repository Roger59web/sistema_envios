package com.grupo.envios.service;

import com.grupo.envios.dao.DkprRutaDAO;
import com.grupo.envios.model.DkprRuta;
import com.grupo.envios.util.DkprNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

public class DkprRutaService {

    private static final BigDecimal MAXIMO_PRECIO = new BigDecimal("99999.99");
    private static final BigDecimal MAXIMO_PESO = new BigDecimal("9999.99");

    private final DkprRutaDAO dkprDao = new DkprRutaDAO();

    public List<DkprRuta> dkprListar() throws SQLException {
        return dkprDao.dkprListar();
    }

    public List<DkprRuta> dkprListarPorOrigen(int dkprIdAgenciaOrigen) throws SQLException {
        return dkprDao.dkprListarPorOrigen(dkprIdAgenciaOrigen);
    }

    public void dkprGuardar(DkprRuta dkprR) throws SQLException {
        if (dkprR.dkprGetIdAgenciaOrigen() <= 0 || dkprR.dkprGetIdAgenciaDestino() <= 0) {
            throw new DkprNegocioException("Selecciona la agencia de origen y la de destino.");
        }
        if (dkprR.dkprGetIdAgenciaOrigen() == dkprR.dkprGetIdAgenciaDestino()) {
            throw new DkprNegocioException("El origen y el destino deben ser agencias distintas.");
        }
        if (dkprR.dkprGetDiasEstimados() < 1 || dkprR.dkprGetDiasEstimados() > 30) {
            throw new DkprNegocioException("Los días estimados deben estar entre 1 y 30.");
        }
        dkprR.dkprSetPrecioBase(dkprValidarMonto(dkprR.dkprGetPrecioBase(), "Precio base", MAXIMO_PRECIO, true));
        dkprR.dkprSetPrecioKgExtra(dkprValidarMonto(dkprR.dkprGetPrecioKgExtra(), "Precio por kg extra", MAXIMO_PRECIO, true));
        dkprR.dkprSetPesoMaxKg(dkprValidarMonto(dkprR.dkprGetPesoMaxKg(), "Peso máximo", MAXIMO_PESO, false));

        try {
            if (dkprR.dkprGetIdRuta() == 0) {
                dkprDao.dkprInsertar(dkprR);
            } else {
                dkprDao.dkprActualizar(dkprR);
            }
        } catch (SQLException dkprE) {
            if ("23505".equals(dkprE.getSQLState())) {
                throw new DkprNegocioException("Ya existe una ruta entre esas dos agencias.");
            }
            throw dkprE;
        }
    }

    private BigDecimal dkprValidarMonto(BigDecimal dkprValor, String dkprCampo, BigDecimal dkprMaximo, boolean dkprAdmiteCero) {
        if (dkprValor == null) {
            throw new DkprNegocioException("El campo \"" + dkprCampo + "\" es obligatorio.");
        }
        boolean dkprInvalido = dkprAdmiteCero ? dkprValor.signum() < 0 : dkprValor.signum() <= 0;
        if (dkprInvalido) {
            throw new DkprNegocioException("El campo \"" + dkprCampo + "\" debe ser "
                    + (dkprAdmiteCero ? "cero o mayor." : "mayor que cero."));
        }
        if (dkprValor.compareTo(dkprMaximo) > 0) {
            throw new DkprNegocioException("El campo \"" + dkprCampo + "\" no puede superar " + dkprMaximo + ".");
        }
        return dkprValor.setScale(2, RoundingMode.HALF_UP);
    }
}
