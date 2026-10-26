package com.grupo.envios.controller;

import com.grupo.envios.model.Cliente;
import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.ResultadoRegistro;
import com.grupo.envios.model.Ruta;
import com.grupo.envios.model.SolicitudEnvio;
import com.grupo.envios.service.CajaService;
import com.grupo.envios.service.ClienteService;
import com.grupo.envios.service.EnvioService;
import com.grupo.envios.service.RutaService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Formato;
import com.grupo.envios.util.LectorDni;
import com.grupo.envios.util.Sesion;
import com.grupo.envios.util.Validaciones;
import com.grupo.envios.util.VistaTicket;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class RegistrarEnvioController {

    @FXML private Label dkprLblOrigen;
    @FXML private Label dkprLblCaja;
    @FXML private Label dkprLblCosto;
    @FXML private ComboBox<Ruta> dkprCmbRuta;

    @FXML private ComboBox<String> dkprCmbRemTipo;
    @FXML private TextField dkprTxtRemDoc;
    @FXML private TextField dkprTxtRemNombre;
    @FXML private TextField dkprTxtRemTelefono;

    @FXML private ComboBox<String> dkprCmbDesTipo;
    @FXML private TextField dkprTxtDesDoc;
    @FXML private TextField dkprTxtDesNombre;
    @FXML private TextField dkprTxtDesTelefono;

    @FXML private TextField dkprTxtDescripcion;
    @FXML private TextField dkprTxtBultos;
    @FXML private TextField dkprTxtPeso;
    @FXML private TextField dkprTxtValor;
    @FXML private TextField dkprTxtObservaciones;

    @FXML private ComboBox<String> dkprCmbComprobante;
    @FXML private ComboBox<String> dkprCmbMetodo;

    private final EnvioService dkprEnvioService = new EnvioService();
    private final ClienteService dkprClienteService = new ClienteService();
    private final RutaService dkprRutaService = new RutaService();
    private final CajaService dkprCajaService = new CajaService();

    @FXML
    private void initialize() {
        Empleado dkprEmpleado = Sesion.getActual();
        dkprLblOrigen.setText("Agencia de origen: " + dkprEmpleado.getAgencia());
        avisarCaja(dkprEmpleado);

        try {
            var dkprRutas = dkprRutaService.listarPorOrigen(dkprEmpleado.getIdAgencia());
            dkprCmbRuta.setItems(FXCollections.observableArrayList(dkprRutas));
            if (dkprRutas.isEmpty()) {
                dkprLblOrigen.setText(dkprLblOrigen.getText() + "  (no hay rutas activas con tarifa desde esta agencia)");
            }
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }

        var dkprTipos = FXCollections.observableArrayList("DNI", "RUC", "CE");
        dkprCmbRemTipo.setItems(dkprTipos);
        dkprCmbDesTipo.setItems(FXCollections.observableArrayList(dkprTipos));
        dkprCmbComprobante.setItems(FXCollections.observableArrayList("BOLETA", "FACTURA"));
        dkprCmbMetodo.setItems(FXCollections.observableArrayList("EFECTIVO", "TARJETA", "YAPE", "PLIN"));

        dkprCmbRuta.valueProperty().addListener((dkprObs, dkprAnterior, dkprNueva) -> actualizarCosto());
        dkprTxtPeso.textProperty().addListener((dkprObs, dkprAnterior, dkprNuevo) -> actualizarCosto());

        dkprTxtRemDoc.setOnAction(dkprE -> {
            if (leerDni(dkprCmbRemTipo, dkprTxtRemDoc)
                    && buscarCliente(dkprCmbRemTipo, dkprTxtRemDoc, dkprTxtRemNombre, dkprTxtRemTelefono)) {
                dkprTxtDesDoc.requestFocus();
            }
        });
        dkprTxtDesDoc.setOnAction(dkprE -> {
            if (leerDni(dkprCmbDesTipo, dkprTxtDesDoc)
                    && buscarCliente(dkprCmbDesTipo, dkprTxtDesDoc, dkprTxtDesNombre, dkprTxtDesTelefono)) {
                dkprTxtDescripcion.requestFocus();
            }
        });

        limpiar();
    }

    private void avisarCaja(Empleado dkprEmpleado) {
        boolean dkprSinTurno;
        try {
            dkprSinTurno = !dkprCajaService.tieneTurnoAbierto(dkprEmpleado);
        } catch (Exception dkprE) {
            dkprSinTurno = false;
        }
        dkprLblCaja.setVisible(dkprSinTurno);
        dkprLblCaja.setManaged(dkprSinTurno);
    }

    private void actualizarCosto() {
        try {
            BigDecimal dkprPeso = Validaciones.decimal(dkprTxtPeso.getText(), "Peso");
            dkprLblCosto.setText("S/ " + dkprEnvioService.calcularCosto(dkprCmbRuta.getValue(), dkprPeso).toPlainString());
        } catch (Exception dkprE) {
            dkprLblCosto.setText("S/ --");
        }
    }

    private void limpiar() {
        dkprCmbRuta.setValue(null);
        dkprCmbRemTipo.setValue("DNI");
        dkprCmbDesTipo.setValue("DNI");
        dkprTxtRemDoc.clear();
        dkprTxtRemNombre.clear();
        dkprTxtRemTelefono.clear();
        dkprTxtDesDoc.clear();
        dkprTxtDesNombre.clear();
        dkprTxtDesTelefono.clear();
        dkprTxtDescripcion.clear();
        dkprTxtBultos.setText("1");
        dkprTxtPeso.clear();
        dkprTxtValor.clear();
        dkprTxtObservaciones.clear();
        dkprCmbComprobante.setValue("BOLETA");
        dkprCmbMetodo.setValue("EFECTIVO");
        dkprLblCosto.setText("S/ --");
    }

    private boolean leerDni(ComboBox<String> dkprTipo, TextField dkprDoc) {
        String dkprTexto = dkprDoc.getText();
        if (!LectorDni.pareceEscaneado(dkprTexto)) {
            return true;
        }
        String dkprDni = LectorDni.extraer(dkprTexto);
        if (dkprDni == null) {
            Alertas.advertencia("No se encontró un número de DNI en lo que leyó el escáner:\n" + dkprTexto.trim()
                    + "\n\nEscribe el número a mano.");
            dkprDoc.clear();
            return false;
        }
        dkprTipo.setValue("DNI");
        dkprDoc.setText(dkprDni);
        return true;
    }

    private boolean buscarCliente(ComboBox<String> dkprTipo, TextField dkprDoc, TextField dkprNombre, TextField dkprTelefono) {
        try {
            Cliente dkprC = dkprClienteService.buscarPorDocumento(dkprTipo.getValue(), dkprDoc.getText());
            if (dkprC == null) {
                dkprNombre.clear();
                dkprTelefono.clear();
                Alertas.info("Cliente nuevo: completa su nombre y teléfono. Se registrará junto con el envío.");
                dkprNombre.requestFocus();
                return false;
            }
            dkprNombre.setText(dkprC.getNombreCompleto());
            dkprTelefono.setText(dkprC.getTelefono());
            return true;
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
            return false;
        }
    }

    @FXML
    private void onBuscarRemitente() {
        if (leerDni(dkprCmbRemTipo, dkprTxtRemDoc)) {
            buscarCliente(dkprCmbRemTipo, dkprTxtRemDoc, dkprTxtRemNombre, dkprTxtRemTelefono);
        }
    }

    @FXML
    private void onBuscarDestinatario() {
        if (leerDni(dkprCmbDesTipo, dkprTxtDesDoc)) {
            buscarCliente(dkprCmbDesTipo, dkprTxtDesDoc, dkprTxtDesNombre, dkprTxtDesTelefono);
        }
    }

    @FXML
    private void onLimpiar() {
        limpiar();
    }

    @FXML
    private void onRegistrar() {
        try {
            BigDecimal dkprValor = Validaciones.vacio(dkprTxtValor.getText())
                    ? BigDecimal.ZERO
                    : Validaciones.decimal(dkprTxtValor.getText(), "Valor declarado");

            SolicitudEnvio dkprSolicitud = new SolicitudEnvio(
                    dkprCmbRuta.getValue(),
                    dkprCmbRemTipo.getValue(), dkprTxtRemDoc.getText(), dkprTxtRemNombre.getText(), dkprTxtRemTelefono.getText(),
                    dkprCmbDesTipo.getValue(), dkprTxtDesDoc.getText(), dkprTxtDesNombre.getText(), dkprTxtDesTelefono.getText(),
                    dkprTxtDescripcion.getText(),
                    Validaciones.entero(dkprTxtBultos.getText(), "Bultos"),
                    Validaciones.decimal(dkprTxtPeso.getText(), "Peso (kg)"),
                    dkprValor,
                    dkprTxtObservaciones.getText(),
                    dkprCmbComprobante.getValue(),
                    dkprCmbMetodo.getValue());

            ResultadoRegistro dkprResultado = dkprEnvioService.registrar(dkprSolicitud, Sesion.getActual());
            limpiar();
            mostrarTicket(dkprResultado);
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private void mostrarTicket(ResultadoRegistro dkprResultado) {
        String dkprResumen = "Envío registrado correctamente.\n"
                + "Código: " + dkprResultado.codigoSeguimiento()
                + "   |   Comprobante: " + dkprResultado.comprobante()
                + "\nTotal cobrado: " + Formato.soles(dkprResultado.total())
                + "\nImprime el ticket para el cliente y las etiquetas para pegar en cada bulto.";
        try {
            VistaTicket.mostrar(dkprEnvioService.detalle(dkprResultado.codigoSeguimiento()),
                    dkprLblCosto.getScene().getWindow(), dkprResumen);
        } catch (Exception dkprE) {
            dkprE.printStackTrace();
            Alertas.info(dkprResumen + "\n\nPuedes reimprimir el ticket desde Seguimiento.");
        }
    }
}
