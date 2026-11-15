package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.model.DkprResumenFila;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DkprEnvioDAO {

    private static final String SELECT_BASE = """
            SELECT e.id_envio, e.codigo_seguimiento, e.id_ruta,
                   ru.id_agencia_origen, ru.id_agencia_destino,
                   ao.nombre AS origen, ad.nombre AS destino,
                   ao.direccion AS origen_direccion, ao.telefono AS origen_telefono,
                   e.id_remitente, rem.nombre_completo AS remitente, rem.numero_documento AS remitente_doc,
                   rem.telefono AS remitente_tel,
                   e.id_destinatario, des.nombre_completo AS destinatario, des.numero_documento AS destinatario_doc,
                   des.telefono AS destinatario_tel,
                   e.id_empleado_registro, em.nombres || ' ' || em.apellidos AS empleado_registro,
                   e.id_estado_actual, es.nombre AS estado, e.fecha_registro,
                   e.descripcion, e.cantidad_bultos, e.peso_kg, e.valor_declarado, e.costo_envio, e.observaciones,
                   ru.dias_estimados, man.codigo AS manifiesto, man.placa AS manifiesto_placa,
                   man.conductor AS manifiesto_conductor,
                   (SELECT MAX(h.fecha) FROM historial_envio h WHERE h.id_envio = e.id_envio) AS fecha_ultimo_mov
            FROM envio e
            JOIN ruta ru ON ru.id_ruta = e.id_ruta
            JOIN agencia ao ON ao.id_agencia = ru.id_agencia_origen
            JOIN agencia ad ON ad.id_agencia = ru.id_agencia_destino
            JOIN cliente rem ON rem.id_cliente = e.id_remitente
            JOIN cliente des ON des.id_cliente = e.id_destinatario
            JOIN estado_envio es ON es.id_estado = e.id_estado_actual
            JOIN empleado em ON em.id_empleado = e.id_empleado_registro
            LEFT JOIN manifiesto man ON man.id_manifiesto = e.id_manifiesto
            """;

    public int dkprSiguienteId(Connection dkprCn) throws SQLException {
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(
                "SELECT nextval(pg_get_serial_sequence('envio', 'id_envio'))");
             ResultSet dkprRs = dkprPs.executeQuery()) {
            dkprRs.next();
            return dkprRs.getInt(1);
        }
    }

    public void dkprInsertar(Connection dkprCn, DkprEnvio dkprE) throws SQLException {
        String dkprSql = "INSERT INTO envio (id_envio, codigo_seguimiento, id_ruta, id_remitente, id_destinatario, "
                + "id_empleado_registro, id_estado_actual, descripcion, cantidad_bultos, peso_kg, "
                + "valor_declarado, costo_envio, observaciones) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprE.dkprGetIdEnvio());
            dkprPs.setString(2, dkprE.dkprGetCodigoSeguimiento());
            dkprPs.setInt(3, dkprE.dkprGetIdRuta());
            dkprPs.setInt(4, dkprE.dkprGetIdRemitente());
            dkprPs.setInt(5, dkprE.dkprGetIdDestinatario());
            dkprPs.setInt(6, dkprE.dkprGetIdEmpleadoRegistro());
            dkprPs.setInt(7, dkprE.dkprGetIdEstadoActual());
            dkprPs.setString(8, dkprE.dkprGetDescripcion());
            dkprPs.setInt(9, dkprE.dkprGetCantidadBultos());
            dkprPs.setBigDecimal(10, dkprE.dkprGetPesoKg());
            dkprPs.setBigDecimal(11, dkprE.dkprGetValorDeclarado());
            dkprPs.setBigDecimal(12, dkprE.dkprGetCostoEnvio());
            dkprPs.setString(13, dkprE.dkprGetObservaciones());
            dkprPs.executeUpdate();
        }
    }

    public boolean dkprActualizarEstadoSi(Connection dkprCn, int dkprIdEnvio, int dkprEstadoEsperado, int dkprNuevoEstado,
                                      Integer dkprIdManifiesto) throws SQLException {
        String dkprSql = "UPDATE envio SET id_estado_actual = ?, id_manifiesto = COALESCE(?, id_manifiesto) "
                + "WHERE id_envio = ? AND id_estado_actual = ?";
        try (PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprNuevoEstado);
            dkprPs.setObject(2, dkprIdManifiesto);
            dkprPs.setInt(3, dkprIdEnvio);
            dkprPs.setInt(4, dkprEstadoEsperado);
            return dkprPs.executeUpdate() == 1;
        }
    }

    public List<DkprEnvio> dkprListarPorEstadoYAgencia(int dkprIdEstado, int dkprIdAgencia, boolean dkprComoOrigen) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.id_estado_actual = ? AND "
                + (dkprComoOrigen ? "ru.id_agencia_origen" : "ru.id_agencia_destino") + " = ? "
                + "ORDER BY e.fecha_registro, e.id_envio";
        List<DkprEnvio> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEstado);
            dkprPs.setInt(2, dkprIdAgencia);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(dkprMapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    public DkprEnvio dkprBuscarPorCodigo(String dkprCodigo) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.codigo_seguimiento = ?";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setString(1, dkprCodigo);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? dkprMapear(dkprRs) : null;
            }
        }
    }

    public DkprEnvio dkprBuscarPorId(int dkprIdEnvio) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.id_envio = ?";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEnvio);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                return dkprRs.next() ? dkprMapear(dkprRs) : null;
            }
        }
    }

    public List<DkprEnvio> dkprListarPorManifiesto(int dkprIdManifiesto) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.id_manifiesto = ? ORDER BY e.id_envio";
        List<DkprEnvio> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdManifiesto);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(dkprMapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    public List<DkprEnvio> dkprListarEstancados(int dkprDias, int dkprIdAgencia) throws SQLException {
        String dkprSql = "SELECT * FROM (" + SELECT_BASE + """
                 WHERE e.id_estado_actual IN (1, 2, 3)) x
                WHERE COALESCE(x.fecha_ultimo_mov, x.fecha_registro)::date < CURRENT_DATE - ?
                  AND (? = 0
                       OR (x.id_estado_actual = 1 AND x.id_agencia_origen = ?)
                       OR (x.id_estado_actual = 2 AND ? IN (x.id_agencia_origen, x.id_agencia_destino))
                       OR (x.id_estado_actual = 3 AND x.id_agencia_destino = ?))
                ORDER BY COALESCE(x.fecha_ultimo_mov, x.fecha_registro), x.id_envio
                """;
        List<DkprEnvio> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprDias);
            for (int dkprI = 2; dkprI <= 5; dkprI++) {
                dkprPs.setInt(dkprI, dkprIdAgencia);
            }
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(dkprMapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    public List<DkprEnvio> dkprListarPorFechas(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE e.fecha_registro >= ? AND e.fecha_registro < ? "
                + "ORDER BY e.fecha_registro DESC, e.id_envio DESC";
        List<DkprEnvio> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setTimestamp(1, Timestamp.valueOf(dkprDesde.atStartOfDay()));
            dkprPs.setTimestamp(2, Timestamp.valueOf(dkprHasta.plusDays(1).atStartOfDay()));
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(dkprMapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    public List<DkprResumenFila> dkprResumenPorAgencia(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        String dkprSql = """
                SELECT ao.nombre AS etiqueta, COUNT(*) AS cantidad,
                       COALESCE(SUM(CASE WHEN e.id_estado_actual <> 5 THEN e.costo_envio ELSE 0 END), 0) AS monto
                FROM envio e
                JOIN ruta ru ON ru.id_ruta = e.id_ruta
                JOIN agencia ao ON ao.id_agencia = ru.id_agencia_origen
                WHERE e.fecha_registro >= ? AND e.fecha_registro < ?
                GROUP BY ao.nombre
                ORDER BY ao.nombre
                """;
        return dkprResumen(dkprSql, dkprDesde, dkprHasta);
    }

    public List<DkprResumenFila> dkprResumenPorEstado(LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        String dkprSql = """
                SELECT es.nombre AS etiqueta, COUNT(*) AS cantidad, COALESCE(SUM(e.costo_envio), 0) AS monto
                FROM envio e
                JOIN estado_envio es ON es.id_estado = e.id_estado_actual
                WHERE e.fecha_registro >= ? AND e.fecha_registro < ?
                GROUP BY es.nombre, es.orden
                ORDER BY es.orden
                """;
        return dkprResumen(dkprSql, dkprDesde, dkprHasta);
    }

    private List<DkprResumenFila> dkprResumen(String dkprSql, LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        List<DkprResumenFila> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setTimestamp(1, Timestamp.valueOf(dkprDesde.atStartOfDay()));
            dkprPs.setTimestamp(2, Timestamp.valueOf(dkprHasta.plusDays(1).atStartOfDay()));
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(new DkprResumenFila(dkprRs.getString("etiqueta"), dkprRs.getInt("cantidad"),
                            dkprRs.getBigDecimal("monto")));
                }
            }
        }
        return dkprLista;
    }

    private DkprEnvio dkprMapear(ResultSet dkprRs) throws SQLException {
        DkprEnvio dkprE = new DkprEnvio();
        dkprE.dkprSetIdEnvio(dkprRs.getInt("id_envio"));
        dkprE.dkprSetCodigoSeguimiento(dkprRs.getString("codigo_seguimiento"));
        dkprE.dkprSetIdRuta(dkprRs.getInt("id_ruta"));
        dkprE.dkprSetIdAgenciaOrigen(dkprRs.getInt("id_agencia_origen"));
        dkprE.dkprSetIdAgenciaDestino(dkprRs.getInt("id_agencia_destino"));
        dkprE.dkprSetOrigen(dkprRs.getString("origen"));
        dkprE.dkprSetDestino(dkprRs.getString("destino"));
        dkprE.dkprSetOrigenDireccion(dkprRs.getString("origen_direccion"));
        dkprE.dkprSetOrigenTelefono(dkprRs.getString("origen_telefono"));
        dkprE.dkprSetIdRemitente(dkprRs.getInt("id_remitente"));
        dkprE.dkprSetRemitente(dkprRs.getString("remitente"));
        dkprE.dkprSetRemitenteDocumento(dkprRs.getString("remitente_doc"));
        dkprE.dkprSetRemitenteTelefono(dkprRs.getString("remitente_tel"));
        dkprE.dkprSetIdDestinatario(dkprRs.getInt("id_destinatario"));
        dkprE.dkprSetDestinatario(dkprRs.getString("destinatario"));
        dkprE.dkprSetDestinatarioDocumento(dkprRs.getString("destinatario_doc"));
        dkprE.dkprSetDestinatarioTelefono(dkprRs.getString("destinatario_tel"));
        dkprE.dkprSetIdEmpleadoRegistro(dkprRs.getInt("id_empleado_registro"));
        dkprE.dkprSetEmpleadoRegistro(dkprRs.getString("empleado_registro"));
        dkprE.dkprSetIdEstadoActual(dkprRs.getInt("id_estado_actual"));
        dkprE.dkprSetEstado(dkprRs.getString("estado"));
        dkprE.dkprSetFechaRegistro(dkprRs.getTimestamp("fecha_registro").toLocalDateTime());
        dkprE.dkprSetDescripcion(dkprRs.getString("descripcion"));
        dkprE.dkprSetCantidadBultos(dkprRs.getInt("cantidad_bultos"));
        dkprE.dkprSetPesoKg(dkprRs.getBigDecimal("peso_kg"));
        dkprE.dkprSetValorDeclarado(dkprRs.getBigDecimal("valor_declarado"));
        dkprE.dkprSetCostoEnvio(dkprRs.getBigDecimal("costo_envio"));
        dkprE.dkprSetObservaciones(dkprRs.getString("observaciones"));
        dkprE.dkprSetDiasEstimados(dkprRs.getInt("dias_estimados"));
        dkprE.dkprSetManifiesto(dkprRs.getString("manifiesto"));
        dkprE.dkprSetPlaca(dkprRs.getString("manifiesto_placa"));
        dkprE.dkprSetConductor(dkprRs.getString("manifiesto_conductor"));
        Timestamp dkprUltimo = dkprRs.getTimestamp("fecha_ultimo_mov");
        dkprE.dkprSetFechaUltimoMovimiento(dkprUltimo == null ? null : dkprUltimo.toLocalDateTime());
        return dkprE;
    }
}
