package com.grupo.envios.controller;

import com.grupo.envios.model.Agencia;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.service.AgenciaService;
import com.grupo.envios.service.EnvioService;
import com.grupo.envios.util.Alertas;
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
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * Trabajo diario de una agencia: lo que hay que despachar, lo que viene en camino y lo que
 * espera ser recogido. Los estados se cambian en lote, marcando (o escaneando) los paquetes.
 */
public class BandejaController {

    private static final String TODOS_LOS_DESTINOS = "Todos los destinos";

    @FXML private ComboBox<Agencia> cmbAgencia;
    @FXML private TabPane tabs;
    @FXML private Tab tabDespachar;
    @FXML private Tab tabRecibir;
    @FXML private Tab tabEntregar;

    @FXML private ComboBox<String> cmbDestino;
    @FXML private TextField txtEscanearDespacho;
    @FXML private TableView<Envio> tablaDespachar;
    @FXML private TableColumn<Envio, Boolean> colDespMarca;
    @FXML private TableColumn<Envio, String> colDespCodigo;
    @FXML private TableColumn<Envio, String> colDespDestino;
    @FXML private TableColumn<Envio, String> colDespDestinatario;
    @FXML private TableColumn<Envio, String> colDespBultos;
    @FXML private TableColumn<Envio, String> colDespPeso;
    @FXML private TableColumn<Envio, String> colDespFecha;
    @FXML private TableColumn<Envio, String> colDespAlerta;
    @FXML private TextField txtPlaca;
    @FXML private TextField txtConductor;
    @FXML private TextField txtObsDespacho;
    @FXML private Button btnDespachar;

    @FXML private TextField txtEscanearRecibir;
    @FXML private TableView<Envio> tablaRecibir;
    @FXML private TableColumn<Envio, Boolean> colRecMarca;
    @FXML private TableColumn<Envio, String> colRecCodigo;
    @FXML private TableColumn<Envio, String> colRecOrigen;
    @FXML private TableColumn<Envio, String> colRecManifiesto;
    @FXML private TableColumn<Envio, String> colRecBultos;
    @FXML private TableColumn<Envio, String> colRecSalida;
    @FXML private TableColumn<Envio, String> colRecEstimada;
    @FXML private TableColumn<Envio, String> colRecAlerta;
    @FXML private TextField txtObsRecepcion;
    @FXML private Button btnRecibir;

    @FXML private TableView<Envio> tablaEntregar;
    @FXML private TableColumn<Envio, String> colEntCodigo;
    @FXML private TableColumn<Envio, String> colEntDestinatario;
    @FXML private TableColumn<Envio, String> colEntDocumento;
    @FXML private TableColumn<Envio, String> colEntTelefono;
    @FXML private TableColumn<Envio, String> colEntOrigen;
    @FXML private TableColumn<Envio, String> colEntLlego;
    @FXML private TableColumn<Envio, String> colEntAlerta;

    private final EnvioService service = new EnvioService();
    private final AgenciaService agenciaService = new AgenciaService();

    /** Casilla marcada de cada envío (por id), compartida por las tablas con selección en lote. */
    private final Map<Integer, BooleanProperty> marcas = new HashMap<>();
    private FilteredList<Envio> despachoFiltrado = new FilteredList<>(FXCollections.observableArrayList());

    @FXML
    private void initialize() {
        configurarColumnas();
        tablaDespachar.setRowFactory(t -> filaConAlerta());
        tablaRecibir.setRowFactory(t -> filaConAlerta());
        tablaEntregar.setRowFactory(t -> {
            TableRow<Envio> fila = filaConAlerta();
            fila.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !fila.isEmpty()) {
                    abrirEntrega(fila.getItem());
                }
            });
            return fila;
        });

        cmbDestino.setOnAction(e -> filtrarDestino());
        txtEscanearDespacho.setOnAction(e -> escanear(txtEscanearDespacho, tablaDespachar));
        txtEscanearRecibir.setOnAction(e -> escanear(txtEscanearRecibir, tablaRecibir));
        cmbAgencia.setOnAction(e -> cargar());

        Empleado empleado = Sesion.getActual();
        boolean admin = Roles.ADMINISTRADOR.equals(empleado.getRol());
        if (!service.puedeOperarAlmacen(empleado, empleado.getIdAgencia())) {
            tabs.getTabs().removeAll(tabDespachar, tabRecibir);
        }
        if (!service.puedeEntregarEn(empleado, empleado.getIdAgencia())) {
            tabs.getTabs().remove(tabEntregar);
        }

        try {
            List<Agencia> agencias = agenciaService.listar(true);
            cmbAgencia.setItems(FXCollections.observableArrayList(agencias));
            agencias.stream().filter(a -> a.getIdAgencia() == empleado.getIdAgencia())
                    .findFirst().ifPresent(cmbAgencia::setValue);
        } catch (Exception e) {
            Alertas.manejar(e);
        }
        // Solo el administrador puede ver la bandeja de otra agencia.
        cmbAgencia.setDisable(!admin);
        cargar();
    }

    private void configurarColumnas() {
        colDespMarca.setCellValueFactory(c -> marca(c.getValue()));
        colDespMarca.setCellFactory(CheckBoxTableCell.forTableColumn(colDespMarca));
        texto(colDespCodigo, Envio::getCodigoSeguimiento);
        texto(colDespDestino, Envio::getDestino);
        texto(colDespDestinatario, Envio::getDestinatario);
        texto(colDespBultos, e -> String.valueOf(e.getCantidadBultos()));
        texto(colDespPeso, e -> e.getPesoKg().toPlainString());
        texto(colDespFecha, Envio::getFechaTexto);
        texto(colDespAlerta, Envio::getAlerta);

        colRecMarca.setCellValueFactory(c -> marca(c.getValue()));
        colRecMarca.setCellFactory(CheckBoxTableCell.forTableColumn(colRecMarca));
        texto(colRecCodigo, Envio::getCodigoSeguimiento);
        texto(colRecOrigen, Envio::getOrigen);
        texto(colRecManifiesto, e -> e.getManifiesto() == null ? "-" : e.getManifiesto());
        texto(colRecBultos, e -> String.valueOf(e.getCantidadBultos()));
        texto(colRecSalida, Envio::getUltimoMovimientoTexto);
        texto(colRecEstimada, Envio::getFechaEstimadaTexto);
        texto(colRecAlerta, Envio::getAlerta);

        texto(colEntCodigo, Envio::getCodigoSeguimiento);
        texto(colEntDestinatario, Envio::getDestinatario);
        texto(colEntDocumento, Envio::getDestinatarioDocumento);
        texto(colEntTelefono, e -> e.getDestinatarioTelefono() == null ? "-" : e.getDestinatarioTelefono());
        texto(colEntOrigen, Envio::getOrigen);
        texto(colEntLlego, Envio::getUltimoMovimientoTexto);
        texto(colEntAlerta, Envio::getAlerta);
    }

    private void texto(TableColumn<Envio, String> col, Function<Envio, String> valor) {
        col.setCellValueFactory(c -> new SimpleStringProperty(valor.apply(c.getValue())));
    }

    /** Pinta la fila cuando el envío está atrasado o lleva demasiado tiempo en la agencia. */
    private TableRow<Envio> filaConAlerta() {
        return new TableRow<>() {
            @Override
            protected void updateItem(Envio envio, boolean vacia) {
                super.updateItem(envio, vacia);
                getStyleClass().remove("fila-alerta");
                if (!vacia && envio != null && !envio.getAlerta().isEmpty()) {
                    getStyleClass().add("fila-alerta");
                }
            }
        };
    }

    private BooleanProperty marca(Envio e) {
        return marcas.computeIfAbsent(e.getIdEnvio(), id -> {
            BooleanProperty p = new SimpleBooleanProperty(false);
            p.addListener((obs, antes, ahora) -> actualizarBotones());
            return p;
        });
    }

    private boolean marcado(Envio e) {
        BooleanProperty p = marcas.get(e.getIdEnvio());
        return p != null && p.get();
    }

    private List<Envio> marcados(List<Envio> filas) {
        return filas.stream().filter(this::marcado).toList();
    }

    private void actualizarBotones() {
        int despacho = marcados(despachoFiltrado).size();
        int recepcion = marcados(tablaRecibir.getItems()).size();
        btnDespachar.setText(despacho == 0 ? "Despachar" : "Despachar " + despacho + " envío(s)");
        btnDespachar.setDisable(despacho == 0);
        btnRecibir.setText(recepcion == 0 ? "Confirmar llegada" : "Confirmar llegada de " + recepcion);
        btnRecibir.setDisable(recepcion == 0);
    }

    // -------------------------------------------------------------- carga

    @FXML
    private void onActualizar() {
        cargar();
    }

    private void cargar() {
        Agencia agencia = cmbAgencia.getValue();
        marcas.clear();
        if (agencia == null) {
            return;
        }
        int id = agencia.getIdAgencia();
        try {
            if (tabs.getTabs().contains(tabDespachar)) {
                List<Envio> pendientes = service.porDespachar(id);
                despachoFiltrado = new FilteredList<>(FXCollections.observableArrayList(pendientes));
                tablaDespachar.setItems(despachoFiltrado);
                List<String> destinos = pendientes.stream().map(Envio::getDestino).distinct().sorted().toList();
                cmbDestino.getItems().setAll(TODOS_LOS_DESTINOS);
                cmbDestino.getItems().addAll(destinos);
                // Con un solo destino ya queda listo para armar el manifiesto.
                cmbDestino.setValue(destinos.size() == 1 ? destinos.get(0) : TODOS_LOS_DESTINOS);
                filtrarDestino();
                tabDespachar.setText("Por despachar (" + pendientes.size() + ")");

                List<Envio> enCamino = service.porRecibir(id);
                tablaRecibir.setItems(FXCollections.observableArrayList(enCamino));
                tabRecibir.setText("Por recibir (" + enCamino.size() + ")");
            }
            if (tabs.getTabs().contains(tabEntregar)) {
                List<Envio> enAgencia = service.porEntregar(id);
                tablaEntregar.setItems(FXCollections.observableArrayList(enAgencia));
                tabEntregar.setText("Por entregar (" + enAgencia.size() + ")");
            }
        } catch (Exception e) {
            Alertas.manejar(e);
        }
        actualizarBotones();
    }

    private void filtrarDestino() {
        String destino = cmbDestino.getValue();
        // Al cambiar de destino se desmarca todo: un manifiesto va a un solo destino.
        despachoFiltrado.getSource().forEach(e -> marca(e).set(false));
        despachoFiltrado.setPredicate(e -> destino == null || TODOS_LOS_DESTINOS.equals(destino)
                || destino.equals(e.getDestino()));
        actualizarBotones();
    }

    // ------------------------------------------------------------ escaneo

    /** Acepta el código completo (ENV-000015) o solo el número (15). */
    private static String normalizarCodigo(String texto) {
        String t = texto == null ? "" : texto.trim().toUpperCase(Locale.ROOT);
        if (t.matches("\\d{1,6}")) {
            return String.format("ENV-%06d", Integer.parseInt(t));
        }
        return t;
    }

    /** Marca en la tabla el envío leído por la pistola de código de barras (o tipeado). */
    private void escanear(TextField campo, TableView<Envio> tabla) {
        String codigo = normalizarCodigo(campo.getText());
        campo.clear();
        if (codigo.isEmpty()) {
            return;
        }
        for (Envio e : tabla.getItems()) {
            if (e.getCodigoSeguimiento().equals(codigo)) {
                marca(e).set(true);
                tabla.getSelectionModel().select(e);
                tabla.scrollTo(e);
                return;
            }
        }
        String detalle;
        try {
            Envio e = service.buscar(codigo);
            detalle = codigo + " no está en esta lista (estado: " + e.getEstado() + ", ruta " + e.getRuta() + ").";
            if (tabla == tablaDespachar && despachoFiltrado.getSource().stream().anyMatch(x -> x.getIdEnvio() == e.getIdEnvio())) {
                detalle = codigo + " va a " + e.getDestino() + ": cambia el filtro de destino.";
            }
        } catch (Exception ex) {
            detalle = "No existe ningún envío con el código " + codigo + ".";
        }
        Alertas.advertencia(detalle);
    }

    @FXML
    private void onMarcarTodosDespacho() {
        String destino = cmbDestino.getValue();
        if (destino == null || TODOS_LOS_DESTINOS.equals(destino)) {
            Alertas.advertencia("Elige primero el destino: un manifiesto agrupa envíos de un solo destino.");
            return;
        }
        despachoFiltrado.forEach(e -> marca(e).set(true));
    }

    @FXML
    private void onMarcarTodosRecibir() {
        tablaRecibir.getItems().forEach(e -> marca(e).set(true));
    }

    // ------------------------------------------------------------ acciones

    @FXML
    private void onDespachar() {
        List<Envio> seleccion = marcados(despachoFiltrado);
        if (seleccion.isEmpty()) {
            Alertas.advertencia("Marca los envíos que suben al vehículo.");
            return;
        }
        String destino = seleccion.get(0).getDestino();
        if (!Alertas.confirmar("¿Despachar " + seleccion.size() + " envío(s) hacia " + destino + "?")) {
            return;
        }
        try {
            String manifiesto = service.despachar(seleccion, txtPlaca.getText(), txtConductor.getText(),
                    txtObsDespacho.getText(), Sesion.getActual());
            Alertas.info("Manifiesto " + manifiesto + " generado: " + seleccion.size()
                    + " envío(s) EN TRANSITO hacia " + destino + ".");
            txtPlaca.clear();
            txtConductor.clear();
            txtObsDespacho.clear();
            cargar();
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    @FXML
    private void onRecibir() {
        List<Envio> seleccion = marcados(tablaRecibir.getItems());
        if (seleccion.isEmpty()) {
            Alertas.advertencia("Marca o escanea los paquetes que llegaron.");
            return;
        }
        int faltan = tablaRecibir.getItems().size() - seleccion.size();
        String mensaje = "¿Confirmar la llegada de " + seleccion.size() + " envío(s)?";
        if (faltan > 0) {
            mensaje += "\n" + faltan + " envío(s) sin marcar seguirán EN TRANSITO.";
        }
        if (!Alertas.confirmar(mensaje)) {
            return;
        }
        try {
            service.recibir(seleccion, txtObsRecepcion.getText(), Sesion.getActual());
            Alertas.info(seleccion.size() + " envío(s) recibidos. Ya aparecen en \"Por entregar\".");
            txtObsRecepcion.clear();
            cargar();
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    @FXML
    private void onEntregar() {
        Envio envio = tablaEntregar.getSelectionModel().getSelectedItem();
        if (envio == null) {
            Alertas.advertencia("Selecciona el envío que van a recoger.");
            return;
        }
        abrirEntrega(envio);
    }

    /** Abre la pantalla de Entregas con el envío ya cargado. */
    private void abrirEntrega(Envio envio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/entregas.fxml"));
            Parent vista = loader.load();
            Node raiz = tabs.getParent();
            ((Pane) raiz.getParent()).getChildren().setAll(vista);
            loader.<EntregasController>getController().cargar(envio.getCodigoSeguimiento());
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }
}
