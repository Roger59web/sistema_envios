package com.grupo.envios.controller;

import com.grupo.envios.model.Envio;
import com.grupo.envios.model.ResumenFila;
import com.grupo.envios.service.EnvioService;
import com.grupo.envios.service.ReporteService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.GeneradorPdf;
import com.grupo.envios.util.VistaTicket;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportesController {

    @FXML private DatePicker dkprDpDesde;
    @FXML private DatePicker dkprDpHasta;
    @FXML private Label dkprLblTotalEnvios;
    @FXML private Label dkprLblIngresos;

    @FXML private TableView<ResumenFila> dkprTablaAgencias;
    @FXML private TableColumn<ResumenFila, String> dkprColAgEtiqueta;
    @FXML private TableColumn<ResumenFila, Integer> dkprColAgCantidad;
    @FXML private TableColumn<ResumenFila, BigDecimal> dkprColAgMonto;

    @FXML private TableView<ResumenFila> dkprTablaEstados;
    @FXML private TableColumn<ResumenFila, String> dkprColEstEtiqueta;
    @FXML private TableColumn<ResumenFila, Integer> dkprColEstCantidad;
    @FXML private TableColumn<ResumenFila, BigDecimal> dkprColEstMonto;

    @FXML private TableView<Envio> dkprTablaDetalle;
    @FXML private TableColumn<Envio, String> dkprColCodigo;
    @FXML private TableColumn<Envio, String> dkprColFecha;
    @FXML private TableColumn<Envio, String> dkprColRuta;
    @FXML private TableColumn<Envio, String> dkprColRemitente;
    @FXML private TableColumn<Envio, String> dkprColDestinatario;
    @FXML private TableColumn<Envio, String> dkprColEstado;
    @FXML private TableColumn<Envio, BigDecimal> dkprColCosto;

    private final ReporteService dkprService = new ReporteService();
    private final EnvioService dkprEnvioService = new EnvioService();

    @FXML
    private void initialize() {
        dkprColAgEtiqueta.setCellValueFactory(new PropertyValueFactory<>("etiqueta"));
        dkprColAgCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        dkprColAgMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        dkprColEstEtiqueta.setCellValueFactory(new PropertyValueFactory<>("etiqueta"));
        dkprColEstCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        dkprColEstMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));

        dkprColCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoSeguimiento"));
        dkprColFecha.setCellValueFactory(new PropertyValueFactory<>("fechaTexto"));
        dkprColRuta.setCellValueFactory(new PropertyValueFactory<>("ruta"));
        dkprColRemitente.setCellValueFactory(new PropertyValueFactory<>("remitente"));
        dkprColDestinatario.setCellValueFactory(new PropertyValueFactory<>("destinatario"));
        dkprColEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        dkprColCosto.setCellValueFactory(new PropertyValueFactory<>("costoEnvio"));

        dkprTablaDetalle.setRowFactory(dkprTabla -> {
            TableRow<Envio> dkprFila = new TableRow<>();
            dkprFila.setOnMouseClicked(dkprEv -> {
                if (dkprEv.getClickCount() == 2 && !dkprFila.isEmpty()) {
                    verTicket(dkprFila.getItem());
                }
            });
            return dkprFila;
        });

        LocalDate dkprHoy = LocalDate.now();
        dkprDpDesde.setValue(dkprHoy.withDayOfMonth(1));
        dkprDpHasta.setValue(dkprHoy);
        onGenerar();
    }

    @FXML
    private void onGenerar() {
        try {
            LocalDate dkprDesde = dkprDpDesde.getValue();
            LocalDate dkprHasta = dkprDpHasta.getValue();

            List<ResumenFila> dkprPorAgencia = dkprService.porAgencia(dkprDesde, dkprHasta);
            List<ResumenFila> dkprPorEstado = dkprService.porEstado(dkprDesde, dkprHasta);
            List<Envio> dkprDetalle = dkprService.detalle(dkprDesde, dkprHasta);

            dkprTablaAgencias.setItems(FXCollections.observableArrayList(dkprPorAgencia));
            dkprTablaEstados.setItems(FXCollections.observableArrayList(dkprPorEstado));
            dkprTablaDetalle.setItems(FXCollections.observableArrayList(dkprDetalle));

            BigDecimal dkprIngresos = dkprPorAgencia.stream()
                    .map(ResumenFila::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dkprLblTotalEnvios.setText(String.valueOf(dkprDetalle.size()));
            dkprLblIngresos.setText("S/ " + dkprIngresos.setScale(2).toPlainString());
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onExportar() {
        List<Envio> dkprDetalle = new ArrayList<>(dkprTablaDetalle.getItems());
        if (dkprDetalle.isEmpty()) {
            Alertas.advertencia("No hay envíos en el reporte para exportar.");
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
            dkprW.write('﻿'); // para que Excel reconozca las tildes
            dkprW.write("Codigo,Fecha,Origen,Destino,Remitente,Destinatario,Estado,Costo (S/)\n");
            for (Envio dkprE : dkprDetalle) {
                dkprW.write(String.join(",",
                        csv(dkprE.getCodigoSeguimiento()), csv(dkprE.getFechaTexto()), csv(dkprE.getOrigen()),
                        csv(dkprE.getDestino()), csv(dkprE.getRemitente()), csv(dkprE.getDestinatario()),
                        csv(dkprE.getEstado()), csv(dkprE.getCostoEnvio().toPlainString())));
                dkprW.write("\n");
            }
            Alertas.info("Reporte guardado en:\n" + dkprArchivo.getAbsolutePath());
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onExportarPdf() {
        List<Envio> dkprDetalle = new ArrayList<>(dkprTablaDetalle.getItems());
        if (dkprDetalle.isEmpty()) {
            Alertas.advertencia("No hay envíos en el reporte para exportar.");
            return;
        }
        File dkprArchivo = GeneradorPdf.elegirDestino(dkprTablaDetalle.getScene().getWindow(), "Guardar reporte de envíos",
                "reporte_envios_" + dkprDpDesde.getValue() + "_" + dkprDpHasta.getValue() + ".pdf");
        if (dkprArchivo == null) {
            return;
        }
        try {
            GeneradorPdf.reporte(dkprDpDesde.getValue(), dkprDpHasta.getValue(),
                    new ArrayList<>(dkprTablaAgencias.getItems()), new ArrayList<>(dkprTablaEstados.getItems()),
                    dkprDetalle, dkprArchivo);
            GeneradorPdf.ofrecerAbrir(dkprArchivo);
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private void verTicket(Envio dkprEnvio) {
        try {
            VistaTicket.mostrar(dkprEnvioService.detalle(dkprEnvio.getCodigoSeguimiento()),
                    dkprTablaDetalle.getScene().getWindow());
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private String csv(String dkprValor) {
        String dkprV = dkprValor == null ? "" : dkprValor;
        if (dkprV.contains(",") || dkprV.contains("\"") || dkprV.contains("\n")) {
            return "\"" + dkprV.replace("\"", "\"\"") + "\"";
        }
        return dkprV;
    }
}
