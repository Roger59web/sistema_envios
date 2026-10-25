package com.grupo.envios.controller;

import com.grupo.envios.model.Agencia;
import com.grupo.envios.model.Ruta;
import com.grupo.envios.service.AgenciaService;
import com.grupo.envios.service.RutaService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Validaciones;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;

public class RutasController {

    @FXML private TableView<Ruta> dkprTabla;
    @FXML private TableColumn<Ruta, String> dkprColOrigen;
    @FXML private TableColumn<Ruta, String> dkprColDestino;
    @FXML private TableColumn<Ruta, Integer> dkprColDias;
    @FXML private TableColumn<Ruta, BigDecimal> dkprColBase;
    @FXML private TableColumn<Ruta, BigDecimal> dkprColExtra;
    @FXML private TableColumn<Ruta, BigDecimal> dkprColPesoMax;
    @FXML private TableColumn<Ruta, String> dkprColEstado;

    @FXML private Label dkprLblModo;
    @FXML private ComboBox<Agencia> dkprCmbOrigen;
    @FXML private ComboBox<Agencia> dkprCmbDestino;
    @FXML private TextField dkprTxtDias;
    @FXML private TextField dkprTxtPrecioBase;
    @FXML private TextField dkprTxtPrecioKg;
    @FXML private TextField dkprTxtPesoMax;
    @FXML private CheckBox dkprChkActivo;

    private final RutaService dkprRutaService = new RutaService();
    private final AgenciaService dkprAgenciaService = new AgenciaService();
    private Ruta dkprSeleccionada;

    @FXML
    private void initialize() {
        dkprColOrigen.setCellValueFactory(new PropertyValueFactory<>("origen"));
        dkprColDestino.setCellValueFactory(new PropertyValueFactory<>("destino"));
        dkprColDias.setCellValueFactory(new PropertyValueFactory<>("diasEstimados"));
        dkprColBase.setCellValueFactory(new PropertyValueFactory<>("precioBase"));
        dkprColExtra.setCellValueFactory(new PropertyValueFactory<>("precioKgExtra"));
        dkprColPesoMax.setCellValueFactory(new PropertyValueFactory<>("pesoMaxKg"));
        dkprColEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));

        try {
            var dkprAgencias = FXCollections.observableArrayList(dkprAgenciaService.listar(false));
            dkprCmbOrigen.setItems(dkprAgencias);
            dkprCmbDestino.setItems(dkprAgencias);
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }

        dkprTabla.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAnterior, dkprNueva) -> {
            if (dkprNueva != null) {
                mostrar(dkprNueva);
            }
        });

        limpiar();
        cargar();
    }

    private void cargar() {
        try {
            dkprTabla.setItems(FXCollections.observableArrayList(dkprRutaService.listar()));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private void mostrar(Ruta dkprR) {
        dkprSeleccionada = dkprR;
        dkprLblModo.setText("Editando ruta (el origen y el destino no se pueden cambiar)");
        seleccionarAgencia(dkprCmbOrigen, dkprR.getIdAgenciaOrigen());
        seleccionarAgencia(dkprCmbDestino, dkprR.getIdAgenciaDestino());
        dkprCmbOrigen.setDisable(true);
        dkprCmbDestino.setDisable(true);
        dkprTxtDias.setText(String.valueOf(dkprR.getDiasEstimados()));
        dkprTxtPrecioBase.setText(dkprR.getPrecioBase() == null ? "" : dkprR.getPrecioBase().toPlainString());
        dkprTxtPrecioKg.setText(dkprR.getPrecioKgExtra() == null ? "" : dkprR.getPrecioKgExtra().toPlainString());
        dkprTxtPesoMax.setText(dkprR.getPesoMaxKg() == null ? "" : dkprR.getPesoMaxKg().toPlainString());
        dkprChkActivo.setSelected(dkprR.isActivo());
    }

    private void seleccionarAgencia(ComboBox<Agencia> dkprCombo, int dkprIdAgencia) {
        for (Agencia dkprA : dkprCombo.getItems()) {
            if (dkprA.getIdAgencia() == dkprIdAgencia) {
                dkprCombo.setValue(dkprA);
                return;
            }
        }
        dkprCombo.setValue(null);
    }

    private void limpiar() {
        dkprSeleccionada = null;
        dkprTabla.getSelectionModel().clearSelection();
        dkprLblModo.setText("Nueva ruta");
        dkprCmbOrigen.setDisable(false);
        dkprCmbDestino.setDisable(false);
        dkprCmbOrigen.setValue(null);
        dkprCmbDestino.setValue(null);
        dkprTxtDias.setText("1");
        dkprTxtPrecioBase.clear();
        dkprTxtPrecioKg.clear();
        dkprTxtPesoMax.setText("50");
        dkprChkActivo.setSelected(true);
    }

    @FXML
    private void onNuevo() {
        limpiar();
    }

    @FXML
    private void onGuardar() {
        try {
            Ruta dkprR = new Ruta();
            dkprR.setIdRuta(dkprSeleccionada == null ? 0 : dkprSeleccionada.getIdRuta());
            dkprR.setIdTarifa(dkprSeleccionada == null ? 0 : dkprSeleccionada.getIdTarifa());
            dkprR.setIdAgenciaOrigen(dkprCmbOrigen.getValue() == null ? 0 : dkprCmbOrigen.getValue().getIdAgencia());
            dkprR.setIdAgenciaDestino(dkprCmbDestino.getValue() == null ? 0 : dkprCmbDestino.getValue().getIdAgencia());
            dkprR.setDiasEstimados(Validaciones.entero(dkprTxtDias.getText(), "Días estimados"));
            dkprR.setPrecioBase(Validaciones.decimal(dkprTxtPrecioBase.getText(), "Precio base"));
            dkprR.setPrecioKgExtra(Validaciones.decimal(dkprTxtPrecioKg.getText(), "Precio por kg extra"));
            dkprR.setPesoMaxKg(Validaciones.decimal(dkprTxtPesoMax.getText(), "Peso máximo"));
            dkprR.setActivo(dkprChkActivo.isSelected());

            dkprRutaService.guardar(dkprR);
            Alertas.info("Ruta guardada correctamente.");
            limpiar();
            cargar();
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }
}
