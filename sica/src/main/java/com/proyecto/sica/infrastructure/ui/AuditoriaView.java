package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.domain.entity.RegistroAuditoria;
import com.proyecto.sica.infrastructure.ui.components.EncabezadoModulo;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Vista de solo lectura para consultar eventos de auditoria. */
public final class AuditoriaView {
    private final BorderPane root = new BorderPane();
    private final TableView<RegistroAuditoria> tabla = new TableView<>();
    private final TextField usuario = campo("ID usuario");
    private final TextField accion = campo("Accion exacta");
    private final TextField desde = campo("Desde AAAA-MM-DD");
    private final TextField hasta = campo("Hasta AAAA-MM-DD");
    private final TextField entidad = campo("Tabla");
    private final TextField registro = campo("ID registro");
    private final TextField persona = campo("ID persona");
    private final TextField visita = campo("ID visita");
    private final Button buscar = new Button("Consultar");

    public AuditoriaView() { construir(); }

    private void construir() {
        root.getStyleClass().add("modulo-root");
        buscar.getStyleClass().add("boton-principal");
        FlowPane filtros = new FlowPane(9, 9, usuario, accion, desde, hasta,
                entidad, registro, persona, visita, buscar);
        filtros.getStyleClass().add("panel-filtros");
        VBox cabecera = new VBox(16,
                new EncabezadoModulo("Bitacora de auditoria",
                        "Consulta el historial de acciones del sistema.", TipoIcono.BITACORA),
                filtros);
        columna("Fecha", r -> r.fechaEvento().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 145);
        columna("Usuario", r -> r.nombreUsuario() == null
                ? texto(r.identificadorIntentado()) : r.nombreUsuario(), 180);
        columna("Accion", RegistroAuditoria::accion, 170);
        columna("Tabla", RegistroAuditoria::entidad, 100);
        columna("Registro", RegistroAuditoria::entidadId, 85);
        columna("Resultado", RegistroAuditoria::resultado, 85);
        columna("Detalles", RegistroAuditoria::descripcion, 320);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        root.setTop(cabecera);
        root.setCenter(tabla);
        BorderPane.setMargin(tabla, new Insets(16, 0, 0, 0));
    }

    private void columna(String titulo, java.util.function.Function<RegistroAuditoria, String> valor,
            double ancho) {
        TableColumn<RegistroAuditoria, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(c -> new SimpleStringProperty(texto(valor.apply(c.getValue()))));
        columna.setPrefWidth(ancho); tabla.getColumns().add(columna);
    }
    private TextField campo(String prompt) { TextField c = new TextField(); c.setPromptText(prompt); return c; }
    private String texto(String valor) { return valor == null ? "" : valor; }
    public Parent getRoot() { return root; }
    public TableView<RegistroAuditoria> getTabla() { return tabla; }
    public TextField getUsuario() { return usuario; }
    public TextField getAccion() { return accion; }
    public TextField getDesde() { return desde; }
    public TextField getHasta() { return hasta; }
    public TextField getEntidad() { return entidad; }
    public TextField getRegistro() { return registro; }
    public TextField getPersona() { return persona; }
    public TextField getVisita() { return visita; }
    public Button getBuscar() { return buscar; }
    public void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR); alerta.setHeaderText(null);
        alerta.setContentText(mensaje); alerta.showAndWait();
    }
}
