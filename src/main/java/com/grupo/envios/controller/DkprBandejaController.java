package com.grupo.envios.controller;

import com.grupo.envios.model.DkprAgencia;
import com.grupo.envios.model.DkprConductor;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.model.DkprVehiculo;
import com.grupo.envios.service.DkprAgenciaService;
import com.grupo.envios.service.DkprEnvioService;
import com.grupo.envios.service.DkprVehiculoService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprCodigosBarras;
import com.grupo.envios.util.DkprRoles;
import com.grupo.envios.util.DkprSesion;
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

public class DkprBandejaController {

    private static final String TODOS_LOS_DESTINOS = "Todos los destinos";

    @FXML private ComboBox<DkprAgencia> dkprCmbAgencia;
    @FXML private TabPane dkprTabs;
    @FXML private Tab dkprTabDespachar;
    @FXML private Tab dkprTabRecibir;
    @FXML private Tab dkprTabEntregar;

    @FXML private ComboBox<String> dkprCmbDestino;
    @FXML private TextField dkprTxtEscanearDespacho;
    @FXML private TableView<DkprEnvio> dkprTablaDespachar;
    @FXML private TableColumn<DkprEnvio, Boolean> dkprColDespMarca;
    @FXML private TableColumn<DkprEnvio, String> dkprColDespCodigo;
    @FXML private TableColumn<DkprEnvio, String> dkprColDespDestino;
    @FXML private TableColumn<DkprEnvio, String> dkprColDespDestinatario;
    @FXML private TableColumn<DkprEnvio, String> dkprColDespBultos;
    @FXML private TableColumn<DkprEnvio, String> dkprColDespPeso;
    @FXML private TableColumn<DkprEnvio, String> dkprColDespFecha;
    @FXML private TableColumn<DkprEnvio, String> dkprColDespAlerta;
    @FXML private ComboBox<DkprVehiculo> dkprCmbVehiculo;
    @FXML private ComboBox<DkprConductor> dkprCmbConductor;
    @FXML private TextField dkprTxtObsDespacho;
    @FXML private Button dkprBtnDespachar;

    @FXML private TextField dkprTxtEscanearRecibir;
    @FXML private TableView<DkprEnvio> dkprTablaRecibir;
    @FXML private TableColumn<DkprEnvio, Boolean> dkprColRecMarca;
    @FXML private TableColumn<DkprEnvio, String> dkprColRecCodigo;
    @FXML private TableColumn<DkprEnvio, String> dkprColRecOrigen;
    @FXML private TableColumn<DkprEnvio, String> dkprColRecManifiesto;
    @FXML private TableColumn<DkprEnvio, String> dkprColRecVehiculo;
    @FXML private TableColumn<DkprEnvio, String> dkprColRecBultos;
    @FXML private TableColumn<DkprEnvio, String> dkprColRecSalida;
    @FXML private TableColumn<DkprEnvio, String> dkprColRecEstimada;
    @FXML private TableColumn<DkprEnvio, String> dkprColRecAlerta;
    @FXML private TextField dkprTxtObsRecepcion;
    @FXML private Button dkprBtnRecibir;

    @FXML private TableView<DkprEnvio> dkprTablaEntregar;
    @FXML private TableColumn<DkprEnvio, String> dkprColEntCodigo;
    @FXML private TableColumn<DkprEnvio, String> dkprColEntDestinatario;
    @FXML private TableColumn<DkprEnvio, String> dkprColEntDocumento;
    @FXML private TableColumn<DkprEnvio, String> dkprColEntTelefono;
    @FXML private TableColumn<DkprEnvio, String> dkprColEntOrigen;
    @FXML private TableColumn<DkprEnvio, String> dkprColEntLlego;
    @FXML private TableColumn<DkprEnvio, String> dkprColEntAlerta;

    private final DkprEnvioService dkprService = new DkprEnvioService();
    private final DkprAgenciaService dkprAgenciaService = new DkprAgenciaService();
    private final DkprVehiculoService dkprVehiculoService = new DkprVehiculoService();

    private final Map<Integer, BooleanProperty> dkprMarcas = new HashMap<>();
    private FilteredList<DkprEnvio> dkprDespachoFiltrado = new FilteredList<>(FXCollections.observableArrayList());

    @FXML
    private void initialize() {
        dkprConfigurarColumnas();
        dkprTablaDespachar.setRowFactory(dkprT -> dkprFilaConAlerta());
        dkprTablaRecibir.setRowFactory(dkprT -> dkprFilaConAlerta());
        dkprTablaEntregar.setRowFactory(dkprT -> {
            TableRow<DkprEnvio> dkprFila = dkprFilaConAlerta();
            dkprFila.setOnMouseClicked(dkprEv -> {
                if (dkprEv.getClickCount() == 2 && !dkprFila.isEmpty()) {
                    dkprAbrirEntrega(dkprFila.getItem());
                }
            });
            return dkprFila;
        });

        dkprCmbDestino.setOnAction(dkprE -> dkprFiltrarDestino());
        dkprTxtEscanearDespacho.setOnAction(dkprE -> dkprEscanear(dkprTxtEscanearDespacho, dkprTablaDespachar));
        dkprTxtEscanearRecibir.setOnAction(dkprE -> dkprEscanear(dkprTxtEscanearRecibir, dkprTablaRecibir));
        dkprCmbAgencia.setOnAction(dkprE -> dkprCargar());

        DkprEmpleado dkprEmpleado = DkprSesion.dkprGetActual();
        boolean dkprAdmin = DkprRoles.ADMINISTRADOR.equals(dkprEmpleado.dkprGetRol());
        if (!dkprService.dkprPuedeOperarAlmacen(dkprEmpleado, dkprEmpleado.dkprGetIdAgencia())) {
            dkprTabs.getTabs().removeAll(dkprTabDespachar, dkprTabRecibir);
        }
        if (!dkprService.dkprPuedeEntregarEn(dkprEmpleado, dkprEmpleado.dkprGetIdAgencia())) {
            dkprTabs.getTabs().remove(dkprTabEntregar);
        }

        try {
            List<DkprAgencia> dkprAgencias = dkprAgenciaService.dkprListar(true);
            dkprCmbAgencia.setItems(FXCollections.observableArrayList(dkprAgencias));
            dkprAgencias.stream().filter(dkprA -> dkprA.dkprGetIdAgencia() == dkprEmpleado.dkprGetIdAgencia())
                    .findFirst().ifPresent(dkprCmbAgencia::setValue);
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
        dkprCmbAgencia.setDisable(!dkprAdmin);
        if (dkprTabs.getTabs().contains(dkprTabDespachar)) {
            dkprCargarVehiculos();
        }
        dkprCargar();
    }

    private void dkprCargarVehiculos() {
        try {
            dkprCmbVehiculo.setItems(FXCollections.observableArrayList(dkprVehiculoService.dkprListarVehiculos(true)));
            dkprCmbConductor.setItems(FXCollections.observableArrayList(dkprVehiculoService.dkprListarConductores(true)));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprConfigurarColumnas() {
        dkprColDespMarca.setCellValueFactory(dkprC -> dkprMarca(dkprC.getValue()));
        dkprColDespMarca.setCellFactory(CheckBoxTableCell.forTableColumn(dkprColDespMarca));
        dkprTexto(dkprColDespCodigo, DkprEnvio::dkprGetCodigoSeguimiento);
        dkprTexto(dkprColDespDestino, DkprEnvio::dkprGetDestino);
        dkprTexto(dkprColDespDestinatario, DkprEnvio::dkprGetDestinatario);
        dkprTexto(dkprColDespBultos, dkprE -> String.valueOf(dkprE.dkprGetCantidadBultos()));
        dkprTexto(dkprColDespPeso, dkprE -> dkprE.dkprGetPesoKg().toPlainString());
        dkprTexto(dkprColDespFecha, DkprEnvio::dkprGetFechaTexto);
        dkprTexto(dkprColDespAlerta, DkprEnvio::dkprGetAlerta);

        dkprColRecMarca.setCellValueFactory(dkprC -> dkprMarca(dkprC.getValue()));
        dkprColRecMarca.setCellFactory(CheckBoxTableCell.forTableColumn(dkprColRecMarca));
        dkprTexto(dkprColRecCodigo, DkprEnvio::dkprGetCodigoSeguimiento);
        dkprTexto(dkprColRecOrigen, DkprEnvio::dkprGetOrigen);
        dkprTexto(dkprColRecManifiesto, dkprE -> dkprE.dkprGetManifiesto() == null ? "-" : dkprE.dkprGetManifiesto());
        dkprTexto(dkprColRecVehiculo, dkprE -> dkprE.dkprGetPlaca() == null ? "-" : dkprE.dkprGetPlaca());
        dkprTexto(dkprColRecBultos, dkprE -> String.valueOf(dkprE.dkprGetCantidadBultos()));
        dkprTexto(dkprColRecSalida, DkprEnvio::dkprGetUltimoMovimientoTexto);
        dkprTexto(dkprColRecEstimada, DkprEnvio::dkprGetFechaEstimadaTexto);
        dkprTexto(dkprColRecAlerta, DkprEnvio::dkprGetAlerta);

        dkprTexto(dkprColEntCodigo, DkprEnvio::dkprGetCodigoSeguimiento);
        dkprTexto(dkprColEntDestinatario, DkprEnvio::dkprGetDestinatario);
        dkprTexto(dkprColEntDocumento, DkprEnvio::dkprGetDestinatarioDocumento);
        dkprTexto(dkprColEntTelefono, dkprE -> dkprE.dkprGetDestinatarioTelefono() == null ? "-" : dkprE.dkprGetDestinatarioTelefono());
        dkprTexto(dkprColEntOrigen, DkprEnvio::dkprGetOrigen);
        dkprTexto(dkprColEntLlego, DkprEnvio::dkprGetUltimoMovimientoTexto);
        dkprTexto(dkprColEntAlerta, DkprEnvio::dkprGetAlerta);
    }

    private void dkprTexto(TableColumn<DkprEnvio, String> dkprCol, Function<DkprEnvio, String> dkprValor) {
        dkprCol.setCellValueFactory(dkprC -> new SimpleStringProperty(dkprValor.apply(dkprC.getValue())));
    }

    private TableRow<DkprEnvio> dkprFilaConAlerta() {
        return new TableRow<>() {
            @Override
            protected void updateItem(DkprEnvio dkprEnvio, boolean dkprVacia) {
                super.updateItem(dkprEnvio, dkprVacia);
                getStyleClass().remove("fila-alerta");
                if (!dkprVacia && dkprEnvio != null && !dkprEnvio.dkprGetAlerta().isEmpty()) {
                    getStyleClass().add("fila-alerta");
                }
            }
        };
    }

    private BooleanProperty dkprMarca(DkprEnvio dkprE) {
        return dkprMarcas.computeIfAbsent(dkprE.dkprGetIdEnvio(), dkprId -> {
            BooleanProperty dkprP = new SimpleBooleanProperty(false);
            dkprP.addListener((dkprObs, dkprAntes, dkprAhora) -> dkprActualizarBotones());
            return dkprP;
        });
    }

    private boolean dkprMarcado(DkprEnvio dkprE) {
        BooleanProperty dkprP = dkprMarcas.get(dkprE.dkprGetIdEnvio());
        return dkprP != null && dkprP.get();
    }

    private List<DkprEnvio> dkprMarcados(List<DkprEnvio> dkprFilas) {
        return dkprFilas.stream().filter(this::dkprMarcado).toList();
    }

    private void dkprActualizarBotones() {
        int dkprDespacho = dkprMarcados(dkprDespachoFiltrado).size();
        int dkprRecepcion = dkprMarcados(dkprTablaRecibir.getItems()).size();
        dkprBtnDespachar.setText(dkprDespacho == 0 ? "Despachar" : "Despachar " + dkprDespacho + " envío(s)");
        dkprBtnDespachar.setDisable(dkprDespacho == 0);
        dkprBtnRecibir.setText(dkprRecepcion == 0 ? "Confirmar llegada" : "Confirmar llegada de " + dkprRecepcion);
        dkprBtnRecibir.setDisable(dkprRecepcion == 0);
    }

    @FXML
    private void dkprOnActualizar() {
        dkprCargar();
    }

    private void dkprCargar() {
        DkprAgencia dkprAgencia = dkprCmbAgencia.getValue();
        dkprMarcas.clear();
        if (dkprAgencia == null) {
            return;
        }
        int dkprId = dkprAgencia.dkprGetIdAgencia();
        try {
            if (dkprTabs.getTabs().contains(dkprTabDespachar)) {
                List<DkprEnvio> dkprPendientes = dkprService.dkprPorDespachar(dkprId);
                dkprDespachoFiltrado = new FilteredList<>(FXCollections.observableArrayList(dkprPendientes));
                dkprTablaDespachar.setItems(dkprDespachoFiltrado);
                List<String> dkprDestinos = dkprPendientes.stream().map(DkprEnvio::dkprGetDestino).distinct().sorted().toList();
                dkprCmbDestino.getItems().setAll(TODOS_LOS_DESTINOS);
                dkprCmbDestino.getItems().addAll(dkprDestinos);
                dkprCmbDestino.setValue(dkprDestinos.size() == 1 ? dkprDestinos.get(0) : TODOS_LOS_DESTINOS);
                dkprFiltrarDestino();
                dkprTabDespachar.setText("Por despachar (" + dkprPendientes.size() + ")");

                List<DkprEnvio> dkprEnCamino = dkprService.dkprPorRecibir(dkprId);
                dkprTablaRecibir.setItems(FXCollections.observableArrayList(dkprEnCamino));
                dkprTabRecibir.setText("Por recibir (" + dkprEnCamino.size() + ")");
            }
            if (dkprTabs.getTabs().contains(dkprTabEntregar)) {
                List<DkprEnvio> dkprEnAgencia = dkprService.dkprPorEntregar(dkprId);
                dkprTablaEntregar.setItems(FXCollections.observableArrayList(dkprEnAgencia));
                dkprTabEntregar.setText("Por entregar (" + dkprEnAgencia.size() + ")");
            }
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
        dkprActualizarBotones();
    }

    private void dkprFiltrarDestino() {
        String dkprDestino = dkprCmbDestino.getValue();
        dkprDespachoFiltrado.getSource().forEach(dkprE -> dkprMarca(dkprE).set(false));
        dkprDespachoFiltrado.setPredicate(dkprE -> dkprDestino == null || TODOS_LOS_DESTINOS.equals(dkprDestino)
                || dkprDestino.equals(dkprE.dkprGetDestino()));
        dkprActualizarBotones();
    }

    private static String dkprNormalizarCodigo(String dkprTexto) {
        return DkprCodigosBarras.dkprNormalizarEnvio(dkprTexto);
    }

    private void dkprEscanear(TextField dkprCampo, TableView<DkprEnvio> dkprTabla) {
        String dkprCodigo = dkprNormalizarCodigo(dkprCampo.getText());
        dkprCampo.clear();
        if (dkprCodigo.isEmpty()) {
            return;
        }
        for (DkprEnvio dkprE : dkprTabla.getItems()) {
            if (dkprE.dkprGetCodigoSeguimiento().equals(dkprCodigo)) {
                dkprMarca(dkprE).set(true);
                dkprTabla.getSelectionModel().select(dkprE);
                dkprTabla.scrollTo(dkprE);
                return;
            }
        }
        String dkprDetalle;
        try {
            DkprEnvio dkprE = dkprService.dkprBuscar(dkprCodigo);
            dkprDetalle = dkprCodigo + " no está en esta lista (estado: " + dkprE.dkprGetEstado() + ", ruta " + dkprE.dkprGetRuta() + ").";
            if (dkprTabla == dkprTablaDespachar && dkprDespachoFiltrado.getSource().stream().anyMatch(dkprX -> dkprX.dkprGetIdEnvio() == dkprE.dkprGetIdEnvio())) {
                dkprDetalle = dkprCodigo + " va a " + dkprE.dkprGetDestino() + ": cambia el filtro de destino.";
            }
        } catch (Exception dkprEx) {
            dkprDetalle = "No existe ningún envío con el código " + dkprCodigo + ".";
        }
        DkprAlertas.dkprAdvertencia(dkprDetalle);
    }

    @FXML
    private void dkprOnMarcarTodosDespacho() {
        String dkprDestino = dkprCmbDestino.getValue();
        if (dkprDestino == null || TODOS_LOS_DESTINOS.equals(dkprDestino)) {
            DkprAlertas.dkprAdvertencia("Elige primero el destino: un manifiesto agrupa envíos de un solo destino.");
            return;
        }
        dkprDespachoFiltrado.forEach(dkprE -> dkprMarca(dkprE).set(true));
    }

    @FXML
    private void dkprOnMarcarTodosRecibir() {
        dkprTablaRecibir.getItems().forEach(dkprE -> dkprMarca(dkprE).set(true));
    }

    @FXML
    private void dkprOnDespachar() {
        List<DkprEnvio> dkprSeleccion = dkprMarcados(dkprDespachoFiltrado);
        if (dkprSeleccion.isEmpty()) {
            DkprAlertas.dkprAdvertencia("Marca los envíos que suben al vehículo.");
            return;
        }
        String dkprDestino = dkprSeleccion.get(0).dkprGetDestino();
        if (!DkprAlertas.dkprConfirmar("¿Despachar " + dkprSeleccion.size() + " envío(s) hacia " + dkprDestino + "?")) {
            return;
        }
        try {
            String dkprManifiesto = dkprService.dkprDespachar(dkprSeleccion,
                    dkprCmbVehiculo.getValue() == null ? null : dkprCmbVehiculo.getValue().dkprGetIdVehiculo(),
                    dkprCmbConductor.getValue() == null ? null : dkprCmbConductor.getValue().dkprGetIdConductor(),
                    dkprTxtObsDespacho.getText(), DkprSesion.dkprGetActual());
            DkprAlertas.dkprInfo("Manifiesto " + dkprManifiesto + " generado: " + dkprSeleccion.size()
                    + " envío(s) EN TRANSITO hacia " + dkprDestino + ".");
            dkprCmbVehiculo.setValue(null);
            dkprCmbConductor.setValue(null);
            dkprTxtObsDespacho.clear();
            dkprCargar();
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnRecibir() {
        List<DkprEnvio> dkprSeleccion = dkprMarcados(dkprTablaRecibir.getItems());
        if (dkprSeleccion.isEmpty()) {
            DkprAlertas.dkprAdvertencia("Marca o escanea los paquetes que llegaron.");
            return;
        }
        int dkprFaltan = dkprTablaRecibir.getItems().size() - dkprSeleccion.size();
        String dkprMensaje = "¿Confirmar la llegada de " + dkprSeleccion.size() + " envío(s)?";
        if (dkprFaltan > 0) {
            dkprMensaje += "\n" + dkprFaltan + " envío(s) sin marcar seguirán EN TRANSITO.";
        }
        if (!DkprAlertas.dkprConfirmar(dkprMensaje)) {
            return;
        }
        try {
            dkprService.dkprRecibir(dkprSeleccion, dkprTxtObsRecepcion.getText(), DkprSesion.dkprGetActual());
            DkprAlertas.dkprInfo(dkprSeleccion.size() + " envío(s) recibidos. Ya aparecen en \"Por entregar\".");
            dkprTxtObsRecepcion.clear();
            dkprCargar();
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnEntregar() {
        DkprEnvio dkprEnvio = dkprTablaEntregar.getSelectionModel().getSelectedItem();
        if (dkprEnvio == null) {
            DkprAlertas.dkprAdvertencia("Selecciona el envío que van a recoger.");
            return;
        }
        dkprAbrirEntrega(dkprEnvio);
    }

    private void dkprAbrirEntrega(DkprEnvio dkprEnvio) {
        try {
            FXMLLoader dkprLoader = new FXMLLoader(getClass().getResource("/view/entregas.fxml"));
            Parent dkprVista = dkprLoader.load();
            Node dkprRaiz = dkprTabs.getParent();
            ((Pane) dkprRaiz.getParent()).getChildren().setAll(dkprVista);
            dkprLoader.<DkprEntregasController>getController().dkprCargar(dkprEnvio.dkprGetCodigoSeguimiento());
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }
}
