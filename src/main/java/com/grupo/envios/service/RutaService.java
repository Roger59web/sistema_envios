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

    private final RutaDAO dao = new RutaDAO();

    public List<Ruta> listar() throws SQLException {
        return dao.listar();
    }

    public List<Ruta> listarPorOrigen(int idAgenciaOrigen) throws SQLException {
        return dao.listarPorOrigen(idAgenciaOrigen);
    }

    public void guardar(Ruta r) throws SQLException {
        if (r.getIdAgenciaOrigen() <= 0 || r.getIdAgenciaDestino() <= 0) {
            throw new NegocioException("Selecciona la agencia de origen y la de destino.");
        }
        if (r.getIdAgenciaOrigen() == r.getIdAgenciaDestino()) {
            throw new NegocioException("El origen y el destino deben ser agencias distintas.");
        }
        if (r.getDiasEstimados() < 1 || r.getDiasEstimados() > 30) {
            throw new NegocioException("Los días estimados deben estar entre 1 y 30.");
        }
        r.setPrecioBase(validarMonto(r.getPrecioBase(), "Precio base", MAXIMO_PRECIO, true));
        r.setPrecioKgExtra(validarMonto(r.getPrecioKgExtra(), "Precio por kg extra", MAXIMO_PRECIO, true));
        r.setPesoMaxKg(validarMonto(r.getPesoMaxKg(), "Peso máximo", MAXIMO_PESO, false));

        try {
            if (r.getIdRuta() == 0) {
                dao.insertar(r);
            } else {
                dao.actualizar(r);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new NegocioException("Ya existe una ruta entre esas dos agencias.");
            }
            throw e;
        }
    }

    private BigDecimal validarMonto(BigDecimal valor, String campo, BigDecimal maximo, boolean admiteCero) {
        if (valor == null) {
            throw new NegocioException("El campo \"" + campo + "\" es obligatorio.");
        }
        boolean invalido = admiteCero ? valor.signum() < 0 : valor.signum() <= 0;
        if (invalido) {
            throw new NegocioException("El campo \"" + campo + "\" debe ser "
                    + (admiteCero ? "cero o mayor." : "mayor que cero."));
        }
        if (valor.compareTo(maximo) > 0) {
            throw new NegocioException("El campo \"" + campo + "\" no puede superar " + maximo + ".");
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
