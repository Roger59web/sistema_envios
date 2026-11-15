package com.grupo.envios.controller;

import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.service.DkprEnvioService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprEstados;
import com.grupo.envios.util.DkprLectorDni;
import com.grupo.envios.util.DkprRoles;
import com.grupo.envios.util.DkprSesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DkprEntregasController {

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

    private final DkprEnvioService dkprService = new DkprEnvioService();
    private DkprEnvio dkprEnvio;

    @FXML
    private void initialize() {
        dkprTxtCodigo.setOnAction(dkprE -> dkprOnBuscar());
        dkprTxtDniReceptor.setOnAction(dkprE -> dkprLeerDniReceptor());
        dkprLimpiar();
    }

    private void dkprLeerDniReceptor() {
        String dkprTexto = dkprTxtDniReceptor.getText();
        if (DkprLectorDni.dkprPareceEscaneado(dkprTexto)) {
            String dkprDni = DkprLectorDni.dkprExtraer(dkprTexto);
            if (dkprDni == null) {
                DkprAlertas.dkprAdvertencia("No se encontró un número de DNI en lo que leyó el escáner. Escríbelo a mano.");
                dkprTxtDniReceptor.clear();
                return;
            }
            dkprTxtDniReceptor.setText(dkprDni);
        }
        dkprTxtObservacion.requestFocus();
    }

    private void dkprLimpiar() {
        dkprEnvio = null;
        dkprLblCodigo.setText("");
        dkprLblEstado.setText("");
        DkprEstados.dkprColorear(dkprLblEstado, null);
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

    private void dkprMostrar(DkprEnvio dkprE) {
        dkprEnvio = dkprE;
        dkprLblCodigo.setText(dkprE.dkprGetCodigoSeguimiento());
        dkprLblEstado.setText(dkprE.dkprGetEstado());
        DkprEstados.dkprColorear(dkprLblEstado, dkprE.dkprGetEstado());
        dkprLblRuta.setText(dkprE.dkprGetRuta());
        dkprLblRemitente.setText(dkprE.dkprGetRemitente() + " (" + dkprE.dkprGetRemitenteDocumento() + ")");
        dkprLblDestinatario.setText(dkprE.dkprGetDestinatario() + " (" + dkprE.dkprGetDestinatarioDocumento() + ")");
        dkprLblDescripcion.setText(dkprE.dkprGetDescripcion());
        dkprTxtReceptor.clear();
        dkprTxtDniReceptor.clear();
        dkprTxtObservacion.clear();

        DkprEmpleado dkprEmpleado = DkprSesion.dkprGetActual();
        boolean dkprAdmin = DkprRoles.ADMINISTRADOR.equals(dkprEmpleado.dkprGetRol());

        if (dkprE.dkprGetIdEstadoActual() != DkprEstados.EN_AGENCIA_DESTINO) {
            dkprLblAviso.setText("Solo se entregan envíos en estado EN AGENCIA DESTINO. Estado actual: " + dkprE.dkprGetEstado() + ".");
            dkprPanelEntrega.setDisable(true);
        } else if (!dkprAdmin && dkprEmpleado.dkprGetIdAgencia() != dkprE.dkprGetIdAgenciaDestino()) {
            dkprLblAviso.setText("Este envío se entrega en la agencia " + dkprE.dkprGetDestino() + ".");
            dkprPanelEntrega.setDisable(true);
        } else {
            long dkprDias = dkprE.dkprGetDiasEnEstado();
            String dkprAviso = "Listo para entregar: verifica el documento de quien recoge. En agencia desde hace "
                    + dkprDias + (dkprDias == 1 ? " día." : " días.");
            if (dkprDias > DkprEnvio.DIAS_ALMACEN_GRATIS) {
                dkprAviso += " Superó los " + DkprEnvio.DIAS_ALMACEN_GRATIS + " días de almacenaje gratuito.";
            }
            dkprLblAviso.setText(dkprAviso);
            dkprPanelEntrega.setDisable(false);
        }
    }

    public void dkprCargar(String dkprCodigo) {
        dkprTxtCodigo.setText(dkprCodigo);
        dkprOnBuscar();
    }

    @FXML
    private void dkprOnBuscar() {
        try {
            dkprMostrar(dkprService.dkprBuscar(dkprTxtCodigo.getText()));
        } catch (Exception dkprE) {
            dkprLimpiar();
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    @FXML
    private void dkprOnUsarDestinatario() {
        if (dkprEnvio == null) {
            return;
        }
        dkprTxtReceptor.setText(dkprEnvio.dkprGetDestinatario());
        String dkprDoc = dkprEnvio.dkprGetDestinatarioDocumento();
        if (dkprDoc != null && dkprDoc.matches("\\d{8}")) {
            dkprTxtDniReceptor.setText(dkprDoc);
        }
    }

    @FXML
    private void dkprOnRegistrarEntrega() {
        if (dkprEnvio == null) {
            return;
        }
        if (!DkprAlertas.dkprConfirmar("¿Confirmas la entrega del envío " + dkprEnvio.dkprGetCodigoSeguimiento() + "?")) {
            return;
        }
        try {
            dkprService.dkprRegistrarEntrega(dkprEnvio.dkprGetCodigoSeguimiento(), dkprTxtReceptor.getText(),
                    dkprTxtDniReceptor.getText(), dkprTxtObservacion.getText(), DkprSesion.dkprGetActual());
            DkprAlertas.dkprInfo("Entrega registrada correctamente.");
            dkprMostrar(dkprService.dkprBuscar(dkprEnvio.dkprGetCodigoSeguimiento()));
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }
}
