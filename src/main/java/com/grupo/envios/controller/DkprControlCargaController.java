package com.grupo.envios.controller;

import com.grupo.envios.dao.DkprManifiestoDAO;
import com.grupo.envios.model.DkprAgencia;
import com.grupo.envios.model.DkprConductor;
import com.grupo.envios.model.DkprControlCarga;
import com.grupo.envios.model.DkprVehiculo;
import com.grupo.envios.service.DkprVehiculoService;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.model.DkprLecturaCarga;
import com.grupo.envios.model.DkprManifiesto;
import com.grupo.envios.service.DkprAgenciaService;
import com.grupo.envios.service.DkprControlCargaService;
import com.grupo.envios.service.DkprControlCargaService.DkprResultadoCierre;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprEtiqueta;
import com.grupo.envios.util.DkprGeneradorPdf;
import com.grupo.envios.util.DkprNegocioException;
import com.grupo.envios.util.DkprRoles;
import com.grupo.envios.util.DkprSesion;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.awt.Toolkit;
import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DkprControlCargaController {

    private static final int MAX_VISTA_PREVIA = 12;
    private static final int MAX_LINEAS_AVISO = 12;

    private static final String ESTILO_ERROR = "-fx-background-color: #fdedec; -fx-text-fill: #c0392b; "
            + "-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 12;";
    private static final String ESTILO_DUPLICADO = "-fx-background-color: #fef9e7; -fx-text-fill: #7d6608; "
            + "-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 12;";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private ComboBox<DkprAgencia> dkprCmbAgencia;
    @FXML private TabPane dkprTabs;
    @FXML private Tab dkprTabEtiquetas;
    @FXML private Tab dkprTabDespacho;
    @FXML private Tab dkprTabRecepcion;

    @FXML private TextField dkprTxtBuscarEtiqueta;
    @FXML private TableView<DkprEnvio> dkprTablaEtiquetas;
    @FXML private VBox dkprPreviaEtiquetas;
    @FXML private Label dkprLblEtiquetas;

    @FXML private ComboBox<DkprOpcionRuta> dkprCmbRuta;
    @FXML private Label dkprLblSesionDespacho;
    @FXML private TextField dkprTxtEscanearDespacho;
    @FXML private Label dkprLblAvisoDespacho;
    @FXML private Label dkprLblAvanceDespacho;
    @FXML private TableView<DkprEnvio> dkprTablaEspDespacho;
    @FXML private TableView<DkprLecturaCarga> dkprTablaLecDespacho;
    @FXML private ComboBox<DkprVehiculo> dkprCmbVehiculo;
    @FXML private ComboBox<DkprConductor> dkprCmbConductor;
    @FXML private TextField dkprTxtObsDespacho;

    @FXML private TextField dkprTxtManifiesto;
    @FXML private ComboBox<DkprManifiesto> dkprCmbManifiesto;
    @FXML private Label dkprLblSesionRecepcion;
    @FXML private TextField dkprTxtEscanearRecepcion;
    @FXML private Label dkprLblAvisoRecepcion;
    @FXML private Label dkprLblAvanceRecepcion;
    @FXML private TableView<DkprEnvio> dkprTablaEspRecepcion;
    @FXML private TableView<DkprLecturaCarga> dkprTablaLecRecepcion;
    @FXML private TextField dkprTxtObsRecepcion;

    private final DkprControlCargaService dkprService = new DkprControlCargaService();
    private final DkprAgenciaService dkprAgenciaService = new DkprAgenciaService();
    private final DkprVehiculoService dkprVehiculoService = new DkprVehiculoService();

    private DkprPanelEscaneo dkprDespacho;
    private DkprPanelEscaneo dkprRecepcion;
    private final ObservableList<DkprEnvio> dkprEnviosEtiquetas = FXCollections.observableArrayList();
    private String dkprUltimoManifiesto;

    private record DkprOpcionRuta(int dkprIdRuta, String dkprDestino, int dkprEnvios, int dkprBultos) {
        @Override
        public String toString() {
            return dkprDestino + "  ·  " + dkprEnvios + " envío(s), " + dkprBultos + " bulto(s)";
        }
    }

    @FXML
    private void initialize() {
        dkprConfigurarEtiquetas();
        dkprDespacho = new DkprPanelEscaneo(dkprTxtEscanearDespacho, dkprLblSesionDespacho, dkprLblAvisoDespacho,
                dkprLblAvanceDespacho, dkprTablaEspDespacho, dkprTablaLecDespacho);
        dkprRecepcion = new DkprPanelEscaneo(dkprTxtEscanearRecepcion, dkprLblSesionRecepcion, dkprLblAvisoRecepcion,
                dkprLblAvanceRecepcion, dkprTablaEspRecepcion, dkprTablaLecRecepcion);

        DkprEmpleado dkprEmpleado = DkprSesion.dkprGetActual();
        boolean dkprAdmin = DkprRoles.ADMINISTRADOR.equals(dkprEmpleado.dkprGetRol());
        if (!dkprService.dkprPuedeOperar(dkprEmpleado, dkprEmpleado.dkprGetIdAgencia())) {
            dkprTabs.getTabs().removeAll(dkprTabDespacho, dkprTabRecepcion);
        } else {
            dkprCargarVehiculos();
        }
        dkprTxtManifiesto.setOnAction(dkprE -> dkprEscanearManifiesto());
        dkprTabs.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAntes, dkprAhora) -> {
            if (dkprAhora == dkprTabDespacho) {
                dkprDespacho.dkprEnfocar();
            } else if (dkprAhora == dkprTabRecepcion) {
                dkprRecepcion.dkprEnfocar();
            }
        });

        try {
            List<DkprAgencia> dkprAgencias = dkprAgenciaService.dkprListar(true);
            dkprCmbAgencia.setItems(FXCollections.observableArrayList(dkprAgencias));
            dkprAgencias.stream().filter(dkprA -> dkprA.dkprGetIdAgencia() == dkprEmpleado.dkprGetIdAgencia())
                    .findFirst().ifPresent(dkprCmbAgencia::setValue);
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
        dkprCmbAgencia.setDisable(!dkprAdmin);
        dkprCmbAgencia.setOnAction(dkprE -> dkprCargar(true));
        dkprCargar(true);
    }

    @FXML
    private void dkprOnActualizar() {
        dkprCargar(false);
    }

    private void dkprCargar(boolean dkprCambioAgencia) {
        DkprAgencia dkprAgencia = dkprCmbAgencia.getValue();
        if (dkprAgencia == null) {
            return;
        }
        int dkprId = dkprAgencia.dkprGetIdAgencia();
        try {
            List<DkprEnvio> dkprPendientes = dkprService.dkprPorEtiquetar(dkprId);
            dkprEnviosEtiquetas.setAll(dkprPendientes);
            dkprActualizarPrevia();

            if (dkprTabs.getTabs().contains(dkprTabDespacho)) {
                DkprOpcionRuta dkprAnterior = dkprCmbRuta.getValue();
                dkprCmbRuta.setItems(FXCollections.observableArrayList(dkprOpcionesRuta(dkprPendientes)));
                if (dkprAnterior != null && !dkprCambioAgencia) {
                    dkprCmbRuta.getItems().stream().filter(dkprO -> dkprO.dkprIdRuta() == dkprAnterior.dkprIdRuta())
                            .findFirst().ifPresent(dkprCmbRuta::setValue);
                }
                DkprManifiesto dkprManifiestoAnterior = dkprCmbManifiesto.getValue();
                dkprCmbManifiesto.setItems(FXCollections.observableArrayList(dkprService.dkprManifiestosPorRecibir(dkprId)));
                if (dkprManifiestoAnterior != null && !dkprCambioAgencia) {
                    dkprCmbManifiesto.getItems().stream()
                            .filter(dkprM -> dkprM.dkprGetIdManifiesto() == dkprManifiestoAnterior.dkprGetIdManifiesto())
                            .findFirst().ifPresent(dkprCmbManifiesto::setValue);
                }
                if (dkprCambioAgencia) {
                    dkprDespacho.dkprLimpiar();
                    dkprRecepcion.dkprLimpiar();
                } else {
                    dkprDespacho.dkprRefrescar();
                    dkprRecepcion.dkprRefrescar();
                }
            }
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private static List<DkprOpcionRuta> dkprOpcionesRuta(List<DkprEnvio> dkprPendientes) {
        Map<Integer, List<DkprEnvio>> dkprPorRuta = new LinkedHashMap<>();
        for (DkprEnvio dkprE : dkprPendientes) {
            dkprPorRuta.computeIfAbsent(dkprE.dkprGetIdRuta(), dkprK -> new ArrayList<>()).add(dkprE);
        }
        List<DkprOpcionRuta> dkprOpciones = new ArrayList<>();
        dkprPorRuta.forEach((dkprIdRuta, dkprEnvios) -> dkprOpciones.add(new DkprOpcionRuta(dkprIdRuta,
                dkprEnvios.get(0).dkprGetDestino(), dkprEnvios.size(), DkprEtiqueta.dkprContar(dkprEnvios))));
        return dkprOpciones;
    }

    private Window dkprVentana() {
        return dkprTabs.getScene().getWindow();
    }

    private void dkprConfigurarEtiquetas() {
        dkprTablaEtiquetas.setItems(dkprEnviosEtiquetas);
        dkprTablaEtiquetas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dkprTablaEtiquetas.getColumns().add(dkprColumna("Código", 110, DkprEnvio::dkprGetCodigoSeguimiento));
        dkprTablaEtiquetas.getColumns().add(dkprColumna("Destino", 150, DkprEnvio::dkprGetDestino));
        dkprTablaEtiquetas.getColumns().add(dkprColumna("Destinatario", 190, DkprEnvio::dkprGetDestinatario));
        dkprTablaEtiquetas.getColumns().add(dkprColumna("Bultos", 60, dkprE -> String.valueOf(dkprE.dkprGetCantidadBultos())));
        dkprTablaEtiquetas.getColumns().add(dkprColumna("Estado", 120, DkprEnvio::dkprGetEstado));
        dkprTablaEtiquetas.getColumns().add(dkprColumna("Registrado", 125, DkprEnvio::dkprGetFechaTexto));
        dkprTablaEtiquetas.getSelectionModel().getSelectedItems()
                .addListener((ListChangeListener<DkprEnvio>) dkprCambio -> dkprActualizarPrevia());
        dkprTxtBuscarEtiqueta.setOnAction(dkprE -> dkprOnAgregarEtiqueta());
    }

    private List<DkprEnvio> dkprSeleccionEtiquetas() {
        return new ArrayList<>(dkprTablaEtiquetas.getSelectionModel().getSelectedItems());
    }

    private void dkprActualizarPrevia() {
        List<DkprEnvio> dkprSeleccion = dkprSeleccionEtiquetas();
        int dkprTotal = DkprEtiqueta.dkprContar(dkprSeleccion);
        dkprPreviaEtiquetas.getChildren().clear();
        int dkprDibujadas = 0;
        for (DkprEnvio dkprE : dkprSeleccion) {
            for (int dkprN = 1; dkprN <= dkprE.dkprGetCantidadBultos() && dkprDibujadas < MAX_VISTA_PREVIA; dkprN++) {
                dkprPreviaEtiquetas.getChildren().add(DkprEtiqueta.dkprCrear(dkprE, dkprN));
                dkprDibujadas++;
            }
        }
        if (dkprTotal > dkprDibujadas) {
            dkprPreviaEtiquetas.getChildren().add(new Label("… y " + (dkprTotal - dkprDibujadas) + " etiqueta(s) más."));
        }
        dkprLblEtiquetas.setText(dkprTotal == 0 ? "Selecciona uno o varios envíos para ver sus etiquetas."
                : dkprTotal + " etiqueta(s) de " + dkprSeleccion.size() + " envío(s): una por bulto.");
    }

    @FXML
    private void dkprOnAgregarEtiqueta() {
        try {
            DkprEnvio dkprEncontrado = dkprService.dkprBuscarEnvio(dkprTxtBuscarEtiqueta.getText());
            dkprTxtBuscarEtiqueta.clear();
            DkprEnvio dkprFila = dkprEnviosEtiquetas.stream()
                    .filter(dkprE -> dkprE.dkprGetIdEnvio() == dkprEncontrado.dkprGetIdEnvio())
                    .findFirst().orElse(null);
            if (dkprFila == null) {
                dkprEnviosEtiquetas.add(0, dkprEncontrado);
                dkprFila = dkprEncontrado;
            }
            dkprTablaEtiquetas.getSelectionModel().select(dkprFila);
            dkprTablaEtiquetas.scrollTo(dkprFila);
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
        dkprTxtBuscarEtiqueta.requestFocus();
    }

    @FXML
    private void dkprOnImprimirEtiquetas() {
        List<DkprEnvio> dkprSeleccion = dkprSeleccionEtiquetas();
        if (dkprSeleccion.isEmpty()) {
            DkprAlertas.dkprAdvertencia("Selecciona los envíos cuyas etiquetas quieres imprimir.");
            return;
        }
        DkprEtiqueta.dkprImprimir(dkprSeleccion, dkprVentana());
    }

    @FXML
    private void dkprOnEtiquetasPdf() {
        List<DkprEnvio> dkprSeleccion = dkprSeleccionEtiquetas();
        if (dkprSeleccion.isEmpty()) {
            DkprAlertas.dkprAdvertencia("Selecciona los envíos cuyas etiquetas quieres guardar.");
            return;
        }
        String dkprNombre = dkprSeleccion.size() == 1
                ? "etiquetas_" + dkprSeleccion.get(0).dkprGetCodigoSeguimiento() + ".pdf" : "etiquetas.pdf";
        File dkprArchivo = DkprGeneradorPdf.dkprElegirDestino(dkprVentana(), "Guardar etiquetas", dkprNombre);
        if (dkprArchivo == null) {
            return;
        }
        try {
            DkprGeneradorPdf.dkprEtiquetas(dkprSeleccion, dkprArchivo);
            DkprGeneradorPdf.dkprOfrecerAbrir(dkprArchivo);
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnIniciarDespacho() {
        DkprOpcionRuta dkprRuta = dkprCmbRuta.getValue();
        if (dkprRuta == null) {
            DkprAlertas.dkprAdvertencia("Elige el destino del vehículo.");
            return;
        }
        try {
            dkprDespacho.dkprMostrar(dkprService.dkprIniciarDespacho(dkprCmbAgencia.getValue().dkprGetIdAgencia(), dkprRuta.dkprIdRuta(),
                    DkprSesion.dkprGetActual()));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnCerrarDespacho() {
        DkprControlCarga dkprSesion = dkprDespacho.dkprGetSesion();
        if (dkprSesion == null) {
            DkprAlertas.dkprAdvertencia("Primero elige el destino e inicia la carga.");
            return;
        }
        try {
            dkprDespacho.dkprRefrescar();
            List<DkprEnvio> dkprCompletos = dkprDespacho.dkprCompletos();
            if (dkprCompletos.isEmpty()) {
                DkprAlertas.dkprAdvertencia("Ningún envío tiene todos sus bultos escaneados. Escanea los bultos o cancela la carga.");
                return;
            }
            String dkprMensaje = "¿Cerrar la carga y despachar " + dkprCompletos.size() + " envío(s) completo(s) ("
                    + DkprEtiqueta.dkprContar(dkprCompletos) + " bulto(s)) hacia " + dkprSesion.dkprGetDestino() + "?"
                    + dkprLista("Se quedan en la agencia porque les faltan bultos:", dkprDespacho.dkprFaltantes());
            if (!DkprAlertas.dkprConfirmar(dkprMensaje)) {
                return;
            }
            DkprResultadoCierre dkprR = dkprService.dkprCerrarDespacho(dkprSesion,
                    dkprCmbVehiculo.getValue() == null ? null : dkprCmbVehiculo.getValue().dkprGetIdVehiculo(),
                    dkprCmbConductor.getValue() == null ? null : dkprCmbConductor.getValue().dkprGetIdConductor(),
                    dkprTxtObsDespacho.getText(), DkprSesion.dkprGetActual());
            dkprUltimoManifiesto = dkprR.dkprManifiesto();
            dkprCmbVehiculo.setValue(null);
            dkprCmbConductor.setValue(null);
            dkprTxtObsDespacho.clear();
            dkprDespacho.dkprLimpiar();
            dkprCargar(false);
            if (DkprAlertas.dkprConfirmar("Manifiesto " + dkprR.dkprManifiesto() + " generado: " + dkprR.dkprMovidos().size()
                    + " envío(s) EN TRANSITO hacia " + dkprSesion.dkprGetDestino() + "."
                    + dkprLista("Faltantes (se quedan en la agencia):", dkprR.dkprFaltantes())
                    + "\n\n¿Guardar el manifiesto en PDF para imprimirlo?")) {
                dkprGuardarManifiesto(dkprService.dkprBuscarManifiesto(dkprR.dkprIdManifiesto()));
            }
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnCancelarDespacho() {
        dkprCancelar(dkprDespacho, "¿Cancelar la carga? No se despachará ningún envío (las lecturas quedan guardadas como constancia).");
    }

    @FXML
    private void dkprOnImprimirManifiesto() {
        TextInputDialog dkprDialogo = new TextInputDialog(dkprUltimoManifiesto == null ? "" : dkprUltimoManifiesto);
        dkprDialogo.setTitle("Imprimir manifiesto");
        dkprDialogo.setHeaderText(null);
        dkprDialogo.setContentText("Código del manifiesto (MAN-000001 o 1):");
        dkprDialogo.initOwner(dkprVentana());
        dkprDialogo.showAndWait().ifPresent(dkprCodigo -> {
            try {
                dkprGuardarManifiesto(dkprService.dkprBuscarManifiesto(dkprCodigo));
            } catch (Exception dkprE) {
                DkprAlertas.dkprManejar(dkprE);
            }
        });
    }

    private void dkprGuardarManifiesto(DkprManifiesto dkprM) throws Exception {
        File dkprArchivo = DkprGeneradorPdf.dkprElegirDestino(dkprVentana(), "Guardar manifiesto", "manifiesto_" + dkprM.dkprGetCodigo() + ".pdf");
        if (dkprArchivo == null) {
            return;
        }
        DkprGeneradorPdf.dkprManifiesto(dkprM, dkprService.dkprEnviosDeManifiesto(dkprM.dkprGetIdManifiesto()), dkprArchivo);
        DkprGeneradorPdf.dkprOfrecerAbrir(dkprArchivo);
    }

    private void dkprCargarVehiculos() {
        try {
            dkprCmbVehiculo.setItems(FXCollections.observableArrayList(dkprVehiculoService.dkprListarVehiculos(true)));
            dkprCmbConductor.setItems(FXCollections.observableArrayList(dkprVehiculoService.dkprListarConductores(true)));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprEscanearManifiesto() {
        String dkprTexto = dkprTxtManifiesto.getText() == null ? "" : dkprTxtManifiesto.getText().trim().toUpperCase();
        dkprTxtManifiesto.clear();
        Matcher dkprM = Pattern.compile("^(?:MAN)?[^A-Z0-9]*(\\d{1,6})$").matcher(dkprTexto);
        if (!dkprM.matches()) {
            DkprAlertas.dkprAdvertencia("Escanea el código de barras del manifiesto o escribe su número (ej: MAN-000001 o 1).");
            return;
        }
        int dkprId = Integer.parseInt(dkprM.group(1));
        DkprManifiesto dkprEnLista = dkprCmbManifiesto.getItems().stream()
                .filter(dkprX -> dkprX.dkprGetIdManifiesto() == dkprId).findFirst().orElse(null);
        if (dkprEnLista != null) {
            dkprCmbManifiesto.setValue(dkprEnLista);
            dkprOnIniciarRecepcion();
            return;
        }
        try {
            DkprManifiesto dkprM2 = dkprService.dkprBuscarManifiesto(dkprId);
            if (dkprM2 == null) {
                DkprAlertas.dkprAdvertencia("No existe el manifiesto " + DkprManifiestoDAO.dkprCodigo(dkprId) + ".");
            } else if (dkprM2.dkprGetIdAgenciaDestino() != dkprCmbAgencia.getValue().dkprGetIdAgencia()) {
                DkprAlertas.dkprAdvertencia("El manifiesto " + dkprM2.dkprGetCodigo() + " va a " + dkprM2.dkprGetDestino() + ", no a esta agencia.");
            } else {
                DkprAlertas.dkprInfo("El manifiesto " + dkprM2.dkprGetCodigo() + " ya no tiene envíos en tránsito: ya se recibió todo.");
            }
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnIniciarRecepcion() {
        DkprManifiesto dkprM = dkprCmbManifiesto.getValue();
        if (dkprM == null) {
            DkprAlertas.dkprAdvertencia(dkprCmbManifiesto.getItems().isEmpty()
                    ? "No hay manifiestos en camino hacia esta agencia." : "Elige el manifiesto que llega.");
            return;
        }
        try {
            dkprRecepcion.dkprMostrar(dkprService.dkprIniciarRecepcion(dkprCmbAgencia.getValue().dkprGetIdAgencia(), dkprM, DkprSesion.dkprGetActual()));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnCerrarRecepcion() {
        DkprControlCarga dkprSesion = dkprRecepcion.dkprGetSesion();
        if (dkprSesion == null) {
            DkprAlertas.dkprAdvertencia("Primero elige el manifiesto e inicia la descarga.");
            return;
        }
        try {
            dkprRecepcion.dkprRefrescar();
            List<DkprEnvio> dkprCompletos = dkprRecepcion.dkprCompletos();
            if (dkprCompletos.isEmpty()) {
                DkprAlertas.dkprAdvertencia("Ningún envío llegó con todos sus bultos. Si el vehículo aún no termina de descargar, "
                        + "deja la recepción abierta; si no llegó, cancélala.");
                return;
            }
            String dkprMensaje = "¿Cerrar la recepción de " + dkprSesion.dkprGetManifiesto() + " y confirmar la llegada de "
                    + dkprCompletos.size() + " envío(s) completo(s)?"
                    + dkprLista("Seguirán EN TRANSITO porque les faltan bultos:", dkprRecepcion.dkprFaltantes());
            if (!DkprAlertas.dkprConfirmar(dkprMensaje)) {
                return;
            }
            DkprResultadoCierre dkprR = dkprService.dkprCerrarRecepcion(dkprSesion, dkprTxtObsRecepcion.getText(), DkprSesion.dkprGetActual());
            dkprTxtObsRecepcion.clear();
            dkprRecepcion.dkprLimpiar();
            dkprCargar(false);
            DkprAlertas.dkprInfo(dkprR.dkprMovidos().size() + " envío(s) recibidos. Ya aparecen en \"Por entregar\" de la Bandeja."
                    + dkprLista("Faltantes (siguen EN TRANSITO y se marcarán como atrasados):", dkprR.dkprFaltantes()));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnCancelarRecepcion() {
        dkprCancelar(dkprRecepcion, "¿Cancelar la descarga? Ningún envío cambiará de estado (las lecturas quedan guardadas como constancia).");
    }

    private void dkprCancelar(DkprPanelEscaneo dkprPanel, String dkprPregunta) {
        if (dkprPanel.dkprGetSesion() == null) {
            DkprAlertas.dkprAdvertencia("No hay ninguna sesión abierta.");
            return;
        }
        if (!DkprAlertas.dkprConfirmar(dkprPregunta)) {
            return;
        }
        try {
            dkprService.dkprCancelar(dkprPanel.dkprGetSesion(), DkprSesion.dkprGetActual());
            dkprPanel.dkprLimpiar();
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private static String dkprLista(String dkprTitulo, List<String> dkprLineas) {
        if (dkprLineas.isEmpty()) {
            return "";
        }
        StringBuilder dkprSb = new StringBuilder("\n\n").append(dkprTitulo);
        dkprLineas.stream().limit(MAX_LINEAS_AVISO).forEach(dkprL -> dkprSb.append("\n- ").append(dkprL));
        if (dkprLineas.size() > MAX_LINEAS_AVISO) {
            dkprSb.append("\n… y ").append(dkprLineas.size() - MAX_LINEAS_AVISO).append(" más.");
        }
        return dkprSb.toString();
    }

    private static <T> TableColumn<T, String> dkprColumna(String dkprTitulo, double dkprAncho, Function<T, String> dkprValor) {
        TableColumn<T, String> dkprCol = new TableColumn<>(dkprTitulo);
        dkprCol.setPrefWidth(dkprAncho);
        dkprCol.setCellValueFactory(dkprC -> new SimpleStringProperty(dkprValor.apply(dkprC.getValue())));
        return dkprCol;
    }

    private static void dkprPitido() {
        Thread dkprHilo = new Thread(() -> {
            Toolkit dkprToolkit = Toolkit.getDefaultToolkit();
            dkprToolkit.beep();
            try {
                Thread.sleep(200);
            } catch (InterruptedException dkprE) {
                return;
            }
            dkprToolkit.beep();
        });
        dkprHilo.setDaemon(true);
        dkprHilo.start();
    }

    private class DkprPanelEscaneo {

        private final TextField dkprTxtEscanear;
        private final Label dkprLblSesion;
        private final Label dkprLblAviso;
        private final Label dkprLblAvance;
        private final TableView<DkprEnvio> dkprTablaEsperados;
        private final TableView<DkprLecturaCarga> dkprTablaLecturas;

        private DkprControlCarga dkprSesion;
        private Map<Integer, Set<Integer>> dkprLeidos = Map.of();

        DkprPanelEscaneo(TextField dkprTxtEscanear, Label dkprLblSesion, Label dkprLblAviso, Label dkprLblAvance,
                     TableView<DkprEnvio> dkprTablaEsperados, TableView<DkprLecturaCarga> dkprTablaLecturas) {
            this.dkprTxtEscanear = dkprTxtEscanear;
            this.dkprLblSesion = dkprLblSesion;
            this.dkprLblAviso = dkprLblAviso;
            this.dkprLblAvance = dkprLblAvance;
            this.dkprTablaEsperados = dkprTablaEsperados;
            this.dkprTablaLecturas = dkprTablaLecturas;

            dkprTablaEsperados.getColumns().add(dkprColumna("Código", 105, DkprEnvio::dkprGetCodigoSeguimiento));
            dkprTablaEsperados.getColumns().add(dkprColumna("Destinatario", 150, DkprEnvio::dkprGetDestinatario));
            dkprTablaEsperados.getColumns().add(dkprColumna("Bultos", 70,
                    dkprE -> dkprLeidosDe(dkprE) + " de " + dkprE.dkprGetCantidadBultos()));
            dkprTablaEsperados.getColumns().add(dkprColumna("Estado", 95, this::dkprEstadoCarga));
            dkprTablaEsperados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            dkprTablaEsperados.setRowFactory(dkprT -> new TableRow<>() {
                @Override
                protected void updateItem(DkprEnvio dkprE, boolean dkprVacia) {
                    super.updateItem(dkprE, dkprVacia);
                    getStyleClass().remove("fila-alerta");
                    if (!dkprVacia && dkprE != null && dkprLeidosDe(dkprE) > 0 && dkprLeidosDe(dkprE) < dkprE.dkprGetCantidadBultos()) {
                        getStyleClass().add("fila-alerta");
                    }
                }
            });

            dkprTablaLecturas.getColumns().add(dkprColumna("Hora", 75, DkprLecturaCarga::dkprGetHoraTexto));
            dkprTablaLecturas.getColumns().add(dkprColumna("Leído", 130, DkprLecturaCarga::dkprGetCodigoLeido));
            dkprTablaLecturas.getColumns().add(dkprColumna("Resultado", 125, DkprLecturaCarga::dkprGetResultadoTexto));
            dkprTablaLecturas.getColumns().add(dkprColumna("Detalle", 340, DkprLecturaCarga::dkprGetDetalle));
            dkprTablaLecturas.setRowFactory(dkprT -> new TableRow<>() {
                @Override
                protected void updateItem(DkprLecturaCarga dkprL, boolean dkprVacia) {
                    super.updateItem(dkprL, dkprVacia);
                    getStyleClass().remove("fila-alerta");
                    if (!dkprVacia && dkprL != null && !dkprL.dkprEsOk()) {
                        getStyleClass().add("fila-alerta");
                    }
                }
            });

            dkprTxtEscanear.setOnAction(dkprE -> dkprEscanear());
            dkprLimpiar();
        }

        DkprControlCarga dkprGetSesion() {
            return dkprSesion;
        }

        void dkprEnfocar() {
            if (dkprSesion != null) {
                Platform.runLater(dkprTxtEscanear::requestFocus);
            }
        }

        void dkprLimpiar() {
            dkprSesion = null;
            dkprLeidos = Map.of();
            dkprTablaEsperados.getItems().clear();
            dkprTablaLecturas.getItems().clear();
            dkprTxtEscanear.clear();
            dkprTxtEscanear.setDisable(true);
            dkprLblSesion.setText("No hay ninguna sesión abierta.");
            dkprLblAvance.setText("");
            dkprOcultarAviso();
        }

        void dkprMostrar(DkprControlCarga dkprC) throws SQLException {
            dkprSesion = dkprC;
            dkprTxtEscanear.setDisable(false);
            String dkprQue = dkprC.dkprEsDespacho() ? "Carga hacia " + dkprC.dkprGetDestino()
                    : "Descarga de " + dkprC.dkprGetManifiesto() + " (desde " + dkprC.dkprGetOrigen() + ")";
            dkprLblSesion.setText(dkprQue + "  ·  abierta el " + dkprC.dkprGetFechaInicio().format(FORMATO) + " por "
                    + dkprC.dkprGetEmpleado() + "  ·  si se cierra el programa, se retoma donde quedó");
            dkprOcultarAviso();
            dkprRefrescar();
            dkprEnfocar();
        }

        void dkprRefrescar() throws SQLException {
            if (dkprSesion == null) {
                return;
            }
            List<DkprEnvio> dkprEsperados = dkprService.dkprEsperados(dkprSesion);
            List<DkprLecturaCarga> dkprLecturas = dkprService.dkprLecturas(dkprSesion);
            dkprLeidos = DkprControlCargaService.dkprBultosLeidos(dkprLecturas);
            dkprTablaEsperados.setItems(FXCollections.observableArrayList(dkprEsperados));
            dkprTablaEsperados.refresh();
            List<DkprLecturaCarga> dkprRecientes = new ArrayList<>(dkprLecturas);
            Collections.reverse(dkprRecientes);
            dkprTablaLecturas.setItems(FXCollections.observableArrayList(dkprRecientes));

            int dkprBultos = DkprEtiqueta.dkprContar(dkprEsperados);
            int dkprBultosLeidos = dkprEsperados.stream().mapToInt(dkprE -> Math.min(dkprLeidosDe(dkprE), dkprE.dkprGetCantidadBultos())).sum();
            long dkprRechazadas = dkprLecturas.stream().filter(dkprL -> !dkprL.dkprEsOk()).count();
            dkprLblAvance.setText("Bultos leídos: " + dkprBultosLeidos + " de " + dkprBultos
                    + "   ·   Envíos completos: " + dkprCompletos().size() + " de " + dkprEsperados.size()
                    + "   ·   Lecturas rechazadas: " + dkprRechazadas);
        }

        List<DkprEnvio> dkprCompletos() {
            return dkprTablaEsperados.getItems().stream()
                    .filter(dkprE -> DkprControlCargaService.dkprCompleto(dkprE, dkprLeidos)).toList();
        }

        List<String> dkprFaltantes() {
            return DkprControlCargaService.dkprFaltantes(dkprTablaEsperados.getItems(), dkprLeidos);
        }

        private int dkprLeidosDe(DkprEnvio dkprE) {
            return dkprLeidos.getOrDefault(dkprE.dkprGetIdEnvio(), Set.of()).size();
        }

        private String dkprEstadoCarga(DkprEnvio dkprE) {
            int dkprN = dkprLeidosDe(dkprE);
            if (dkprN >= dkprE.dkprGetCantidadBultos()) {
                return "✔ Completo";
            }
            return dkprN == 0 ? "Falta" : "Parcial";
        }

        private void dkprEscanear() {
            if (dkprSesion == null) {
                return;
            }
            String dkprTexto = dkprTxtEscanear.getText();
            dkprTxtEscanear.clear();
            try {
                DkprLecturaCarga dkprL = dkprService.dkprEscanear(dkprSesion, dkprTexto, DkprSesion.dkprGetActual());
                if (dkprL != null) {
                    dkprAvisar(dkprL);
                    dkprRefrescar();
                }
            } catch (DkprNegocioException dkprE) {
                dkprMostrarAviso("✖ " + dkprE.getMessage(), ESTILO_ERROR);
                dkprPitido();
            } catch (Exception dkprE) {
                DkprAlertas.dkprManejar(dkprE);
            }
            dkprTxtEscanear.requestFocus();
        }

        private void dkprAvisar(DkprLecturaCarga dkprL) {
            if (dkprL.dkprEsOk()) {
                dkprMostrarAviso("✔ " + dkprL.dkprGetDetalle(), null);
            } else if (DkprLecturaCarga.DUPLICADO.equals(dkprL.dkprGetResultado())) {
                dkprMostrarAviso("⚠ DUPLICADO: " + dkprL.dkprGetDetalle(), ESTILO_DUPLICADO);
                dkprPitido();
            } else {
                dkprMostrarAviso("✖ " + dkprL.dkprGetDetalle(), ESTILO_ERROR);
                dkprPitido();
            }
        }

        private void dkprMostrarAviso(String dkprTexto, String dkprEstilo) {
            dkprLblAviso.setText(dkprTexto);
            dkprLblAviso.getStyleClass().remove("aviso-exito");
            if (dkprEstilo == null) {
                dkprLblAviso.getStyleClass().add("aviso-exito");
                dkprLblAviso.setStyle("-fx-font-size: 16px;");
            } else {
                dkprLblAviso.setStyle(dkprEstilo);
            }
            dkprLblAviso.setVisible(true);
            dkprLblAviso.setManaged(true);
        }

        private void dkprOcultarAviso() {
            dkprLblAviso.setText("");
            dkprLblAviso.setVisible(false);
            dkprLblAviso.setManaged(false);
        }
    }
}
