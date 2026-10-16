package com.grupo.envios.service;

import com.grupo.envios.dao.ClienteDAO;
import com.grupo.envios.model.Cliente;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Validaciones;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ClienteService {

    private final ClienteDAO dao = new ClienteDAO();

    public List<Cliente> buscar(String filtro) throws SQLException {
        return dao.listar(filtro == null ? "" : filtro.trim());
    }

    /** Devuelve el cliente con ese documento, o null si todavía no está registrado. */
    public Cliente buscarPorDocumento(String tipo, String numero) throws SQLException {
        Validaciones.validarDocumento(tipo, numero);
        return dao.buscarPorDocumento(tipo, numero.trim());
    }

    public void guardar(Cliente c) throws SQLException {
        Validaciones.validarDocumento(c.getTipoDocumento(), c.getNumeroDocumento());
        c.setNumeroDocumento(c.getNumeroDocumento().trim());
        c.setNombreCompleto(Validaciones.requerido(c.getNombreCompleto(), "Nombre / Razón social", 150));
        c.setTelefono(Validaciones.opcional(c.getTelefono(), "Teléfono", 15));
        c.setEmail(Validaciones.opcional(c.getEmail(), "Correo", 100));
        c.setDireccion(Validaciones.opcional(c.getDireccion(), "Dirección", 150));
        Validaciones.validarTelefono(c.getTelefono());
        Validaciones.validarEmail(c.getEmail());

        try {
            if (c.getIdCliente() == 0) {
                dao.insertar(c);
            } else {
                dao.actualizar(c);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new NegocioException("Ya existe un cliente con ese tipo y número de documento.");
            }
            throw e;
        }
    }

    /**
     * Usado al registrar un envío: devuelve el cliente si ya existe; si no, lo crea
     * (por eso exige el nombre) dentro de la misma transacción.
     *
     * @param etiqueta "remitente" o "destinatario", solo para armar los mensajes de error
     */
    public Cliente obtenerOCrear(Connection cn, String tipo, String documento, String nombre,
                                 String telefono, String etiqueta) throws SQLException {
        Validaciones.validarDocumento(tipo, documento);
        String doc = documento.trim();

        Cliente existente = dao.buscarPorDocumento(cn, tipo, doc);
        if (existente != null) {
            return existente;
        }

        Cliente nuevo = new Cliente();
        nuevo.setTipoDocumento(tipo);
        nuevo.setNumeroDocumento(doc);
        nuevo.setNombreCompleto(Validaciones.requerido(nombre, "Nombre del " + etiqueta, 150));
        String tel = Validaciones.opcional(telefono, "Teléfono del " + etiqueta, 15);
        Validaciones.validarTelefono(tel);
        nuevo.setTelefono(tel);
        nuevo.setIdCliente(dao.insertar(cn, nuevo));
        return nuevo;
    }
}
