package com.grupo.envios.controller;

import com.grupo.envios.model.Agencia;
import com.grupo.envios.service.AgenciaService;
import com.grupo.envios.util.Alertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class AgenciasController {

    @FXML private TableView<Agencia> tabla;
    @FXML private TableColumn<Agencia, String> colNombre;
    @FXML private TableColumn<Agencia, String> colCiudad;
    @FXML private TableColumn<Agencia, String> colDireccion;
    @FXML private TableColumn<Agencia, String> colTelefono;
    @FXML private TableColumn<Agencia, String> colEstado;

    @FXML private Label lblModo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private CheckBox chkActivo;

    private final AgenciaService service = new AgenciaService();
    private Agencia seleccionada;

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nueva) -> {
            if (nueva != null) {
                mostrar(nueva);
            }
        });

        limpiar();
        cargar();
    }

    private void cargar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(service.listar(false)));
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    private void mostrar(Agencia a) {
        seleccionada = a;
        lblModo.setText("Editando agencia");
        txtNombre.setText(a.getNombre());
        txtCiudad.setText(a.getCiudad());
        txtDireccion.setText(a.getDireccion());
        txtTelefono.setText(a.getTelefono());
        chkActivo.setSelected(a.isActivo());
    }

    private void limpiar() {
        seleccionada = null;
        tabla.getSelectionModel().clearSelection();
        lblModo.setText("Nueva agencia");
        txtNombre.clear();
        txtCiudad.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        chkActivo.setSelected(true);
    }

    @FXML
    private void onNuevo() {
        limpiar();
    }

    @FXML
    private void onGuardar() {
        Agencia a = new Agencia();
        a.setIdAgencia(seleccionada == null ? 0 : seleccionada.getIdAgencia());
        a.setNombre(txtNombre.getText());
        a.setCiudad(txtCiudad.getText());
        a.setDireccion(txtDireccion.getText());
        a.setTelefono(txtTelefono.getText());
        a.setActivo(chkActivo.isSelected());
        try {
            service.guardar(a);
            Alertas.info("Agencia guardada correctamente.");
            limpiar();
            cargar();
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }
}
