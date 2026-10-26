package com.grupo.envios.controller;

import com.grupo.envios.model.CajaTurno;
import com.grupo.envios.model.CobroCaja;
import com.grupo.envios.service.CajaService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Formato;
import com.grupo.envios.util.GeneradorPdf;
import com.grupo.envios.util.Sesion;
import com.grupo.envios.util.Validaciones;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CajaController {

    private static final String VERDE = "-fx-text-fill: #1e8449;";
    private static final String ROJO = "-fx-text-fill: #c0392b;";

    @FXML private TabPane dkprTabs;
    @FXML private VBox dkprPanelApertura;
    @FXML private TextField dkprTxtFondo;
    @FXML private VBox dkprPanelTurno;
    @FXML private Label dkprLblTurno;
    @FXML private GridPane dkprGrid;
    @FXML private Label dkprLblAnulados;
    @FXML private Label dkprLblResultado;
    @FXML private TextField dkprTxtObservacion;
    @FXML private TableView<CobroCaja> dkprTablaCobros;

    @FXML private DatePicker dkprDpDesde;
    @FXML private DatePicker dkprDpHasta;
    @FXML private TableView<CajaTurno> dkprTablaHistorial;

    private final CajaService dkprService = new CajaService();

    private CajaTurno dkprTurno;
    private List<CobroCaja> dkprCobros = List.of();
    private final Map<String, Label> dkprLblCantidad = new HashMap<>();
    private final Map<String, Label> dkprLblEsperado = new HashMap<>();
    private final Map<String, TextField> dkprTxtContado = new HashMap<>();
    private final Map<String, Label> dkprLblDiferencia = new HashMap<>();
    private final Label dkprLblTotalCantidad = new Label();
    private final Label dkprLblTotalEsperado = new Label();
    private final Label dkprLblTotalContado = new Label();
    private final Label dkprLblTotalDiferencia = new Label();

    @FXML
    private void initialize() {
        armarGrilla();
        dkprTablaCobros.getColumns().add(columna("Hora", 95, CobroCaja::getFechaTexto));
        dkprTablaCobros.getColumns().add(columna("Comprobante", 130, CobroCaja::getComprobante));
        dkprTablaCobros.getColumns().add(columna("Envío", 110, CobroCaja::getCodigoEnvio));
        dkprTablaCobros.getColumns().add(columna("Método", 100, CobroCaja::getMetodoPago));
        dkprTablaCobros.getColumns().add(columna("Total", 90, dkprC -> Formato.soles(dkprC.getTotal())));
        dkprTablaCobros.getColumns().add(columna("Estado", 180, CobroCaja::getEstadoTexto));
        dkprTablaCobros.setRowFactory(dkprT -> new TableRow<>() {
            @Override
            protected void updateItem(CobroCaja dkprC, boolean dkprVacia) {
                super.updateItem(dkprC, dkprVacia);
                getStyleClass().remove("fila-alerta");
                if (!dkprVacia && dkprC != null && dkprC.isAnulado()) {
                    getStyleClass().add("fila-alerta");
                }
            }
        });

        dkprTablaHistorial.getColumns().add(columnaTurno("Apertura", 125, CajaTurno::getFechaAperturaTexto));
        dkprTablaHistorial.getColumns().add(columnaTurno("Cierre", 125, CajaTurno::getFechaCierreTexto));
        dkprTablaHistorial.getColumns().add(columnaTurno("Cajero", 150, CajaTurno::getEmpleado));
        dkprTablaHistorial.getColumns().add(columnaTurno("Agencia", 150, CajaTurno::getAgencia));
        dkprTablaHistorial.getColumns().add(columnaTurno("Fondo", 90, dkprT -> Formato.soles(dkprT.getMontoInicial())));
        dkprTablaHistorial.getColumns().add(columnaTurno("Esperado", 100, dkprT -> dkprT.isAbierto() ? "-" : Formato.soles(dkprT.getTotalEsperado())));
        dkprTablaHistorial.getColumns().add(columnaTurno("Contado", 100, dkprT -> dkprT.getTotalContado() == null ? "-" : Formato.soles(dkprT.getTotalContado())));
        dkprTablaHistorial.getColumns().add(columnaTurno("Resultado", 140, dkprT -> dkprT.isAbierto() ? "ABIERTO" : CajaTurno.resultado(dkprT.getDiferenciaTotal())));
        dkprTablaHistorial.setRowFactory(dkprT -> {
            TableRow<CajaTurno> dkprFila = new TableRow<>() {
                @Override
                protected void updateItem(CajaTurno dkprTurnoFila, boolean dkprVacia) {
                    super.updateItem(dkprTurnoFila, dkprVacia);
                    getStyleClass().remove("fila-alerta");
                    if (!dkprVacia && dkprTurnoFila != null && dkprTurnoFila.getDiferenciaTotal() != null
                            && dkprTurnoFila.getDiferenciaTotal().signum() != 0) {
                        getStyleClass().add("fila-alerta");
                    }
                }
            };
            dkprFila.setOnMouseClicked(dkprEv -> {
                if (dkprEv.getClickCount() == 2 && !dkprFila.isEmpty()) {
                    reporte(dkprFila.getItem());
                }
            });
            return dkprFila;
        });
        dkprDpDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dkprDpHasta.setValue(LocalDate.now());

        dkprTxtFondo.setOnAction(dkprE -> onAbrir());
        cargarTurno();
        onBuscarHistorial();
    }

    private void armarGrilla() {
        String[] dkprTitulos = {"Método", "Cobros", "Esperado", "Contado", "Diferencia"};
        for (int dkprC = 0; dkprC < dkprTitulos.length; dkprC++) {
            Label dkprL = new Label(dkprTitulos[dkprC]);
            dkprL.getStyleClass().add("etiqueta");
            dkprGrid.add(dkprL, dkprC, 0);
        }
        int dkprFila = 1;
        for (String dkprM : CajaTurno.METODOS) {
            dkprLblCantidad.put(dkprM, new Label());
            dkprLblEsperado.put(dkprM, new Label());
            TextField dkprTxt = new TextField();
            dkprTxt.setPromptText("0.00");
            dkprTxt.setPrefWidth(110);
            dkprTxt.textProperty().addListener((dkprObs, dkprAntes, dkprAhora) -> pintarDiferencias());
            dkprTxtContado.put(dkprM, dkprTxt);
            dkprLblDiferencia.put(dkprM, new Label());
            dkprGrid.addRow(dkprFila++, new Label("EFECTIVO".equals(dkprM) ? "Efectivo (incluye fondo)" : capitalizar(dkprM)),
                    dkprLblCantidad.get(dkprM), dkprLblEsperado.get(dkprM), dkprTxt, dkprLblDiferencia.get(dkprM));
        }
        Label dkprTotal = new Label("TOTAL");
        dkprTotal.getStyleClass().add("etiqueta");
        for (Label dkprL : List.of(dkprLblTotalCantidad, dkprLblTotalEsperado, dkprLblTotalContado, dkprLblTotalDiferencia)) {
            dkprL.getStyleClass().add("etiqueta");
        }
        dkprGrid.addRow(dkprFila, dkprTotal, dkprLblTotalCantidad, dkprLblTotalEsperado, dkprLblTotalContado, dkprLblTotalDiferencia);
        for (javafx.scene.Node dkprN : dkprGrid.getChildren()) {
            Integer dkprCol = GridPane.getColumnIndex(dkprN);
            if (dkprCol != null && dkprCol != 0 && dkprCol != 3) {
                GridPane.setHalignment(dkprN, HPos.RIGHT);
            }
        }
    }

    private static String capitalizar(String dkprM) {
        return dkprM.charAt(0) + dkprM.substring(1).toLowerCase();
    }

    private void cargarTurno() {
        try {
            dkprTurno = dkprService.turnoAbierto(Sesion.getActual());
            dkprCobros = dkprTurno == null ? List.of() : dkprService.cobros(dkprTurno);
        } catch (Exception dkprE) {
            dkprTurno = null;
            dkprCobros = List.of();
            Alertas.manejar(dkprE);
        }
        boolean dkprAbierto = dkprTurno != null;
        dkprPanelApertura.setVisible(!dkprAbierto);
        dkprPanelApertura.setManaged(!dkprAbierto);
        dkprPanelTurno.setVisible(dkprAbierto);
        dkprPanelTurno.setManaged(dkprAbierto);
        if (!dkprAbierto) {
            dkprTxtFondo.clear();
            return;
        }
        dkprLblTurno.setText("Turno abierto el " + dkprTurno.getFechaAperturaTexto() + " en " + dkprTurno.getAgencia()
                + "  ·  fondo inicial " + Formato.soles(dkprTurno.getMontoInicial()));
        for (String dkprM : CajaTurno.METODOS) {
            dkprLblCantidad.get(dkprM).setText(String.valueOf(dkprTurno.getCantidad(dkprM)));
            dkprLblEsperado.get(dkprM).setText(Formato.soles(dkprTurno.getEsperado(dkprM)));
        }
        dkprLblTotalCantidad.setText(String.valueOf(dkprTurno.getCantidadTotal()));
        dkprLblTotalEsperado.setText(Formato.soles(dkprTurno.getTotalEsperado()));
        dkprLblAnulados.setText(dkprTurno.getAnuladosCantidad() == 0 ? "Sin envíos anulados en el turno."
                : dkprTurno.getAnuladosCantidad() + " envío(s) anulado(s) por " + Formato.soles(dkprTurno.getAnuladosMonto())
                + ": no se cuentan en lo esperado (el dinero se devolvió al cliente).");
        dkprTablaCobros.setItems(FXCollections.observableArrayList(dkprCobros));
        pintarDiferencias();
    }

    private Map<String, BigDecimal> contado() {
        Map<String, BigDecimal> dkprMapa = new HashMap<>();
        for (String dkprM : CajaTurno.METODOS) {
            String dkprTexto = dkprTxtContado.get(dkprM).getText();
            try {
                dkprMapa.put(dkprM, Validaciones.vacio(dkprTexto) ? null : Validaciones.decimal(dkprTexto, dkprM));
            } catch (Exception dkprE) {
                dkprMapa.put(dkprM, null);
            }
        }
        return dkprMapa;
    }

    private void pintarDiferencias() {
        if (dkprTurno == null) {
            return;
        }
        Map<String, BigDecimal> dkprContado = contado();
        for (String dkprM : CajaTurno.METODOS) {
            dkprTurno.setContado(dkprM, dkprContado.get(dkprM));
            pintar(dkprLblDiferencia.get(dkprM), dkprTurno.getDiferencia(dkprM));
        }
        BigDecimal dkprTotalContado = dkprTurno.getTotalContado();
        dkprLblTotalContado.setText(dkprTotalContado == null ? "-" : Formato.soles(dkprTotalContado));
        pintar(dkprLblTotalDiferencia, dkprTurno.getDiferenciaTotal());
        BigDecimal dkprDiferencia = dkprTurno.getDiferenciaTotal();
        dkprLblResultado.setText(dkprDiferencia == null ? "Escribe lo contado en cada método" : CajaTurno.resultado(dkprDiferencia));
        dkprLblResultado.setStyle(dkprDiferencia == null ? "-fx-font-size: 16px; -fx-text-fill: #44546a;"
                : dkprDiferencia.signum() == 0 ? VERDE : ROJO);
    }

    private static void pintar(Label dkprL, BigDecimal dkprDiferencia) {
        dkprL.setText(dkprDiferencia == null ? "-" : CajaTurno.resultado(dkprDiferencia));
        dkprL.setStyle(dkprDiferencia == null ? "" : (dkprDiferencia.signum() == 0 ? VERDE : ROJO) + " -fx-font-weight: bold;");
    }

    @FXML
    private void onAbrir() {
        try {
            BigDecimal dkprFondo = Validaciones.vacio(dkprTxtFondo.getText()) ? BigDecimal.ZERO
                    : Validaciones.decimal(dkprTxtFondo.getText(), "Fondo inicial");
            if (!Alertas.confirmar("¿Abrir el turno de caja con un fondo inicial de " + Formato.soles(dkprFondo) + "?")) {
                return;
            }
            dkprService.abrir(Sesion.getActual(), dkprFondo);
            cargarTurno();
            onBuscarHistorial();
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onActualizar() {
        Map<String, String> dkprEscrito = new HashMap<>();
        dkprTxtContado.forEach((dkprM, dkprTxt) -> dkprEscrito.put(dkprM, dkprTxt.getText()));
        cargarTurno();
        dkprEscrito.forEach((dkprM, dkprTexto) -> dkprTxtContado.get(dkprM).setText(dkprTexto));
    }

    @FXML
    private void onCerrar() {
        if (dkprTurno == null) {
            return;
        }
        try {
            onActualizar();
            Map<String, BigDecimal> dkprContado = new HashMap<>();
            for (String dkprM : CajaTurno.METODOS) {
                String dkprTexto = dkprTxtContado.get(dkprM).getText();
                if (Validaciones.vacio(dkprTexto)) {
                    Alertas.advertencia("Escribe lo contado en " + capitalizar(dkprM) + " (pon 0 si no hubo).");
                    dkprTxtContado.get(dkprM).requestFocus();
                    return;
                }
                dkprContado.put(dkprM, Validaciones.decimal(dkprTexto, "Contado en " + capitalizar(dkprM)));
            }
            String dkprResultado = CajaTurno.resultado(dkprTurno.getDiferenciaTotal());
            if (!Alertas.confirmar("¿Cerrar el turno?\n\nEsperado: " + Formato.soles(dkprTurno.getTotalEsperado())
                    + "\nContado: " + Formato.soles(dkprTurno.getTotalContado()) + "\nResultado: " + dkprResultado
                    + "\n\nDespués de cerrar ya no se puede modificar.")) {
                return;
            }
            CajaTurno dkprCerrado = dkprService.cerrar(dkprTurno, dkprContado, dkprTxtObservacion.getText(), Sesion.getActual());
            dkprTxtObservacion.clear();
            dkprTxtContado.values().forEach(TextField::clear);
            cargarTurno();
            onBuscarHistorial();
            if (Alertas.confirmar("Turno cerrado: " + CajaTurno.resultado(dkprCerrado.getDiferenciaTotal())
                    + ".\n\n¿Guardar el reporte de cuadre en PDF para imprimirlo?")) {
                reporte(dkprCerrado);
            }
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onBuscarHistorial() {
        try {
            dkprTablaHistorial.setItems(FXCollections.observableArrayList(
                    dkprService.historial(Sesion.getActual(), dkprDpDesde.getValue(), dkprDpHasta.getValue())));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onReporte() {
        CajaTurno dkprT = dkprTablaHistorial.getSelectionModel().getSelectedItem();
        if (dkprT == null) {
            Alertas.advertencia("Selecciona un turno del historial.");
            return;
        }
        reporte(dkprT);
    }

    private void reporte(CajaTurno dkprT) {
        try {
            List<CobroCaja> dkprLista = dkprService.cobros(dkprT);
            if (dkprT.isAbierto()) {
                CajaService.calcular(dkprT, dkprLista);
            }
            String dkprNombre = "cuadre_caja_" + dkprT.getFechaApertura().toLocalDate() + "_" + dkprT.getIdTurno() + ".pdf";
            File dkprArchivo = GeneradorPdf.elegirDestino(dkprTabs.getScene().getWindow(), "Guardar cuadre de caja", dkprNombre);
            if (dkprArchivo == null) {
                return;
            }
            GeneradorPdf.cuadreCaja(dkprT, dkprLista, dkprArchivo);
            GeneradorPdf.ofrecerAbrir(dkprArchivo);
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private static TableColumn<CobroCaja, String> columna(String dkprTitulo, double dkprAncho, Function<CobroCaja, String> dkprValor) {
        TableColumn<CobroCaja, String> dkprCol = new TableColumn<>(dkprTitulo);
        dkprCol.setPrefWidth(dkprAncho);
        dkprCol.setCellValueFactory(dkprC -> new SimpleStringProperty(dkprValor.apply(dkprC.getValue())));
        return dkprCol;
    }

    private static TableColumn<CajaTurno, String> columnaTurno(String dkprTitulo, double dkprAncho, Function<CajaTurno, String> dkprValor) {
        TableColumn<CajaTurno, String> dkprCol = new TableColumn<>(dkprTitulo);
        dkprCol.setPrefWidth(dkprAncho);
        dkprCol.setCellValueFactory(dkprC -> new SimpleStringProperty(dkprValor.apply(dkprC.getValue())));
        return dkprCol;
    }
}
