package com.grupo.envios.controller;

import com.grupo.envios.model.Empleado;
import com.grupo.envios.model.Envio;
import com.grupo.envios.service.EstancadoService;
import com.grupo.envios.util.Alertas;
import com.grupo.envios.util.Sesion;
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

public class EstancadosController {

    @FXML private Label dkprLblContador;
    @FXML private Spinner<Integer> dkprSpnDias;
    @FXML private Button dkprBtnGuardarDias;
    @FXML private Label dkprLblAmbito;
    @FXML private TableView<Envio> dkprTabla;

    private final EstancadoService dkprService = new EstancadoService();
    private boolean dkprCargando;

    @FXML
    private void initialize() {
        Empleado dkprEmpleado = Sesion.getActual();
        boolean dkprAdmin = dkprService.puedeConfigurar(dkprEmpleado);
        dkprBtnGuardarDias.setVisible(dkprAdmin);
        dkprBtnGuardarDias.setManaged(dkprAdmin);
        dkprLblAmbito.setText((dkprAdmin ? "Todas las agencias." : "Envíos en los que participa " + dkprEmpleado.getAgencia() + ".")
                + "  Doble clic en un envío para abrirlo en Seguimiento.");

        dkprTabla.getColumns().add(columna("Código", 105, Envio::getCodigoSeguimiento));
        dkprTabla.getColumns().add(columna("Estado", 140, Envio::getEstado));
        dkprTabla.getColumns().add(columna("Días sin moverse", 115, dkprE -> String.valueOf(dkprE.getDiasEnEstado())));
        dkprTabla.getColumns().add(columna("Qué falta", 300, EstancadoService::queFalta));
        dkprTabla.getColumns().add(columna("Último movimiento", 140, Envio::getUltimoMovimientoTexto));
        dkprTabla.getColumns().add(columna("Ruta", 230, Envio::getRuta));
        dkprTabla.getColumns().add(columna("Destinatario", 170, Envio::getDestinatario));
        dkprTabla.getColumns().add(columna("Teléfono", 100, dkprE -> dkprE.getDestinatarioTelefono() == null ? "-" : dkprE.getDestinatarioTelefono()));
        dkprTabla.setRowFactory(dkprT -> {
            TableRow<Envio> dkprFila = new TableRow<>() {
                @Override
                protected void updateItem(Envio dkprE, boolean dkprVacia) {
                    super.updateItem(dkprE, dkprVacia);
                    getStyleClass().remove("fila-alerta");
                    if (!dkprVacia && dkprE != null && dkprE.getDiasEnEstado() > 2L * dkprSpnDias.getValue()) {
                        getStyleClass().add("fila-alerta");
                    }
                }
            };
            dkprFila.setOnMouseClicked(dkprEv -> {
                if (dkprEv.getClickCount() == 2 && !dkprFila.isEmpty()) {
                    abrirSeguimiento(dkprFila.getItem());
                }
            });
            return dkprFila;
        });

        int dkprDias = EstancadoService.DIAS_POR_DEFECTO;
        try {
            dkprDias = dkprService.diasEstancado();
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
        dkprSpnDias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, EstancadoService.DIAS_MAXIMO, dkprDias));
        dkprSpnDias.valueProperty().addListener((dkprObs, dkprAntes, dkprAhora) -> cargar());
        cargar();
    }

    @FXML
    private void onActualizar() {
        cargar();
    }

    private void cargar() {
        if (dkprCargando) {
            return;
        }
        dkprCargando = true;
        try {
            List<Envio> dkprLista = dkprService.listar(Sesion.getActual(), dkprSpnDias.getValue());
            dkprTabla.setItems(FXCollections.observableArrayList(dkprLista));
            dkprLblContador.setText(String.valueOf(dkprLista.size()));
            dkprLblContador.setStyle(dkprLista.isEmpty() ? "" : "-fx-text-fill: #c0392b;");
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        } finally {
            dkprCargando = false;
        }
    }

    @FXML
    private void onGuardarDias() {
        try {
            dkprService.guardarDias(dkprSpnDias.getValue(), Sesion.getActual());
            Alertas.info("Desde ahora un envío se considera estancado después de " + dkprSpnDias.getValue()
                    + " día(s) sin moverse (para todas las agencias).");
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private void abrirSeguimiento(Envio dkprEnvio) {
        try {
            FXMLLoader dkprLoader = new FXMLLoader(getClass().getResource("/view/seguimiento.fxml"));
            Parent dkprVista = dkprLoader.load();
            ((Pane) dkprTabla.getParent().getParent()).getChildren().setAll(dkprVista);
            TextField dkprCodigo = (TextField) dkprLoader.getNamespace().get("dkprTxtCodigo");
            dkprCodigo.setText(dkprEnvio.getCodigoSeguimiento());
            dkprCodigo.fireEvent(new ActionEvent());
        } catch (Exception dkprE) {
            Alertas.manejar(dkprE);
        }
    }

    private static TableColumn<Envio, String> columna(String dkprTitulo, double dkprAncho, Function<Envio, String> dkprValor) {
        TableColumn<Envio, String> dkprCol = new TableColumn<>(dkprTitulo);
        dkprCol.setPrefWidth(dkprAncho);
        dkprCol.setCellValueFactory(dkprC -> new SimpleStringProperty(dkprValor.apply(dkprC.getValue())));
        return dkprCol;
    }
}
