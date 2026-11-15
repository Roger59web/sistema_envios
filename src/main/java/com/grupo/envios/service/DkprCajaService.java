package com.grupo.envios.service;

import com.grupo.envios.dao.DkprCajaDAO;
import com.grupo.envios.model.DkprCajaTurno;
import com.grupo.envios.model.DkprCobroCaja;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.util.DkprFormato;
import com.grupo.envios.util.DkprNegocioException;
import com.grupo.envios.util.DkprRoles;
import com.grupo.envios.util.DkprValidaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class DkprCajaService {

    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("99999999.99");

    private final DkprCajaDAO dkprDao = new DkprCajaDAO();

    public boolean dkprPuedeUsar(DkprEmpleado dkprE) {
        return dkprE != null && (DkprRoles.ADMINISTRADOR.equals(dkprE.dkprGetRol()) || DkprRoles.CAJERO.equals(dkprE.dkprGetRol()));
    }

    private boolean dkprEsAdmin(DkprEmpleado dkprE) {
        return dkprE != null && DkprRoles.ADMINISTRADOR.equals(dkprE.dkprGetRol());
    }

    public DkprCajaTurno dkprTurnoAbierto(DkprEmpleado dkprE) throws SQLException {
        DkprCajaTurno dkprT = dkprDao.dkprBuscarAbierto(dkprE.dkprGetIdEmpleado());
        if (dkprT != null) {
            dkprCalcular(dkprT, dkprCobros(dkprT));
        }
        return dkprT;
    }

    public boolean dkprTieneTurnoAbierto(DkprEmpleado dkprE) throws SQLException {
        return dkprDao.dkprBuscarAbierto(dkprE.dkprGetIdEmpleado()) != null;
    }

    public DkprCajaTurno dkprAbrir(DkprEmpleado dkprE, BigDecimal dkprMontoInicial) throws SQLException {
        if (!dkprPuedeUsar(dkprE)) {
            throw new DkprNegocioException("Solo el cajero o el administrador maneja caja.");
        }
        BigDecimal dkprMonto = dkprValidarMonto(dkprMontoInicial, "El fondo inicial");
        try {
            dkprDao.dkprAbrir(dkprE.dkprGetIdEmpleado(), dkprE.dkprGetIdAgencia(), dkprMonto);
        } catch (SQLException dkprEx) {
            if ("23505".equals(dkprEx.getSQLState())) {
                throw new DkprNegocioException("Ya tienes un turno de caja abierto.");
            }
            throw dkprEx;
        }
        return dkprTurnoAbierto(dkprE);
    }

    public List<DkprCobroCaja> dkprCobros(DkprCajaTurno dkprT) throws SQLException {
        return dkprDao.dkprCobros(dkprT.dkprGetIdEmpleado(), dkprT.dkprGetFechaApertura(), dkprT.dkprGetFechaCierre());
    }

    public static void dkprCalcular(DkprCajaTurno dkprT, List<DkprCobroCaja> dkprCobros) {
        int dkprAnulados = 0;
        BigDecimal dkprMontoAnulado = BigDecimal.ZERO;
        for (String dkprM : DkprCajaTurno.METODOS) {
            dkprT.dkprSetCantidad(dkprM, 0);
            dkprT.dkprSetEsperado(dkprM, "EFECTIVO".equals(dkprM) ? dkprT.dkprGetMontoInicial() : BigDecimal.ZERO);
        }
        for (DkprCobroCaja dkprC : dkprCobros) {
            if (dkprC.dkprIsAnulado()) {
                dkprAnulados++;
                dkprMontoAnulado = dkprMontoAnulado.add(dkprC.dkprGetTotal());
                continue;
            }
            String dkprM = dkprC.dkprGetMetodoPago();
            dkprT.dkprSetCantidad(dkprM, dkprT.dkprGetCantidad(dkprM) + 1);
            dkprT.dkprSetEsperado(dkprM, dkprT.dkprGetEsperado(dkprM).add(dkprC.dkprGetTotal()));
        }
        dkprT.dkprSetAnuladosCantidad(dkprAnulados);
        dkprT.dkprSetAnuladosMonto(dkprMontoAnulado);
    }

    public DkprCajaTurno dkprCerrar(DkprCajaTurno dkprTurno, Map<String, BigDecimal> dkprContado, String dkprObservacion,
                            DkprEmpleado dkprE) throws SQLException {
        if (dkprTurno.dkprGetIdEmpleado() != dkprE.dkprGetIdEmpleado() && !dkprEsAdmin(dkprE)) {
            throw new DkprNegocioException("Solo el cajero dueño del turno puede cerrarlo.");
        }
        DkprCajaTurno dkprT = dkprDao.dkprBuscar(dkprTurno.dkprGetIdTurno());
        if (dkprT == null || !dkprT.dkprIsAbierto()) {
            throw new DkprNegocioException("Este turno ya fue cerrado.");
        }
        for (String dkprM : DkprCajaTurno.METODOS) {
            BigDecimal dkprMonto = dkprContado.get(dkprM);
            if (dkprMonto == null) {
                throw new DkprNegocioException("Ingresa lo contado en " + dkprM + " (pon 0 si no hubo).");
            }
            dkprT.dkprSetContado(dkprM, dkprValidarMonto(dkprMonto, "Lo contado en " + dkprM));
        }
        dkprT.dkprSetFechaCierre(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        dkprCalcular(dkprT, dkprCobros(dkprT));
        String dkprObs = DkprValidaciones.dkprOpcional(dkprObservacion, "Observación", 200);
        BigDecimal dkprDiferencia = dkprT.dkprGetDiferenciaTotal();
        if (dkprDiferencia.signum() != 0 && dkprObs == null) {
            throw new DkprNegocioException(DkprCajaTurno.dkprResultado(dkprDiferencia) + ": explica el motivo en la observación antes de cerrar.");
        }
        dkprT.dkprSetObservacion(dkprObs);
        if (!dkprDao.dkprCerrar(dkprT)) {
            throw new DkprNegocioException("Este turno ya fue cerrado.");
        }
        return dkprDao.dkprBuscar(dkprT.dkprGetIdTurno());
    }

    public List<DkprCajaTurno> dkprHistorial(DkprEmpleado dkprE, LocalDate dkprDesde, LocalDate dkprHasta) throws SQLException {
        if (dkprDesde == null || dkprHasta == null) {
            throw new DkprNegocioException("Elige las fechas desde y hasta.");
        }
        if (dkprDesde.isAfter(dkprHasta)) {
            throw new DkprNegocioException("La fecha inicial no puede ser posterior a la final.");
        }
        return dkprDao.dkprListar(dkprDesde, dkprHasta, dkprEsAdmin(dkprE) ? 0 : dkprE.dkprGetIdEmpleado());
    }

    private static BigDecimal dkprValidarMonto(BigDecimal dkprMonto, String dkprQue) {
        if (dkprMonto == null || dkprMonto.signum() < 0 || dkprMonto.compareTo(MONTO_MAXIMO) > 0) {
            throw new DkprNegocioException(dkprQue + " no es válido.");
        }
        if (dkprMonto.scale() > 2 && dkprMonto.setScale(2, RoundingMode.DOWN).compareTo(dkprMonto) != 0) {
            throw new DkprNegocioException(dkprQue + " admite como máximo 2 decimales (" + DkprFormato.dkprSoles(dkprMonto) + ").");
        }
        return dkprMonto.setScale(2, RoundingMode.HALF_UP);
    }
}
