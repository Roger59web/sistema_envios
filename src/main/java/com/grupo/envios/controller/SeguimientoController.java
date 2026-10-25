package com.grupo.envios.controller;

import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.model.HistorialEnvio;
import com.grupo.envios.service.EnvioService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Estados;
import com.grupo.envios.util.Sesion;
import com.grupo.envios.util.VistaTicket;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class SeguimientoController {

    @FXML private TextField dkprTxtCodigo;
    @FXML private Button dkprBtnCopiar;
    @FXML private Button dkprBtnPdf;
    @FXML private Button dkprBtnTicket;

    @FXML private Label dkprLblCodigo;
    @FXML private Label dkprLblEstado;
    @FXML private Label dkprLblRuta;
    @FXML private Label dkprLblFecha;
    @FXML private Label dkprLblRemitente;
    @FXML private Label dkprLblDestinatario;
    @FXML private Label dkprLblDescripcion;
    @FXML private Label dkprLblPesoBultos;
    @FXML private Label dkprLblCosto;
    @FXML private Label dkprLblEstimada;
    @FXML private Label dkprLblVehiculo;
    @FXML private Label dkprLblConductor;

    @FXML private TableView<HistorialEnvio> dkprTablaHistorial;
    @FXML private TableColumn<HistorialEnvio, String> dkprColFecha;
    @FXML private TableColumn<HistorialEnvio, String> dkprColEstadoHist;
    @FXML private TableColumn<HistorialEnvio, String> dkprColAgencia;
    @FXML private TableColumn<HistorialEnvio, String> dkprColEmpleado;
    @FXML private TableColumn<HistorialEnvio, String> dkprColObservacion;

    @FXML private Label dkprLblPermiso;
    @FXML private VBox dkprPanelEstado;
    @FXML private ComboBox<String> dkprCmbNuevoEstado;
    @FXML private TextField dkprTxtObservacion;

    private final EnvioService dkprService = new EnvioService();
    private Envio dkprEnvio;

    @FXML
    private void initialize() {
        dkprColFecha.setCellValueFactory(new PropertyValueFactory<>("fechaTexto"));
        dkprColEstadoHist.setCellValueFactory(new PropertyValueFactory<>("estado"));
        dkprColAgencia.setCellValueFactory(new PropertyValueFactory<>("agencia"));
        dkprColEmpleado.setCellValueFactory(new PropertyValueFactory<>("empleado"));
        dkprColObservacion.setCellValueFactory(new PropertyValueFactory<>("observacion"));

        dkprTxtCodigo.setOnAction(dkprE -> onBuscar());
        limpiarDetalle();
    }

    private void limpiarDetalle() {
        dkprEnvio = null;
        dkprLblCodigo.setText("");
        dkprLblEstado.setText("");
        dkprLblRuta.setText("");
        dkprLblFecha.setText("");
        dkprLblRemitente.setText("");
        dkprLblDestinatario.setText("");
        dkprLblDescripcion.setText("");
        dkprLblPesoBultos.setText("");
        dkprLblCosto.setText("");
        dkprLblEstimada.setText("");
        dkprLblVehiculo.setText("");
        dkprLblConductor.setText("");
        Estados.colorear(dkprLblEstado, null);
        dkprTablaHistorial.getItems().clear();
        mostrarPermiso("");
        ocultarPanelEstado();
        habilitarAcciones(false);
    }

    private void habilitarAcciones(boolean dkprHayEnvio) {
        dkprBtnCopiar.setDisable(!dkprHayEnvio);
        dkprBtnPdf.setDisable(!dkprHayEnvio);
        dkprBtnTicket.setDisable(!dkprHayEnvio);
    }

    private void mostrarPermiso(String dkprTexto) {
        dkprLblPermiso.setText(dkprTexto);
        dkprLblPermiso.setVisible(!dkprTexto.isEmpty());
        dkprLblPermiso.setManaged(!dkprTexto.isEmpty());
    }

    private void ocultarPanelEstado() {
        dkprPanelEstado.setVisible(false);
        dkprPanelEstado.setManaged(false);
    }

    private void mostrar(Envio dkprE) throws Exception {
        dkprEnvio = dkprE;
        dkprLblCodigo.setText(dkprE.getCodigoSeguimiento());
        dkprLblEstado.setText(dkprE.getEstado());
        Estados.colorear(dkprLblEstado, dkprE.getEstado());
        habilitarAcciones(true);
        dkprLblRuta.setText(dkprE.getRuta());
        dkprLblFecha.setText(dkprE.getFechaTexto());
        dkprLblRemitente.setText(dkprE.getRemitente() + " (" + dkprE.getRemitenteDocumento() + ")");
        dkprLblDestinatario.setText(dkprE.getDestinatario() + " (" + dkprE.getDestinatarioDocumento() + ")");
        dkprLblDescripcion.setText(dkprE.getDescripcion());
        dkprLblPesoBultos.setText(dkprE.getPesoKg().toPlainString() + " kg  /  " + dkprE.getCantidadBultos() + " bulto(s)");
        dkprLblCosto.setText("S/ " + dkprE.getCostoEnvio().toPlainString());
        dkprLblEstimada.setText(textoLlegada(dkprE));
        // Vehículo y conductor del manifiesto con el que salió (vacío mientras está en la agencia de origen).
        dkprLblVehiculo.setText(dkprE.getManifiesto() == null ? "Aún no sale de la agencia de origen" : dkprE.getVehiculoTexto());
        dkprLblConductor.setText(dkprE.getConductor() == null || dkprE.getConductor().isBlank() ? "-" : dkprE.getConductor());

        dkprTablaHistorial.setItems(FXCollections.observableArrayList(dkprService.historial(dkprE.getIdEnvio())));

        Empleado dkprEmpleado = Sesion.getActual();
        mostrarPermiso(dkprService.avisoSoloConsulta(dkprE, dkprEmpleado));
        List<Integer> dkprSiguientes = dkprService.estadosDisponibles(dkprE, dkprEmpleado);
        boolean dkprMostrarPanel = !dkprSiguientes.isEmpty();
        dkprPanelEstado.setVisible(dkprMostrarPanel);
        dkprPanelEstado.setManaged(dkprMostrarPanel);
        if (dkprMostrarPanel) {
            dkprCmbNuevoEstado.setItems(FXCollections.observableArrayList(
                    dkprSiguientes.stream().map(Estados::nombre).toList()));
            dkprCmbNuevoEstado.setValue(null);
            dkprTxtObservacion.clear();
        }
    }

    /** Fecha prometida al cliente, con el atraso o el manifiesto en que viaja. */
    private String textoLlegada(Envio dkprE) {
        String dkprTexto = dkprE.getFechaEstimadaTexto();
        if (dkprE.getDiasAtraso() > 0) {
            dkprTexto += "  (" + dkprE.getAlerta() + ")";
        }
        if (dkprE.getManifiesto() != null) {
            dkprTexto += "  -  manifiesto " + dkprE.getManifiesto();
        }
        return dkprTexto;
    }

    @FXML
    private void onBuscar() {
        try {
            mostrar(dkprService.buscar(dkprTxtCodigo.getText()));
        } catch (Exception dkprE) {
            limpiarDetalle();
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onCopiarCodigo() {
        if (dkprEnvio == null) {
            return;
        }
        ClipboardContent dkprContenido = new ClipboardContent();
        dkprContenido.putString(dkprEnvio.getCodigoSeguimiento());
        Clipboard.getSystemClipboard().setContent(dkprContenido);
        dkprBtnCopiar.setText("¡Copiado!");
        PauseTransition dkprPausa = new PauseTransition(Duration.seconds(1.5));
        dkprPausa.setOnFinished(dkprEv -> dkprBtnCopiar.setText("Copiar código"));
        dkprPausa.play();
    }

    @FXML
    private void onTicket() {
        if (dkprEnvio == null) {
            return;
        }
        try {
            VistaTicket.mostrar(dkprService.detalle(dkprEnvio.getCodigoSeguimiento()), dkprTxtCodigo.getScene().getWindow());
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onGuardarPdf() {
        if (dkprEnvio == null) {
            return;
        }
        try {
            VistaTicket.guardarPdf(dkprService.detalle(dkprEnvio.getCodigoSeguimiento()), dkprTxtCodigo.getScene().getWindow());
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onActualizarEstado() {
        if (dkprEnvio == null) {
            return;
        }
        String dkprNombreEstado = dkprCmbNuevoEstado.getValue();
        if (dkprNombreEstado == null) {
            Alertas.advertencia("Selecciona el nuevo estado.");
            return;
        }
        int dkprNuevoEstado = Estados.id(dkprNombreEstado);
        if (dkprNuevoEstado == Estados.ANULADO
                && !Alertas.confirmar("¿Seguro que deseas ANULAR el envío " + dkprEnvio.getCodigoSeguimiento() + "?")) {
            return;
        }
        try {
            dkprService.cambiarEstado(dkprEnvio, dkprNuevoEstado, dkprTxtObservacion.getText(), Sesion.getActual());
            Alertas.info("Estado actualizado a " + dkprNombreEstado + ".");
            mostrar(dkprService.buscar(dkprEnvio.getCodigoSeguimiento()));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }
}
