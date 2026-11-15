package com.grupo.envios.dao;

import com.grupo.envios.config.DkprConexion;
import com.grupo.envios.model.DkprCajaTurno;
import com.grupo.envios.model.DkprCobroCaja;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DkprCajaDAO {

    private static final String SELECT_BASE = """
            SELECT t.*, em.nombres || ' ' || em.apellidos AS empleado, ag.nombre AS agencia
            FROM caja_turno t
            JOIN empleado em ON em.id_empleado = t.id_empleado
            JOIN agencia ag ON ag.id_agencia = t.id_agencia
            """;

    public int dkprAbrir(int dkprIdEmpleado, int dkprIdAgencia, BigDecimal dkprMontoInicial) throws SQLException {
        String dkprSql = "INSERT INTO caja_turno (id_empleado, id_agencia, monto_inicial) VALUES (?, ?, ?) RETURNING id_turno";
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEmpleado);
            dkprPs.setInt(2, dkprIdAgencia);
            dkprPs.setBigDecimal(3, dkprMontoInicial);
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                dkprRs.next();
                return dkprRs.getInt(1);
            }
        }
    }

    public DkprCajaTurno dkprBuscarAbierto(int dkprIdEmpleado) throws SQLException {
        List<DkprCajaTurno> dkprL = dkprConsultar(SELECT_BASE + " WHERE t.id_empleado = ? AND t.estado = 'ABIERTO'", dkprIdEmpleado);
        return dkprL.isEmpty() ? null : dkprL.get(0);
    }

    public DkprCajaTurno dkprBuscar(int dkprIdTurno) throws SQLException {
        List<DkprCajaTurno> dkprL = dkprConsultar(SELECT_BASE + " WHERE t.id_turno = ?", dkprIdTurno);
        return dkprL.isEmpty() ? null : dkprL.get(0);
    }

    public List<DkprCajaTurno> dkprListar(LocalDate dkprDesde, LocalDate dkprHasta, int dkprIdEmpleado) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE t.fecha_apertura >= ? AND t.fecha_apertura < ? AND (? = 0 OR t.id_empleado = ?) "
                + "ORDER BY t.fecha_apertura DESC";
        return dkprConsultar(dkprSql, Timestamp.valueOf(dkprDesde.atStartOfDay()), Timestamp.valueOf(dkprHasta.plusDays(1).atStartOfDay()),
                dkprIdEmpleado, dkprIdEmpleado);
    }

    public List<DkprCobroCaja> dkprCobros(int dkprIdEmpleado, LocalDateTime dkprDesde, LocalDateTime dkprHasta) throws SQLException {
        String dkprSql = """
                SELECT c.serie, c.numero, c.fecha_emision, c.metodo_pago, c.total, e.codigo_seguimiento, e.id_estado_actual
                FROM comprobante c
                JOIN envio e ON e.id_envio = c.id_envio
                WHERE e.id_empleado_registro = ? AND c.fecha_emision >= ? AND c.fecha_emision <= COALESCE(?, NOW())
                ORDER BY c.fecha_emision, c.id_comprobante
                """;
        List<DkprCobroCaja> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            dkprPs.setInt(1, dkprIdEmpleado);
            dkprPs.setTimestamp(2, Timestamp.valueOf(dkprDesde));
            if (dkprHasta == null) {
                dkprPs.setNull(3, Types.TIMESTAMP);
            } else {
                dkprPs.setTimestamp(3, Timestamp.valueOf(dkprHasta));
            }
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(new DkprCobroCaja(
                            dkprRs.getString("serie") + "-" + String.format("%08d", dkprRs.getInt("numero")),
                            dkprRs.getString("codigo_seguimiento"),
                            dkprRs.getTimestamp("fecha_emision").toLocalDateTime(),
                            dkprRs.getString("metodo_pago"),
                            dkprRs.getBigDecimal("total"),
                            dkprRs.getInt("id_estado_actual") == 5));
                }
            }
        }
        return dkprLista;
    }

    public boolean dkprCerrar(DkprCajaTurno dkprT) throws SQLException {
        String dkprSql = """
                UPDATE caja_turno SET estado = 'CERRADO', fecha_cierre = ?,
                       esperado_efectivo = ?, esperado_tarjeta = ?, esperado_yape = ?, esperado_plin = ?,
                       contado_efectivo = ?, contado_tarjeta = ?, contado_yape = ?, contado_plin = ?,
                       anulados_cantidad = ?, anulados_monto = ?, observacion = ?
                WHERE id_turno = ? AND estado = 'ABIERTO'
                """;
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            int dkprI = 1;
            dkprPs.setTimestamp(dkprI++, Timestamp.valueOf(dkprT.dkprGetFechaCierre()));
            for (String dkprM : DkprCajaTurno.METODOS) {
                dkprPs.setBigDecimal(dkprI++, dkprT.dkprGetEsperado(dkprM));
            }
            for (String dkprM : DkprCajaTurno.METODOS) {
                dkprPs.setBigDecimal(dkprI++, dkprT.dkprGetContado(dkprM));
            }
            dkprPs.setInt(dkprI++, dkprT.dkprGetAnuladosCantidad());
            dkprPs.setBigDecimal(dkprI++, dkprT.dkprGetAnuladosMonto());
            dkprPs.setString(dkprI++, dkprT.dkprGetObservacion());
            dkprPs.setInt(dkprI, dkprT.dkprGetIdTurno());
            return dkprPs.executeUpdate() == 1;
        }
    }

    private List<DkprCajaTurno> dkprConsultar(String dkprSql, Object... dkprParametros) throws SQLException {
        List<DkprCajaTurno> dkprLista = new ArrayList<>();
        try (Connection dkprCn = DkprConexion.dkprObtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            for (int dkprI = 0; dkprI < dkprParametros.length; dkprI++) {
                dkprPs.setObject(dkprI + 1, dkprParametros[dkprI]);
            }
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(dkprMapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    private DkprCajaTurno dkprMapear(ResultSet dkprRs) throws SQLException {
        DkprCajaTurno dkprT = new DkprCajaTurno();
        dkprT.dkprSetIdTurno(dkprRs.getInt("id_turno"));
        dkprT.dkprSetIdEmpleado(dkprRs.getInt("id_empleado"));
        dkprT.dkprSetEmpleado(dkprRs.getString("empleado"));
        dkprT.dkprSetIdAgencia(dkprRs.getInt("id_agencia"));
        dkprT.dkprSetAgencia(dkprRs.getString("agencia"));
        dkprT.dkprSetFechaApertura(dkprRs.getTimestamp("fecha_apertura").toLocalDateTime());
        Timestamp dkprCierre = dkprRs.getTimestamp("fecha_cierre");
        dkprT.dkprSetFechaCierre(dkprCierre == null ? null : dkprCierre.toLocalDateTime());
        dkprT.dkprSetMontoInicial(dkprRs.getBigDecimal("monto_inicial"));
        dkprT.dkprSetEstado(dkprRs.getString("estado"));
        dkprT.dkprSetObservacion(dkprRs.getString("observacion"));
        for (String dkprM : DkprCajaTurno.METODOS) {
            String dkprColumna = dkprM.toLowerCase(Locale.ROOT);
            dkprT.dkprSetEsperado(dkprM, dkprRs.getBigDecimal("esperado_" + dkprColumna));
            dkprT.dkprSetContado(dkprM, dkprRs.getBigDecimal("contado_" + dkprColumna));
        }
        dkprT.dkprSetAnuladosCantidad(dkprRs.getInt("anulados_cantidad"));
        BigDecimal dkprAnulados = dkprRs.getBigDecimal("anulados_monto");
        dkprT.dkprSetAnuladosMonto(dkprAnulados == null ? BigDecimal.ZERO : dkprAnulados);
        return dkprT;
    }
}
