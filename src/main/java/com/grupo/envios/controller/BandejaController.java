package com.grupo.envios.controller;

import com.grupo.envios.model.Agencia;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.service.AgenciaService;
import com.grupo.envios.service.EnvioService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.CodigosBarras;
import com.grupo.envios.util.Roles;
import com.grupo.envios.util.Sesion;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Trabajo diario de una agencia: lo que hay que despachar, lo que viene en camino y lo que
 * espera ser recogido. Los estados se cambian en lote, marcando (o escaneando) los paquetes.
 */
public class BandejaController {

    private static final String TODOS_LOS_DESTINOS = "Todos los destinos";

    @FXML private ComboBox<Agencia> dkprCmbAgencia;
    @FXML private TabPane dkprTabs;
    @FXML private Tab dkprTabDespachar;
    @FXML private Tab dkprTabRecibir;
    @FXML private Tab dkprTabEntregar;

    @FXML private ComboBox<String> dkprCmbDestino;
    @FXML private TextField dkprTxtEscanearDespacho;
    @FXML private TableView<Envio> dkprTablaDespachar;
    @FXML private TableColumn<Envio, Boolean> dkprColDespMarca;
    @FXML private TableColumn<Envio, String> dkprColDespCodigo;
    @FXML private TableColumn<Envio, String> dkprColDespDestino;
    @FXML private TableColumn<Envio, String> dkprColDespDestinatario;
    @FXML private TableColumn<Envio, String> dkprColDespBultos;
    @FXML private TableColumn<Envio, String> dkprColDespPeso;
    @FXML private TableColumn<Envio, String> dkprColDespFecha;
    @FXML private TableColumn<Envio, String> dkprColDespAlerta;
    @FXML private TextField dkprTxtPlaca;
    @FXML private TextField dkprTxtConductor;
    @FXML private TextField dkprTxtObsDespacho;
    @FXML private Button dkprBtnDespachar;

    @FXML private TextField dkprTxtEscanearRecibir;
    @FXML private TableView<Envio> dkprTablaRecibir;
    @FXML private TableColumn<Envio, Boolean> dkprColRecMarca;
    @FXML private TableColumn<Envio, String> dkprColRecCodigo;
    @FXML private TableColumn<Envio, String> dkprColRecOrigen;
    @FXML private TableColumn<Envio, String> dkprColRecManifiesto;
    @FXML private TableColumn<Envio, String> dkprColRecBultos;
    @FXML private TableColumn<Envio, String> dkprColRecSalida;
    @FXML private TableColumn<Envio, String> dkprColRecEstimada;
    @FXML private TableColumn<Envio, String> dkprColRecAlerta;
    @FXML private TextField dkprTxtObsRecepcion;
    @FXML private Button dkprBtnRecibir;

    @FXML private TableView<Envio> dkprTablaEntregar;
    @FXML private TableColumn<Envio, String> dkprColEntCodigo;
    @FXML private TableColumn<Envio, String> dkprColEntDestinatario;
    @FXML private TableColumn<Envio, String> dkprColEntDocumento;
    @FXML private TableColumn<Envio, String> dkprColEntTelefono;
    @FXML private TableColumn<Envio, String> dkprColEntOrigen;
    @FXML private TableColumn<Envio, String> dkprColEntLlego;
    @FXML private TableColumn<Envio, String> dkprColEntAlerta;

    private final EnvioService dkprService = new EnvioService();
    private final AgenciaService dkprAgenciaService = new AgenciaService();

    /** Casilla marcada de cada envío (por id), compartida por las tablas con selección en lote. */
    private final Map<Integer, BooleanProperty> dkprMarcas = new HashMap<>();
    private FilteredList<Envio> dkprDespachoFiltrado = new FilteredList<>(FXCollections.observableArrayList());

    @FXML
    private void initialize() {
        configurarColumnas();
        dkprTablaDespachar.setRowFactory(dkprT -> filaConAlerta());
        dkprTablaRecibir.setRowFactory(dkprT -> filaConAlerta());
        dkprTablaEntregar.setRowFactory(dkprT -> {
            TableRow<Envio> dkprFila = filaConAlerta();
            dkprFila.setOnMouseClicked(dkprEv -> {
                if (dkprEv.getClickCount() == 2 && !dkprFila.isEmpty()) {
                    abrirEntrega(dkprFila.getItem());
                }
            });
            return dkprFila;
        });

        dkprCmbDestino.setOnAction(dkprE -> filtrarDestino());
        dkprTxtEscanearDespacho.setOnAction(dkprE -> escanear(dkprTxtEscanearDespacho, dkprTablaDespachar));
        dkprTxtEscanearRecibir.setOnAction(dkprE -> escanear(dkprTxtEscanearRecibir, dkprTablaRecibir));
        dkprCmbAgencia.setOnAction(dkprE -> cargar());

        Empleado dkprEmpleado = Sesion.getActual();
        boolean dkprAdmin = Roles.ADMINISTRADOR.equals(dkprEmpleado.getRol());
        if (!dkprService.puedeOperarAlmacen(dkprEmpleado, dkprEmpleado.getIdAgencia())) {
            dkprTabs.getTabs().removeAll(dkprTabDespachar, dkprTabRecibir);
        }
        if (!dkprService.puedeEntregarEn(dkprEmpleado, dkprEmpleado.getIdAgencia())) {
            dkprTabs.getTabs().remove(dkprTabEntregar);
        }

        try {
            List<Agencia> dkprAgencias = dkprAgenciaService.listar(true);
            dkprCmbAgencia.setItems(FXCollections.observableArrayList(dkprAgencias));
            dkprAgencias.stream().filter(dkprA -> dkprA.getIdAgencia() == dkprEmpleado.getIdAgencia())
                    .findFirst().ifPresent(dkprCmbAgencia::setValue);
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
        // Solo el administrador puede ver la bandeja de otra agencia.
        dkprCmbAgencia.setDisable(!dkprAdmin);
        cargar();
    }

    private void configurarColumnas() {
        dkprColDespMarca.setCellValueFactory(dkprC -> marca(dkprC.getValue()));
        dkprColDespMarca.setCellFactory(CheckBoxTableCell.forTableColumn(dkprColDespMarca));
        texto(dkprColDespCodigo, Envio::getCodigoSeguimiento);
        texto(dkprColDespDestino, Envio::getDestino);
        texto(dkprColDespDestinatario, Envio::getDestinatario);
        texto(dkprColDespBultos, dkprE -> String.valueOf(dkprE.getCantidadBultos()));
        texto(dkprColDespPeso, dkprE -> dkprE.getPesoKg().toPlainString());
        texto(dkprColDespFecha, Envio::getFechaTexto);
        texto(dkprColDespAlerta, Envio::getAlerta);

        dkprColRecMarca.setCellValueFactory(dkprC -> marca(dkprC.getValue()));
        dkprColRecMarca.setCellFactory(CheckBoxTableCell.forTableColumn(dkprColRecMarca));
        texto(dkprColRecCodigo, Envio::getCodigoSeguimiento);
        texto(dkprColRecOrigen, Envio::getOrigen);
        texto(dkprColRecManifiesto, dkprE -> dkprE.getManifiesto() == null ? "-" : dkprE.getManifiesto());
        texto(dkprColRecBultos, dkprE -> String.valueOf(dkprE.getCantidadBultos()));
        texto(dkprColRecSalida, Envio::getUltimoMovimientoTexto);
        texto(dkprColRecEstimada, Envio::getFechaEstimadaTexto);
        texto(dkprColRecAlerta, Envio::getAlerta);

        texto(dkprColEntCodigo, Envio::getCodigoSeguimiento);
        texto(dkprColEntDestinatario, Envio::getDestinatario);
        texto(dkprColEntDocumento, Envio::getDestinatarioDocumento);
        texto(dkprColEntTelefono, dkprE -> dkprE.getDestinatarioTelefono() == null ? "-" : dkprE.getDestinatarioTelefono());
        texto(dkprColEntOrigen, Envio::getOrigen);
        texto(dkprColEntLlego, Envio::getUltimoMovimientoTexto);
        texto(dkprColEntAlerta, Envio::getAlerta);
    }

    private void texto(TableColumn<Envio, String> dkprCol, Function<Envio, String> dkprValor) {
        dkprCol.setCellValueFactory(dkprC -> new SimpleStringProperty(dkprValor.apply(dkprC.getValue())));
    }

    /** Pinta la fila cuando el envío está atrasado o lleva demasiado tiempo en la agencia. */
    private TableRow<Envio> filaConAlerta() {
        return new TableRow<>() {
            @Override
            protected void updateItem(Envio dkprEnvio, boolean dkprVacia) {
                super.updateItem(dkprEnvio, dkprVacia);
                getStyleClass().remove("fila-alerta");
                if (!dkprVacia && dkprEnvio != null && !dkprEnvio.getAlerta().isEmpty()) {
                    getStyleClass().add("fila-alerta");
                }
            }
        };
    }

    private BooleanProperty marca(Envio dkprE) {
        return dkprMarcas.computeIfAbsent(dkprE.getIdEnvio(), dkprId -> {
            BooleanProperty dkprP = new SimpleBooleanProperty(false);
            dkprP.addListener((dkprObs, dkprAntes, dkprAhora) -> actualizarBotones());
            return dkprP;
        });
    }

    private boolean marcado(Envio dkprE) {
        BooleanProperty dkprP = dkprMarcas.get(dkprE.getIdEnvio());
        return dkprP != null && dkprP.get();
    }

    private List<Envio> marcados(List<Envio> dkprFilas) {
        return dkprFilas.stream().filter(this::marcado).toList();
    }

    private void actualizarBotones() {
        int dkprDespacho = marcados(dkprDespachoFiltrado).size();
        int dkprRecepcion = marcados(dkprTablaRecibir.getItems()).size();
        dkprBtnDespachar.setText(dkprDespacho == 0 ? "Despachar" : "Despachar " + dkprDespacho + " envío(s)");
        dkprBtnDespachar.setDisable(dkprDespacho == 0);
        dkprBtnRecibir.setText(dkprRecepcion == 0 ? "Confirmar llegada" : "Confirmar llegada de " + dkprRecepcion);
        dkprBtnRecibir.setDisable(dkprRecepcion == 0);
    }

    // -------------------------------------------------------------- carga

    @FXML
    private void onActualizar() {
        cargar();
    }

    private void cargar() {
        Agencia dkprAgencia = dkprCmbAgencia.getValue();
        dkprMarcas.clear();
        if (dkprAgencia == null) {
            return;
        }
        int dkprId = dkprAgencia.getIdAgencia();
        try {
            if (dkprTabs.getTabs().contains(dkprTabDespachar)) {
                List<Envio> dkprPendientes = dkprService.porDespachar(dkprId);
                dkprDespachoFiltrado = new FilteredList<>(FXCollections.observableArrayList(dkprPendientes));
                dkprTablaDespachar.setItems(dkprDespachoFiltrado);
                List<String> dkprDestinos = dkprPendientes.stream().map(Envio::getDestino).distinct().sorted().toList();
                dkprCmbDestino.getItems().setAll(TODOS_LOS_DESTINOS);
                dkprCmbDestino.getItems().addAll(dkprDestinos);
                // Con un solo destino ya queda listo para armar el manifiesto.
                dkprCmbDestino.setValue(dkprDestinos.size() == 1 ? dkprDestinos.get(0) : TODOS_LOS_DESTINOS);
                filtrarDestino();
                dkprTabDespachar.setText("Por despachar (" + dkprPendientes.size() + ")");

                List<Envio> dkprEnCamino = dkprService.porRecibir(dkprId);
                dkprTablaRecibir.setItems(FXCollections.observableArrayList(dkprEnCamino));
                dkprTabRecibir.setText("Por recibir (" + dkprEnCamino.size() + ")");
            }
            if (dkprTabs.getTabs().contains(dkprTabEntregar)) {
                List<Envio> dkprEnAgencia = dkprService.porEntregar(dkprId);
                dkprTablaEntregar.setItems(FXCollections.observableArrayList(dkprEnAgencia));
                dkprTabEntregar.setText("Por entregar (" + dkprEnAgencia.size() + ")");
            }
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
        actualizarBotones();
    }

    private void filtrarDestino() {
        String dkprDestino = dkprCmbDestino.getValue();
        // Al cambiar de destino se desmarca todo: un manifiesto va a un solo destino.
        dkprDespachoFiltrado.getSource().forEach(dkprE -> marca(dkprE).set(false));
        dkprDespachoFiltrado.setPredicate(dkprE -> dkprDestino == null || TODOS_LOS_DESTINOS.equals(dkprDestino)
                || dkprDestino.equals(dkprE.getDestino()));
        actualizarBotones();
    }

    // ------------------------------------------------------------ escaneo

    /** Acepta el código completo (ENV-000015), solo el número (15) o la etiqueta de un bulto (ENV-000015-02). */
    private static String normalizarCodigo(String dkprTexto) {
        return CodigosBarras.normalizarEnvio(dkprTexto);
    }

    /** Marca en la tabla el envío leído por la pistola de código de barras (o tipeado). */
    private void escanear(TextField dkprCampo, TableView<Envio> dkprTabla) {
        String dkprCodigo = normalizarCodigo(dkprCampo.getText());
        dkprCampo.clear();
        if (dkprCodigo.isEmpty()) {
            return;
        }
        for (Envio dkprE : dkprTabla.getItems()) {
            if (dkprE.getCodigoSeguimiento().equals(dkprCodigo)) {
                marca(dkprE).set(true);
                dkprTabla.getSelectionModel().select(dkprE);
                dkprTabla.scrollTo(dkprE);
                return;
            }
        }
        String dkprDetalle;
        try {
            Envio dkprE = dkprService.buscar(dkprCodigo);
            dkprDetalle = dkprCodigo + " no está en esta lista (estado: " + dkprE.getEstado() + ", ruta " + dkprE.getRuta() + ").";
            if (dkprTabla == dkprTablaDespachar && dkprDespachoFiltrado.getSource().stream().anyMatch(dkprX -> dkprX.getIdEnvio() == dkprE.getIdEnvio())) {
                dkprDetalle = dkprCodigo + " va a " + dkprE.getDestino() + ": cambia el filtro de destino.";
            }
        } catch (Exception dkprEx) {
            dkprDetalle = "No existe ningún envío con el código " + dkprCodigo + ".";
        }
        Alertas.advertencia(dkprDetalle);
    }

    @FXML
    private void onMarcarTodosDespacho() {
        String dkprDestino = dkprCmbDestino.getValue();
        if (dkprDestino == null || TODOS_LOS_DESTINOS.equals(dkprDestino)) {
            Alertas.advertencia("Elige primero el destino: un manifiesto agrupa envíos de un solo destino.");
            return;
        }
        dkprDespachoFiltrado.forEach(dkprE -> marca(dkprE).set(true));
    }

    @FXML
    private void onMarcarTodosRecibir() {
        dkprTablaRecibir.getItems().forEach(dkprE -> marca(dkprE).set(true));
    }

    // ------------------------------------------------------------ acciones

    @FXML
    private void onDespachar() {
        List<Envio> dkprSeleccion = marcados(dkprDespachoFiltrado);
        if (dkprSeleccion.isEmpty()) {
            Alertas.advertencia("Marca los envíos que suben al vehículo.");
            return;
        }
        String dkprDestino = dkprSeleccion.get(0).getDestino();
        if (!Alertas.confirmar("¿Despachar " + dkprSeleccion.size() + " envío(s) hacia " + dkprDestino + "?")) {
            return;
        }
        try {
            String dkprManifiesto = dkprService.despachar(dkprSeleccion, dkprTxtPlaca.getText(), dkprTxtConductor.getText(),
                    dkprTxtObsDespacho.getText(), Sesion.getActual());
            Alertas.info("Manifiesto " + dkprManifiesto + " generado: " + dkprSeleccion.size()
                    + " envío(s) EN TRANSITO hacia " + dkprDestino + ".");
            dkprTxtPlaca.clear();
            dkprTxtConductor.clear();
            dkprTxtObsDespacho.clear();
            cargar();
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onRecibir() {
        List<Envio> dkprSeleccion = marcados(dkprTablaRecibir.getItems());
        if (dkprSeleccion.isEmpty()) {
            Alertas.advertencia("Marca o escanea los paquetes que llegaron.");
            return;
        }
        int dkprFaltan = dkprTablaRecibir.getItems().size() - dkprSeleccion.size();
        String dkprMensaje = "¿Confirmar la llegada de " + dkprSeleccion.size() + " envío(s)?";
        if (dkprFaltan > 0) {
            dkprMensaje += "\n" + dkprFaltan + " envío(s) sin marcar seguirán EN TRANSITO.";
        }
        if (!Alertas.confirmar(dkprMensaje)) {
            return;
        }
        try {
            dkprService.recibir(dkprSeleccion, dkprTxtObsRecepcion.getText(), Sesion.getActual());
            Alertas.info(dkprSeleccion.size() + " envío(s) recibidos. Ya aparecen en \"Por entregar\".");
            dkprTxtObsRecepcion.clear();
            cargar();
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onEntregar() {
        Envio dkprEnvio = dkprTablaEntregar.getSelectionModel().getSelectedItem();
        if (dkprEnvio == null) {
            Alertas.advertencia("Selecciona el envío que van a recoger.");
            return;
        }
        abrirEntrega(dkprEnvio);
    }

    /** Abre la pantalla de Entregas con el envío ya cargado. */
    private void abrirEntrega(Envio dkprEnvio) {
        try {
            FXMLLoader dkprLoader = new FXMLLoader(getClass().getResource("/view/entregas.fxml"));
            Parent dkprVista = dkprLoader.load();
            Node dkprRaiz = dkprTabs.getParent();
            ((Pane) dkprRaiz.getParent()).getChildren().setAll(dkprVista);
            dkprLoader.<EntregasController>getController().cargar(dkprEnvio.getCodigoSeguimiento());
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }
}
