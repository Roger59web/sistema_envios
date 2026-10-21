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

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Label lblTotalEnvios;
    @FXML private Label lblIngresos;

    @FXML private TableView<ResumenFila> tablaAgencias;
    @FXML private TableColumn<ResumenFila, String> colAgEtiqueta;
    @FXML private TableColumn<ResumenFila, Integer> colAgCantidad;
    @FXML private TableColumn<ResumenFila, BigDecimal> colAgMonto;

    @FXML private TableView<ResumenFila> tablaEstados;
    @FXML private TableColumn<ResumenFila, String> colEstEtiqueta;
    @FXML private TableColumn<ResumenFila, Integer> colEstCantidad;
    @FXML private TableColumn<ResumenFila, BigDecimal> colEstMonto;

    @FXML private TableView<Envio> tablaDetalle;
    @FXML private TableColumn<Envio, String> colCodigo;
    @FXML private TableColumn<Envio, String> colFecha;
    @FXML private TableColumn<Envio, String> colRuta;
    @FXML private TableColumn<Envio, String> colRemitente;
    @FXML private TableColumn<Envio, String> colDestinatario;
    @FXML private TableColumn<Envio, String> colEstado;
    @FXML private TableColumn<Envio, BigDecimal> colCosto;

    private final ReporteService service = new ReporteService();
    private final EnvioService envioService = new EnvioService();

    @FXML
    private void initialize() {
        colAgEtiqueta.setCellValueFactory(new PropertyValueFactory<>("etiqueta"));
        colAgCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colAgMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colEstEtiqueta.setCellValueFactory(new PropertyValueFactory<>("etiqueta"));
        colEstCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colEstMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoSeguimiento"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaTexto"));
        colRuta.setCellValueFactory(new PropertyValueFactory<>("ruta"));
        colRemitente.setCellValueFactory(new PropertyValueFactory<>("remitente"));
        colDestinatario.setCellValueFactory(new PropertyValueFactory<>("destinatario"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costoEnvio"));

        tablaDetalle.setRowFactory(tabla -> {
            TableRow<Envio> fila = new TableRow<>();
            fila.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !fila.isEmpty()) {
                    verTicket(fila.getItem());
                }
            });
            return fila;
        });

        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(hoy.withDayOfMonth(1));
        dpHasta.setValue(hoy);
        onGenerar();
    }

    @FXML
    private void onGenerar() {
        try {
            LocalDate desde = dpDesde.getValue();
            LocalDate hasta = dpHasta.getValue();

            List<ResumenFila> porAgencia = service.porAgencia(desde, hasta);
            List<ResumenFila> porEstado = service.porEstado(desde, hasta);
            List<Envio> detalle = service.detalle(desde, hasta);

            tablaAgencias.setItems(FXCollections.observableArrayList(porAgencia));
            tablaEstados.setItems(FXCollections.observableArrayList(porEstado));
            tablaDetalle.setItems(FXCollections.observableArrayList(detalle));

            BigDecimal ingresos = porAgencia.stream()
                    .map(ResumenFila::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblTotalEnvios.setText(String.valueOf(detalle.size()));
            lblIngresos.setText("S/ " + ingresos.setScale(2).toPlainString());
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    @FXML
    private void onExportar() {
        List<Envio> detalle = new ArrayList<>(tablaDetalle.getItems());
        if (detalle.isEmpty()) {
            Alertas.advertencia("No hay envíos en el reporte para exportar.");
            return;
        }

        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar reporte de envíos");
        selector.setInitialFileName("reporte_envios_" + dpDesde.getValue() + "_" + dpHasta.getValue() + ".csv");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv"));
        File archivo = selector.showSaveDialog(tablaDetalle.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try (BufferedWriter w = Files.newBufferedWriter(archivo.toPath(), StandardCharsets.UTF_8)) {
            w.write('﻿'); // para que Excel reconozca las tildes
            w.write("Codigo,Fecha,Origen,Destino,Remitente,Destinatario,Estado,Costo (S/)\n");
            for (Envio e : detalle) {
                w.write(String.join(",",
                        csv(e.getCodigoSeguimiento()), csv(e.getFechaTexto()), csv(e.getOrigen()),
                        csv(e.getDestino()), csv(e.getRemitente()), csv(e.getDestinatario()),
                        csv(e.getEstado()), csv(e.getCostoEnvio().toPlainString())));
                w.write("\n");
            }
            Alertas.info("Reporte guardado en:\n" + archivo.getAbsolutePath());
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    @FXML
    private void onExportarPdf() {
        List<Envio> detalle = new ArrayList<>(tablaDetalle.getItems());
        if (detalle.isEmpty()) {
            Alertas.advertencia("No hay envíos en el reporte para exportar.");
            return;
        }
        File archivo = GeneradorPdf.elegirDestino(tablaDetalle.getScene().getWindow(), "Guardar reporte de envíos",
                "reporte_envios_" + dpDesde.getValue() + "_" + dpHasta.getValue() + ".pdf");
        if (archivo == null) {
            return;
        }
        try {
            GeneradorPdf.reporte(dpDesde.getValue(), dpHasta.getValue(),
                    new ArrayList<>(tablaAgencias.getItems()), new ArrayList<>(tablaEstados.getItems()),
                    detalle, archivo);
            GeneradorPdf.ofrecerAbrir(archivo);
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    private void verTicket(Envio envio) {
        try {
            VistaTicket.mostrar(envioService.detalle(envio.getCodigoSeguimiento()),
                    tablaDetalle.getScene().getWindow());
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    private String csv(String valor) {
        String v = valor == null ? "" : valor;
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
