package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.domain.entity.EstadoIncidente;
import com.proyecto.sica.domain.entity.GravedadIncidente;
import com.proyecto.sica.domain.entity.Incidente;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Vista funcional para registro, consulta y seguimiento de incidentes. */
public final class IncidentesView {
    private final BorderPane root = new BorderPane();
    private final TableView<Incidente> tabla = new TableView<>();
    private final TextField filtroVisita = new TextField();
    private final TextField filtroPersona = new TextField();
    private final TextField filtroResponsable = new TextField();
    private final TextField filtroDesde = new TextField();
    private final TextField filtroHasta = new TextField();
    private final ComboBox<String> filtroEstado = new ComboBox<>();
    private final TextField visitaId = new TextField();
    private final TextField tipo = new TextField();
    private final ComboBox<GravedadIncidente> gravedad = new ComboBox<>();
    private final ComboBox<EstadoIncidente> estado = new ComboBox<>();
    private final TextArea descripcion = new TextArea();
    private final TextArea acciones = new TextArea();
    private final TextField fecha = new TextField();
    private final TextField justificacion = new TextField();
    private final Button buscar = new Button("Buscar");
    private final Button registrar = new Button("Registrar incidente");
    private final Button actualizar = new Button("Guardar seguimiento");
    private final Button restringir = new Button("Aplicar prohibicion");

    public IncidentesView() {
        construir();
    }

    private void construir() {
        Label titulo = new Label("Incidentes de seguridad");
        titulo.getStyleClass().add("titulo-seccion");
        filtroEstado.setItems(FXCollections.observableArrayList(
                "", "ABIERTO", "EN_REVISION", "CERRADO"));
        filtroEstado.setValue("");
        preparar(filtroVisita, "ID visita"); preparar(filtroPersona, "ID persona");
        preparar(filtroResponsable, "ID responsable"); preparar(filtroDesde, "Desde AAAA-MM-DD");
        preparar(filtroHasta, "Hasta AAAA-MM-DD");
        HBox filtros = new HBox(8, filtroVisita, filtroPersona, filtroResponsable,
                filtroDesde, filtroHasta, filtroEstado, buscar);

        columna("ID", i -> String.valueOf(i.getId()), 60);
        columna("Fecha", i -> formato(i.getFechaIncidente()), 135);
        columna("Persona", i -> i.getPersona().getNombreCompleto(), 170);
        columna("Visita", i -> i.getVisita().getNumeroSolicitud(), 135);
        columna("Responsable", i -> i.getReportadoPor().getNombreCompleto(), 160);
        columna("Tipo", Incidente::getTipo, 120);
        columna("Gravedad", i -> i.getGravedad().name(), 90);
        columna("Estado", i -> i.getEstado().name(), 100);
        columna("Descripcion", Incidente::getDescripcion, 260);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        gravedad.setItems(FXCollections.observableArrayList(GravedadIncidente.values()));
        gravedad.setValue(GravedadIncidente.BAJA);
        estado.setItems(FXCollections.observableArrayList(EstadoIncidente.values()));
        estado.setValue(EstadoIncidente.ABIERTO);
        preparar(visitaId, "ID de visita"); preparar(tipo, "Tipo de incidente");
        preparar(fecha, "AAAA-MM-DDTHH:MM"); preparar(justificacion, "Justificacion del cambio");
        descripcion.setPromptText("Descripcion del hecho"); descripcion.setPrefRowCount(3);
        acciones.setPromptText("Acciones tomadas (opcional)"); acciones.setPrefRowCount(3);
        GridPane formulario = new GridPane();
        formulario.setHgap(10); formulario.setVgap(8);
        formulario.addRow(0, new Label("Visita"), visitaId, new Label("Tipo"), tipo,
                new Label("Fecha"), fecha);
        formulario.addRow(1, new Label("Gravedad"), gravedad, new Label("Estado"), estado,
                new Label("Justificacion"), justificacion);
        formulario.addRow(2, new Label("Descripcion"), descripcion,
                new Label("Acciones"), acciones);
        HBox accionesBotones = new HBox(10, registrar, actualizar, restringir);
        VBox contenido = new VBox(12, titulo, filtros, tabla, formulario, accionesBotones);
        contenido.setPadding(new Insets(24));
        root.setCenter(contenido);
        tabla.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> mostrarSeleccion(seleccionado));
    }

    private void mostrarSeleccion(Incidente incidente) {
        if (incidente == null) return;
        visitaId.setText(String.valueOf(incidente.getVisita().getId()));
        tipo.setText(incidente.getTipo()); gravedad.setValue(incidente.getGravedad());
        estado.setValue(incidente.getEstado()); descripcion.setText(incidente.getDescripcion());
        acciones.setText(incidente.getAccionesTomadas());
        fecha.setText(formatoEntrada(incidente.getFechaIncidente()));
    }

    private void columna(String titulo, java.util.function.Function<Incidente, String> valor,
            double ancho) {
        TableColumn<Incidente, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(c -> new SimpleStringProperty(valor.apply(c.getValue())));
        columna.setPrefWidth(ancho); tabla.getColumns().add(columna);
    }

    private void preparar(TextField campo, String texto) { campo.setPromptText(texto); }
    private String formato(java.time.LocalDateTime fechaValor) {
        return fechaValor == null ? "" : fechaValor.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    private String formatoEntrada(java.time.LocalDateTime fechaValor) {
        return fechaValor == null ? "" : fechaValor.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }

    public Parent getRoot() { return root; }
    public TableView<Incidente> getTabla() { return tabla; }
    public TextField getFiltroVisita() { return filtroVisita; }
    public TextField getFiltroPersona() { return filtroPersona; }
    public TextField getFiltroResponsable() { return filtroResponsable; }
    public TextField getFiltroDesde() { return filtroDesde; }
    public TextField getFiltroHasta() { return filtroHasta; }
    public ComboBox<String> getFiltroEstado() { return filtroEstado; }
    public TextField getVisitaId() { return visitaId; }
    public TextField getTipo() { return tipo; }
    public ComboBox<GravedadIncidente> getGravedad() { return gravedad; }
    public ComboBox<EstadoIncidente> getEstado() { return estado; }
    public TextArea getDescripcion() { return descripcion; }
    public TextArea getAcciones() { return acciones; }
    public TextField getFecha() { return fecha; }
    public TextField getJustificacion() { return justificacion; }
    public Button getBuscar() { return buscar; }
    public Button getRegistrar() { return registrar; }
    public Button getActualizar() { return actualizar; }
    public Button getRestringir() { return restringir; }
    public Incidente seleccionado() { return tabla.getSelectionModel().getSelectedItem(); }
    public void mostrarError(String mensaje) { alerta(Alert.AlertType.ERROR, "Error", mensaje); }
    public void mostrarInfo(String mensaje) { alerta(Alert.AlertType.INFORMATION, "SICA", mensaje); }
    private void alerta(Alert.AlertType tipoAlerta, String titulo, String mensaje) {
        Alert alerta = new Alert(tipoAlerta); alerta.setTitle(titulo);
        alerta.setHeaderText(null); alerta.setContentText(mensaje); alerta.showAndWait();
    }
}
