package com.grupo.envios.controller;

import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.service.EnvioService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Estados;
import com.grupo.envios.util.LectorDni;
import com.grupo.envios.util.Roles;
import com.grupo.envios.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class EntregasController {

    @FXML private TextField dkprTxtCodigo;

    @FXML private Label dkprLblCodigo;
    @FXML private Label dkprLblEstado;
    @FXML private Label dkprLblRuta;
    @FXML private Label dkprLblRemitente;
    @FXML private Label dkprLblDestinatario;
    @FXML private Label dkprLblDescripcion;
    @FXML private Label dkprLblAviso;

    @FXML private VBox dkprPanelEntrega;
    @FXML private TextField dkprTxtReceptor;
    @FXML private TextField dkprTxtDniReceptor;
    @FXML private TextField dkprTxtObservacion;

    private final EnvioService dkprService = new EnvioService();
    private Envio dkprEnvio;

    @FXML
    private void initialize() {
        dkprTxtCodigo.setOnAction(dkprE -> onBuscar());
        dkprTxtDniReceptor.setOnAction(dkprE -> leerDniReceptor());
        limpiar();
    }

    private void leerDniReceptor() {
        String dkprTexto = dkprTxtDniReceptor.getText();
        if (LectorDni.pareceEscaneado(dkprTexto)) {
            String dkprDni = LectorDni.extraer(dkprTexto);
            if (dkprDni == null) {
                Alertas.advertencia("No se encontró un número de DNI en lo que leyó el escáner. Escríbelo a mano.");
                dkprTxtDniReceptor.clear();
                return;
            }
            dkprTxtDniReceptor.setText(dkprDni);
        }
        dkprTxtObservacion.requestFocus();
    }

    private void limpiar() {
        dkprEnvio = null;
        dkprLblCodigo.setText("");
        dkprLblEstado.setText("");
        Estados.colorear(dkprLblEstado, null);
        dkprLblRuta.setText("");
        dkprLblRemitente.setText("");
        dkprLblDestinatario.setText("");
        dkprLblDescripcion.setText("");
        dkprLblAviso.setText("Busca un envío por su código para registrar su entrega.");
        dkprPanelEntrega.setDisable(true);
        dkprTxtReceptor.clear();
        dkprTxtDniReceptor.clear();
        dkprTxtObservacion.clear();
    }

    private void mostrar(Envio dkprE) {
        dkprEnvio = dkprE;
        dkprLblCodigo.setText(dkprE.getCodigoSeguimiento());
        dkprLblEstado.setText(dkprE.getEstado());
        Estados.colorear(dkprLblEstado, dkprE.getEstado());
        dkprLblRuta.setText(dkprE.getRuta());
        dkprLblRemitente.setText(dkprE.getRemitente() + " (" + dkprE.getRemitenteDocumento() + ")");
        dkprLblDestinatario.setText(dkprE.getDestinatario() + " (" + dkprE.getDestinatarioDocumento() + ")");
        dkprLblDescripcion.setText(dkprE.getDescripcion());
        dkprTxtReceptor.clear();
        dkprTxtDniReceptor.clear();
        dkprTxtObservacion.clear();

        Empleado dkprEmpleado = Sesion.getActual();
        boolean dkprAdmin = Roles.ADMINISTRADOR.equals(dkprEmpleado.getRol());

        if (dkprE.getIdEstadoActual() != Estados.EN_AGENCIA_DESTINO) {
            dkprLblAviso.setText("Solo se entregan envíos en estado EN AGENCIA DESTINO. Estado actual: " + dkprE.getEstado() + ".");
            dkprPanelEntrega.setDisable(true);
        } else if (!dkprAdmin && dkprEmpleado.getIdAgencia() != dkprE.getIdAgenciaDestino()) {
            dkprLblAviso.setText("Este envío se entrega en la agencia " + dkprE.getDestino() + ".");
            dkprPanelEntrega.setDisable(true);
        } else {
            long dkprDias = dkprE.getDiasEnEstado();
            String dkprAviso = "Listo para entregar: verifica el documento de quien recoge. En agencia desde hace "
                    + dkprDias + (dkprDias == 1 ? " día." : " días.");
            if (dkprDias > Envio.DIAS_ALMACEN_GRATIS) {
                dkprAviso += " Superó los " + Envio.DIAS_ALMACEN_GRATIS + " días de almacenaje gratuito.";
            }
            dkprLblAviso.setText(dkprAviso);
            dkprPanelEntrega.setDisable(false);
        }
    }

    public void cargar(String dkprCodigo) {
        dkprTxtCodigo.setText(dkprCodigo);
        onBuscar();
    }

    @FXML
    private void onBuscar() {
        try {
            mostrar(dkprService.buscar(dkprTxtCodigo.getText()));
        } catch (Exception dkprE) {
            limpiar();
            Alertas.manejar(dkprE);
        }
    }

    @FXML
    private void onUsarDestinatario() {
        if (dkprEnvio == null) {
            return;
        }
        dkprTxtReceptor.setText(dkprEnvio.getDestinatario());
        String dkprDoc = dkprEnvio.getDestinatarioDocumento();
        if (dkprDoc != null && dkprDoc.matches("\\d{8}")) {
            dkprTxtDniReceptor.setText(dkprDoc);
        }
    }

    @FXML
    private void onRegistrarEntrega() {
        if (dkprEnvio == null) {
            return;
        }
        if (!Alertas.confirmar("¿Confirmas la entrega del envío " + dkprEnvio.getCodigoSeguimiento() + "?")) {
            return;
        }
        try {
            dkprService.registrarEntrega(dkprEnvio.getCodigoSeguimiento(), dkprTxtReceptor.getText(),
                    dkprTxtDniReceptor.getText(), dkprTxtObservacion.getText(), Sesion.getActual());
            Alertas.info("Entrega registrada correctamente.");
            mostrar(dkprService.buscar(dkprEnvio.getCodigoSeguimiento()));
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }
}
