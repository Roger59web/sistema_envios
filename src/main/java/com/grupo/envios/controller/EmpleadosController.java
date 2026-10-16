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

    @FXML private TableView<Empleado> tabla;
    @FXML private TableColumn<Empleado, String> colDni;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colUsuario;
    @FXML private TableColumn<Empleado, String> colRol;
    @FXML private TableColumn<Empleado, String> colAgencia;
    @FXML private TableColumn<Empleado, String> colEstado;

    @FXML private Label lblModo;
    @FXML private ComboBox<Agencia> cmbAgencia;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private TextField txtDni;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtClave;
    @FXML private CheckBox chkActivo;

    private final EmpleadoService empleadoService = new EmpleadoService();
    private final AgenciaService agenciaService = new AgenciaService();
    private Empleado seleccionado;

    @FXML
    private void initialize() {
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colAgencia.setCellValueFactory(new PropertyValueFactory<>("agencia"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));

        try {
            cmbAgencia.setItems(FXCollections.observableArrayList(agenciaService.listar(false)));
            cmbRol.setItems(FXCollections.observableArrayList(empleadoService.listarRoles()));
        } catch (Exception e) {
            Alertas.manejar(e);
        }

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                mostrar(nuevo);
            }
        });

        limpiar();
        cargar();
    }

    private void cargar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(empleadoService.listar()));
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    private void mostrar(Empleado e) {
        seleccionado = e;
        lblModo.setText("Editando empleado");
        for (Agencia a : cmbAgencia.getItems()) {
            if (a.getIdAgencia() == e.getIdAgencia()) {
                cmbAgencia.setValue(a);
            }
        }
        for (Rol r : cmbRol.getItems()) {
            if (r.getIdRol() == e.getIdRol()) {
                cmbRol.setValue(r);
            }
        }
        txtDni.setText(e.getDni());
        txtNombres.setText(e.getNombres());
        txtApellidos.setText(e.getApellidos());
        txtUsuario.setText(e.getUsuario());
        txtClave.clear();
        txtClave.setPromptText("Vacío = no cambiar");
        chkActivo.setSelected(e.isActivo());
    }

    private void limpiar() {
        seleccionado = null;
        tabla.getSelectionModel().clearSelection();
        lblModo.setText("Nuevo empleado");
        cmbAgencia.setValue(null);
        cmbRol.setValue(null);
        txtDni.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtUsuario.clear();
        txtClave.clear();
        txtClave.setPromptText("Mínimo 6 caracteres");
        chkActivo.setSelected(true);
    }

    @FXML
    private void onNuevo() {
        limpiar();
    }

    @FXML
    private void onGuardar() {
        Empleado e = new Empleado();
        e.setIdEmpleado(seleccionado == null ? 0 : seleccionado.getIdEmpleado());
        e.setIdAgencia(cmbAgencia.getValue() == null ? 0 : cmbAgencia.getValue().getIdAgencia());
        e.setIdRol(cmbRol.getValue() == null ? 0 : cmbRol.getValue().getIdRol());
        e.setDni(txtDni.getText());
        e.setNombres(txtNombres.getText());
        e.setApellidos(txtApellidos.getText());
        e.setUsuario(txtUsuario.getText());
        e.setActivo(chkActivo.isSelected());
        try {
            empleadoService.guardar(e, txtClave.getText());
            Alertas.info("Empleado guardado correctamente.");
            limpiar();
            cargar();
        } catch (Exception ex) {
            Alertas.manejar(ex);
        }
    }
}
