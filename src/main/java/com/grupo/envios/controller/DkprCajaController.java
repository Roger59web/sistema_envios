package com.grupo.envios.controller;

import com.grupo.envios.model.DkprCajaTurno;
import com.grupo.envios.model.DkprCobroCaja;
import com.grupo.envios.service.DkprCajaService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprFormato;
import com.grupo.envios.util.DkprGeneradorPdf;
import com.grupo.envios.util.DkprSesion;
import com.grupo.envios.util.DkprValidaciones;
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

public class DkprCajaController {

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
    @FXML private TableView<DkprCobroCaja> dkprTablaCobros;

    @FXML private DatePicker dkprDpDesde;
    @FXML private DatePicker dkprDpHasta;
    @FXML private TableView<DkprCajaTurno> dkprTablaHistorial;

    private final DkprCajaService dkprService = new DkprCajaService();

    private DkprCajaTurno dkprTurno;
    private List<DkprCobroCaja> dkprCobros = List.of();
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
        dkprArmarGrilla();
        dkprTablaCobros.getColumns().add(dkprColumna("Hora", 95, DkprCobroCaja::dkprGetFechaTexto));
        dkprTablaCobros.getColumns().add(dkprColumna("Comprobante", 130, DkprCobroCaja::dkprGetComprobante));
        dkprTablaCobros.getColumns().add(dkprColumna("Envío", 110, DkprCobroCaja::dkprGetCodigoEnvio));
        dkprTablaCobros.getColumns().add(dkprColumna("Método", 100, DkprCobroCaja::dkprGetMetodoPago));
        dkprTablaCobros.getColumns().add(dkprColumna("Total", 90, dkprC -> DkprFormato.dkprSoles(dkprC.dkprGetTotal())));
        dkprTablaCobros.getColumns().add(dkprColumna("Estado", 180, DkprCobroCaja::dkprGetEstadoTexto));
        dkprTablaCobros.setRowFactory(dkprT -> new TableRow<>() {
            @Override
            protected void updateItem(DkprCobroCaja dkprC, boolean dkprVacia) {
                super.updateItem(dkprC, dkprVacia);
                getStyleClass().remove("fila-alerta");
                if (!dkprVacia && dkprC != null && dkprC.dkprIsAnulado()) {
                    getStyleClass().add("fila-alerta");
                }
            }
        });

        dkprTablaHistorial.getColumns().add(dkprColumnaTurno("Apertura", 125, DkprCajaTurno::dkprGetFechaAperturaTexto));
        dkprTablaHistorial.getColumns().add(dkprColumnaTurno("Cierre", 125, DkprCajaTurno::dkprGetFechaCierreTexto));
        dkprTablaHistorial.getColumns().add(dkprColumnaTurno("Cajero", 150, DkprCajaTurno::dkprGetEmpleado));
        dkprTablaHistorial.getColumns().add(dkprColumnaTurno("Agencia", 150, DkprCajaTurno::dkprGetAgencia));
        dkprTablaHistorial.getColumns().add(dkprColumnaTurno("Fondo", 90, dkprT -> DkprFormato.dkprSoles(dkprT.dkprGetMontoInicial())));
        dkprTablaHistorial.getColumns().add(dkprColumnaTurno("Esperado", 100, dkprT -> dkprT.dkprIsAbierto() ? "-" : DkprFormato.dkprSoles(dkprT.dkprGetTotalEsperado())));
        dkprTablaHistorial.getColumns().add(dkprColumnaTurno("Contado", 100, dkprT -> dkprT.dkprGetTotalContado() == null ? "-" : DkprFormato.dkprSoles(dkprT.dkprGetTotalContado())));
        dkprTablaHistorial.getColumns().add(dkprColumnaTurno("Resultado", 140, dkprT -> dkprT.dkprIsAbierto() ? "ABIERTO" : DkprCajaTurno.dkprResultado(dkprT.dkprGetDiferenciaTotal())));
        dkprTablaHistorial.setRowFactory(dkprT -> {
            TableRow<DkprCajaTurno> dkprFila = new TableRow<>() {
                @Override
                protected void updateItem(DkprCajaTurno dkprTurnoFila, boolean dkprVacia) {
                    super.updateItem(dkprTurnoFila, dkprVacia);
                    getStyleClass().remove("fila-alerta");
                    if (!dkprVacia && dkprTurnoFila != null && dkprTurnoFila.dkprGetDiferenciaTotal() != null
                            && dkprTurnoFila.dkprGetDiferenciaTotal().signum() != 0) {
                        getStyleClass().add("fila-alerta");
                    }
                }
            };
            dkprFila.setOnMouseClicked(dkprEv -> {
                if (dkprEv.getClickCount() == 2 && !dkprFila.isEmpty()) {
                    dkprReporte(dkprFila.getItem());
                }
            });
            return dkprFila;
        });
        dkprDpDesde.setValue(LocalDate.now().withDayOfMonth(1));
        dkprDpHasta.setValue(LocalDate.now());

        dkprTxtFondo.setOnAction(dkprE -> dkprOnAbrir());
        dkprCargarTurno();
        dkprOnBuscarHistorial();
    }

    private void dkprArmarGrilla() {
        String[] dkprTitulos = {"Método", "Cobros", "Esperado", "Contado", "Diferencia"};
        for (int dkprC = 0; dkprC < dkprTitulos.length; dkprC++) {
            Label dkprL = new Label(dkprTitulos[dkprC]);
            dkprL.getStyleClass().add("etiqueta");
            dkprGrid.add(dkprL, dkprC, 0);
        }
        int dkprFila = 1;
        for (String dkprM : DkprCajaTurno.METODOS) {
            dkprLblCantidad.put(dkprM, new Label());
            dkprLblEsperado.put(dkprM, new Label());
            TextField dkprTxt = new TextField();
            dkprTxt.setPromptText("0.00");
            dkprTxt.setPrefWidth(110);
            dkprTxt.textProperty().addListener((dkprObs, dkprAntes, dkprAhora) -> dkprPintarDiferencias());
            dkprTxtContado.put(dkprM, dkprTxt);
            dkprLblDiferencia.put(dkprM, new Label());
            dkprGrid.addRow(dkprFila++, new Label("EFECTIVO".equals(dkprM) ? "Efectivo (incluye fondo)" : dkprCapitalizar(dkprM)),
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

    private static String dkprCapitalizar(String dkprM) {
        return dkprM.charAt(0) + dkprM.substring(1).toLowerCase();
    }

    private void dkprCargarTurno() {
        try {
            dkprTurno = dkprService.dkprTurnoAbierto(DkprSesion.dkprGetActual());
            dkprCobros = dkprTurno == null ? List.of() : dkprService.dkprCobros(dkprTurno);
        } catch (Exception dkprE) {
            dkprTurno = null;
            dkprCobros = List.of();
            DkprAlertas.dkprManejar(dkprE);
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
        dkprLblTurno.setText("Turno abierto el " + dkprTurno.dkprGetFechaAperturaTexto() + " en " + dkprTurno.dkprGetAgencia()
                + "  ·  fondo inicial " + DkprFormato.dkprSoles(dkprTurno.dkprGetMontoInicial()));
        for (String dkprM : DkprCajaTurno.METODOS) {
            dkprLblCantidad.get(dkprM).setText(String.valueOf(dkprTurno.dkprGetCantidad(dkprM)));
            dkprLblEsperado.get(dkprM).setText(DkprFormato.dkprSoles(dkprTurno.dkprGetEsperado(dkprM)));
        }
        dkprLblTotalCantidad.setText(String.valueOf(dkprTurno.dkprGetCantidadTotal()));
        dkprLblTotalEsperado.setText(DkprFormato.dkprSoles(dkprTurno.dkprGetTotalEsperado()));
        dkprLblAnulados.setText(dkprTurno.dkprGetAnuladosCantidad() == 0 ? "Sin envíos anulados en el turno."
                : dkprTurno.dkprGetAnuladosCantidad() + " envío(s) anulado(s) por " + DkprFormato.dkprSoles(dkprTurno.dkprGetAnuladosMonto())
                + ": no se cuentan en lo esperado (el dinero se devolvió al cliente).");
        dkprTablaCobros.setItems(FXCollections.observableArrayList(dkprCobros));
        dkprPintarDiferencias();
    }

    private Map<String, BigDecimal> dkprContado() {
        Map<String, BigDecimal> dkprMapa = new HashMap<>();
        for (String dkprM : DkprCajaTurno.METODOS) {
            String dkprTexto = dkprTxtContado.get(dkprM).getText();
            try {
                dkprMapa.put(dkprM, DkprValidaciones.dkprVacio(dkprTexto) ? null : DkprValidaciones.dkprDecimal(dkprTexto, dkprM));
            } catch (Exception dkprE) {
                dkprMapa.put(dkprM, null);
            }
        }
        return dkprMapa;
    }

    private void dkprPintarDiferencias() {
        if (dkprTurno == null) {
            return;
        }
        Map<String, BigDecimal> dkprContado = dkprContado();
        for (String dkprM : DkprCajaTurno.METODOS) {
            dkprTurno.dkprSetContado(dkprM, dkprContado.get(dkprM));
            dkprPintar(dkprLblDiferencia.get(dkprM), dkprTurno.dkprGetDiferencia(dkprM));
        }
        BigDecimal dkprTotalContado = dkprTurno.dkprGetTotalContado();
        dkprLblTotalContado.setText(dkprTotalContado == null ? "-" : DkprFormato.dkprSoles(dkprTotalContado));
        dkprPintar(dkprLblTotalDiferencia, dkprTurno.dkprGetDiferenciaTotal());
        BigDecimal dkprDiferencia = dkprTurno.dkprGetDiferenciaTotal();
        dkprLblResultado.setText(dkprDiferencia == null ? "Escribe lo contado en cada método" : DkprCajaTurno.dkprResultado(dkprDiferencia));
        dkprLblResultado.setStyle(dkprDiferencia == null ? "-fx-font-size: 16px; -fx-text-fill: #44546a;"
                : dkprDiferencia.signum() == 0 ? VERDE : ROJO);
    }

    private static void dkprPintar(Label dkprL, BigDecimal dkprDiferencia) {
        dkprL.setText(dkprDiferencia == null ? "-" : DkprCajaTurno.dkprResultado(dkprDiferencia));
        dkprL.setStyle(dkprDiferencia == null ? "" : (dkprDiferencia.signum() == 0 ? VERDE : ROJO) + " -fx-font-weight: bold;");
    }

    @FXML
    private void dkprOnAbrir() {
        try {
            BigDecimal dkprFondo = DkprValidaciones.dkprVacio(dkprTxtFondo.getText()) ? BigDecimal.ZERO
                    : DkprValidaciones.dkprDecimal(dkprTxtFondo.getText(), "Fondo inicial");
            if (!DkprAlertas.dkprConfirmar("¿Abrir el turno de caja con un fondo inicial de " + DkprFormato.dkprSoles(dkprFondo) + "?")) {
                return;
            }
            dkprService.dkprAbrir(DkprSesion.dkprGetActual(), dkprFondo);
            dkprCargarTurno();
            dkprOnBuscarHistorial();
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnActualizar() {
        Map<String, String> dkprEscrito = new HashMap<>();
        dkprTxtContado.forEach((dkprM, dkprTxt) -> dkprEscrito.put(dkprM, dkprTxt.getText()));
        dkprCargarTurno();
        dkprEscrito.forEach((dkprM, dkprTexto) -> dkprTxtContado.get(dkprM).setText(dkprTexto));
    }

    @FXML
    private void dkprOnCerrar() {
        if (dkprTurno == null) {
            return;
        }
        try {
            dkprOnActualizar();
            Map<String, BigDecimal> dkprContado = new HashMap<>();
            for (String dkprM : DkprCajaTurno.METODOS) {
                String dkprTexto = dkprTxtContado.get(dkprM).getText();
                if (DkprValidaciones.dkprVacio(dkprTexto)) {
                    DkprAlertas.dkprAdvertencia("Escribe lo contado en " + dkprCapitalizar(dkprM) + " (pon 0 si no hubo).");
                    dkprTxtContado.get(dkprM).requestFocus();
                    return;
                }
                dkprContado.put(dkprM, DkprValidaciones.dkprDecimal(dkprTexto, "Contado en " + dkprCapitalizar(dkprM)));
            }
            String dkprResultado = DkprCajaTurno.dkprResultado(dkprTurno.dkprGetDiferenciaTotal());
            if (!DkprAlertas.dkprConfirmar("¿Cerrar el turno?\n\nEsperado: " + DkprFormato.dkprSoles(dkprTurno.dkprGetTotalEsperado())
                    + "\nContado: " + DkprFormato.dkprSoles(dkprTurno.dkprGetTotalContado()) + "\nResultado: " + dkprResultado
                    + "\n\nDespués de cerrar ya no se puede modificar.")) {
                return;
            }
            DkprCajaTurno dkprCerrado = dkprService.dkprCerrar(dkprTurno, dkprContado, dkprTxtObservacion.getText(), DkprSesion.dkprGetActual());
            dkprTxtObservacion.clear();
            dkprTxtContado.values().forEach(TextField::clear);
            dkprCargarTurno();
            dkprOnBuscarHistorial();
            if (DkprAlertas.dkprConfirmar("Turno cerrado: " + DkprCajaTurno.dkprResultado(dkprCerrado.dkprGetDiferenciaTotal())
                    + ".\n\n¿Guardar el reporte de cuadre en PDF para imprimirlo?")) {
                dkprReporte(dkprCerrado);
            }
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnBuscarHistorial() {
        try {
            dkprTablaHistorial.setItems(FXCollections.observableArrayList(
                    dkprService.dkprHistorial(DkprSesion.dkprGetActual(), dkprDpDesde.getValue(), dkprDpHasta.getValue())));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnReporte() {
        DkprCajaTurno dkprT = dkprTablaHistorial.getSelectionModel().getSelectedItem();
        if (dkprT == null) {
            DkprAlertas.dkprAdvertencia("Selecciona un turno del historial.");
            return;
        }
        dkprReporte(dkprT);
    }

    private void dkprReporte(DkprCajaTurno dkprT) {
        try {
            List<DkprCobroCaja> dkprLista = dkprService.dkprCobros(dkprT);
            if (dkprT.dkprIsAbierto()) {
                DkprCajaService.dkprCalcular(dkprT, dkprLista);
            }
            String dkprNombre = "cuadre_caja_" + dkprT.dkprGetFechaApertura().toLocalDate() + "_" + dkprT.dkprGetIdTurno() + ".pdf";
            File dkprArchivo = DkprGeneradorPdf.dkprElegirDestino(dkprTabs.getScene().getWindow(), "Guardar cuadre de caja", dkprNombre);
            if (dkprArchivo == null) {
                return;
            }
            DkprGeneradorPdf.dkprCuadreCaja(dkprT, dkprLista, dkprArchivo);
            DkprGeneradorPdf.dkprOfrecerAbrir(dkprArchivo);
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private static TableColumn<DkprCobroCaja, String> dkprColumna(String dkprTitulo, double dkprAncho, Function<DkprCobroCaja, String> dkprValor) {
        TableColumn<DkprCobroCaja, String> dkprCol = new TableColumn<>(dkprTitulo);
        dkprCol.setPrefWidth(dkprAncho);
        dkprCol.setCellValueFactory(dkprC -> new SimpleStringProperty(dkprValor.apply(dkprC.getValue())));
        return dkprCol;
    }

    private static TableColumn<DkprCajaTurno, String> dkprColumnaTurno(String dkprTitulo, double dkprAncho, Function<DkprCajaTurno, String> dkprValor) {
        TableColumn<DkprCajaTurno, String> dkprCol = new TableColumn<>(dkprTitulo);
        dkprCol.setPrefWidth(dkprAncho);
        dkprCol.setCellValueFactory(dkprC -> new SimpleStringProperty(dkprValor.apply(dkprC.getValue())));
        return dkprCol;
    }
}
