package com.grupo.envios.controller;

import com.grupo.envios.model.DkprEmpleado;
import com.grupo.envios.model.DkprEnvio;
import com.grupo.envios.service.DkprEstancadoService;
import com.grupo.envios.util.DkprAlertas;
import com.grupo.envios.util.DkprSesion;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.util.List;
import java.util.function.Function;

public class DkprEstancadosController {

    @FXML private Label dkprLblContador;
    @FXML private Spinner<Integer> dkprSpnDias;
    @FXML private Button dkprBtnGuardarDias;
    @FXML private Label dkprLblAmbito;
    @FXML private TableView<DkprEnvio> dkprTabla;

    private final DkprEstancadoService dkprService = new DkprEstancadoService();
    private boolean dkprCargando;

    @FXML
    private void initialize() {
        DkprEmpleado dkprEmpleado = DkprSesion.dkprGetActual();
        boolean dkprAdmin = dkprService.dkprPuedeConfigurar(dkprEmpleado);
        dkprBtnGuardarDias.setVisible(dkprAdmin);
        dkprBtnGuardarDias.setManaged(dkprAdmin);
        dkprLblAmbito.setText((dkprAdmin ? "Todas las agencias." : "Envíos en los que participa " + dkprEmpleado.dkprGetAgencia() + ".")
                + "  Doble clic en un envío para abrirlo en Seguimiento.");

        dkprTabla.getColumns().add(dkprColumna("Código", 105, DkprEnvio::dkprGetCodigoSeguimiento));
        dkprTabla.getColumns().add(dkprColumna("Estado", 140, DkprEnvio::dkprGetEstado));
        dkprTabla.getColumns().add(dkprColumna("Días sin moverse", 115, dkprE -> String.valueOf(dkprE.dkprGetDiasEnEstado())));
        dkprTabla.getColumns().add(dkprColumna("Qué falta", 300, DkprEstancadoService::dkprQueFalta));
        dkprTabla.getColumns().add(dkprColumna("Último movimiento", 140, DkprEnvio::dkprGetUltimoMovimientoTexto));
        dkprTabla.getColumns().add(dkprColumna("Ruta", 230, DkprEnvio::dkprGetRuta));
        dkprTabla.getColumns().add(dkprColumna("Destinatario", 170, DkprEnvio::dkprGetDestinatario));
        dkprTabla.getColumns().add(dkprColumna("Teléfono", 100, dkprE -> dkprE.dkprGetDestinatarioTelefono() == null ? "-" : dkprE.dkprGetDestinatarioTelefono()));
        dkprTabla.setRowFactory(dkprT -> {
            TableRow<DkprEnvio> dkprFila = new TableRow<>() {
                @Override
                protected void updateItem(DkprEnvio dkprE, boolean dkprVacia) {
                    super.updateItem(dkprE, dkprVacia);
                    getStyleClass().remove("fila-alerta");
                    if (!dkprVacia && dkprE != null && dkprE.dkprGetDiasEnEstado() > 2L * dkprSpnDias.getValue()) {
                        getStyleClass().add("fila-alerta");
                    }
                }
            };
            dkprFila.setOnMouseClicked(dkprEv -> {
                if (dkprEv.getClickCount() == 2 && !dkprFila.isEmpty()) {
                    dkprAbrirSeguimiento(dkprFila.getItem());
                }
            });
            return dkprFila;
        });

        int dkprDias = DkprEstancadoService.DIAS_POR_DEFECTO;
        try {
            dkprDias = dkprService.dkprDiasEstancado();
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
        dkprSpnDias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, DkprEstancadoService.DIAS_MAXIMO, dkprDias));
        dkprSpnDias.valueProperty().addListener((dkprObs, dkprAntes, dkprAhora) -> dkprCargar());
        dkprCargar();
    }

    @FXML
    private void dkprOnActualizar() {
        dkprCargar();
    }

    private void dkprCargar() {
        if (dkprCargando) {
            return;
        }
        dkprCargando = true;
        try {
            List<DkprEnvio> dkprLista = dkprService.dkprListar(DkprSesion.dkprGetActual(), dkprSpnDias.getValue());
            dkprTabla.setItems(FXCollections.observableArrayList(dkprLista));
            dkprLblContador.setText(String.valueOf(dkprLista.size()));
            dkprLblContador.setStyle(dkprLista.isEmpty() ? "" : "-fx-text-fill: #c0392b;");
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        } finally {
            dkprCargando = false;
        }
    }

    @FXML
    private void dkprOnGuardarDias() {
        try {
            dkprService.dkprGuardarDias(dkprSpnDias.getValue(), DkprSesion.dkprGetActual());
            DkprAlertas.dkprInfo("Desde ahora un envío se considera estancado después de " + dkprSpnDias.getValue()
                    + " día(s) sin moverse (para todas las agencias).");
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private void dkprAbrirSeguimiento(DkprEnvio dkprEnvio) {
        try {
            FXMLLoader dkprLoader = new FXMLLoader(getClass().getResource("/view/seguimiento.fxml"));
            Parent dkprVista = dkprLoader.load();
            ((Pane) dkprTabla.getParent().getParent()).getChildren().setAll(dkprVista);
            TextField dkprCodigo = (TextField) dkprLoader.getNamespace().get("dkprTxtCodigo");
            dkprCodigo.setText(dkprEnvio.dkprGetCodigoSeguimiento());
            dkprCodigo.fireEvent(new ActionEvent());
        } catch (Exception dkprE) {
            DkprAlertas.dkprManejar(dkprE);
        }
    }

    private static TableColumn<DkprEnvio, String> dkprColumna(String dkprTitulo, double dkprAncho, Function<DkprEnvio, String> dkprValor) {
        TableColumn<DkprEnvio, String> dkprCol = new TableColumn<>(dkprTitulo);
        dkprCol.setPrefWidth(dkprAncho);
        dkprCol.setCellValueFactory(dkprC -> new SimpleStringProperty(dkprValor.apply(dkprC.getValue())));
        return dkprCol;
    }
}
