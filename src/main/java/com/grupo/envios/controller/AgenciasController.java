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

    @FXML private TableView<Agencia> dkprTabla;
    @FXML private TableColumn<Agencia, String> dkprColNombre;
    @FXML private TableColumn<Agencia, String> dkprColCiudad;
    @FXML private TableColumn<Agencia, String> dkprColDireccion;
    @FXML private TableColumn<Agencia, String> dkprColTelefono;
    @FXML private TableColumn<Agencia, String> dkprColEstado;

    @FXML private Label dkprLblModo;
    @FXML private TextField dkprTxtNombre;
    @FXML private TextField dkprTxtCiudad;
    @FXML private TextField dkprTxtDireccion;
    @FXML private TextField dkprTxtTelefono;
    @FXML private CheckBox dkprChkActivo;

    private final AgenciaService dkprService = new AgenciaService();
    private Agencia dkprSeleccionada;

    @FXML
    private void initialize() {
        dkprColNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        dkprColCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        dkprColDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        dkprColTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        dkprColEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));

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
            dkprTabla.setItems(FXCollections.observableArrayList(dkprService.listar(false)));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private void mostrar(Agencia dkprA) {
        dkprSeleccionada = dkprA;
        dkprLblModo.setText("Editando agencia");
        dkprTxtNombre.setText(dkprA.getNombre());
        dkprTxtCiudad.setText(dkprA.getCiudad());
        dkprTxtDireccion.setText(dkprA.getDireccion());
        dkprTxtTelefono.setText(dkprA.getTelefono());
        dkprChkActivo.setSelected(dkprA.isActivo());
    }

    private void limpiar() {
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
    private void onNuevo() {
        limpiar();
    }

    @FXML
    private void onGuardar() {
        Agencia dkprA = new Agencia();
        dkprA.setIdAgencia(dkprSeleccionada == null ? 0 : dkprSeleccionada.getIdAgencia());
        dkprA.setNombre(dkprTxtNombre.getText());
        dkprA.setCiudad(dkprTxtCiudad.getText());
        dkprA.setDireccion(dkprTxtDireccion.getText());
        dkprA.setTelefono(dkprTxtTelefono.getText());
        dkprA.setActivo(dkprChkActivo.isSelected());
        try {
            dkprService.guardar(dkprA);
            Alertas.info("Agencia guardada correctamente.");
            limpiar();
            cargar();
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }
}
