package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.model.DkprControlCarga;
import com.grupo.envios.model.DkprLecturaCarga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DkprControlCargaDAO {

    private static final String SELECT_BASE = """
            SELECT cc.id_control, cc.tipo, cc.id_agencia, cc.id_ruta, cc.id_manifiesto, man.codigo AS manifiesto,
                   ao.nombre AS origen, ad.nombre AS destino, em.nombres || ' ' || em.apellidos AS empleado,
                   cc.fecha_inicio, cc.estado
            FROM control_carga cc
            JOIN ruta ru ON ru.id_ruta = cc.id_ruta
            JOIN agencia ao ON ao.id_agencia = ru.id_agencia_origen
            JOIN agencia ad ON ad.id_agencia = ru.id_agencia_destino
            JOIN empleado em ON em.id_empleado = cc.id_empleado
            LEFT JOIN manifiesto man ON man.id_manifiesto = cc.id_manifiesto
            """;

    public DkprControlCarga dkprBuscarDespachoAbierto(int dkprIdAgencia, int dkprIdRuta) throws SQLException {
        return dkprBuscarUno(SELECT_BASE + " WHERE cc.tipo = 'DESPACHO' AND cc.estado = 'ABIERTO' "
                + "AND cc.id_agencia = ? AND cc.id_ruta = ?", dkprIdAgencia, dkprIdRuta);
    }

    public DkprControlCarga dkprBuscarRecepcionAbierta(int dkprIdManifiesto) throws SQLException {
        return dkprBuscarUno(SELECT_BASE + " WHERE cc.tipo = 'RECEPCION' AND cc.estado = 'ABIERTO' "
                + "AND cc.id_manifiesto = ?", dkprIdManifiesto);
    }

    public DkprControlCarga dkprBuscar(int dkprIdControl) throws SQLException {
        return dkprBuscarUno(SELECT_BASE + " WHERE cc.id_control = ?", dkprIdControl);
    }

    public int dkprInsertar(String dkprTipo, int dkprIdAgencia, int dkprIdRuta, Integer dkprIdManifiesto, int dkprIdEmpleado)
            throws SQLException {
        String dkprSql = "INSERT INTO control_carga (tipo, id_agencia, id_ruta, id_manifiesto, id_empleado) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id_control";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprTipo);
            dkprPs.setInt(2, dkprIdAgencia);
            dkprPs.setInt(3, dkprIdRuta);
            dkprPs.setObject(4, dkprIdManifiesto);
            dkprPs.setInt(5, dkprIdEmpleado);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                dkprRs.next();
                return dkprRs.getInt(1);
            }
        }
    }

    public void dkprInsertarLectura(int dkprIdControl, DkprLecturaCarga dkprL, int dkprIdEmpleado) throws SQLException {
        String dkprSql = "INSERT INTO control_carga_lectura (id_control, codigo_leido, id_envio, nro_bulto, resultado, "
                + "detalle, id_empleado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdControl);
            dkprPs.setString(2, dkprRecortar(dkprL.dkprGetCodigoLeido(), 60));
            dkprPs.setObject(3, dkprL.dkprGetIdEnvio());
            dkprPs.setObject(4, dkprL.dkprGetNroBulto());
            dkprPs.setString(5, dkprL.dkprGetResultado());
            dkprPs.setString(6, dkprRecortar(dkprL.dkprGetDetalle(), 200));
            dkprPs.setInt(7, dkprIdEmpleado);
            dkprPs.executeUpdate();
        }
    }

    public List<DkprLecturaCarga> dkprListarLecturas(int dkprIdControl) throws SQLException {
        String dkprSql = "SELECT codigo_leido, id_envio, nro_bulto, resultado, detalle, fecha "
                + "FROM control_carga_lectura WHERE id_control = ? ORDER BY fecha, id_lectura";
        List<DkprLecturaCarga> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdControl);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    DkprLecturaCarga dkprL = new DkprLecturaCarga();
                    dkprL.dkprSetCodigoLeido(dkprRs.getString("codigo_leido"));
                    dkprL.dkprSetIdEnvio((Integer) dkprRs.getObject("id_envio"));
                    dkprL.dkprSetNroBulto((Integer) dkprRs.getObject("nro_bulto"));
                    dkprL.dkprSetResultado(dkprRs.getString("resultado"));
                    dkprL.dkprSetDetalle(dkprRs.getString("detalle"));
                    dkprL.dkprSetFecha(dkprRs.getTimestamp("fecha").toLocalDateTime());
                    dkprLista.add(dkprL);
                }
            }
        }
        return dkprLista;
    }

    public boolean dkprCerrar(Connection dkprCn, int dkprIdControl, String dkprEstado, Integer dkprIdManifiesto,
                          String dkprObservacion) throws SQLException {
        String dkprSql = "UPDATE control_carga SET estado = ?, fecha_cierre = NOW(), "
                + "id_manifiesto = COALESCE(?, id_manifiesto), observacion = ? "
                + "WHERE id_control = ? AND estado = 'ABIERTO'";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprEstado);
            dkprPs.setObject(2, dkprIdManifiesto);
            dkprPs.setString(3, dkprObservacion);
            dkprPs.setInt(4, dkprIdControl);
            return dkprPs.executeUpdate() == 1;
        }
    }

    private DkprControlCarga dkprBuscarUno(String dkprSql, Object... dkprParametros) throws SQLException {
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            for (int dkprI = 0; dkprI < dkprParametros.length; dkprI++) {
                dkprPs.setObject(dkprI + 1, dkprParametros[dkprI]);
            }
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? dkprMapear(dkprRs) : null;
            }
        }
    }

    private DkprControlCarga dkprMapear(ResultSet dkprRs) throws SQLException {
        DkprControlCarga dkprC = new DkprControlCarga();
        dkprC.dkprSetIdControl(dkprRs.getInt("id_control"));
        dkprC.dkprSetTipo(dkprRs.getString("tipo"));
        dkprC.dkprSetIdAgencia(dkprRs.getInt("id_agencia"));
        dkprC.dkprSetIdRuta(dkprRs.getInt("id_ruta"));
        dkprC.dkprSetIdManifiesto((Integer) dkprRs.getObject("id_manifiesto"));
        dkprC.dkprSetManifiesto(dkprRs.getString("manifiesto"));
        dkprC.dkprSetOrigen(dkprRs.getString("origen"));
        dkprC.dkprSetDestino(dkprRs.getString("destino"));
        dkprC.dkprSetEmpleado(dkprRs.getString("empleado"));
        dkprC.dkprSetFechaInicio(dkprRs.getTimestamp("fecha_inicio").toLocalDateTime());
        dkprC.dkprSetEstado(dkprRs.getString("estado"));
        return dkprC;
    }

    private static String dkprRecortar(String dkprTexto, int dkprMax) {
        return dkprTexto == null || dkprTexto.length() <= dkprMax ? dkprTexto : dkprTexto.substring(0, dkprMax);
    }
}
