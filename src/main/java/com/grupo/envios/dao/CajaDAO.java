package com.grupo.envios.dao;

import com.grupo.envios.config.Conexion;
import com.grupo.envios.model.CajaTurno;
import com.grupo.envios.model.CobroCaja;

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

public class CajaDAO {

    private static final String SELECT_BASE = """
            SELECT t.*, em.nombres || ' ' || em.apellidos AS empleado, ag.nombre AS agencia
            FROM caja_turno t
            JOIN empleado em ON em.id_empleado = t.id_empleado
            JOIN agencia ag ON ag.id_agencia = t.id_agencia
            """;

    public int abrir(int dkprIdEmpleado, int dkprIdAgencia, BigDecimal dkprMontoInicial) throws SQLException {
        String dkprSql = "INSERT INTO caja_turno (id_empleado, id_agencia, monto_inicial) VALUES (?, ?, ?) RETURNING id_turno";
        try (Connection dkprCn = Conexion.obtener();
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

    public CajaTurno buscarAbierto(int dkprIdEmpleado) throws SQLException {
        List<CajaTurno> dkprL = consultar(SELECT_BASE + " WHERE t.id_empleado = ? AND t.estado = 'ABIERTO'", dkprIdEmpleado);
        return dkprL.isEmpty() ? null : dkprL.get(0);
    }

    public CajaTurno buscar(int dkprIdTurno) throws SQLException {
        List<CajaTurno> dkprL = consultar(SELECT_BASE + " WHERE t.id_turno = ?", dkprIdTurno);
        return dkprL.isEmpty() ? null : dkprL.get(0);
    }

    public List<CajaTurno> listar(LocalDate dkprDesde, LocalDate dkprHasta, int dkprIdEmpleado) throws SQLException {
        String dkprSql = SELECT_BASE + " WHERE t.fecha_apertura >= ? AND t.fecha_apertura < ? AND (? = 0 OR t.id_empleado = ?) "
                + "ORDER BY t.fecha_apertura DESC";
        return consultar(dkprSql, Timestamp.valueOf(dkprDesde.atStartOfDay()), Timestamp.valueOf(dkprHasta.plusDays(1).atStartOfDay()),
                dkprIdEmpleado, dkprIdEmpleado);
    }

    public List<CobroCaja> cobros(int dkprIdEmpleado, LocalDateTime dkprDesde, LocalDateTime dkprHasta) throws SQLException {
        String dkprSql = """
                SELECT c.serie, c.numero, c.fecha_emision, c.metodo_pago, c.total, e.codigo_seguimiento, e.id_estado_actual
                FROM comprobante c
                JOIN envio e ON e.id_envio = c.id_envio
                WHERE e.id_empleado_registro = ? AND c.fecha_emision >= ? AND c.fecha_emision <= COALESCE(?, NOW())
                ORDER BY c.fecha_emision, c.id_comprobante
                """;
        List<CobroCaja> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
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
                    dkprLista.add(new CobroCaja(
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

    public boolean cerrar(CajaTurno dkprT) throws SQLException {
        String dkprSql = """
                UPDATE caja_turno SET estado = 'CERRADO', fecha_cierre = ?,
                       esperado_efectivo = ?, esperado_tarjeta = ?, esperado_yape = ?, esperado_plin = ?,
                       contado_efectivo = ?, contado_tarjeta = ?, contado_yape = ?, contado_plin = ?,
                       anulados_cantidad = ?, anulados_monto = ?, observacion = ?
                WHERE id_turno = ? AND estado = 'ABIERTO'
                """;
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            int dkprI = 1;
            dkprPs.setTimestamp(dkprI++, Timestamp.valueOf(dkprT.getFechaCierre()));
            for (String dkprM : CajaTurno.METODOS) {
                dkprPs.setBigDecimal(dkprI++, dkprT.getEsperado(dkprM));
            }
            for (String dkprM : CajaTurno.METODOS) {
                dkprPs.setBigDecimal(dkprI++, dkprT.getContado(dkprM));
            }
            dkprPs.setInt(dkprI++, dkprT.getAnuladosCantidad());
            dkprPs.setBigDecimal(dkprI++, dkprT.getAnuladosMonto());
            dkprPs.setString(dkprI++, dkprT.getObservacion());
            dkprPs.setInt(dkprI, dkprT.getIdTurno());
            return dkprPs.executeUpdate() == 1;
        }
    }

    private List<CajaTurno> consultar(String dkprSql, Object... dkprParametros) throws SQLException {
        List<CajaTurno> dkprLista = new ArrayList<>();
        try (Connection dkprCn = Conexion.obtener();
             PreparedStatement dkprPs = dkprCn.prepareStatement(dkprSql)) {
            for (int dkprI = 0; dkprI < dkprParametros.length; dkprI++) {
                dkprPs.setObject(dkprI + 1, dkprParametros[dkprI]);
            }
            try (ResultSet dkprRs = dkprPs.executeQuery()) {
                while (dkprRs.next()) {
                    dkprLista.add(mapear(dkprRs));
                }
            }
        }
        return dkprLista;
    }

    private CajaTurno mapear(ResultSet dkprRs) throws SQLException {
        CajaTurno dkprT = new CajaTurno();
        dkprT.setIdTurno(dkprRs.getInt("id_turno"));
        dkprT.setIdEmpleado(dkprRs.getInt("id_empleado"));
        dkprT.setEmpleado(dkprRs.getString("empleado"));
        dkprT.setIdAgencia(dkprRs.getInt("id_agencia"));
        dkprT.setAgencia(dkprRs.getString("agencia"));
        dkprT.setFechaApertura(dkprRs.getTimestamp("fecha_apertura").toLocalDateTime());
        Timestamp dkprCierre = dkprRs.getTimestamp("fecha_cierre");
        dkprT.setFechaCierre(dkprCierre == null ? null : dkprCierre.toLocalDateTime());
        dkprT.setMontoInicial(dkprRs.getBigDecimal("monto_inicial"));
        dkprT.setEstado(dkprRs.getString("estado"));
        dkprT.setObservacion(dkprRs.getString("observacion"));
        for (String dkprM : CajaTurno.METODOS) {
            String dkprColumna = dkprM.toLowerCase(Locale.ROOT);
            dkprT.setEsperado(dkprM, dkprRs.getBigDecimal("esperado_" + dkprColumna));
            dkprT.setContado(dkprM, dkprRs.getBigDecimal("contado_" + dkprColumna));
        }
        dkprT.setAnuladosCantidad(dkprRs.getInt("anulados_cantidad"));
        BigDecimal dkprAnulados = dkprRs.getBigDecimal("anulados_monto");
        dkprT.setAnuladosMonto(dkprAnulados == null ? BigDecimal.ZERO : dkprAnulados);
        return dkprT;
    }
}
