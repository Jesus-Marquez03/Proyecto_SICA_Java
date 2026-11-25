package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.domain.entity.EstadoIncidente;
import com.proyecto.sica.domain.entity.GravedadIncidente;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.infrastructure.ui.components.EncabezadoModulo;
import com.proyecto.sica.infrastructure.ui.components.ComboBoxLegible;
import com.proyecto.sica.infrastructure.ui.components.TablaCopiable;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
        root.getStyleClass().add("modulo-root");
        filtroEstado.setItems(FXCollections.observableArrayList(
                "", "ABIERTO", "EN_REVISION", "CERRADO"));
        filtroEstado.setValue("");
        filtroEstado.setPromptText("Todos los estados");
        ComboBoxLegible.configurar(filtroEstado, valor -> valor.isBlank() ? "Todos los estados" : valor);
        preparar(filtroVisita, "ID visita"); preparar(filtroPersona, "ID persona");
        preparar(filtroResponsable, "ID responsable"); preparar(filtroDesde, "Desde AAAA-MM-DD");
        preparar(filtroHasta, "Hasta AAAA-MM-DD");
        buscar.getStyleClass().add("boton-principal");
        FlowPane filtros = new FlowPane(9, 9, filtroVisita, filtroPersona, filtroResponsable,
                filtroDesde, filtroHasta, filtroEstado, buscar);
        filtros.getStyleClass().add("panel-filtros");
        VBox cabecera = new VBox(16,
                new EncabezadoModulo("Incidentes de seguridad",
                        "Registra novedades y realiza seguimiento de incidentes.",
                        TipoIcono.INCIDENTES), filtros);

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
        tabla.setMinHeight(235);
        TablaCopiable.habilitar(tabla);

        gravedad.setItems(FXCollections.observableArrayList(GravedadIncidente.values()));
        gravedad.setValue(GravedadIncidente.BAJA);
        estado.setItems(FXCollections.observableArrayList(EstadoIncidente.values()));
        estado.setValue(EstadoIncidente.ABIERTO);
        ComboBoxLegible.configurar(gravedad, Enum::name);
        ComboBoxLegible.configurar(estado, Enum::name);
        preparar(visitaId, "ID de visita"); preparar(tipo, "Tipo de incidente");
        preparar(fecha, "AAAA-MM-DDTHH:MM"); preparar(justificacion, "Justificacion del cambio");
        descripcion.setPromptText("Descripcion del hecho"); descripcion.setPrefRowCount(3);
        acciones.setPromptText("Acciones tomadas (opcional)"); acciones.setPrefRowCount(3);
        GridPane formulario = new GridPane();
        formulario.getStyleClass().add("formulario-incidente");
        formulario.setHgap(10); formulario.setVgap(10);
        agregarCampo(formulario, 0, 0, "Visita", visitaId);
        agregarCampo(formulario, 0, 2, "Tipo", tipo);
        agregarCampo(formulario, 0, 4, "Fecha", fecha);
        agregarCampo(formulario, 1, 0, "Gravedad", gravedad);
        agregarCampo(formulario, 1, 2, "Estado", estado);
        agregarCampo(formulario, 1, 4, "Justificacion", justificacion);
        agregarCampo(formulario, 2, 0, "Descripcion", descripcion);
        GridPane.setColumnSpan(descripcion, 2);
        agregarCampo(formulario, 2, 3, "Acciones", acciones);
        GridPane.setColumnSpan(acciones, 2);
        registrar.getStyleClass().add("boton-principal");
        actualizar.getStyleClass().add("boton-neutro");
        restringir.getStyleClass().add("boton-critico");
        HBox accionesBotones = new HBox(10, registrar, actualizar, restringir);
        Label tituloFormulario = new Label("Registro y seguimiento");
        tituloFormulario.getStyleClass().add("titulo-formulario-interno");
        VBox formularioCaja = new VBox(13, tituloFormulario, formulario, accionesBotones);
        formularioCaja.getStyleClass().add("tarjeta-formulario");
        VBox cuerpo = new VBox(14, tabla, formularioCaja);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        root.setTop(cabecera);
        root.setCenter(cuerpo);
        BorderPane.setMargin(cuerpo, new Insets(16, 0, 0, 0));
        tabla.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> mostrarSeleccion(seleccionado));
    }

    private void agregarCampo(GridPane grid, int fila, int columna, String texto,
            javafx.scene.control.Control campo) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("etiqueta-campo");
        campo.getStyleClass().add("campo-formulario");
        campo.setMaxWidth(Double.MAX_VALUE);
        grid.add(etiqueta, columna, fila);
        grid.add(campo, columna + 1, fila);
        GridPane.setHgrow(campo, Priority.ALWAYS);
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
    private Consumer<String> onConfirmacion = mensaje -> { };
    public void alConfirmar(Consumer<String> accion) { onConfirmacion = accion; }
    public void mostrarError(String mensaje) { alerta(Alert.AlertType.ERROR, "Error", mensaje); }
    public void mostrarInfo(String mensaje) { onConfirmacion.accept(mensaje); }
    public void mostrarAviso(String mensaje) { alerta(Alert.AlertType.INFORMATION, "SICA", mensaje); }
    private void alerta(Alert.AlertType tipoAlerta, String titulo, String mensaje) {
        Alert alerta = new Alert(tipoAlerta); alerta.setTitle(titulo);
        alerta.setHeaderText(null); alerta.setContentText(mensaje); alerta.showAndWait();
    }
}
