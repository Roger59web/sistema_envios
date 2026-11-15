package com.grupo.envios.controller;

import com.grupo.envios.model.DkprAgencia;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprRol;
import com.grupo.envios.service.DkprAgenciaService;
import com.grupo.envios.service.DkprEmpleadoService;
import com.grupo.envios.util.DkprAlertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.ReadOnlyObjectWrapper;

public class DkprEmpleadosController {

    @FXML private TableView<DkprEmpleado> dkprTabla;
    @FXML private TableColumn<DkprEmpleado, String> dkprColDni;
    @FXML private TableColumn<DkprEmpleado, String> dkprColNombre;
    @FXML private TableColumn<DkprEmpleado, String> dkprColUsuario;
    @FXML private TableColumn<DkprEmpleado, String> dkprColRol;
    @FXML private TableColumn<DkprEmpleado, String> dkprColAgencia;
    @FXML private TableColumn<DkprEmpleado, String> dkprColEstado;

    @FXML private Label dkprLblModo;
    @FXML private ComboBox<DkprAgencia> dkprCmbAgencia;
    @FXML private ComboBox<DkprRol> dkprCmbRol;
    @FXML private TextField dkprTxtDni;
    @FXML private TextField dkprTxtNombres;
    @FXML private TextField dkprTxtApellidos;
    @FXML private TextField dkprTxtUsuario;
    @FXML private PasswordField dkprTxtClave;
    @FXML private CheckBox dkprChkActivo;

    private final DkprEmpleadoService dkprEmpleadoService = new DkprEmpleadoService();
    private final DkprAgenciaService dkprAgenciaService = new DkprAgenciaService();
    private DkprEmpleado dkprSeleccionado;

    @FXML
    private void initialize() {
        dkprColDni.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetDni()));
        dkprColNombre.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetNombreCompleto()));
        dkprColUsuario.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetUsuario()));
        dkprColRol.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetRol()));
        dkprColAgencia.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetAgencia()));
        dkprColEstado.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetEstadoTexto()));

        try {
            dkprCmbAgencia.setItems(FXCollections.observableArrayList(dkprAgenciaService.dkprListar(false)));
            dkprCmbRol.setItems(FXCollections.observableArrayList(dkprEmpleadoService.dkprListarRoles()));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }

        dkprTabla.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAnterior, dkprNuevo) -> {
            if (dkprNuevo != null) {
                dkprMostrar(dkprNuevo);
            }
        });

        dkprLimpiar();
        dkprCargar();
    }

    private void dkprCargar() {
        try {
            dkprTabla.setItems(FXCollections.observableArrayList(dkprEmpleadoService.dkprListar()));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprMostrar(DkprEmpleado dkprE) {
        dkprSeleccionado = dkprE;
        dkprLblModo.setText("Editando empleado");
        for (DkprAgencia dkprA : dkprCmbAgencia.getItems()) {
            if (dkprA.dkprGetIdAgencia() == dkprE.dkprGetIdAgencia()) {
                dkprCmbAgencia.setValue(dkprA);
            }
        }
        for (DkprRol dkprR : dkprCmbRol.getItems()) {
            if (dkprR.dkprGetIdRol() == dkprE.dkprGetIdRol()) {
                dkprCmbRol.setValue(dkprR);
            }
        }
        dkprTxtDni.setText(dkprE.dkprGetDni());
        dkprTxtNombres.setText(dkprE.dkprGetNombres());
        dkprTxtApellidos.setText(dkprE.dkprGetApellidos());
        dkprTxtUsuario.setText(dkprE.dkprGetUsuario());
        dkprTxtClave.clear();
        dkprTxtClave.setPromptText("Vacío = no cambiar");
        dkprChkActivo.setSelected(dkprE.dkprIsActivo());
    }

    private void dkprLimpiar() {
        dkprSeleccionado = null;
        dkprTabla.getSelectionModel().clearSelection();
        dkprLblModo.setText("Nuevo empleado");
        dkprCmbAgencia.setValue(null);
        dkprCmbRol.setValue(null);
        dkprTxtDni.clear();
        dkprTxtNombres.clear();
        dkprTxtApellidos.clear();
        dkprTxtUsuario.clear();
        dkprTxtClave.clear();
        dkprTxtClave.setPromptText("Mínimo 6 caracteres");
        dkprChkActivo.setSelected(true);
    }

    @FXML
    private void dkprOnNuevo() {
        dkprLimpiar();
    }

    @FXML
    private void dkprOnGuardar() {
        DkprEmpleado dkprE = new DkprEmpleado();
        dkprE.dkprSetIdEmpleado(dkprSeleccionado == null ? 0 : dkprSeleccionado.dkprGetIdEmpleado());
        dkprE.dkprSetIdAgencia(dkprCmbAgencia.getValue() == null ? 0 : dkprCmbAgencia.getValue().dkprGetIdAgencia());
        dkprE.dkprSetIdRol(dkprCmbRol.getValue() == null ? 0 : dkprCmbRol.getValue().dkprGetIdRol());
        dkprE.dkprSetDni(dkprTxtDni.getText());
        dkprE.dkprSetNombres(dkprTxtNombres.getText());
        dkprE.dkprSetApellidos(dkprTxtApellidos.getText());
        dkprE.dkprSetUsuario(dkprTxtUsuario.getText());
        dkprE.dkprSetActivo(dkprChkActivo.isSelected());
        try {
            dkprEmpleadoService.dkprGuardar(dkprE, dkprTxtClave.getText());
            DkprAlertas.dkprInfo("Empleado guardado correctamente.");
            dkprLimpiar();
            dkprCargar();
        } catch (Exception dkprEx) {
            DkprAlertas.dkprManejar(dkprEx);
        }
    }
}
