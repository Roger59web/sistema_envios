package com.grupo.envios.controller;

import com.grupo.envios.model.DkprConductor;
import com.grupo.envios.model.DkprVehiculo;
import com.grupo.envios.service.DkprVehiculoService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprLectorDni;
import com.grupo.envios.util.DkprValidaciones;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.ReadOnlyObjectWrapper;

public class DkprVehiculosController {

    @FXML private TableView<DkprVehiculo> dkprTablaVehiculos;
    @FXML private TableColumn<DkprVehiculo, String> dkprColPlaca;
    @FXML private TableColumn<DkprVehiculo, String> dkprColDescripcion;
    @FXML private TableColumn<DkprVehiculo, String> dkprColCapacidad;
    @FXML private TableColumn<DkprVehiculo, String> dkprColEstadoVehiculo;
    @FXML private Label dkprLblModoVehiculo;
    @FXML private TextField dkprTxtPlaca;
    @FXML private TextField dkprTxtDescripcion;
    @FXML private TextField dkprTxtCapacidad;
    @FXML private CheckBox dkprChkVehiculoActivo;

    @FXML private TableView<DkprConductor> dkprTablaConductores;
    @FXML private TableColumn<DkprConductor, String> dkprColNombre;
    @FXML private TableColumn<DkprConductor, String> dkprColDni;
    @FXML private TableColumn<DkprConductor, String> dkprColLicencia;
    @FXML private TableColumn<DkprConductor, String> dkprColTelefono;
    @FXML private TableColumn<DkprConductor, String> dkprColEstadoConductor;
    @FXML private Label dkprLblModoConductor;
    @FXML private TextField dkprTxtDni;
    @FXML private TextField dkprTxtNombre;
    @FXML private TextField dkprTxtLicencia;
    @FXML private TextField dkprTxtTelefono;
    @FXML private CheckBox dkprChkConductorActivo;

    private final DkprVehiculoService dkprService = new DkprVehiculoService();
    private DkprVehiculo dkprVehiculoSeleccionado;
    private DkprConductor dkprConductorSeleccionado;

    @FXML
    private void initialize() {
        dkprColPlaca.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetPlaca()));
        dkprColDescripcion.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetDescripcion()));
        dkprColCapacidad.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetCapacidadTexto()));
        dkprColEstadoVehiculo.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetEstadoTexto()));
        dkprTablaVehiculos.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAntes, dkprAhora) -> {
            if (dkprAhora != null) {
                dkprMostrarVehiculo(dkprAhora);
            }
        });

        dkprColNombre.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetNombreCompleto()));
        dkprColDni.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetDni()));
        dkprColLicencia.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetLicencia()));
        dkprColTelefono.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetTelefono()));
        dkprColEstadoConductor.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetEstadoTexto()));
        dkprTablaConductores.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAntes, dkprAhora) -> {
            if (dkprAhora != null) {
                dkprMostrarConductor(dkprAhora);
            }
        });
        dkprTxtDni.setOnAction(dkprE -> {
            if (DkprLectorDni.dkprPareceEscaneado(dkprTxtDni.getText())) {
                String dkprDni = DkprLectorDni.dkprExtraer(dkprTxtDni.getText());
                dkprTxtDni.setText(dkprDni == null ? "" : dkprDni);
            }
            dkprTxtNombre.requestFocus();
        });

        dkprLimpiarVehiculo();
        dkprLimpiarConductor();
        dkprCargar();
    }

    private void dkprCargar() {
        try {
            dkprTablaVehiculos.setItems(FXCollections.observableArrayList(dkprService.dkprListarVehiculos(false)));
            dkprTablaConductores.setItems(FXCollections.observableArrayList(dkprService.dkprListarConductores(false)));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprMostrarVehiculo(DkprVehiculo dkprV) {
        dkprVehiculoSeleccionado = dkprV;
        dkprLblModoVehiculo.setText("Editando vehículo");
        dkprTxtPlaca.setText(dkprV.dkprGetPlaca());
        dkprTxtDescripcion.setText(dkprV.dkprGetDescripcion());
        dkprTxtCapacidad.setText(dkprV.dkprGetCapacidadKg() == null ? "" : dkprV.dkprGetCapacidadKg().toPlainString());
        dkprChkVehiculoActivo.setSelected(dkprV.dkprIsActivo());
    }

    private void dkprLimpiarVehiculo() {
        dkprVehiculoSeleccionado = null;
        dkprTablaVehiculos.getSelectionModel().clearSelection();
        dkprLblModoVehiculo.setText("Nuevo vehículo");
        dkprTxtPlaca.clear();
        dkprTxtDescripcion.clear();
        dkprTxtCapacidad.clear();
        dkprChkVehiculoActivo.setSelected(true);
    }

    @FXML
    private void dkprOnNuevoVehiculo() {
        dkprLimpiarVehiculo();
    }

    @FXML
    private void dkprOnGuardarVehiculo() {
        try {
            DkprVehiculo dkprV = new DkprVehiculo();
            dkprV.dkprSetIdVehiculo(dkprVehiculoSeleccionado == null ? 0 : dkprVehiculoSeleccionado.dkprGetIdVehiculo());
            dkprV.dkprSetPlaca(dkprTxtPlaca.getText());
            dkprV.dkprSetDescripcion(dkprTxtDescripcion.getText());
            dkprV.dkprSetCapacidadKg(DkprValidaciones.dkprVacio(dkprTxtCapacidad.getText()) ? null
                    : DkprValidaciones.dkprDecimal(dkprTxtCapacidad.getText(), "Capacidad"));
            dkprV.dkprSetActivo(dkprChkVehiculoActivo.isSelected());
            dkprService.dkprGuardarVehiculo(dkprV);
            DkprAlertas.dkprInfo("Vehículo " + dkprV.dkprGetPlaca() + " guardado correctamente.");
            dkprLimpiarVehiculo();
            dkprCargar();
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprMostrarConductor(DkprConductor dkprC) {
        dkprConductorSeleccionado = dkprC;
        dkprLblModoConductor.setText("Editando conductor");
        dkprTxtDni.setText(dkprC.dkprGetDni());
        dkprTxtNombre.setText(dkprC.dkprGetNombreCompleto());
        dkprTxtLicencia.setText(dkprC.dkprGetLicencia());
        dkprTxtTelefono.setText(dkprC.dkprGetTelefono());
        dkprChkConductorActivo.setSelected(dkprC.dkprIsActivo());
    }

    private void dkprLimpiarConductor() {
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
    private void dkprOnNuevoConductor() {
        dkprLimpiarConductor();
    }

    @FXML
    private void dkprOnGuardarConductor() {
        try {
            DkprConductor dkprC = new DkprConductor();
            dkprC.dkprSetIdConductor(dkprConductorSeleccionado == null ? 0 : dkprConductorSeleccionado.dkprGetIdConductor());
            dkprC.dkprSetDni(dkprTxtDni.getText());
            dkprC.dkprSetNombreCompleto(dkprTxtNombre.getText());
            dkprC.dkprSetLicencia(dkprTxtLicencia.getText());
            dkprC.dkprSetTelefono(dkprTxtTelefono.getText());
            dkprC.dkprSetActivo(dkprChkConductorActivo.isSelected());
            dkprService.dkprGuardarConductor(dkprC);
            DkprAlertas.dkprInfo("Conductor " + dkprC.dkprGetNombreCompleto() + " guardado correctamente.");
            dkprLimpiarConductor();
            dkprCargar();
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }
}
