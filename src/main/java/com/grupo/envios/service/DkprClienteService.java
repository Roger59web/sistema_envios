package com.grupo.envios.service;

import com.grupo.envios.dao.DkprClienteDAO;
import com.grupo.envios.model.DkprCliente;
import com.grupo.envios.util.DkprNegocioException;
import com.grupo.envios.util.DkprValidaciones;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DkprClienteService {

    private final DkprClienteDAO dkprDao = new DkprClienteDAO();

    public List<DkprCliente> dkprBuscar(String dkprFiltro) throws SQLException {
        return dkprDao.dkprListar(dkprFiltro == null ? "" : dkprFiltro.trim());
    }

    public DkprCliente dkprBuscarPorDocumento(String dkprTipo, String dkprNumero) throws SQLException {
        DkprValidaciones.dkprValidarDocumento(dkprTipo, dkprNumero);
        return dkprDao.dkprBuscarPorDocumento(dkprTipo, dkprNumero.trim());
    }

    public void dkprGuardar(DkprCliente dkprC) throws SQLException {
        DkprValidaciones.dkprValidarDocumento(dkprC.dkprGetTipoDocumento(), dkprC.dkprGetNumeroDocumento());
        dkprC.dkprSetNumeroDocumento(dkprC.dkprGetNumeroDocumento().trim());
        dkprC.dkprSetNombreCompleto(DkprValidaciones.dkprRequerido(dkprC.dkprGetNombreCompleto(), "Nombre / Razón social", 150));
        dkprC.dkprSetTelefono(DkprValidaciones.dkprOpcional(dkprC.dkprGetTelefono(), "Teléfono", 15));
        dkprC.dkprSetEmail(DkprValidaciones.dkprOpcional(dkprC.dkprGetEmail(), "Correo", 100));
        dkprC.dkprSetDireccion(DkprValidaciones.dkprOpcional(dkprC.dkprGetDireccion(), "Dirección", 150));
        DkprValidaciones.dkprValidarTelefono(dkprC.dkprGetTelefono());
        DkprValidaciones.dkprValidarEmail(dkprC.dkprGetEmail());

        try {
            if (dkprC.dkprGetIdCliente() == 0) {
                dkprDao.dkprInsertar(dkprC);
            } else {
                dkprDao.dkprActualizar(dkprC);
            }
        } catch (SQLException dkprE) {
            if ("23505".equals(dkprE.getSQLState())) {
                throw new DkprNegocioException("Ya existe un cliente con ese tipo y número de documento.");
            }
            throw dkprE;
        }
    }

    public DkprCliente dkprObtenerOCrear(Connection dkprCn, String dkprTipo, String dkprDocumento, String dkprNombre,
                                 String dkprTelefono, String dkprEtiqueta) throws SQLException {
        DkprValidaciones.dkprValidarDocumento(dkprTipo, dkprDocumento);
        String dkprDoc = dkprDocumento.trim();

        DkprCliente dkprExistente = dkprDao.dkprBuscarPorDocumento(dkprCn, dkprTipo, dkprDoc);
        if (dkprExistente != null) {
            return dkprExistente;
        }

        DkprCliente dkprNuevo = new DkprCliente();
        dkprNuevo.dkprSetTipoDocumento(dkprTipo);
        dkprNuevo.dkprSetNumeroDocumento(dkprDoc);
        dkprNuevo.dkprSetNombreCompleto(DkprValidaciones.dkprRequerido(dkprNombre, "Nombre del " + dkprEtiqueta, 150));
        String dkprTel = DkprValidaciones.dkprOpcional(dkprTelefono, "Teléfono del " + dkprEtiqueta, 15);
        DkprValidaciones.dkprValidarTelefono(dkprTel);
        dkprNuevo.dkprSetTelefono(dkprTel);
        dkprNuevo.dkprSetIdCliente(dkprDao.dkprInsertar(dkprCn, dkprNuevo));
        return dkprNuevo;
    }
}
