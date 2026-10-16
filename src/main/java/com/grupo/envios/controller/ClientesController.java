package com.grupo.envios.controller;

import com.grupo.envios.model.Cliente;
import com.grupo.envios.service.ClienteService;
import com.grupo.envios.util.Alertas;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClientesController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Cliente> tabla;
    @FXML private TableColumn<Cliente, String> colTipo;
    @FXML private TableColumn<Cliente, String> colDocumento;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colTelefono;

    @FXML private Label lblModo;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtDocumento;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDireccion;

    private final ClienteService service = new ClienteService();
    private Cliente seleccionado;

    @FXML
    private void initialize() {
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoDocumento"));
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        cmbTipo.setItems(FXCollections.observableArrayList("DNI", "RUC", "CE"));
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                mostrar(nuevo);
            }
        });
        txtBuscar.setOnAction(e -> cargar());

        limpiar();
        cargar();
    }

    private void cargar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(service.buscar(txtBuscar.getText())));
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    private void mostrar(Cliente c) {
        seleccionado = c;
        lblModo.setText("Editando cliente");
        cmbTipo.setValue(c.getTipoDocumento());
        txtDocumento.setText(c.getNumeroDocumento());
        txtNombre.setText(c.getNombreCompleto());
        txtTelefono.setText(c.getTelefono());
        txtEmail.setText(c.getEmail());
        txtDireccion.setText(c.getDireccion());
    }

    private void limpiar() {
        seleccionado = null;
        tabla.getSelectionModel().clearSelection();
        lblModo.setText("Nuevo cliente");
        cmbTipo.setValue("DNI");
        txtDocumento.clear();
        txtNombre.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
    }

    @FXML
    private void onBuscar() {
        cargar();
    }

    @FXML
    private void onNuevo() {
        limpiar();
    }

    @FXML
    private void onGuardar() {
        Cliente c = new Cliente();
        c.setIdCliente(seleccionado == null ? 0 : seleccionado.getIdCliente());
        c.setTipoDocumento(cmbTipo.getValue());
        c.setNumeroDocumento(txtDocumento.getText());
        c.setNombreCompleto(txtNombre.getText());
        c.setTelefono(txtTelefono.getText());
        c.setEmail(txtEmail.getText());
        c.setDireccion(txtDireccion.getText());
        try {
            service.guardar(c);
            Alertas.info("Cliente guardado correctamente.");
            limpiar();
            cargar();
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }
}
