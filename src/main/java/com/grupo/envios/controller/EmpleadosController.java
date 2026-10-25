package com.grupo.envios.controller;

import com.grupo.envios.model.Agencia;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Rol;
import com.grupo.envios.service.AgenciaService;
import com.grupo.envios.service.EmpleadoService;
import com.grupo.envios.util.Alertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class EmpleadosController {

    @FXML private TableView<Empleado> dkprTabla;
    @FXML private TableColumn<Empleado, String> dkprColDni;
    @FXML private TableColumn<Empleado, String> dkprColNombre;
    @FXML private TableColumn<Empleado, String> dkprColUsuario;
    @FXML private TableColumn<Empleado, String> dkprColRol;
    @FXML private TableColumn<Empleado, String> dkprColAgencia;
    @FXML private TableColumn<Empleado, String> dkprColEstado;

    @FXML private Label dkprLblModo;
    @FXML private ComboBox<Agencia> dkprCmbAgencia;
    @FXML private ComboBox<Rol> dkprCmbRol;
    @FXML private TextField dkprTxtDni;
    @FXML private TextField dkprTxtNombres;
    @FXML private TextField dkprTxtApellidos;
    @FXML private TextField dkprTxtUsuario;
    @FXML private PasswordField dkprTxtClave;
    @FXML private CheckBox dkprChkActivo;

    private final EmpleadoService dkprEmpleadoService = new EmpleadoService();
    private final AgenciaService dkprAgenciaService = new AgenciaService();
    private Empleado dkprSeleccionado;

    @FXML
    private void initialize() {
        dkprColDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        dkprColNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        dkprColUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        dkprColRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        dkprColAgencia.setCellValueFactory(new PropertyValueFactory<>("agencia"));
        dkprColEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));

        try {
            dkprCmbAgencia.setItems(FXCollections.observableArrayList(dkprAgenciaService.listar(false)));
            dkprCmbRol.setItems(FXCollections.observableArrayList(dkprEmpleadoService.listarRoles()));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }

        dkprTabla.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAnterior, dkprNuevo) -> {
            if (dkprNuevo != null) {
                mostrar(dkprNuevo);
            }
        });

        limpiar();
        cargar();
    }

    private void cargar() {
        try {
            dkprTabla.setItems(FXCollections.observableArrayList(dkprEmpleadoService.listar()));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private void mostrar(Empleado dkprE) {
        dkprSeleccionado = dkprE;
        dkprLblModo.setText("Editando empleado");
        for (Agencia dkprA : dkprCmbAgencia.getItems()) {
            if (dkprA.getIdAgencia() == dkprE.getIdAgencia()) {
                dkprCmbAgencia.setValue(dkprA);
            }
        }
        for (Rol dkprR : dkprCmbRol.getItems()) {
            if (dkprR.getIdRol() == dkprE.getIdRol()) {
                dkprCmbRol.setValue(dkprR);
            }
        }
        dkprTxtDni.setText(dkprE.getDni());
        dkprTxtNombres.setText(dkprE.getNombres());
        dkprTxtApellidos.setText(dkprE.getApellidos());
        dkprTxtUsuario.setText(dkprE.getUsuario());
        dkprTxtClave.clear();
        dkprTxtClave.setPromptText("Vacío = no cambiar");
        dkprChkActivo.setSelected(dkprE.isActivo());
    }

    private void limpiar() {
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
    private void onNuevo() {
        limpiar();
    }

    @FXML
    private void onGuardar() {
        Empleado dkprE = new Empleado();
        dkprE.setIdEmpleado(dkprSeleccionado == null ? 0 : dkprSeleccionado.getIdEmpleado());
        dkprE.setIdAgencia(dkprCmbAgencia.getValue() == null ? 0 : dkprCmbAgencia.getValue().getIdAgencia());
        dkprE.setIdRol(dkprCmbRol.getValue() == null ? 0 : dkprCmbRol.getValue().getIdRol());
        dkprE.setDni(dkprTxtDni.getText());
        dkprE.setNombres(dkprTxtNombres.getText());
        dkprE.setApellidos(dkprTxtApellidos.getText());
        dkprE.setUsuario(dkprTxtUsuario.getText());
        dkprE.setActivo(dkprChkActivo.isSelected());
        try {
            dkprEmpleadoService.guardar(dkprE, dkprTxtClave.getText());
            Alertas.info("Empleado guardado correctamente.");
            limpiar();
            cargar();
        } catch (Exception dkprEx) {
            Alertas.manejar(dkprEx);
        }
    }
}
