package com.grupo.envios.controller;

import com.grupo.envios.model.Conductor;
import com.grupo.envios.model.Vehiculo;
import com.grupo.envios.service.VehiculoService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.LectorDni;
import com.grupo.envios.util.Validaciones;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class VehiculosController {

    @FXML private TableView<Vehiculo> dkprTablaVehiculos;
    @FXML private TableColumn<Vehiculo, String> dkprColPlaca;
    @FXML private TableColumn<Vehiculo, String> dkprColDescripcion;
    @FXML private TableColumn<Vehiculo, String> dkprColCapacidad;
    @FXML private TableColumn<Vehiculo, String> dkprColEstadoVehiculo;
    @FXML private Label dkprLblModoVehiculo;
    @FXML private TextField dkprTxtPlaca;
    @FXML private TextField dkprTxtDescripcion;
    @FXML private TextField dkprTxtCapacidad;
    @FXML private CheckBox dkprChkVehiculoActivo;

    @FXML private TableView<Conductor> dkprTablaConductores;
    @FXML private TableColumn<Conductor, String> dkprColNombre;
    @FXML private TableColumn<Conductor, String> dkprColDni;
    @FXML private TableColumn<Conductor, String> dkprColLicencia;
    @FXML private TableColumn<Conductor, String> dkprColTelefono;
    @FXML private TableColumn<Conductor, String> dkprColEstadoConductor;
    @FXML private Label dkprLblModoConductor;
    @FXML private TextField dkprTxtDni;
    @FXML private TextField dkprTxtNombre;
    @FXML private TextField dkprTxtLicencia;
    @FXML private TextField dkprTxtTelefono;
    @FXML private CheckBox dkprChkConductorActivo;

    private final VehiculoService dkprService = new VehiculoService();
    private Vehiculo dkprVehiculoSeleccionado;
    private Conductor dkprConductorSeleccionado;

    @FXML
    private void initialize() {
        dkprColPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        dkprColDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        dkprColCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidadTexto"));
        dkprColEstadoVehiculo.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
        dkprTablaVehiculos.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAntes, dkprAhora) -> {
            if (dkprAhora != null) {
                mostrarVehiculo(dkprAhora);
            }
        });

        dkprColNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        dkprColDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        dkprColLicencia.setCellValueFactory(new PropertyValueFactory<>("licencia"));
        dkprColTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        dkprColEstadoConductor.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
        dkprTablaConductores.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAntes, dkprAhora) -> {
            if (dkprAhora != null) {
                mostrarConductor(dkprAhora);
            }
        });
        dkprTxtDni.setOnAction(dkprE -> {
            if (LectorDni.pareceEscaneado(dkprTxtDni.getText())) {
                String dkprDni = LectorDni.extraer(dkprTxtDni.getText());
                dkprTxtDni.setText(dkprDni == null ? "" : dkprDni);
            }
            dkprTxtNombre.requestFocus();
        });

        limpiarVehiculo();
        limpiarConductor();
        cargar();
    }

    private void cargar() {
        try {
            dkprTablaVehiculos.setItems(FXCollections.observableArrayList(dkprService.listarVehiculos(false)));
            dkprTablaConductores.setItems(FXCollections.observableArrayList(dkprService.listarConductores(false)));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private void mostrarVehiculo(Vehiculo dkprV) {
        dkprVehiculoSeleccionado = dkprV;
        dkprLblModoVehiculo.setText("Editando vehículo");
        dkprTxtPlaca.setText(dkprV.getPlaca());
        dkprTxtDescripcion.setText(dkprV.getDescripcion());
        dkprTxtCapacidad.setText(dkprV.getCapacidadKg() == null ? "" : dkprV.getCapacidadKg().toPlainString());
        dkprChkVehiculoActivo.setSelected(dkprV.isActivo());
    }

    private void limpiarVehiculo() {
        dkprVehiculoSeleccionado = null;
        dkprTablaVehiculos.getSelectionModel().clearSelection();
        dkprLblModoVehiculo.setText("Nuevo vehículo");
        dkprTxtPlaca.clear();
        dkprTxtDescripcion.clear();
        dkprTxtCapacidad.clear();
        dkprChkVehiculoActivo.setSelected(true);
    }

    @FXML
    private void onNuevoVehiculo() {
        limpiarVehiculo();
    }

    @FXML
    private void onGuardarVehiculo() {
        try {
            Vehiculo dkprV = new Vehiculo();
            dkprV.setIdVehiculo(dkprVehiculoSeleccionado == null ? 0 : dkprVehiculoSeleccionado.getIdVehiculo());
            dkprV.setPlaca(dkprTxtPlaca.getText());
            dkprV.setDescripcion(dkprTxtDescripcion.getText());
            dkprV.setCapacidadKg(Validaciones.vacio(dkprTxtCapacidad.getText()) ? null
                    : Validaciones.decimal(dkprTxtCapacidad.getText(), "Capacidad"));
            dkprV.setActivo(dkprChkVehiculoActivo.isSelected());
            dkprService.guardarVehiculo(dkprV);
            Alertas.info("Vehículo " + dkprV.getPlaca() + " guardado correctamente.");
            limpiarVehiculo();
            cargar();
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private void mostrarConductor(Conductor dkprC) {
        dkprConductorSeleccionado = dkprC;
        dkprLblModoConductor.setText("Editando conductor");
        dkprTxtDni.setText(dkprC.getDni());
        dkprTxtNombre.setText(dkprC.getNombreCompleto());
        dkprTxtLicencia.setText(dkprC.getLicencia());
        dkprTxtTelefono.setText(dkprC.getTelefono());
        dkprChkConductorActivo.setSelected(dkprC.isActivo());
    }

    private void limpiarConductor() {
        dkprConductorSeleccionado = null;
        dkprTablaConductores.getSelectionModel().clearSelection();
        dkprLblModoConductor.setText("Nuevo conductor");
        dkprTxtDni.clear();
        dkprTxtNombre.clear();
        dkprTxtLicencia.clear();
        dkprTxtTelefono.clear();
        dkprChkConductorActivo.setSelected(true);
    }

    @FXML
    private void onNuevoConductor() {
        limpiarConductor();
    }

    @FXML
    private void onGuardarConductor() {
        try {
            Conductor dkprC = new Conductor();
            dkprC.setIdConductor(dkprConductorSeleccionado == null ? 0 : dkprConductorSeleccionado.getIdConductor());
            dkprC.setDni(dkprTxtDni.getText());
            dkprC.setNombreCompleto(dkprTxtNombre.getText());
            dkprC.setLicencia(dkprTxtLicencia.getText());
            dkprC.setTelefono(dkprTxtTelefono.getText());
            dkprC.setActivo(dkprChkConductorActivo.isSelected());
            dkprService.guardarConductor(dkprC);
            Alertas.info("Conductor " + dkprC.getNombreCompleto() + " guardado correctamente.");
            limpiarConductor();
            cargar();
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }
}
