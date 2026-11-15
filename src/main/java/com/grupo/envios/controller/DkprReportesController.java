package com.grupo.envios.controller;

import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.model.DkprResumenFila;
import com.grupo.envios.service.DkprEnvioService;
import com.grupo.envios.service.DkprReporteService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprGeneradorPdf;
import com.grupo.envios.util.DkprVistaTicket;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DkprReportesController {

    @FXML private DatePicker dkprDpDesde;
    @FXML private DatePicker dkprDpHasta;
    @FXML private Label dkprLblTotalEnvios;
    @FXML private Label dkprLblIngresos;

    @FXML private TableView<DkprResumenFila> dkprTablaAgencias;
    @FXML private TableColumn<DkprResumenFila, String> dkprColAgEtiqueta;
    @FXML private TableColumn<DkprResumenFila, Integer> dkprColAgCantidad;
    @FXML private TableColumn<DkprResumenFila, BigDecimal> dkprColAgMonto;

    @FXML private TableView<DkprResumenFila> dkprTablaEstados;
    @FXML private TableColumn<DkprResumenFila, String> dkprColEstEtiqueta;
    @FXML private TableColumn<DkprResumenFila, Integer> dkprColEstCantidad;
    @FXML private TableColumn<DkprResumenFila, BigDecimal> dkprColEstMonto;

    @FXML private TableView<DkprEnvio> dkprTablaDetalle;
    @FXML private TableColumn<DkprEnvio, String> dkprColCodigo;
    @FXML private TableColumn<DkprEnvio, String> dkprColFecha;
    @FXML private TableColumn<DkprEnvio, String> dkprColRuta;
    @FXML private TableColumn<DkprEnvio, String> dkprColRemitente;
    @FXML private TableColumn<DkprEnvio, String> dkprColDestinatario;
    @FXML private TableColumn<DkprEnvio, String> dkprColEstado;
    @FXML private TableColumn<DkprEnvio, BigDecimal> dkprColCosto;

    private final DkprReporteService dkprService = new DkprReporteService();
    private final DkprEnvioService dkprEnvioService = new DkprEnvioService();

    @FXML
    private void initialize() {
        dkprColAgEtiqueta.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetEtiqueta()));
        dkprColAgCantidad.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetCantidad()));
        dkprColAgMonto.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetMonto()));
        dkprColEstEtiqueta.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetEtiqueta()));
        dkprColEstCantidad.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetCantidad()));
        dkprColEstMonto.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetMonto()));

        dkprColCodigo.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetCodigoSeguimiento()));
        dkprColFecha.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetFechaTexto()));
        dkprColRuta.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetRuta()));
        dkprColRemitente.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetRemitente()));
        dkprColDestinatario.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetDestinatario()));
        dkprColEstado.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetEstado()));
        dkprColCosto.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetCostoEnvio()));

        dkprTablaDetalle.setRowFactory(dkprTabla -> {
            TableRow<DkprEnvio> dkprFila = new TableRow<>();
            dkprFila.setOnMouseClicked(dkprEv -> {
                if (dkprEv.getClickCount() == 2 && !dkprFila.isEmpty()) {
                    dkprVerTicket(dkprFila.getItem());
                }
            });
            return dkprFila;
        });

        LocalDate dkprHoy = LocalDate.now();
        dkprDpDesde.setValue(dkprHoy.withDayOfMonth(1));
        dkprDpHasta.setValue(dkprHoy);
        dkprOnGenerar();
    }

    @FXML
    private void dkprOnGenerar() {
        try {
            LocalDate dkprDesde = dkprDpDesde.getValue();
            LocalDate dkprHasta = dkprDpHasta.getValue();

            List<DkprResumenFila> dkprPorAgencia = dkprService.dkprPorAgencia(dkprDesde, dkprHasta);
            List<DkprResumenFila> dkprPorEstado = dkprService.dkprPorEstado(dkprDesde, dkprHasta);
            List<DkprEnvio> dkprDetalle = dkprService.dkprDetalle(dkprDesde, dkprHasta);

            dkprTablaAgencias.setItems(FXCollections.observableArrayList(dkprPorAgencia));
            dkprTablaEstados.setItems(FXCollections.observableArrayList(dkprPorEstado));
            dkprTablaDetalle.setItems(FXCollections.observableArrayList(dkprDetalle));

            BigDecimal dkprIngresos = dkprPorAgencia.stream()
                    .map(DkprResumenFila::dkprGetMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dkprLblTotalEnvios.setText(String.valueOf(dkprDetalle.size()));
            dkprLblIngresos.setText("S/ " + dkprIngresos.setScale(2).toPlainString());
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnExportar() {
        List<DkprEnvio> dkprDetalle = new ArrayList<>(dkprTablaDetalle.getItems());
        if (dkprDetalle.isEmpty()) {
            DkprAlertas.dkprAdvertencia("No hay envíos en el reporte para exportar.");
            return;
        }

        FileChooser dkprSelector = new FileChooser();
        dkprSelector.setTitle("Guardar reporte de envíos");
        dkprSelector.setInitialFileName("reporte_envios_" + dkprDpDesde.getValue() + "_" + dkprDpHasta.getValue() + ".csv");
        dkprSelector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv"));
        File dkprArchivo = dkprSelector.showSaveDialog(dkprTablaDetalle.getScene().getWindow());
        if (dkprArchivo == null) {
            return;
        }

        try (BufferedWriter dkprW = Files.newBufferedWriter(dkprArchivo.toPath(), StandardCharsets.UTF_8)) {
            dkprW.write('﻿');
            dkprW.write("Codigo,Fecha,Origen,Destino,Remitente,Destinatario,Estado,Costo (S/)\n");
            for (DkprEnvio dkprE : dkprDetalle) {
                dkprW.write(String.join(",",
                        dkprCsv(dkprE.dkprGetCodigoSeguimiento()), dkprCsv(dkprE.dkprGetFechaTexto()), dkprCsv(dkprE.dkprGetOrigen()),
                        dkprCsv(dkprE.dkprGetDestino()), dkprCsv(dkprE.dkprGetRemitente()), dkprCsv(dkprE.dkprGetDestinatario()),
                        dkprCsv(dkprE.dkprGetEstado()), dkprCsv(dkprE.dkprGetCostoEnvio().toPlainString())));
                dkprW.write("\n");
            }
            DkprAlertas.dkprInfo("Reporte guardado en:\n" + dkprArchivo.getAbsolutePath());
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnExportarPdf() {
        List<DkprEnvio> dkprDetalle = new ArrayList<>(dkprTablaDetalle.getItems());
        if (dkprDetalle.isEmpty()) {
            DkprAlertas.dkprAdvertencia("No hay envíos en el reporte para exportar.");
            return;
        }
        File dkprArchivo = DkprGeneradorPdf.dkprElegirDestino(dkprTablaDetalle.getScene().getWindow(), "Guardar reporte de envíos",
                "reporte_envios_" + dkprDpDesde.getValue() + "_" + dkprDpHasta.getValue() + ".pdf");
        if (dkprArchivo == null) {
            return;
        }
        try {
            DkprGeneradorPdf.dkprReporte(dkprDpDesde.getValue(), dkprDpHasta.getValue(),
                    new ArrayList<>(dkprTablaAgencias.getItems()), new ArrayList<>(dkprTablaEstados.getItems()),
                    dkprDetalle, dkprArchivo);
            DkprGeneradorPdf.dkprOfrecerAbrir(dkprArchivo);
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprVerTicket(DkprEnvio dkprEnvio) {
        try {
            DkprVistaTicket.dkprMostrar(dkprEnvioService.dkprDetalle(dkprEnvio.dkprGetCodigoSeguimiento()),
                    dkprTablaDetalle.getScene().getWindow());
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private String dkprCsv(String dkprValor) {
        String dkprV = dkprValor == null ? "" : dkprValor;
        if (dkprV.contains(",") || dkprV.contains("\"") || dkprV.contains("\n")) {
            return "\"" + dkprV.replace("\"", "\"\"") + "\"";
        }
        return dkprV;
    }
}
