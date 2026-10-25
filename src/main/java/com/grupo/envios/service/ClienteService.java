package com.grupo.envios.service;

import com.grupo.envios.dao.ClienteDAO;
import com.grupo.envios.model.Cliente;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Validaciones;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private final ClienteDAO dkprDao = new ClienteDAO();

    public List<Cliente> buscar(String dkprFiltro) throws SQLException {
        return dkprDao.listar(dkprFiltro == null ? "" : dkprFiltro.trim());
    }

    /** Devuelve el cliente con ese documento, o null si todavía no está registrado. */
    public Cliente buscarPorDocumento(String dkprTipo, String dkprNumero) throws SQLException {
        Validaciones.validarDocumento(dkprTipo, dkprNumero);
        return dkprDao.buscarPorDocumento(dkprTipo, dkprNumero.trim());
    }

    public void guardar(Cliente dkprC) throws SQLException {
        Validaciones.validarDocumento(dkprC.getTipoDocumento(), dkprC.getNumeroDocumento());
        dkprC.setNumeroDocumento(dkprC.getNumeroDocumento().trim());
        dkprC.setNombreCompleto(Validaciones.requerido(dkprC.getNombreCompleto(), "Nombre / Razón social", 150));
        dkprC.setTelefono(Validaciones.opcional(dkprC.getTelefono(), "Teléfono", 15));
        dkprC.setEmail(Validaciones.opcional(dkprC.getEmail(), "Correo", 100));
        dkprC.setDireccion(Validaciones.opcional(dkprC.getDireccion(), "Dirección", 150));
        Validaciones.validarTelefono(dkprC.getTelefono());
        Validaciones.validarEmail(dkprC.getEmail());

        try {
            if (dkprC.getIdCliente() == 0) {
                dkprDao.insertar(dkprC);
            } else {
                dkprDao.actualizar(dkprC);
            }
        } catch (SQLException dkprE) {
            if ("23505".equals(dkprE.getSQLState())) {
                throw new NegocioException("Ya existe un cliente con ese tipo y número de documento.");
            }
            throw dkprE;
        }
    }

    /**
     * Usado al registrar un envío: devuelve el cliente si ya existe; si no, lo crea
     * (por eso exige el nombre) dentro de la misma transacción.
     *
     * @param etiqueta "remitente" o "destinatario", solo para armar los mensajes de error
     */
    public Cliente obtenerOCrear(Connection dkprCn, String dkprTipo, String dkprDocumento, String dkprNombre,
                                 String dkprTelefono, String dkprEtiqueta) throws SQLException {
        Validaciones.validarDocumento(dkprTipo, dkprDocumento);
        String dkprDoc = dkprDocumento.trim();

        Cliente dkprExistente = dkprDao.buscarPorDocumento(dkprCn, dkprTipo, dkprDoc);
        if (dkprExistente != null) {
            return dkprExistente;
        }

        Cliente dkprNuevo = new Cliente();
        dkprNuevo.setTipoDocumento(dkprTipo);
        dkprNuevo.setNumeroDocumento(dkprDoc);
        dkprNuevo.setNombreCompleto(Validaciones.requerido(dkprNombre, "Nombre del " + dkprEtiqueta, 150));
        String dkprTel = Validaciones.opcional(dkprTelefono, "Teléfono del " + dkprEtiqueta, 15);
        Validaciones.validarTelefono(dkprTel);
        dkprNuevo.setTelefono(dkprTel);
        dkprNuevo.setIdCliente(dkprDao.insertar(dkprCn, dkprNuevo));
        return dkprNuevo;
    }
}
