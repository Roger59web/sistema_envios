package com.grupo.envios.controller;

import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.service.EnvioService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Estados;
import com.grupo.envios.util.Roles;
import com.grupo.envios.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class EntregasController {

    @FXML private TextField txtCodigo;

    @FXML private Label lblCodigo;
    @FXML private Label lblEstado;
    @FXML private Label lblRuta;
    @FXML private Label lblRemitente;
    @FXML private Label lblDestinatario;
    @FXML private Label lblDescripcion;
    @FXML private Label lblAviso;

    @FXML private VBox panelEntrega;
    @FXML private TextField txtReceptor;
    @FXML private TextField txtDniReceptor;
    @FXML private TextField txtObservacion;

    private final EnvioService service = new EnvioService();
    private Envio envio;

    @FXML
    private void initialize() {
        txtCodigo.setOnAction(e -> onBuscar());
        limpiar();
    }

    private void limpiar() {
        envio = null;
        lblCodigo.setText("");
        lblEstado.setText("");
        Estados.colorear(lblEstado, null);
        lblRuta.setText("");
        lblRemitente.setText("");
        lblDestinatario.setText("");
        lblDescripcion.setText("");
        lblAviso.setText("Busca un envío por su código para registrar su entrega.");
        panelEntrega.setDisable(true);
        txtReceptor.clear();
        txtDniReceptor.clear();
        txtObservacion.clear();
    }

    private void mostrar(Envio e) {
        envio = e;
        lblCodigo.setText(e.getCodigoSeguimiento());
        lblEstado.setText(e.getEstado());
        Estados.colorear(lblEstado, e.getEstado());
        lblRuta.setText(e.getRuta());
        lblRemitente.setText(e.getRemitente() + " (" + e.getRemitenteDocumento() + ")");
        lblDestinatario.setText(e.getDestinatario() + " (" + e.getDestinatarioDocumento() + ")");
        lblDescripcion.setText(e.getDescripcion());
        txtReceptor.clear();
        txtDniReceptor.clear();
        txtObservacion.clear();

        Empleado empleado = Sesion.getActual();
        boolean admin = Roles.ADMINISTRADOR.equals(empleado.getRol());

        if (e.getIdEstadoActual() != Estados.EN_AGENCIA_DESTINO) {
            lblAviso.setText("Solo se entregan envíos en estado EN AGENCIA DESTINO. Estado actual: " + e.getEstado() + ".");
            panelEntrega.setDisable(true);
        } else if (!admin && empleado.getIdAgencia() != e.getIdAgenciaDestino()) {
            lblAviso.setText("Este envío se entrega en la agencia " + e.getDestino() + ".");
            panelEntrega.setDisable(true);
        } else {
            long dias = e.getDiasEnEstado();
            String aviso = "Listo para entregar: verifica el documento de quien recoge. En agencia desde hace "
                    + dias + (dias == 1 ? " día." : " días.");
            if (dias > Envio.DIAS_ALMACEN_GRATIS) {
                aviso += " Superó los " + Envio.DIAS_ALMACEN_GRATIS + " días de almacenaje gratuito.";
            }
            lblAviso.setText(aviso);
            panelEntrega.setDisable(false);
        }
    }

    /** Abre la pantalla con un envío ya buscado (se usa desde la bandeja de la agencia). */
    public void cargar(String codigo) {
        txtCodigo.setText(codigo);
        onBuscar();
    }

    @FXML
    private void onBuscar() {
        try {
            mostrar(service.buscar(txtCodigo.getText()));
        } catch (Exception e) {
            limpiar();
            Alertas.manejar(e);
        }
    }

    /** Copia el nombre y, si es un DNI, el documento del destinatario registrado. */
    @FXML
    private void onUsarDestinatario() {
        if (envio == null) {
            return;
        }
        txtReceptor.setText(envio.getDestinatario());
        String doc = envio.getDestinatarioDocumento();
        if (doc != null && doc.matches("\\d{8}")) {
            txtDniReceptor.setText(doc);
        }
    }

    @FXML
    private void onRegistrarEntrega() {
        if (envio == null) {
            return;
        }
        if (!Alertas.confirmar("¿Confirmas la entrega del envío " + envio.getCodigoSeguimiento() + "?")) {
            return;
        }
        try {
            service.registrarEntrega(envio.getCodigoSeguimiento(), txtReceptor.getText(),
                    txtDniReceptor.getText(), txtObservacion.getText(), Sesion.getActual());
            Alertas.info("Entrega registrada correctamente.");
            mostrar(service.buscar(envio.getCodigoSeguimiento()));
        } catch (Exception e) {
            Alertas.manejar(e);
        }
    }
}
