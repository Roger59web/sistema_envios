package com.grupo.envios.controller;

import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.model.HistorialEnvio;
import com.grupo.envios.service.EnvioService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Estados;
import com.grupo.envios.util.Sesion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.List;

public class SeguimientoController {

    @FXML private TextField txtCodigo;

    @FXML private Label lblCodigo;
    @FXML private Label lblEstado;
    @FXML private Label lblRuta;
    @FXML private Label lblFecha;
    @FXML private Label lblRemitente;
    @FXML private Label lblDestinatario;
    @FXML private Label lblDescripcion;
    @FXML private Label lblPesoBultos;
    @FXML private Label lblCosto;

    @FXML private TableView<HistorialEnvio> tablaHistorial;
    @FXML private TableColumn<HistorialEnvio, String> colFecha;
    @FXML private TableColumn<HistorialEnvio, String> colEstadoHist;
    @FXML private TableColumn<HistorialEnvio, String> colAgencia;
    @FXML private TableColumn<HistorialEnvio, String> colEmpleado;
    @FXML private TableColumn<HistorialEnvio, String> colObservacion;

    @FXML private VBox panelEstado;
    @FXML private ComboBox<String> cmbNuevoEstado;
    @FXML private TextField txtObservacion;

    private final EnvioService service = new EnvioService();
    private Envio envio;

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaTexto"));
        colEstadoHist.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colAgencia.setCellValueFactory(new PropertyValueFactory<>("agencia"));
        colEmpleado.setCellValueFactory(new PropertyValueFactory<>("empleado"));
        colObservacion.setCellValueFactory(new PropertyValueFactory<>("observacion"));

        txtCodigo.setOnAction(e -> onBuscar());
        limpiarDetalle();
    }

    private void limpiarDetalle() {
        envio = null;
        lblCodigo.setText("");
        lblEstado.setText("");
        lblRuta.setText("");
        lblFecha.setText("");
        lblRemitente.setText("");
        lblDestinatario.setText("");
        lblDescripcion.setText("");
        lblPesoBultos.setText("");
        lblCosto.setText("");
        tablaHistorial.getItems().clear();
        ocultarPanelEstado();
    }

    private void ocultarPanelEstado() {
        panelEstado.setVisible(false);
        panelEstado.setManaged(false);
    }

    private void mostrar(Envio e) throws Exception {
        envio = e;
        lblCodigo.setText(e.getCodigoSeguimiento());
        lblEstado.setText(e.getEstado());
        lblRuta.setText(e.getRuta());
        lblFecha.setText(e.getFechaTexto());
        lblRemitente.setText(e.getRemitente() + " (" + e.getRemitenteDocumento() + ")");
        lblDestinatario.setText(e.getDestinatario() + " (" + e.getDestinatarioDocumento() + ")");
        lblDescripcion.setText(e.getDescripcion());
        lblPesoBultos.setText(e.getPesoKg().toPlainString() + " kg  /  " + e.getCantidadBultos() + " bulto(s)");
        lblCosto.setText("S/ " + e.getCostoEnvio().toPlainString());

        tablaHistorial.setItems(FXCollections.observableArrayList(service.historial(e.getIdEnvio())));

        Empleado empleado = Sesion.getActual();
        List<Integer> siguientes = service.estadosDisponibles(e, empleado);
        boolean mostrarPanel = !siguientes.isEmpty();
        panelEstado.setVisible(mostrarPanel);
        panelEstado.setManaged(mostrarPanel);
        if (mostrarPanel) {
            cmbNuevoEstado.setItems(FXCollections.observableArrayList(
                    siguientes.stream().map(Estados::nombre).toList()));
            cmbNuevoEstado.setValue(null);
            txtObservacion.clear();
        }
    }

    @FXML
    private void onBuscar() {
        try {
            mostrar(service.buscar(txtCodigo.getText()));
        } catch (Exception e) {
            limpiarDetalle();
            Alertas.manejar(e);
        }
    }

    @FXML
    private void onActualizarEstado() {
        if (envio == null) {
            return;
        }
        String nombreEstado = cmbNuevoEstado.getValue();
        if (nombreEstado == null) {
            Alertas.advertencia("Selecciona el nuevo estado.");
            return;
        }
        int nuevoEstado = Estados.id(nombreEstado);
        if (nuevoEstado == Estados.ANULADO
                && !Alertas.confirmar("¿Seguro que deseas ANULAR el envío " + envio.getCodigoSeguimiento() + "?")) {
            return;
        }
        try {
            service.cambiarEstado(envio, nuevoEstado, txtObservacion.getText(), Sesion.getActual());
            Alertas.info("Estado actualizado a " + nombreEstado + ".");
            mostrar(service.buscar(envio.getCodigoSeguimiento()));
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }
}
