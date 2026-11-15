package com.grupo.envios.controller;

import com.grupo.envios.model.DkprCliente;
import com.grupo.envios.service.DkprClienteService;
import com.grupo.envios.util.DkprAlertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.ReadOnlyObjectWrapper;

public class DkprClientesController {

    @FXML private TextField dkprTxtBuscar;
    @FXML private TableView<DkprCliente> dkprTabla;
    @FXML private TableColumn<DkprCliente, String> dkprColTipo;
    @FXML private TableColumn<DkprCliente, String> dkprColDocumento;
    @FXML private TableColumn<DkprCliente, String> dkprColNombre;
    @FXML private TableColumn<DkprCliente, String> dkprColTelefono;

    @FXML private Label dkprLblModo;
    @FXML private ComboBox<String> dkprCmbTipo;
    @FXML private TextField dkprTxtDocumento;
    @FXML private TextField dkprTxtNombre;
    @FXML private TextField dkprTxtTelefono;
    @FXML private TextField dkprTxtEmail;
    @FXML private TextField dkprTxtDireccion;

    private final DkprClienteService dkprService = new DkprClienteService();
    private DkprCliente dkprSeleccionado;

    @FXML
    private void initialize() {
        dkprColTipo.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetTipoDocumento()));
        dkprColDocumento.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetNumeroDocumento()));
        dkprColNombre.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetNombreCompleto()));
        dkprColTelefono.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetTelefono()));

        dkprCmbTipo.setItems(FXCollections.observableArrayList("DNI", "RUC", "CE"));
        dkprTabla.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAnterior, dkprNuevo) -> {
            if (dkprNuevo != null) {
                dkprMostrar(dkprNuevo);
            }
        });
        dkprTxtBuscar.setOnAction(dkprE -> dkprCargar());

        dkprLimpiar();
        dkprCargar();
    }

    private void dkprCargar() {
        try {
            dkprTabla.setItems(FXCollections.observableArrayList(dkprService.dkprBuscar(dkprTxtBuscar.getText())));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprMostrar(DkprCliente dkprC) {
        dkprSeleccionado = dkprC;
        dkprLblModo.setText("Editando cliente");
        dkprCmbTipo.setValue(dkprC.dkprGetTipoDocumento());
        dkprTxtDocumento.setText(dkprC.dkprGetNumeroDocumento());
        dkprTxtNombre.setText(dkprC.dkprGetNombreCompleto());
        dkprTxtTelefono.setText(dkprC.dkprGetTelefono());
        dkprTxtEmail.setText(dkprC.dkprGetEmail());
        dkprTxtDireccion.setText(dkprC.dkprGetDireccion());
    }

    private void dkprLimpiar() {
        dkprSeleccionado = null;
        dkprTabla.getSelectionModel().clearSelection();
        dkprLblModo.setText("Nuevo cliente");
        dkprCmbTipo.setValue("DNI");
        dkprTxtDocumento.clear();
        dkprTxtNombre.clear();
        dkprTxtTelefono.clear();
        dkprTxtEmail.clear();
        dkprTxtDireccion.clear();
    }

    @FXML
    private void dkprOnBuscar() {
        dkprCargar();
    }

    @FXML
    private void dkprOnNuevo() {
        dkprLimpiar();
    }

    @FXML
    private void dkprOnGuardar() {
        DkprCliente dkprC = new DkprCliente();
        dkprC.dkprSetIdCliente(dkprSeleccionado == null ? 0 : dkprSeleccionado.dkprGetIdCliente());
        dkprC.dkprSetTipoDocumento(dkprCmbTipo.getValue());
        dkprC.dkprSetNumeroDocumento(dkprTxtDocumento.getText());
        dkprC.dkprSetNombreCompleto(dkprTxtNombre.getText());
        dkprC.dkprSetTelefono(dkprTxtTelefono.getText());
        dkprC.dkprSetEmail(dkprTxtEmail.getText());
        dkprC.dkprSetDireccion(dkprTxtDireccion.getText());
        try {
            dkprService.dkprGuardar(dkprC);
            DkprAlertas.dkprInfo("Cliente guardado correctamente.");
            dkprLimpiar();
            dkprCargar();
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }
}
