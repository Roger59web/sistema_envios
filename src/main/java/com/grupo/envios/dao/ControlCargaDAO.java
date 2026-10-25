package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.ControlCarga;
import com.grupo.envios.model.LecturaCarga;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ControlCargaDAO {

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

    /** Carga abierta de la agencia hacia esa ruta, o null si no hay ninguna. */
    public ControlCarga buscarDespachoAbierto(int dkprIdAgencia, int dkprIdRuta) throws SQLException {
        return buscarUno(SELECT_BASE + " WHERE cc.tipo = 'DESPACHO' AND cc.estado = 'ABIERTO' "
                + "AND cc.id_agencia = ? AND cc.id_ruta = ?", dkprIdAgencia, dkprIdRuta);
    }

    /** Descarga abierta de ese manifiesto, o null si no hay ninguna. */
    public ControlCarga buscarRecepcionAbierta(int dkprIdManifiesto) throws SQLException {
        return buscarUno(SELECT_BASE + " WHERE cc.tipo = 'RECEPCION' AND cc.estado = 'ABIERTO' "
                + "AND cc.id_manifiesto = ?", dkprIdManifiesto);
    }

    public ControlCarga buscar(int dkprIdControl) throws SQLException {
        return buscarUno(SELECT_BASE + " WHERE cc.id_control = ?", dkprIdControl);
    }

    /** Crea la sesión y devuelve su id. */
    public int insertar(String dkprTipo, int dkprIdAgencia, int dkprIdRuta, Integer dkprIdManifiesto, int dkprIdEmpleado)
            throws SQLException {
        String dkprSql = "INSERT INTO control_carga (tipo, id_agencia, id_ruta, id_manifiesto, id_empleado) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING id_control";
        try (Connection dkprCn = Conexion.obtener();
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

    /** Guarda la lectura. Si el bulto ya se había leído como OK lanza una violación de unicidad (23505). */
    public void insertarLectura(int dkprIdControl, LecturaCarga dkprL, int dkprIdEmpleado) throws SQLException {
        String dkprSql = "INSERT INTO control_carga_lectura (id_control, codigo_leido, id_envio, nro_bulto, resultado, "
                + "detalle, id_empleado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdControl);
            dkprPs.setString(2, recortar(dkprL.getCodigoLeido(), 60));
            dkprPs.setObject(3, dkprL.getIdEnvio());
            dkprPs.setObject(4, dkprL.getNroBulto());
            dkprPs.setString(5, dkprL.getResultado());
            dkprPs.setString(6, recortar(dkprL.getDetalle(), 200));
            dkprPs.setInt(7, dkprIdEmpleado);
            dkprPs.executeUpdate();
        }
    }

    /** Lecturas de la sesión en el orden en que se hicieron. */
    public List<LecturaCarga> listarLecturas(int dkprIdControl) throws SQLException {
        String dkprSql = "SELECT codigo_leido, id_envio, nro_bulto, resultado, detalle, fecha "
                + "FROM control_carga_lectura WHERE id_control = ? ORDER BY fecha, id_lectura";
        List<LecturaCarga> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdControl);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    LecturaCarga dkprL = new LecturaCarga();
                    dkprL.setCodigoLeido(dkprRs.getString("codigo_leido"));
                    dkprL.setIdEnvio((Integer) dkprRs.getObject("id_envio"));
                    dkprL.setNroBulto((Integer) dkprRs.getObject("nro_bulto"));
                    dkprL.setResultado(dkprRs.getString("resultado"));
                    dkprL.setDetalle(dkprRs.getString("detalle"));
                    dkprL.setFecha(dkprRs.getTimestamp("fecha").toLocalDateTime());
                    dkprLista.add(dkprL);
                }
            }
        }
        return dkprLista;
    }

    /**
     * Cierra (o cancela) la sesión solo si sigue abierta. Devuelve false si otra persona ya la cerró.
     *
     * @param idManifiesto en un despacho, el manifiesto generado al cerrar (null para dejar el que tenía)
     */
    public boolean cerrar(Connection dkprCn, int dkprIdControl, String dkprEstado, Integer dkprIdManifiesto,
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

    private ControlCarga buscarUno(String dkprSql, Object... dkprParametros) throws SQLException {
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            for (int dkprI = 0; dkprI < dkprParametros.length; dkprI++) {
                dkprPs.setObject(dkprI + 1, dkprParametros[dkprI]);
            }
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? mapear(dkprRs) : null;
            }
        }
    }

    private ControlCarga mapear(ResultSet dkprRs) throws SQLException {
        ControlCarga dkprC = new ControlCarga();
        dkprC.setIdControl(dkprRs.getInt("id_control"));
        dkprC.setTipo(dkprRs.getString("tipo"));
        dkprC.setIdAgencia(dkprRs.getInt("id_agencia"));
        dkprC.setIdRuta(dkprRs.getInt("id_ruta"));
        dkprC.setIdManifiesto((Integer) dkprRs.getObject("id_manifiesto"));
        dkprC.setManifiesto(dkprRs.getString("manifiesto"));
        dkprC.setOrigen(dkprRs.getString("origen"));
        dkprC.setDestino(dkprRs.getString("destino"));
        dkprC.setEmpleado(dkprRs.getString("empleado"));
        dkprC.setFechaInicio(dkprRs.getTimestamp("fecha_inicio").toLocalDateTime());
        dkprC.setEstado(dkprRs.getString("estado"));
        return dkprC;
    }

    private static String recortar(String dkprTexto, int dkprMax) {
        return dkprTexto == null || dkprTexto.length() <= dkprMax ? dkprTexto : dkprTexto.substring(0, dkprMax);
    }
}
