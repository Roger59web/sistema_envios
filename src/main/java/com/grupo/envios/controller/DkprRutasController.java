package com.grupo.envios.controller;

import com.grupo.envios.model.DkprAgencia;
import com.grupo.envios.model.DkprRuta;
import com.grupo.envios.service.DkprAgenciaService;
import com.grupo.envios.service.DkprRutaService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprValidaciones;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.math.BigDecimal;

public class DkprRutasController {

    @FXML private TableView<DkprRuta> dkprTabla;
    @FXML private TableColumn<DkprRuta, String> dkprColOrigen;
    @FXML private TableColumn<DkprRuta, String> dkprColDestino;
    @FXML private TableColumn<DkprRuta, Integer> dkprColDias;
    @FXML private TableColumn<DkprRuta, BigDecimal> dkprColBase;
    @FXML private TableColumn<DkprRuta, BigDecimal> dkprColExtra;
    @FXML private TableColumn<DkprRuta, BigDecimal> dkprColPesoMax;
    @FXML private TableColumn<DkprRuta, String> dkprColEstado;

    @FXML private Label dkprLblModo;
    @FXML private ComboBox<DkprAgencia> dkprCmbOrigen;
    @FXML private ComboBox<DkprAgencia> dkprCmbDestino;
    @FXML private TextField dkprTxtDias;
    @FXML private TextField dkprTxtPrecioBase;
    @FXML private TextField dkprTxtPrecioKg;
    @FXML private TextField dkprTxtPesoMax;
    @FXML private CheckBox dkprChkActivo;

    private final DkprRutaService dkprRutaService = new DkprRutaService();
    private final DkprAgenciaService dkprAgenciaService = new DkprAgenciaService();
    private DkprRuta dkprSeleccionada;

    @FXML
    private void initialize() {
        dkprColOrigen.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetOrigen()));
        dkprColDestino.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetDestino()));
        dkprColDias.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetDiasEstimados()));
        dkprColBase.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetPrecioBase()));
        dkprColExtra.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetPrecioKgExtra()));
        dkprColPesoMax.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetPesoMaxKg()));
        dkprColEstado.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetEstadoTexto()));

        try {
            var dkprAgencias = FXCollections.observableArrayList(dkprAgenciaService.dkprListar(false));
            dkprCmbOrigen.setItems(dkprAgencias);
            dkprCmbDestino.setItems(dkprAgencias);
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }

        dkprTabla.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAnterior, dkprNueva) -> {
            if (dkprNueva != null) {
                dkprMostrar(dkprNueva);
            }
        });

        dkprLimpiar();
        dkprCargar();
    }

    private void dkprCargar() {
        try {
            dkprTabla.setItems(FXCollections.observableArrayList(dkprRutaService.dkprListar()));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprMostrar(DkprRuta dkprR) {
        dkprSeleccionada = dkprR;
        dkprLblModo.setText("Editando ruta (el origen y el destino no se pueden cambiar)");
        dkprSeleccionarAgencia(dkprCmbOrigen, dkprR.dkprGetIdAgenciaOrigen());
        dkprSeleccionarAgencia(dkprCmbDestino, dkprR.dkprGetIdAgenciaDestino());
        dkprCmbOrigen.setDisable(true);
        dkprCmbDestino.setDisable(true);
        dkprTxtDias.setText(String.valueOf(dkprR.dkprGetDiasEstimados()));
        dkprTxtPrecioBase.setText(dkprR.dkprGetPrecioBase() == null ? "" : dkprR.dkprGetPrecioBase().toPlainString());
        dkprTxtPrecioKg.setText(dkprR.dkprGetPrecioKgExtra() == null ? "" : dkprR.dkprGetPrecioKgExtra().toPlainString());
        dkprTxtPesoMax.setText(dkprR.dkprGetPesoMaxKg() == null ? "" : dkprR.dkprGetPesoMaxKg().toPlainString());
        dkprChkActivo.setSelected(dkprR.dkprIsActivo());
    }

    private void dkprSeleccionarAgencia(ComboBox<DkprAgencia> dkprCombo, int dkprIdAgencia) {
        for (DkprAgencia dkprA : dkprCombo.getItems()) {
            if (dkprA.dkprGetIdAgencia() == dkprIdAgencia) {
                dkprCombo.setValue(dkprA);
                return;
            }
        }
        dkprCombo.setValue(null);
    }

    private void dkprLimpiar() {
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
    private void dkprOnNuevo() {
        dkprLimpiar();
    }

    @FXML
    private void dkprOnGuardar() {
        try {
            DkprRuta dkprR = new DkprRuta();
            dkprR.dkprSetIdRuta(dkprSeleccionada == null ? 0 : dkprSeleccionada.dkprGetIdRuta());
            dkprR.dkprSetIdTarifa(dkprSeleccionada == null ? 0 : dkprSeleccionada.dkprGetIdTarifa());
            dkprR.dkprSetIdAgenciaOrigen(dkprCmbOrigen.getValue() == null ? 0 : dkprCmbOrigen.getValue().dkprGetIdAgencia());
            dkprR.dkprSetIdAgenciaDestino(dkprCmbDestino.getValue() == null ? 0 : dkprCmbDestino.getValue().dkprGetIdAgencia());
            dkprR.dkprSetDiasEstimados(DkprValidaciones.dkprEntero(dkprTxtDias.getText(), "Días estimados"));
            dkprR.dkprSetPrecioBase(DkprValidaciones.dkprDecimal(dkprTxtPrecioBase.getText(), "Precio base"));
            dkprR.dkprSetPrecioKgExtra(DkprValidaciones.dkprDecimal(dkprTxtPrecioKg.getText(), "Precio por kg extra"));
            dkprR.dkprSetPesoMaxKg(DkprValidaciones.dkprDecimal(dkprTxtPesoMax.getText(), "Peso máximo"));
            dkprR.dkprSetActivo(dkprChkActivo.isSelected());

            dkprRutaService.dkprGuardar(dkprR);
            DkprAlertas.dkprInfo("Ruta guardada correctamente.");
            dkprLimpiar();
            dkprCargar();
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }
}
