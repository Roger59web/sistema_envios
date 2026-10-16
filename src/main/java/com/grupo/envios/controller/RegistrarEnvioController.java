package com.grupo.envios.controller;

import com.grupo.envios.model.Cliente;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.ResultadoRegistro;
import com.grupo.envios.model.Ruta;
import com.grupo.envios.model.SolicitudEnvio;
import com.grupo.envios.service.ClienteService;
import com.grupo.envios.service.EnvioService;
import com.grupo.envios.service.RutaService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Sesion;
import com.grupo.envios.util.Validaciones;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class RegistrarEnvioController {

    @FXML private Label lblOrigen;
    @FXML private Label lblCosto;
    @FXML private ComboBox<Ruta> cmbRuta;

    @FXML private ComboBox<String> cmbRemTipo;
    @FXML private TextField txtRemDoc;
    @FXML private TextField txtRemNombre;
    @FXML private TextField txtRemTelefono;

    @FXML private ComboBox<String> cmbDesTipo;
    @FXML private TextField txtDesDoc;
    @FXML private TextField txtDesNombre;
    @FXML private TextField txtDesTelefono;

    @FXML private TextField txtDescripcion;
    @FXML private TextField txtBultos;
    @FXML private TextField txtPeso;
    @FXML private TextField txtValor;
    @FXML private TextField txtObservaciones;

    @FXML private ComboBox<String> cmbComprobante;
    @FXML private ComboBox<String> cmbMetodo;

    private final EnvioService envioService = new EnvioService();
    private final ClienteService clienteService = new ClienteService();
    private final RutaService rutaService = new RutaService();

    @FXML
    private void initialize() {
        Empleado empleado = Sesion.getActual();
        lblOrigen.setText("Agencia de origen: " + empleado.getAgencia());

        try {
            var rutas = rutaService.listarPorOrigen(empleado.getIdAgencia());
            cmbRuta.setItems(FXCollections.observableArrayList(rutas));
            if (rutas.isEmpty()) {
                lblOrigen.setText(lblOrigen.getText() + "  (no hay rutas activas con tarifa desde esta agencia)");
            }
        } catch (Exception e) {
            Alertas.manejar(e);
        }

        var tipos = FXCollections.observableArrayList("DNI", "RUC", "CE");
        cmbRemTipo.setItems(tipos);
        cmbDesTipo.setItems(FXCollections.observableArrayList(tipos));
        cmbComprobante.setItems(FXCollections.observableArrayList("BOLETA", "FACTURA"));
        cmbMetodo.setItems(FXCollections.observableArrayList("EFECTIVO", "TARJETA", "YAPE", "PLIN"));

        cmbRuta.valueProperty().addListener((obs, anterior, nueva) -> actualizarCosto());
        txtPeso.textProperty().addListener((obs, anterior, nuevo) -> actualizarCosto());

        limpiar();
    }

    private void actualizarCosto() {
        try {
            BigDecimal peso = Validaciones.decimal(txtPeso.getText(), "Peso");
            lblCosto.setText("S/ " + envioService.calcularCosto(cmbRuta.getValue(), peso).toPlainString());
        } catch (Exception e) {
            lblCosto.setText("S/ --");
        }
    }

    private void limpiar() {
        cmbRuta.setValue(null);
        cmbRemTipo.setValue("DNI");
        cmbDesTipo.setValue("DNI");
        txtRemDoc.clear();
        txtRemNombre.clear();
        txtRemTelefono.clear();
        txtDesDoc.clear();
        txtDesNombre.clear();
        txtDesTelefono.clear();
        txtDescripcion.clear();
        txtBultos.setText("1");
        txtPeso.clear();
        txtValor.clear();
        txtObservaciones.clear();
        cmbComprobante.setValue("BOLETA");
        cmbMetodo.setValue("EFECTIVO");
        lblCosto.setText("S/ --");
    }

    private void buscarCliente(ComboBox<String> tipo, TextField doc, TextField nombre, TextField telefono) {
        try {
            Cliente c = clienteService.buscarPorDocumento(tipo.getValue(), doc.getText());
            if (c == null) {
                nombre.clear();
                telefono.clear();
                Alertas.info("Cliente nuevo: completa su nombre y teléfono. Se registrará junto con el envío.");
            } else {
                nombre.setText(c.getNombreCompleto());
                telefono.setText(c.getTelefono());
            }
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }

    @FXML
    private void onBuscarRemitente() {
        buscarCliente(cmbRemTipo, txtRemDoc, txtRemNombre, txtRemTelefono);
    }

    @FXML
    private void onBuscarDestinatario() {
        buscarCliente(cmbDesTipo, txtDesDoc, txtDesNombre, txtDesTelefono);
    }

    @FXML
    private void onLimpiar() {
        limpiar();
    }

    @FXML
    private void onRegistrar() {
        try {
            BigDecimal valor = Validaciones.vacio(txtValor.getText())
                    ? BigDecimal.ZERO
                    : Validaciones.decimal(txtValor.getText(), "Valor declarado");

            SolicitudEnvio solicitud = new SolicitudEnvio(
                    cmbRuta.getValue(),
                    cmbRemTipo.getValue(), txtRemDoc.getText(), txtRemNombre.getText(), txtRemTelefono.getText(),
                    cmbDesTipo.getValue(), txtDesDoc.getText(), txtDesNombre.getText(), txtDesTelefono.getText(),
                    txtDescripcion.getText(),
                    Validaciones.entero(txtBultos.getText(), "Bultos"),
                    Validaciones.decimal(txtPeso.getText(), "Peso (kg)"),
                    valor,
                    txtObservaciones.getText(),
                    cmbComprobante.getValue(),
                    cmbMetodo.getValue());

            ResultadoRegistro resultado = envioService.registrar(solicitud, Sesion.getActual());
            Alertas.info("Envío registrado correctamente.\n\n"
                    + "Código de seguimiento: " + resultado.codigoSeguimiento() + "\n"
                    + "Comprobante: " + resultado.comprobante() + "\n"
                    + "Total cobrado: S/ " + resultado.total().toPlainString());
            limpiar();
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }
}
