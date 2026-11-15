package com.grupo.envios.controller;

import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.model.DkprHistorialEnvio;
import com.grupo.envios.service.DkprEnvioService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprEstados;
import com.grupo.envios.util.DkprSesion;
import com.grupo.envios.util.DkprVistaTicket;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class DkprSeguimientoController {

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

    @FXML private TableView<DkprHistorialEnvio> dkprTablaHistorial;
    @FXML private TableColumn<DkprHistorialEnvio, String> dkprColFecha;
    @FXML private TableColumn<DkprHistorialEnvio, String> dkprColEstadoHist;
    @FXML private TableColumn<DkprHistorialEnvio, String> dkprColAgencia;
    @FXML private TableColumn<DkprHistorialEnvio, String> dkprColEmpleado;
    @FXML private TableColumn<DkprHistorialEnvio, String> dkprColObservacion;

    @FXML private Label dkprLblPermiso;
    @FXML private VBox dkprPanelEstado;
    @FXML private ComboBox<String> dkprCmbNuevoEstado;
    @FXML private TextField dkprTxtObservacion;

    private final DkprEnvioService dkprService = new DkprEnvioService();
    private DkprEnvio dkprEnvio;

    @FXML
    private void initialize() {
        dkprColFecha.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetFechaTexto()));
        dkprColEstadoHist.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetEstado()));
        dkprColAgencia.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetAgencia()));
        dkprColEmpleado.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetEmpleado()));
        dkprColObservacion.setCellValueFactory(dkprC -> new ReadOnlyObjectWrapper<>(dkprC.getValue().dkprGetObservacion()));

        dkprTxtCodigo.setOnAction(dkprE -> dkprOnBuscar());
        dkprLimpiarDetalle();
    }

    private void dkprLimpiarDetalle() {
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
        DkprEstados.dkprColorear(dkprLblEstado, null);
        dkprTablaHistorial.getItems().clear();
        dkprMostrarPermiso("");
        dkprOcultarPanelEstado();
        dkprHabilitarAcciones(false);
    }

    private void dkprHabilitarAcciones(boolean dkprHayEnvio) {
        dkprBtnCopiar.setDisable(!dkprHayEnvio);
        dkprBtnPdf.setDisable(!dkprHayEnvio);
        dkprBtnTicket.setDisable(!dkprHayEnvio);
    }

    private void dkprMostrarPermiso(String dkprTexto) {
        dkprLblPermiso.setText(dkprTexto);
        dkprLblPermiso.setVisible(!dkprTexto.isEmpty());
        dkprLblPermiso.setManaged(!dkprTexto.isEmpty());
    }

    private void dkprOcultarPanelEstado() {
        dkprPanelEstado.setVisible(false);
        dkprPanelEstado.setManaged(false);
    }

    private void dkprMostrar(DkprEnvio dkprE) throws Exception {
        dkprEnvio = dkprE;
        dkprLblCodigo.setText(dkprE.dkprGetCodigoSeguimiento());
        dkprLblEstado.setText(dkprE.dkprGetEstado());
        DkprEstados.dkprColorear(dkprLblEstado, dkprE.dkprGetEstado());
        dkprHabilitarAcciones(true);
        dkprLblRuta.setText(dkprE.dkprGetRuta());
        dkprLblFecha.setText(dkprE.dkprGetFechaTexto());
        dkprLblRemitente.setText(dkprE.dkprGetRemitente() + " (" + dkprE.dkprGetRemitenteDocumento() + ")");
        dkprLblDestinatario.setText(dkprE.dkprGetDestinatario() + " (" + dkprE.dkprGetDestinatarioDocumento() + ")");
        dkprLblDescripcion.setText(dkprE.dkprGetDescripcion());
        dkprLblPesoBultos.setText(dkprE.dkprGetPesoKg().toPlainString() + " kg  /  " + dkprE.dkprGetCantidadBultos() + " bulto(s)");
        dkprLblCosto.setText("S/ " + dkprE.dkprGetCostoEnvio().toPlainString());
        dkprLblEstimada.setText(dkprTextoLlegada(dkprE));
        dkprLblVehiculo.setText(dkprE.dkprGetManifiesto() == null ? "Aún no sale de la agencia de origen" : dkprE.dkprGetVehiculoTexto());
        dkprLblConductor.setText(dkprE.dkprGetConductor() == null || dkprE.dkprGetConductor().isBlank() ? "-" : dkprE.dkprGetConductor());

        dkprTablaHistorial.setItems(FXCollections.observableArrayList(dkprService.dkprHistorial(dkprE.dkprGetIdEnvio())));

        DkprEmpleado dkprEmpleado = DkprSesion.dkprGetActual();
        dkprMostrarPermiso(dkprService.dkprAvisoSoloConsulta(dkprE, dkprEmpleado));
        List<Integer> dkprSiguientes = dkprService.dkprEstadosDisponibles(dkprE, dkprEmpleado);
        boolean dkprMostrarPanel = !dkprSiguientes.isEmpty();
        dkprPanelEstado.setVisible(dkprMostrarPanel);
        dkprPanelEstado.setManaged(dkprMostrarPanel);
        if (dkprMostrarPanel) {
            dkprCmbNuevoEstado.setItems(FXCollections.observableArrayList(
                    dkprSiguientes.stream().map(DkprEstados::dkprNombre).toList()));
            dkprCmbNuevoEstado.setValue(null);
            dkprTxtObservacion.clear();
        }
    }

    private String dkprTextoLlegada(DkprEnvio dkprE) {
        String dkprTexto = dkprE.dkprGetFechaEstimadaTexto();
        if (dkprE.dkprGetDiasAtraso() > 0) {
            dkprTexto += "  (" + dkprE.dkprGetAlerta() + ")";
        }
        if (dkprE.dkprGetManifiesto() != null) {
            dkprTexto += "  -  manifiesto " + dkprE.dkprGetManifiesto();
        }
        return dkprTexto;
    }

    @FXML
    private void dkprOnBuscar() {
        try {
            dkprMostrar(dkprService.dkprBuscar(dkprTxtCodigo.getText()));
        } catch (Exception dkprE) {
            dkprLimpiarDetalle();
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnCopiarCodigo() {
        if (dkprEnvio == null) {
            return;
        }
        ClipboardContent dkprContenido = new ClipboardContent();
        dkprContenido.putString(dkprEnvio.dkprGetCodigoSeguimiento());
        Clipboard.getSystemClipboard().setContent(dkprContenido);
        dkprBtnCopiar.setText("¡Copiado!");
        PauseTransition dkprPausa = new PauseTransition(Duration.seconds(1.5));
        dkprPausa.setOnFinished(dkprEv -> dkprBtnCopiar.setText("Copiar código"));
        dkprPausa.play();
    }

    @FXML
    private void dkprOnTicket() {
        if (dkprEnvio == null) {
            return;
        }
        try {
            DkprVistaTicket.dkprMostrar(dkprService.dkprDetalle(dkprEnvio.dkprGetCodigoSeguimiento()), dkprTxtCodigo.getScene().getWindow());
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnGuardarPdf() {
        if (dkprEnvio == null) {
            return;
        }
        try {
            DkprVistaTicket.dkprGuardarPdf(dkprService.dkprDetalle(dkprEnvio.dkprGetCodigoSeguimiento()), dkprTxtCodigo.getScene().getWindow());
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnActualizarEstado() {
        if (dkprEnvio == null) {
            return;
        }
        String dkprNombreEstado = dkprCmbNuevoEstado.getValue();
        if (dkprNombreEstado == null) {
            DkprAlertas.dkprAdvertencia("Selecciona el nuevo estado.");
            return;
        }
        int dkprNuevoEstado = DkprEstados.dkprId(dkprNombreEstado);
        if (dkprNuevoEstado == DkprEstados.ANULADO
                && !DkprAlertas.dkprConfirmar("¿Seguro que deseas ANULAR el envío " + dkprEnvio.dkprGetCodigoSeguimiento() + "?")) {
            return;
        }
        try {
            dkprService.dkprCambiarEstado(dkprEnvio, dkprNuevoEstado, dkprTxtObservacion.getText(), DkprSesion.dkprGetActual());
            DkprAlertas.dkprInfo("Estado actualizado a " + dkprNombreEstado + ".");
            dkprMostrar(dkprService.dkprBuscar(dkprEnvio.dkprGetCodigoSeguimiento()));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }
}
