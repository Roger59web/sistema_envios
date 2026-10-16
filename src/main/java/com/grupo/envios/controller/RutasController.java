package com.grupo.envios.controller;

import com.grupo.envios.model.Agencia;
import com.grupo.envios.model.Ruta;
import com.grupo.envios.service.AgenciaService;
import com.grupo.envios.service.RutaService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Validaciones;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;

public class RutasController {

    @FXML private TableView<Ruta> tabla;
    @FXML private TableColumn<Ruta, String> colOrigen;
    @FXML private TableColumn<Ruta, String> colDestino;
    @FXML private TableColumn<Ruta, Integer> colDias;
    @FXML private TableColumn<Ruta, BigDecimal> colBase;
    @FXML private TableColumn<Ruta, BigDecimal> colExtra;
    @FXML private TableColumn<Ruta, BigDecimal> colPesoMax;
    @FXML private TableColumn<Ruta, String> colEstado;

    @FXML private Label lblModo;
    @FXML private ComboBox<Agencia> cmbOrigen;
    @FXML private ComboBox<Agencia> cmbDestino;
    @FXML private TextField txtDias;
    @FXML private TextField txtPrecioBase;
    @FXML private TextField txtPrecioKg;
    @FXML private TextField txtPesoMax;
    @FXML private CheckBox chkActivo;

    private final RutaService rutaService = new RutaService();
    private final AgenciaService agenciaService = new AgenciaService();
    private Ruta seleccionada;

    @FXML
    private void initialize() {
        colOrigen.setCellValueFactory(new PropertyValueFactory<>("origen"));
        colDestino.setCellValueFactory(new PropertyValueFactory<>("destino"));
        colDias.setCellValueFactory(new PropertyValueFactory<>("diasEstimados"));
        colBase.setCellValueFactory(new PropertyValueFactory<>("precioBase"));
        colExtra.setCellValueFactory(new PropertyValueFactory<>("precioKgExtra"));
        colPesoMax.setCellValueFactory(new PropertyValueFactory<>("pesoMaxKg"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));

        try {
            var agencias = FXCollections.observableArrayList(agenciaService.listar(false));
            cmbOrigen.setItems(agencias);
            cmbDestino.setItems(agencias);
        } catch (Exception e) {
            Alertas.manejar(e);
        }

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nueva) -> {
            if (nueva != null) {
                mostrar(nueva);
            }
        });

        limpiar();
        cargar();
    }

    private void cargar() {
        try {
            tabla.setItems(FXCollections.observableArrayList(rutaService.listar()));
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    private void mostrar(Ruta r) {
        seleccionada = r;
        lblModo.setText("Editando ruta (el origen y el destino no se pueden cambiar)");
        seleccionarAgencia(cmbOrigen, r.getIdAgenciaOrigen());
        seleccionarAgencia(cmbDestino, r.getIdAgenciaDestino());
        cmbOrigen.setDisable(true);
        cmbDestino.setDisable(true);
        txtDias.setText(String.valueOf(r.getDiasEstimados()));
        txtPrecioBase.setText(r.getPrecioBase() == null ? "" : r.getPrecioBase().toPlainString());
        txtPrecioKg.setText(r.getPrecioKgExtra() == null ? "" : r.getPrecioKgExtra().toPlainString());
        txtPesoMax.setText(r.getPesoMaxKg() == null ? "" : r.getPesoMaxKg().toPlainString());
        chkActivo.setSelected(r.isActivo());
    }

    private void seleccionarAgencia(ComboBox<Agencia> combo, int idAgencia) {
        for (Agencia a : combo.getItems()) {
            if (a.getIdAgencia() == idAgencia) {
                combo.setValue(a);
                return;
            }
        }
        combo.setValue(null);
    }

    private void limpiar() {
        seleccionada = null;
        tabla.getSelectionModel().clearSelection();
        lblModo.setText("Nueva ruta");
        cmbOrigen.setDisable(false);
        cmbDestino.setDisable(false);
        cmbOrigen.setValue(null);
        cmbDestino.setValue(null);
        txtDias.setText("1");
        txtPrecioBase.clear();
        txtPrecioKg.clear();
        txtPesoMax.setText("50");
        chkActivo.setSelected(true);
    }

    @FXML
    private void onNuevo() {
        limpiar();
    }

    @FXML
    private void onGuardar() {
        try {
            Ruta r = new Ruta();
            r.setIdRuta(seleccionada == null ? 0 : seleccionada.getIdRuta());
            r.setIdTarifa(seleccionada == null ? 0 : seleccionada.getIdTarifa());
            r.setIdAgenciaOrigen(cmbOrigen.getValue() == null ? 0 : cmbOrigen.getValue().getIdAgencia());
            r.setIdAgenciaDestino(cmbDestino.getValue() == null ? 0 : cmbDestino.getValue().getIdAgencia());
            r.setDiasEstimados(Validaciones.entero(txtDias.getText(), "Días estimados"));
            r.setPrecioBase(Validaciones.decimal(txtPrecioBase.getText(), "Precio base"));
            r.setPrecioKgExtra(Validaciones.decimal(txtPrecioKg.getText(), "Precio por kg extra"));
            r.setPesoMaxKg(Validaciones.decimal(txtPesoMax.getText(), "Peso máximo"));
            r.setActivo(chkActivo.isSelected());

            rutaService.guardar(r);
            Alertas.info("Ruta guardada correctamente.");
            limpiar();
            cargar();
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }
}
