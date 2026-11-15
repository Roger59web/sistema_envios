package com.grupo.envios.controller;

import com.grupo.envios.model.DkprAgencia;
import com.grupo.envios.service.DkprAgenciaService;
import com.grupo.envios.util.DkprAlertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.ReadOnlyObjectWrapper;

public class DkprAgenciasController {

    @FXML private TableView<DkprAgencia> dkprTabla;
    @FXML private TableColumn<DkprAgencia, String> dkprColNombre;
    @FXML private TableColumn<DkprAgencia, String> dkprColCiudad;
    @FXML private TableColumn<DkprAgencia, String> dkprColDireccion;
    @FXML private TableColumn<DkprAgencia, String> dkprColTelefono;
    @FXML private TableColumn<DkprAgencia, String> dkprColEstado;

    @FXML private Label dkprLblModo;
    @FXML private TextField dkprTxtNombre;
    @FXML private TextField dkprTxtCiudad;
    @FXML private TextField dkprTxtDireccion;
    @FXML private TextField dkprTxtTelefono;
    @FXML private CheckBox dkprChkActivo;

    private final DkprAgenciaService dkprService = new DkprAgenciaService();
    private DkprAgencia dkprSeleccionada;

    @FXML
    private void initialize() {
        dkprColNombre.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetNombre()));
        dkprColCiudad.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetCiudad()));
        dkprColDireccion.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetDireccion()));
        dkprColTelefono.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetTelefono()));
        dkprColEstado.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetEstadoTexto()));

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
            dkprTabla.setItems(FXCollections.observableArrayList(dkprService.dkprListar(false)));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprMostrar(DkprAgencia dkprA) {
        dkprSeleccionada = dkprA;
        dkprLblModo.setText("Editando agencia");
        dkprTxtNombre.setText(dkprA.dkprGetNombre());
        dkprTxtCiudad.setText(dkprA.dkprGetCiudad());
        dkprTxtDireccion.setText(dkprA.dkprGetDireccion());
        dkprTxtTelefono.setText(dkprA.dkprGetTelefono());
        dkprChkActivo.setSelected(dkprA.dkprIsActivo());
    }

    private void dkprLimpiar() {
        dkprSeleccionada = null;
        dkprTabla.getSelectionModel().clearSelection();
        dkprLblModo.setText("Nueva agencia");
        dkprTxtNombre.clear();
        dkprTxtCiudad.clear();
        dkprTxtDireccion.clear();
        dkprTxtTelefono.clear();
        dkprChkActivo.setSelected(true);
    }

    @FXML
    private void dkprOnNuevo() {
        dkprLimpiar();
    }

    @FXML
    private void dkprOnGuardar() {
        DkprAgencia dkprA = new DkprAgencia();
        dkprA.dkprSetIdAgencia(dkprSeleccionada == null ? 0 : dkprSeleccionada.dkprGetIdAgencia());
        dkprA.dkprSetNombre(dkprTxtNombre.getText());
        dkprA.dkprSetCiudad(dkprTxtCiudad.getText());
        dkprA.dkprSetDireccion(dkprTxtDireccion.getText());
        dkprA.dkprSetTelefono(dkprTxtTelefono.getText());
        dkprA.dkprSetActivo(dkprChkActivo.isSelected());
        try {
            dkprService.dkprGuardar(dkprA);
            DkprAlertas.dkprInfo("Agencia guardada correctamente.");
            dkprLimpiar();
            dkprCargar();
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }
}
