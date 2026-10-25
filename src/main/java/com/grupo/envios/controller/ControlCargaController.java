package com.grupo.envios.controller;

import com.grupo.envios.dao.ManifiestoDAO;
import com.grupo.envios.model.Agencia;
import com.grupo.envios.model.Conductor;
import com.grupo.envios.model.ControlCarga;
import com.grupo.envios.model.Vehiculo;
import com.grupo.envios.service.VehiculoService;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.model.LecturaCarga;
import com.grupo.envios.model.Manifiesto;
import com.grupo.envios.service.AgenciaService;
import com.grupo.envios.service.ControlCargaService;
import com.grupo.envios.service.ControlCargaService.ResultadoCierre;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Etiqueta;
import com.grupo.envios.util.GeneradorPdf;
import com.grupo.envios.util.NegocioException;
import com.grupo.envios.util.Roles;
import com.grupo.envios.util.Sesion;
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

/**
 * Control de carga por bulto: etiquetas con código de barras y QR, escaneo al cargar el vehículo
 * (despacho, genera el manifiesto) y al descargarlo en destino (recepción).
 */
public class ControlCargaController {

    /** Etiquetas que se dibujan en la vista previa (al imprimir o en el PDF salen todas). */
    private static final int MAX_VISTA_PREVIA = 12;
    /** Líneas de faltantes que se muestran en un aviso antes de resumir "y N más". */
    private static final int MAX_LINEAS_AVISO = 12;

    private static final String ESTILO_ERROR = "-fx-background-color: #fdedec; -fx-text-fill: #c0392b; "
            + "-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 12;";
    private static final String ESTILO_DUPLICADO = "-fx-background-color: #fef9e7; -fx-text-fill: #7d6608; "
            + "-fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10 12;";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private ComboBox<Agencia> dkprCmbAgencia;
    @FXML private TabPane dkprTabs;
    @FXML private Tab dkprTabEtiquetas;
    @FXML private Tab dkprTabDespacho;
    @FXML private Tab dkprTabRecepcion;

    @FXML private TextField dkprTxtBuscarEtiqueta;
    @FXML private TableView<Envio> dkprTablaEtiquetas;
    @FXML private VBox dkprPreviaEtiquetas;
    @FXML private Label dkprLblEtiquetas;

    @FXML private ComboBox<OpcionRuta> dkprCmbRuta;
    @FXML private Label dkprLblSesionDespacho;
    @FXML private TextField dkprTxtEscanearDespacho;
    @FXML private Label dkprLblAvisoDespacho;
    @FXML private Label dkprLblAvanceDespacho;
    @FXML private TableView<Envio> dkprTablaEspDespacho;
    @FXML private TableView<LecturaCarga> dkprTablaLecDespacho;
    @FXML private ComboBox<Vehiculo> dkprCmbVehiculo;
    @FXML private ComboBox<Conductor> dkprCmbConductor;
    @FXML private TextField dkprTxtObsDespacho;

    @FXML private TextField dkprTxtManifiesto;
    @FXML private ComboBox<Manifiesto> dkprCmbManifiesto;
    @FXML private Label dkprLblSesionRecepcion;
    @FXML private TextField dkprTxtEscanearRecepcion;
    @FXML private Label dkprLblAvisoRecepcion;
    @FXML private Label dkprLblAvanceRecepcion;
    @FXML private TableView<Envio> dkprTablaEspRecepcion;
    @FXML private TableView<LecturaCarga> dkprTablaLecRecepcion;
    @FXML private TextField dkprTxtObsRecepcion;

    private final ControlCargaService dkprService = new ControlCargaService();
    private final AgenciaService dkprAgenciaService = new AgenciaService();
    private final VehiculoService dkprVehiculoService = new VehiculoService();

    private PanelEscaneo dkprDespacho;
    private PanelEscaneo dkprRecepcion;
    private final ObservableList<Envio> dkprEnviosEtiquetas = FXCollections.observableArrayList();
    private String dkprUltimoManifiesto;

    /** Destino que se puede cargar, con lo que hay pendiente hacia él. */
    private record OpcionRuta(int idRuta, String destino, int envios, int bultos) {
        @Override
        public String toString() {
            return destino + "  ·  " + envios + " envío(s), " + bultos + " bulto(s)";
        }
    }

    @FXML
    private void initialize() {
        configurarEtiquetas();
        dkprDespacho = new PanelEscaneo(dkprTxtEscanearDespacho, dkprLblSesionDespacho, dkprLblAvisoDespacho,
                dkprLblAvanceDespacho, dkprTablaEspDespacho, dkprTablaLecDespacho);
        dkprRecepcion = new PanelEscaneo(dkprTxtEscanearRecepcion, dkprLblSesionRecepcion, dkprLblAvisoRecepcion,
                dkprLblAvanceRecepcion, dkprTablaEspRecepcion, dkprTablaLecRecepcion);

        Empleado dkprEmpleado = Sesion.getActual();
        boolean dkprAdmin = Roles.ADMINISTRADOR.equals(dkprEmpleado.getRol());
        // Etiquetas las puede imprimir cualquiera; cargar y descargar es trabajo de almacén.
        if (!dkprService.puedeOperar(dkprEmpleado, dkprEmpleado.getIdAgencia())) {
            dkprTabs.getTabs().removeAll(dkprTabDespacho, dkprTabRecepcion);
        } else {
            cargarVehiculos();
        }
        dkprTxtManifiesto.setOnAction(dkprE -> escanearManifiesto());
        // Al entrar a una pestaña de escaneo, el cursor queda listo para la pistola.
        dkprTabs.getSelectionModel().selectedItemProperty().addListener((dkprObs, dkprAntes, dkprAhora) -> {
            if (dkprAhora == dkprTabDespacho) {
                dkprDespacho.enfocar();
            } else if (dkprAhora == dkprTabRecepcion) {
                dkprRecepcion.enfocar();
            }
        });

        try {
            List<Agencia> dkprAgencias = dkprAgenciaService.listar(true);
            dkprCmbAgencia.setItems(FXCollections.observableArrayList(dkprAgencias));
            dkprAgencias.stream().filter(dkprA -> dkprA.getIdAgencia() == dkprEmpleado.getIdAgencia())
                    .findFirst().ifPresent(dkprCmbAgencia::setValue);
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
        // Solo el administrador puede trabajar con otra agencia.
        dkprCmbAgencia.setDisable(!dkprAdmin);
        dkprCmbAgencia.setOnAction(dkprE -> cargar(true));
        cargar(true);
    }

    // -------------------------------------------------------------- carga

    @FXML
    private void onActualizar() {
        cargar(false);
    }

    /** @param cambioAgencia true: se descartan las sesiones mostradas (siguen abiertas en la base de datos) */
    private void cargar(boolean dkprCambioAgencia) {
        Agencia dkprAgencia = dkprCmbAgencia.getValue();
        if (dkprAgencia == null) {
            return;
        }
        int dkprId = dkprAgencia.getIdAgencia();
        try {
            List<Envio> dkprPendientes = dkprService.porEtiquetar(dkprId);
            dkprEnviosEtiquetas.setAll(dkprPendientes);
            actualizarPrevia();

            if (dkprTabs.getTabs().contains(dkprTabDespacho)) {
                OpcionRuta dkprAnterior = dkprCmbRuta.getValue();
                dkprCmbRuta.setItems(FXCollections.observableArrayList(opcionesRuta(dkprPendientes)));
                if (dkprAnterior != null && !dkprCambioAgencia) {
                    dkprCmbRuta.getItems().stream().filter(dkprO -> dkprO.idRuta() == dkprAnterior.idRuta())
                            .findFirst().ifPresent(dkprCmbRuta::setValue);
                }
                Manifiesto dkprManifiestoAnterior = dkprCmbManifiesto.getValue();
                dkprCmbManifiesto.setItems(FXCollections.observableArrayList(dkprService.manifiestosPorRecibir(dkprId)));
                if (dkprManifiestoAnterior != null && !dkprCambioAgencia) {
                    dkprCmbManifiesto.getItems().stream()
                            .filter(dkprM -> dkprM.getIdManifiesto() == dkprManifiestoAnterior.getIdManifiesto())
                            .findFirst().ifPresent(dkprCmbManifiesto::setValue);
                }
                if (dkprCambioAgencia) {
                    dkprDespacho.limpiar();
                    dkprRecepcion.limpiar();
                } else {
                    dkprDespacho.refrescar();
                    dkprRecepcion.refrescar();
                }
            }
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    /** Destinos con envíos pendientes, en el orden en que aparecen (los más antiguos primero). */
    private static List<OpcionRuta> opcionesRuta(List<Envio> dkprPendientes) {
        Map<Integer, List<Envio>> dkprPorRuta = new LinkedHashMap<>();
        for (Envio dkprE : dkprPendientes) {
            dkprPorRuta.computeIfAbsent(dkprE.getIdRuta(), dkprK -> new ArrayList<>()).add(dkprE);
        }
        List<OpcionRuta> dkprOpciones = new ArrayList<>();
        dkprPorRuta.forEach((dkprIdRuta, dkprEnvios) -> dkprOpciones.add(new OpcionRuta(dkprIdRuta,
                dkprEnvios.get(0).getDestino(), dkprEnvios.size(), Etiqueta.contar(dkprEnvios))));
        return dkprOpciones;
    }

    private Window ventana() {
        return dkprTabs.getScene().getWindow();
    }

    // ---------------------------------------------------------- etiquetas

    private void configurarEtiquetas() {
        dkprTablaEtiquetas.setItems(dkprEnviosEtiquetas);
        dkprTablaEtiquetas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        dkprTablaEtiquetas.getColumns().add(columna("Código", 110, Envio::getCodigoSeguimiento));
        dkprTablaEtiquetas.getColumns().add(columna("Destino", 150, Envio::getDestino));
        dkprTablaEtiquetas.getColumns().add(columna("Destinatario", 190, Envio::getDestinatario));
        dkprTablaEtiquetas.getColumns().add(columna("Bultos", 60, dkprE -> String.valueOf(dkprE.getCantidadBultos())));
        dkprTablaEtiquetas.getColumns().add(columna("Estado", 120, Envio::getEstado));
        dkprTablaEtiquetas.getColumns().add(columna("Registrado", 125, Envio::getFechaTexto));
        dkprTablaEtiquetas.getSelectionModel().getSelectedItems()
                .addListener((ListChangeListener<Envio>) dkprCambio -> actualizarPrevia());
        dkprTxtBuscarEtiqueta.setOnAction(dkprE -> onAgregarEtiqueta());
    }

    private List<Envio> seleccionEtiquetas() {
        return new ArrayList<>(dkprTablaEtiquetas.getSelectionModel().getSelectedItems());
    }

    private void actualizarPrevia() {
        List<Envio> dkprSeleccion = seleccionEtiquetas();
        int dkprTotal = Etiqueta.contar(dkprSeleccion);
        dkprPreviaEtiquetas.getChildren().clear();
        int dkprDibujadas = 0;
        for (Envio dkprE : dkprSeleccion) {
            for (int dkprN = 1; dkprN <= dkprE.getCantidadBultos() && dkprDibujadas < MAX_VISTA_PREVIA; dkprN++) {
                dkprPreviaEtiquetas.getChildren().add(Etiqueta.crear(dkprE, dkprN));
                dkprDibujadas++;
            }
        }
        if (dkprTotal > dkprDibujadas) {
            dkprPreviaEtiquetas.getChildren().add(new Label("… y " + (dkprTotal - dkprDibujadas) + " etiqueta(s) más."));
        }
        dkprLblEtiquetas.setText(dkprTotal == 0 ? "Selecciona uno o varios envíos para ver sus etiquetas."
                : dkprTotal + " etiqueta(s) de " + dkprSeleccion.size() + " envío(s): una por bulto.");
    }

    /** Agrega a la lista un envío escaneado o escrito (sirve también para reimprimir etiquetas). */
    @FXML
    private void onAgregarEtiqueta() {
        try {
            Envio dkprEncontrado = dkprService.buscarEnvio(dkprTxtBuscarEtiqueta.getText());
            dkprTxtBuscarEtiqueta.clear();
            Envio dkprFila = dkprEnviosEtiquetas.stream()
                    .filter(dkprE -> dkprE.getIdEnvio() == dkprEncontrado.getIdEnvio())
                    .findFirst().orElse(null);
            if (dkprFila == null) {
                dkprEnviosEtiquetas.add(0, dkprEncontrado);
                dkprFila = dkprEncontrado;
            }
            dkprTablaEtiquetas.getSelectionModel().select(dkprFila);
            dkprTablaEtiquetas.scrollTo(dkprFila);
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
        dkprTxtBuscarEtiqueta.requestFocus();
    }

    @FXML
    private void onImprimirEtiquetas() {
        List<Envio> dkprSeleccion = seleccionEtiquetas();
        if (dkprSeleccion.isEmpty()) {
            Alertas.advertencia("Selecciona los envíos cuyas etiquetas quieres imprimir.");
            return;
        }
        Etiqueta.imprimir(dkprSeleccion, ventana());
    }

    @FXML
    private void onEtiquetasPdf() {
        List<Envio> dkprSeleccion = seleccionEtiquetas();
        if (dkprSeleccion.isEmpty()) {
            Alertas.advertencia("Selecciona los envíos cuyas etiquetas quieres guardar.");
            return;
        }
        String dkprNombre = dkprSeleccion.size() == 1
                ? "etiquetas_" + dkprSeleccion.get(0).getCodigoSeguimiento() + ".pdf" : "etiquetas.pdf";
        File dkprArchivo = GeneradorPdf.elegirDestino(ventana(), "Guardar etiquetas", dkprNombre);
        if (dkprArchivo == null) {
            return;
        }
        try {
            GeneradorPdf.etiquetas(dkprSeleccion, dkprArchivo);
            GeneradorPdf.ofrecerAbrir(dkprArchivo);
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    // ----------------------------------------------------------- despacho

    @FXML
    private void onIniciarDespacho() {
        OpcionRuta dkprRuta = dkprCmbRuta.getValue();
        if (dkprRuta == null) {
            Alertas.advertencia("Elige el destino del vehículo.");
            return;
        }
        try {
            dkprDespacho.mostrar(dkprService.iniciarDespacho(dkprCmbAgencia.getValue().getIdAgencia(), dkprRuta.idRuta(),
                    Sesion.getActual()));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onCerrarDespacho() {
        ControlCarga dkprSesion = dkprDespacho.getSesion();
        if (dkprSesion == null) {
            Alertas.advertencia("Primero elige el destino e inicia la carga.");
            return;
        }
        try {
            dkprDespacho.refrescar();
            List<Envio> dkprCompletos = dkprDespacho.completos();
            if (dkprCompletos.isEmpty()) {
                Alertas.advertencia("Ningún envío tiene todos sus bultos escaneados. Escanea los bultos o cancela la carga.");
                return;
            }
            String dkprMensaje = "¿Cerrar la carga y despachar " + dkprCompletos.size() + " envío(s) completo(s) ("
                    + Etiqueta.contar(dkprCompletos) + " bulto(s)) hacia " + dkprSesion.getDestino() + "?"
                    + lista("Se quedan en la agencia porque les faltan bultos:", dkprDespacho.faltantes());
            if (!Alertas.confirmar(dkprMensaje)) {
                return;
            }
            ResultadoCierre dkprR = dkprService.cerrarDespacho(dkprSesion,
                    dkprCmbVehiculo.getValue() == null ? null : dkprCmbVehiculo.getValue().getIdVehiculo(),
                    dkprCmbConductor.getValue() == null ? null : dkprCmbConductor.getValue().getIdConductor(),
                    dkprTxtObsDespacho.getText(), Sesion.getActual());
            dkprUltimoManifiesto = dkprR.manifiesto();
            dkprCmbVehiculo.setValue(null);
            dkprCmbConductor.setValue(null);
            dkprTxtObsDespacho.clear();
            dkprDespacho.limpiar();
            cargar(false);
            if (Alertas.confirmar("Manifiesto " + dkprR.manifiesto() + " generado: " + dkprR.movidos().size()
                    + " envío(s) EN TRANSITO hacia " + dkprSesion.getDestino() + "."
                    + lista("Faltantes (se quedan en la agencia):", dkprR.faltantes())
                    + "\n\n¿Guardar el manifiesto en PDF para imprimirlo?")) {
                guardarManifiesto(dkprService.buscarManifiesto(dkprR.idManifiesto()));
            }
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onCancelarDespacho() {
        cancelar(dkprDespacho, "¿Cancelar la carga? No se despachará ningún envío (las lecturas quedan guardadas como constancia).");
    }

    @FXML
    private void onImprimirManifiesto() {
        TextInputDialog dkprDialogo = new TextInputDialog(dkprUltimoManifiesto == null ? "" : dkprUltimoManifiesto);
        dkprDialogo.setTitle("Imprimir manifiesto");
        dkprDialogo.setHeaderText(null);
        dkprDialogo.setContentText("Código del manifiesto (MAN-000001 o 1):");
        dkprDialogo.initOwner(ventana());
        dkprDialogo.showAndWait().ifPresent(dkprCodigo -> {
            try {
                guardarManifiesto(dkprService.buscarManifiesto(dkprCodigo));
            } catch (Exception dkprE) {
                Alertas.manejar(dkprE);
            }
        });
    }

    private void guardarManifiesto(Manifiesto dkprM) throws Exception {
        File dkprArchivo = GeneradorPdf.elegirDestino(ventana(), "Guardar manifiesto", "manifiesto_" + dkprM.getCodigo() + ".pdf");
        if (dkprArchivo == null) {
            return;
        }
        GeneradorPdf.manifiesto(dkprM, dkprService.enviosDeManifiesto(dkprM.getIdManifiesto()), dkprArchivo);
        GeneradorPdf.ofrecerAbrir(dkprArchivo);
    }

    /** Vehículos y conductores activos para el manifiesto (se registran en Vehículos y conductores). */
    private void cargarVehiculos() {
        try {
            dkprCmbVehiculo.setItems(FXCollections.observableArrayList(dkprVehiculoService.listarVehiculos(true)));
            dkprCmbConductor.setItems(FXCollections.observableArrayList(dkprVehiculoService.listarConductores(true)));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    // ---------------------------------------------------------- recepción

    /**
     * El conductor entrega el manifiesto impreso: al escanear su código de barras (o escribir MAN-000001 / 1)
     * se elige ese manifiesto y empieza la descarga.
     */
    private void escanearManifiesto() {
        String dkprTexto = dkprTxtManifiesto.getText() == null ? "" : dkprTxtManifiesto.getText().trim().toUpperCase();
        dkprTxtManifiesto.clear();
        Matcher dkprM = Pattern.compile("^(?:MAN)?[^A-Z0-9]*(\\d{1,6})$").matcher(dkprTexto);
        if (!dkprM.matches()) {
            Alertas.advertencia("Escanea el código de barras del manifiesto o escribe su número (ej: MAN-000001 o 1).");
            return;
        }
        int dkprId = Integer.parseInt(dkprM.group(1));
        Manifiesto dkprEnLista = dkprCmbManifiesto.getItems().stream()
                .filter(dkprX -> dkprX.getIdManifiesto() == dkprId).findFirst().orElse(null);
        if (dkprEnLista != null) {
            dkprCmbManifiesto.setValue(dkprEnLista);
            onIniciarRecepcion();
            return;
        }
        // No está entre los que vienen hacia aquí: se explica por qué.
        try {
            Manifiesto dkprM2 = dkprService.buscarManifiesto(dkprId);
            if (dkprM2 == null) {
                Alertas.advertencia("No existe el manifiesto " + ManifiestoDAO.codigo(dkprId) + ".");
            } else if (dkprM2.getIdAgenciaDestino() != dkprCmbAgencia.getValue().getIdAgencia()) {
                Alertas.advertencia("El manifiesto " + dkprM2.getCodigo() + " va a " + dkprM2.getDestino() + ", no a esta agencia.");
            } else {
                Alertas.info("El manifiesto " + dkprM2.getCodigo() + " ya no tiene envíos en tránsito: ya se recibió todo.");
            }
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onIniciarRecepcion() {
        Manifiesto dkprM = dkprCmbManifiesto.getValue();
        if (dkprM == null) {
            Alertas.advertencia(dkprCmbManifiesto.getItems().isEmpty()
                    ? "No hay manifiestos en camino hacia esta agencia." : "Elige el manifiesto que llega.");
            return;
        }
        try {
            dkprRecepcion.mostrar(dkprService.iniciarRecepcion(dkprCmbAgencia.getValue().getIdAgencia(), dkprM, Sesion.getActual()));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onCerrarRecepcion() {
        ControlCarga dkprSesion = dkprRecepcion.getSesion();
        if (dkprSesion == null) {
            Alertas.advertencia("Primero elige el manifiesto e inicia la descarga.");
            return;
        }
        try {
            dkprRecepcion.refrescar();
            List<Envio> dkprCompletos = dkprRecepcion.completos();
            if (dkprCompletos.isEmpty()) {
                Alertas.advertencia("Ningún envío llegó con todos sus bultos. Si el vehículo aún no termina de descargar, "
                        + "deja la recepción abierta; si no llegó, cancélala.");
                return;
            }
            String dkprMensaje = "¿Cerrar la recepción de " + dkprSesion.getManifiesto() + " y confirmar la llegada de "
                    + dkprCompletos.size() + " envío(s) completo(s)?"
                    + lista("Seguirán EN TRANSITO porque les faltan bultos:", dkprRecepcion.faltantes());
            if (!Alertas.confirmar(dkprMensaje)) {
                return;
            }
            ResultadoCierre dkprR = dkprService.cerrarRecepcion(dkprSesion, dkprTxtObsRecepcion.getText(), Sesion.getActual());
            dkprTxtObsRecepcion.clear();
            dkprRecepcion.limpiar();
            cargar(false);
            Alertas.info(dkprR.movidos().size() + " envío(s) recibidos. Ya aparecen en \"Por entregar\" de la Bandeja."
                    + lista("Faltantes (siguen EN TRANSITO y se marcarán como atrasados):", dkprR.faltantes()));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onCancelarRecepcion() {
        cancelar(dkprRecepcion, "¿Cancelar la descarga? Ningún envío cambiará de estado (las lecturas quedan guardadas como constancia).");
    }

    // ------------------------------------------------------------- ayudas

    private void cancelar(PanelEscaneo dkprPanel, String dkprPregunta) {
        if (dkprPanel.getSesion() == null) {
            Alertas.advertencia("No hay ninguna sesión abierta.");
            return;
        }
        if (!Alertas.confirmar(dkprPregunta)) {
            return;
        }
        try {
            dkprService.cancelar(dkprPanel.getSesion(), Sesion.getActual());
            dkprPanel.limpiar();
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    /** Arma una lista para un aviso: "\n\nTítulo\n- línea..." (vacío si no hay líneas). */
    private static String lista(String dkprTitulo, List<String> dkprLineas) {
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

    private static <T> TableColumn<T, String> columna(String dkprTitulo, double dkprAncho, Function<T, String> dkprValor) {
        TableColumn<T, String> dkprCol = new TableColumn<>(dkprTitulo);
        dkprCol.setPrefWidth(dkprAncho);
        dkprCol.setCellValueFactory(dkprC -> new SimpleStringProperty(dkprValor.apply(dkprC.getValue())));
        return dkprCol;
    }

    /** Dos pitidos del sistema cuando una lectura se rechaza (para que se note sin mirar la pantalla). */
    private static void pitido() {
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

    /**
     * Lo común a despacho y recepción: campo de escaneo, aviso de la última lectura, avance,
     * tabla de envíos esperados (con sus bultos leídos) y tabla de lecturas.
     */
    private class PanelEscaneo {

        private final TextField dkprTxtEscanear;
        private final Label dkprLblSesion;
        private final Label dkprLblAviso;
        private final Label dkprLblAvance;
        private final TableView<Envio> dkprTablaEsperados;
        private final TableView<LecturaCarga> dkprTablaLecturas;

        private ControlCarga dkprSesion;
        private Map<Integer, Set<Integer>> dkprLeidos = Map.of();

        PanelEscaneo(TextField dkprTxtEscanear, Label dkprLblSesion, Label dkprLblAviso, Label dkprLblAvance,
                     TableView<Envio> dkprTablaEsperados, TableView<LecturaCarga> dkprTablaLecturas) {
            this.dkprTxtEscanear = dkprTxtEscanear;
            this.dkprLblSesion = dkprLblSesion;
            this.dkprLblAviso = dkprLblAviso;
            this.dkprLblAvance = dkprLblAvance;
            this.dkprTablaEsperados = dkprTablaEsperados;
            this.dkprTablaLecturas = dkprTablaLecturas;

            dkprTablaEsperados.getColumns().add(columna("Código", 105, Envio::getCodigoSeguimiento));
            dkprTablaEsperados.getColumns().add(columna("Destinatario", 150, Envio::getDestinatario));
            dkprTablaEsperados.getColumns().add(columna("Bultos", 70,
                    dkprE -> leidosDe(dkprE) + " de " + dkprE.getCantidadBultos()));
            dkprTablaEsperados.getColumns().add(columna("Estado", 95, this::estadoCarga));
            dkprTablaEsperados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            dkprTablaEsperados.setRowFactory(dkprT -> new TableRow<>() {
                @Override
                protected void updateItem(Envio dkprE, boolean dkprVacia) {
                    super.updateItem(dkprE, dkprVacia);
                    getStyleClass().remove("fila-alerta");
                    // Resalta los que tienen bultos a medias: son los que se suelen olvidar.
                    if (!dkprVacia && dkprE != null && leidosDe(dkprE) > 0 && leidosDe(dkprE) < dkprE.getCantidadBultos()) {
                        getStyleClass().add("fila-alerta");
                    }
                }
            });

            dkprTablaLecturas.getColumns().add(columna("Hora", 75, LecturaCarga::getHoraTexto));
            dkprTablaLecturas.getColumns().add(columna("Leído", 130, LecturaCarga::getCodigoLeido));
            dkprTablaLecturas.getColumns().add(columna("Resultado", 125, LecturaCarga::getResultadoTexto));
            dkprTablaLecturas.getColumns().add(columna("Detalle", 340, LecturaCarga::getDetalle));
            dkprTablaLecturas.setRowFactory(dkprT -> new TableRow<>() {
                @Override
                protected void updateItem(LecturaCarga dkprL, boolean dkprVacia) {
                    super.updateItem(dkprL, dkprVacia);
                    getStyleClass().remove("fila-alerta");
                    if (!dkprVacia && dkprL != null && !dkprL.esOk()) {
                        getStyleClass().add("fila-alerta");
                    }
                }
            });

            dkprTxtEscanear.setOnAction(dkprE -> escanear());
            limpiar();
        }

        ControlCarga getSesion() {
            return dkprSesion;
        }

        void enfocar() {
            if (dkprSesion != null) {
                Platform.runLater(dkprTxtEscanear::requestFocus);
            }
        }

        void limpiar() {
            dkprSesion = null;
            dkprLeidos = Map.of();
            dkprTablaEsperados.getItems().clear();
            dkprTablaLecturas.getItems().clear();
            dkprTxtEscanear.clear();
            dkprTxtEscanear.setDisable(true);
            dkprLblSesion.setText("No hay ninguna sesión abierta.");
            dkprLblAvance.setText("");
            ocultarAviso();
        }

        void mostrar(ControlCarga dkprC) throws SQLException {
            dkprSesion = dkprC;
            dkprTxtEscanear.setDisable(false);
            String dkprQue = dkprC.esDespacho() ? "Carga hacia " + dkprC.getDestino()
                    : "Descarga de " + dkprC.getManifiesto() + " (desde " + dkprC.getOrigen() + ")";
            dkprLblSesion.setText(dkprQue + "  ·  abierta el " + dkprC.getFechaInicio().format(FORMATO) + " por "
                    + dkprC.getEmpleado() + "  ·  si se cierra el programa, se retoma donde quedó");
            ocultarAviso();
            refrescar();
            enfocar();
        }

        /** Vuelve a leer de la base los envíos esperados y las lecturas (también las de otras computadoras). */
        void refrescar() throws SQLException {
            if (dkprSesion == null) {
                return;
            }
            List<Envio> dkprEsperados = dkprService.esperados(dkprSesion);
            List<LecturaCarga> dkprLecturas = dkprService.lecturas(dkprSesion);
            dkprLeidos = ControlCargaService.bultosLeidos(dkprLecturas);
            dkprTablaEsperados.setItems(FXCollections.observableArrayList(dkprEsperados));
            dkprTablaEsperados.refresh();
            List<LecturaCarga> dkprRecientes = new ArrayList<>(dkprLecturas);
            Collections.reverse(dkprRecientes); // la última lectura arriba
            dkprTablaLecturas.setItems(FXCollections.observableArrayList(dkprRecientes));

            int dkprBultos = Etiqueta.contar(dkprEsperados);
            int dkprBultosLeidos = dkprEsperados.stream().mapToInt(dkprE -> Math.min(leidosDe(dkprE), dkprE.getCantidadBultos())).sum();
            long dkprRechazadas = dkprLecturas.stream().filter(dkprL -> !dkprL.esOk()).count();
            dkprLblAvance.setText("Bultos leídos: " + dkprBultosLeidos + " de " + dkprBultos
                    + "   ·   Envíos completos: " + completos().size() + " de " + dkprEsperados.size()
                    + "   ·   Lecturas rechazadas: " + dkprRechazadas);
        }

        List<Envio> completos() {
            return dkprTablaEsperados.getItems().stream()
                    .filter(dkprE -> ControlCargaService.completo(dkprE, dkprLeidos)).toList();
        }

        List<String> faltantes() {
            return ControlCargaService.faltantes(dkprTablaEsperados.getItems(), dkprLeidos);
        }

        private int leidosDe(Envio dkprE) {
            return dkprLeidos.getOrDefault(dkprE.getIdEnvio(), Set.of()).size();
        }

        private String estadoCarga(Envio dkprE) {
            int dkprN = leidosDe(dkprE);
            if (dkprN >= dkprE.getCantidadBultos()) {
                return "✔ Completo";
            }
            return dkprN == 0 ? "Falta" : "Parcial";
        }

        private void escanear() {
            if (dkprSesion == null) {
                return;
            }
            String dkprTexto = dkprTxtEscanear.getText();
            dkprTxtEscanear.clear();
            try {
                LecturaCarga dkprL = dkprService.escanear(dkprSesion, dkprTexto, Sesion.getActual());
                if (dkprL != null) {
                    avisar(dkprL);
                    refrescar();
                }
            } catch (NegocioException dkprE) {
                // Sin ventanas emergentes: el Enter de la siguiente lectura las cerraría sin leerlas.
                mostrarAviso("✖ " + dkprE.getMessage(), ESTILO_ERROR);
                pitido();
            } catch (Exception dkprE) {
                Alertas.manejar(dkprE);
            }
            dkprTxtEscanear.requestFocus();
        }

        private void avisar(LecturaCarga dkprL) {
            if (dkprL.esOk()) {
                mostrarAviso("✔ " + dkprL.getDetalle(), null);
            } else if (LecturaCarga.DUPLICADO.equals(dkprL.getResultado())) {
                mostrarAviso("⚠ DUPLICADO: " + dkprL.getDetalle(), ESTILO_DUPLICADO);
                pitido();
            } else {
                mostrarAviso("✖ " + dkprL.getDetalle(), ESTILO_ERROR);
                pitido();
            }
        }

        /** @param estilo null = aviso verde de éxito (clase aviso-exito de estilos.css) */
        private void mostrarAviso(String dkprTexto, String dkprEstilo) {
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

        private void ocultarAviso() {
            dkprLblAviso.setText("");
            dkprLblAviso.setVisible(false);
            dkprLblAviso.setManaged(false);
        }
    }
}
