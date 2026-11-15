package com.grupo.envios.controller;

import com.grupo.envios.model.DkprCliente;
import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprResultadoRegistro;
import com.grupo.envios.model.DkprRuta;
import com.grupo.envios.model.DkprSolicitudEnvio;
import com.grupo.envios.service.DkprCajaService;
import com.grupo.envios.service.DkprClienteService;
import com.grupo.envios.service.DkprEnvioService;
import com.grupo.envios.service.DkprRutaService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprFormato;
import com.grupo.envios.util.DkprLectorDni;
import com.grupo.envios.util.DkprSesion;
import com.grupo.envios.util.DkprValidaciones;
import com.grupo.envios.util.DkprVistaTicket;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

public class DkprRegistrarEnvioController {

    @FXML private Label dkprLblOrigen;
    @FXML private Label dkprLblCaja;
    @FXML private Label dkprLblCosto;
    @FXML private ComboBox<DkprRuta> dkprCmbRuta;

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

    private final DkprEnvioService dkprEnvioService = new DkprEnvioService();
    private final DkprClienteService dkprClienteService = new DkprClienteService();
    private final DkprRutaService dkprRutaService = new DkprRutaService();
    private final DkprCajaService dkprCajaService = new DkprCajaService();

    @FXML
    private void initialize() {
        DkprEmpleado dkprEmpleado = DkprSesion.dkprGetActual();
        dkprLblOrigen.setText("Agencia de origen: " + dkprEmpleado.dkprGetAgencia());
        dkprAvisarCaja(dkprEmpleado);

        try {
            var dkprRutas = dkprRutaService.dkprListarPorOrigen(dkprEmpleado.dkprGetIdAgencia());
            dkprCmbRuta.setItems(FXCollections.observableArrayList(dkprRutas));
            if (dkprRutas.isEmpty()) {
                dkprLblOrigen.setText(dkprLblOrigen.getText() + "  (no hay rutas activas con tarifa desde esta agencia)");
            }
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }

        var dkprTipos = FXCollections.observableArrayList("DNI", "RUC", "CE");
        dkprCmbRemTipo.setItems(dkprTipos);
        dkprCmbDesTipo.setItems(FXCollections.observableArrayList(dkprTipos));
        dkprCmbComprobante.setItems(FXCollections.observableArrayList("BOLETA", "FACTURA"));
        dkprCmbMetodo.setItems(FXCollections.observableArrayList("EFECTIVO", "TARJETA", "YAPE", "PLIN"));

        dkprCmbRuta.valueProperty().addListener((dkprObs, dkprAnterior, dkprNueva) -> dkprActualizarCosto());
        dkprTxtPeso.textProperty().addListener((dkprObs, dkprAnterior, dkprNuevo) -> dkprActualizarCosto());
        dkprTxtBultos.textProperty().addListener((dkprObs, dkprAnterior, dkprNuevo) -> dkprActualizarCosto());

        dkprTxtRemDoc.setOnAction(dkprE -> {
            if (dkprLeerDni(dkprCmbRemTipo, dkprTxtRemDoc)
                    && dkprBuscarCliente(dkprCmbRemTipo, dkprTxtRemDoc, dkprTxtRemNombre, dkprTxtRemTelefono)) {
                dkprTxtDesDoc.requestFocus();
            }
        });
        dkprTxtDesDoc.setOnAction(dkprE -> {
            if (dkprLeerDni(dkprCmbDesTipo, dkprTxtDesDoc)
                    && dkprBuscarCliente(dkprCmbDesTipo, dkprTxtDesDoc, dkprTxtDesNombre, dkprTxtDesTelefono)) {
                dkprTxtDescripcion.requestFocus();
            }
        });

        dkprLimpiar();
    }

    private void dkprAvisarCaja(DkprEmpleado dkprEmpleado) {
        boolean dkprSinTurno;
        try {
            dkprSinTurno = !dkprCajaService.dkprTieneTurnoAbierto(dkprEmpleado);
        } catch (Exception dkprE) {
            dkprSinTurno = false;
        }
        dkprLblCaja.setVisible(dkprSinTurno);
        dkprLblCaja.setManaged(dkprSinTurno);
    }

    private void dkprActualizarCosto() {
        try {
            int dkprBultos = DkprValidaciones.dkprEntero(dkprTxtBultos.getText(), "Bultos");
            BigDecimal dkprPeso = DkprValidaciones.dkprDecimal(dkprTxtPeso.getText(), "Peso");
            dkprLblCosto.setText("S/ " + dkprEnvioService.dkprCalcularCosto(dkprCmbRuta.getValue(), dkprBultos, dkprPeso).toPlainString());
        } catch (Exception dkprE) {
            dkprLblCosto.setText("S/ --");
        }
    }

    private void dkprLimpiar() {
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

    private boolean dkprLeerDni(ComboBox<String> dkprTipo, TextField dkprDoc) {
        String dkprTexto = dkprDoc.getText();
        if (!DkprLectorDni.dkprPareceEscaneado(dkprTexto)) {
            return true;
        }
        String dkprDni = DkprLectorDni.dkprExtraer(dkprTexto);
        if (dkprDni == null) {
            DkprAlertas.dkprAdvertencia("No se encontró un número de DNI en lo que leyó el escáner:\n" + dkprTexto.trim()
                    + "\n\nEscribe el número a mano.");
            dkprDoc.clear();
            return false;
        }
        dkprTipo.setValue("DNI");
        dkprDoc.setText(dkprDni);
        return true;
    }

    private boolean dkprBuscarCliente(ComboBox<String> dkprTipo, TextField dkprDoc, TextField dkprNombre, TextField dkprTelefono) {
        try {
            DkprCliente dkprC = dkprClienteService.dkprBuscarPorDocumento(dkprTipo.getValue(), dkprDoc.getText());
            if (dkprC == null) {
                dkprNombre.clear();
                dkprTelefono.clear();
                DkprAlertas.dkprInfo("Cliente nuevo: completa su nombre y teléfono. Se registrará junto con el envío.");
                dkprNombre.requestFocus();
                return false;
            }
            dkprNombre.setText(dkprC.dkprGetNombreCompleto());
            dkprTelefono.setText(dkprC.dkprGetTelefono());
            return true;
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
            return false;
        }
    }

    @FXML
    private void dkprOnBuscarRemitente() {
        if (dkprLeerDni(dkprCmbRemTipo, dkprTxtRemDoc)) {
            dkprBuscarCliente(dkprCmbRemTipo, dkprTxtRemDoc, dkprTxtRemNombre, dkprTxtRemTelefono);
        }
    }

    @FXML
    private void dkprOnBuscarDestinatario() {
        if (dkprLeerDni(dkprCmbDesTipo, dkprTxtDesDoc)) {
            dkprBuscarCliente(dkprCmbDesTipo, dkprTxtDesDoc, dkprTxtDesNombre, dkprTxtDesTelefono);
        }
    }

    @FXML
    private void dkprOnLimpiar() {
        dkprLimpiar();
    }

    @FXML
    private void dkprOnRegistrar() {
        try {
            BigDecimal dkprValor = DkprValidaciones.dkprVacio(dkprTxtValor.getText())
                    ? BigDecimal.ZERO
                    : DkprValidaciones.dkprDecimal(dkprTxtValor.getText(), "Valor declarado");

            DkprSolicitudEnvio dkprSolicitud = new DkprSolicitudEnvio(
                    dkprCmbRuta.getValue(),
                    dkprCmbRemTipo.getValue(), dkprTxtRemDoc.getText(), dkprTxtRemNombre.getText(), dkprTxtRemTelefono.getText(),
                    dkprCmbDesTipo.getValue(), dkprTxtDesDoc.getText(), dkprTxtDesNombre.getText(), dkprTxtDesTelefono.getText(),
                    dkprTxtDescripcion.getText(),
                    DkprValidaciones.dkprEntero(dkprTxtBultos.getText(), "Bultos"),
                    DkprValidaciones.dkprDecimal(dkprTxtPeso.getText(), "Peso (kg)"),
                    dkprValor,
                    dkprTxtObservaciones.getText(),
                    dkprCmbComprobante.getValue(),
                    dkprCmbMetodo.getValue());

            DkprResultadoRegistro dkprResultado = dkprEnvioService.dkprRegistrar(dkprSolicitud, DkprSesion.dkprGetActual());
            dkprLimpiar();
            dkprMostrarTicket(dkprResultado);
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprMostrarTicket(DkprResultadoRegistro dkprResultado) {
        String dkprResumen = "Envío registrado correctamente.\n"
                + "Código: " + dkprResultado.dkprCodigoSeguimiento()
                + "   |   Comprobante: " + dkprResultado.dkprComprobante()
                + "\nTotal cobrado: " + DkprFormato.dkprSoles(dkprResultado.dkprTotal())
                + "\nImprime el ticket para el cliente y las etiquetas para pegar en cada bulto.";
        try {
            DkprVistaTicket.dkprMostrar(dkprEnvioService.dkprDetalle(dkprResultado.dkprCodigoSeguimiento()),
                    dkprLblCosto.getScene().getWindow(), dkprResumen);
        } catch (Exception dkprE) {
            dkprE.printStackTrace();
            DkprAlertas.dkprInfo(dkprResumen + "\n\nPuedes reimprimir el ticket desde Seguimiento.");
        }
    }
}
