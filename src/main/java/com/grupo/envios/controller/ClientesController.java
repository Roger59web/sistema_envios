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

    @FXML private TextField dkprTxtBuscar;
    @FXML private TableView<Cliente> dkprTabla;
    @FXML private TableColumn<Cliente, String> dkprColTipo;
    @FXML private TableColumn<Cliente, String> dkprColDocumento;
    @FXML private TableColumn<Cliente, String> dkprColNombre;
    @FXML private TableColumn<Cliente, String> dkprColTelefono;

    @FXML private Label dkprLblModo;
    @FXML private ComboBox<String> dkprCmbTipo;
    @FXML private TextField dkprTxtDocumento;
    @FXML private TextField dkprTxtNombre;
    @FXML private TextField dkprTxtTelefono;
    @FXML private TextField dkprTxtEmail;
    @FXML private TextField dkprTxtDireccion;

    private final ClienteService dkprService = new ClienteService();
    private Cliente dkprSeleccionado;

    @FXML
    private void initialize() {
        dkprColTipo.setCellValueFactory(new PropertyValueFactory<>("tipoDocumento"));
        dkprColDocumento.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        dkprColNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        dkprColTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        dkprCmbTipo.setItems(FXCollections.observableArrayList("DNI", "RUC", "CE"));
        dkprTabla.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAnterior, dkprNuevo) -> {
            if (dkprNuevo != null) {
                mostrar(dkprNuevo);
            }
        });
        dkprTxtBuscar.setOnAction(dkprE -> cargar());

        limpiar();
        cargar();
    }

    private void cargar() {
        try {
            dkprTabla.setItems(FXCollections.observableArrayList(dkprService.buscar(dkprTxtBuscar.getText())));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private void mostrar(Cliente dkprC) {
        dkprSeleccionado = dkprC;
        dkprLblModo.setText("Editando cliente");
        dkprCmbTipo.setValue(dkprC.getTipoDocumento());
        dkprTxtDocumento.setText(dkprC.getNumeroDocumento());
        dkprTxtNombre.setText(dkprC.getNombreCompleto());
        dkprTxtTelefono.setText(dkprC.getTelefono());
        dkprTxtEmail.setText(dkprC.getEmail());
        dkprTxtDireccion.setText(dkprC.getDireccion());
    }

    private void limpiar() {
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
    private void onBuscar() {
        cargar();
    }

    @FXML
    private void onNuevo() {
        limpiar();
    }

    @FXML
    private void onGuardar() {
        Cliente dkprC = new Cliente();
        dkprC.setIdCliente(dkprSeleccionado == null ? 0 : dkprSeleccionado.getIdCliente());
        dkprC.setTipoDocumento(dkprCmbTipo.getValue());
        dkprC.setNumeroDocumento(dkprTxtDocumento.getText());
        dkprC.setNombreCompleto(dkprTxtNombre.getText());
        dkprC.setTelefono(dkprTxtTelefono.getText());
        dkprC.setEmail(dkprTxtEmail.getText());
        dkprC.setDireccion(dkprTxtDireccion.getText());
        try {
            dkprService.guardar(dkprC);
            Alertas.info("Cliente guardado correctamente.");
            limpiar();
            cargar();
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }
}
