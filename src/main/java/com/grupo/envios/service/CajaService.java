package com.grupo.envios.service;

import com.grupo.envios.dao.CajaDAO;
import com.grupo.envios.model.CajaTurno;
import com.grupo.envios.model.CobroCaja;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.util.Formato;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Roles;
import com.grupo.envios.util.Validaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class CajaService {

    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("99999999.99");

    private final CajaDAO dkprDao = new CajaDAO();

    public boolean puedeUsar(Empleado dkprE) {
        return dkprE != null && (Roles.ADMINISTRADOR.equals(dkprE.getRol()) || Roles.CAJERO.equals(dkprE.getRol()));
    }

    private boolean esAdmin(Empleado dkprE) {
        return dkprE != null && Roles.ADMINISTRADOR.equals(dkprE.getRol());
    }

    public CajaTurno turnoAbierto(Empleado dkprE) throws SQLException {
        CajaTurno dkprT = dkprDao.buscarAbierto(dkprE.getIdEmpleado());
        if (dkprT != null) {
            calcular(dkprT, cobros(dkprT));
        }
        return dkprT;
    }

    public boolean tieneTurnoAbierto(Empleado dkprE) throws SQLException {
        return dkprDao.buscarAbierto(dkprE.getIdEmpleado()) != null;
    }

    public CajaTurno abrir(Empleado dkprE, BigDecimal dkprMontoInicial) throws SQLException {
        if (!puedeUsar(dkprE)) {
            throw new NegocioException("Solo el cajero o el administrador maneja caja.");
        }
        BigDecimal dkprMonto = validarMonto(dkprMontoInicial, "El fondo inicial");
        try {
            dkprDao.abrir(dkprE.getIdEmpleado(), dkprE.getIdAgencia(), dkprMonto);
        } catch (SQLException dkprEx) {
            if ("23505".equals(dkprEx.getSQLState())) {
                throw new NegocioException("Ya tienes un turno de caja abierto.");
            }
            throw dkprEx;
        }
        return turnoAbierto(dkprE);
    }

    public List<CobroCaja> cobros(CajaTurno dkprT) throws SQLException {
        return dkprDao.cobros(dkprT.getIdEmpleado(), dkprT.getFechaApertura(), dkprT.getFechaCierre());
    }

    public static void calcular(CajaTurno dkprT, List<CobroCaja> dkprCobros) {
        int dkprAnulados = 0;
        BigDecimal dkprMontoAnulado = BigDecimal.ZERO;
        for (String dkprM : CajaTurno.METODOS) {
            dkprT.setCantidad(dkprM, 0);
            dkprT.setEsperado(dkprM, "EFECTIVO".equals(dkprM) ? dkprT.getMontoInicial() : BigDecimal.ZERO);
        }
        for (CobroCaja dkprC : dkprCobros) {
            if (dkprC.isAnulado()) {
                dkprAnulados++;
                dkprMontoAnulado = dkprMontoAnulado.add(dkprC.getTotal());
                continue;
            }
            String dkprM = dkprC.getMetodoPago();
            dkprT.setCantidad(dkprM, dkprT.getCantidad(dkprM) + 1);
            dkprT.setEsperado(dkprM, dkprT.getEsperado(dkprM).add(dkprC.getTotal()));
        }
        dkprT.setAnuladosCantidad(dkprAnulados);
        dkprT.setAnuladosMonto(dkprMontoAnulado);
    }

    public CajaTurno cerrar(CajaTurno dkprTurno, Map<String, BigDecimal> dkprContado, String dkprObservacion,
                            Empleado dkprE) throws SQLException {
        if (dkprTurno.getIdEmpleado() != dkprE.getIdEmpleado() && !esAdmin(dkprE)) {
            throw new NegocioException("Solo el cajero dueño del turno puede cerrarlo.");
        }
        CajaTurno dkprT = dkprDao.buscar(dkprTurno.getIdTurno());
        if (dkprT == null || !dkprT.isAbierto()) {
            throw new NegocioException("Este turno ya fue cerrado.");
        }
        for (String dkprM : CajaTurno.METODOS) {
            BigDecimal dkprMonto = dkprContado.get(dkprM);
            if (dkprMonto == null) {
                throw new NegocioException("Ingresa lo contado en " + dkprM + " (pon 0 si no hubo).");
            }
            dkprT.setContado(dkprM, validarMonto(dkprMonto, "Lo contado en " + dkprM));
        }
        dkprT.setFechaCierre(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        calcular(dkprT, cobros(dkprT));
        String dkprObs = Validaciones.opcional(dkprObservacion, "Observación", 200);
        BigDecimal dkprDiferencia = dkprT.getDiferenciaTotal();
        if (dkprDiferencia.signum() != 0 && dkprObs == null) {
            throw new NegocioException(CajaTurno.resultado(dkprDiferencia) + ": explica el motivo en la observación antes de cerrar.");
        }
        dkprT.setObservacion(dkprObs);
        if (!dkprDao.cerrar(dkprT)) {
            throw new NegocioException("Este turno ya fue cerrado.");
        }
        return dkprDao.buscar(dkprT.getIdTurno());
    }

    public List<CajaTurno> historial(Empleado dkprE, LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        if (dkprDesde == null || dkprHasta == null) {
            throw new NegocioException("Elige las fechas desde y hasta.");
        }
        if (dkprDesde.isAfter(dkprHasta)) {
            throw new NegocioException("La fecha inicial no puede ser posterior a la final.");
        }
        return dkprDao.listar(dkprDesde, dkprHasta, esAdmin(dkprE) ? 0 : dkprE.getIdEmpleado());
    }

    private static BigDecimal validarMonto(BigDecimal dkprMonto, String dkprQue) {
        if (dkprMonto == null || dkprMonto.signum() < 0 || dkprMonto.compareTo(MONTO_MAXIMO) > 0) {
            throw new NegocioException(dkprQue + " no es válido.");
        }
        if (dkprMonto.scale() > 2 && dkprMonto.setScale(2, RoundingMode.DOWN).compareTo(dkprMonto) != 0) {
            throw new NegocioException(dkprQue + " admite como máximo 2 decimales (" + Formato.soles(dkprMonto) + ").");
        }
        return dkprMonto.setScale(2, RoundingMode.HALF_UP);
    }
}
