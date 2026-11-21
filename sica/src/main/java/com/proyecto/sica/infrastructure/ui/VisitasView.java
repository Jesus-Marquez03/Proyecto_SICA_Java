package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Vista desacoplada para guarda, funcionario e historial de visitas. */
public final class VisitasView {
    private final BorderPane root = new BorderPane();
    private final TextField tipoDocumentoField = new TextField("CC");
    private final TextField documentoField = new TextField();
    private final Label personaLabel = new Label("Persona: sin seleccionar");
    private final Label accesoLabel = new Label("Estado de acceso: -");
    private final Label fotoLabel = new Label("Sin fotografia");
    private final ImageView fotoView = new ImageView();
    private final Tab guardaTab = new Tab("Panel del guarda");
    private final Tab funcionarioTab = new Tab("Panel del funcionario");
    private final Tab historialTab = new Tab("Historial");
    private final TableView<Visita> visitasTable = crearTabla();
    private final TableView<Visita> pendientesTable = crearTabla();
    private final TableView<Visita> historialTable = crearTabla();
    private final ComboBox<Empresa> empresaCombo = new ComboBox<>();
    private final ComboBox<Usuario> anfitrionCombo = new ComboBox<>();
    private final TextArea motivoGuardaArea = new TextArea();
    private final TextField placaGuardaField = new TextField();
    private final Button noAnunciadaButton = new Button("Solicitar no anunciada");
    private final Button paseButton = new Button("Solicitar pase temporal");
    private final Button checkInButton = new Button("Check-in");
    private final Button checkOutButton = new Button("Check-out");
    private final Button regularizarButton = new Button("Regularizar salida");
    private final DatePicker fechaProgramadaPicker = new DatePicker(LocalDate.now().plusDays(1));
    private final TextField horaProgramadaField = new TextField("09:00");
    private final TextArea motivoFuncionarioArea = new TextArea();
    private final TextField placaFuncionarioField = new TextField();
    private final Button prerregistrarButton = new Button("Prerregistrar invitado");
    private final Button aprobarButton = new Button("Aprobar");
    private final Button rechazarButton = new Button("Rechazar");
    private final TextField historialDocumentoField = new TextField();
    private final DatePicker historialDesdePicker = new DatePicker();
    private final DatePicker historialHastaPicker = new DatePicker();
    private final TextField historialEstadoField = new TextField();

    public VisitasView() {
        construir();
    }

    private void construir() {
        root.getStyleClass().add("modulo-root");
        Label titulo = new Label("Control de visitas");
        titulo.getStyleClass().add("titulo-dashboard");
        documentoField.setPromptText("Numero de documento");
        Button buscarButton = new Button("Buscar persona");
        buscarButton.getStyleClass().add("boton-neutro");
        buscarButton.setOnAction(evento -> onBuscarPersona.run());
        fotoView.setFitWidth(54);
        fotoView.setFitHeight(54);
        fotoView.setPreserveRatio(true);
        VBox datos = new VBox(3, personaLabel, accesoLabel, fotoLabel);
        HBox cabecera = new HBox(10, titulo, tipoDocumentoField, documentoField,
                buscarButton, fotoView, datos);
        cabecera.getStyleClass().add("barra-modulo");

        construirGuarda();
        construirFuncionario();
        construirHistorial();
        guardaTab.setClosable(false);
        funcionarioTab.setClosable(false);
        historialTab.setClosable(false);
        TabPane tabs = new TabPane(guardaTab, funcionarioTab, historialTab);
        root.setTop(cabecera);
        root.setCenter(tabs);
    }

    private void construirGuarda() {
        empresaCombo.setPromptText("Empresa destino");
        empresaCombo.setOnAction(evento -> onEmpresaSeleccionada.run());
        anfitrionCombo.setPromptText("Funcionario anfitrion");
        motivoGuardaArea.setPromptText("Motivo o justificacion");
        motivoGuardaArea.setPrefRowCount(2);
        placaGuardaField.setPromptText("Placa opcional");
        noAnunciadaButton.setOnAction(evento -> onNoAnunciada.run());
        paseButton.setOnAction(evento -> onPase.run());
        checkInButton.setOnAction(evento -> onCheckIn.run());
        checkOutButton.setOnAction(evento -> onCheckOut.run());
        regularizarButton.setOnAction(evento -> onRegularizar.run());
        estilizarAcciones(noAnunciadaButton, paseButton, checkInButton,
                checkOutButton, regularizarButton);
        GridPane formulario = new GridPane();
        formulario.setHgap(8);
        formulario.setVgap(8);
        formulario.addRow(0, empresaCombo, anfitrionCombo, placaGuardaField);
        formulario.add(motivoGuardaArea, 0, 1, 3, 1);
        VBox contenido = new VBox(10, formulario,
                new HBox(8, noAnunciadaButton, paseButton, checkInButton,
                        checkOutButton, regularizarButton), visitasTable);
        contenido.setPadding(new Insets(14));
        guardaTab.setContent(contenido);
    }

    private void construirFuncionario() {
        motivoFuncionarioArea.setPromptText("Motivo de la visita");
        motivoFuncionarioArea.setPrefRowCount(2);
        placaFuncionarioField.setPromptText("Placa opcional");
        prerregistrarButton.setOnAction(evento -> onPrerregistrar.run());
        aprobarButton.setOnAction(evento -> onAprobar.run());
        rechazarButton.setOnAction(evento -> onRechazar.run());
        estilizarAcciones(prerregistrarButton, aprobarButton, rechazarButton);
        HBox formulario = new HBox(8, fechaProgramadaPicker, horaProgramadaField,
                placaFuncionarioField, prerregistrarButton);
        VBox contenido = new VBox(10, formulario, motivoFuncionarioArea,
                new Label("Solicitudes pendientes asignadas"), pendientesTable,
                new HBox(8, aprobarButton, rechazarButton));
        contenido.setPadding(new Insets(14));
        funcionarioTab.setContent(contenido);
    }

    private void construirHistorial() {
        historialDocumentoField.setPromptText("Documento opcional");
        historialEstadoField.setPromptText("Estado opcional");
        Button consultar = new Button("Consultar historial");
        consultar.getStyleClass().add("boton-neutro");
        consultar.setOnAction(evento -> onHistorial.run());
        HBox filtros = new HBox(8, historialDocumentoField, historialDesdePicker,
                historialHastaPicker, historialEstadoField, consultar);
        VBox contenido = new VBox(10, filtros, historialTable);
        contenido.setPadding(new Insets(14));
        historialTab.setContent(contenido);
    }

    private TableView<Visita> crearTabla() {
        TableView<Visita> tabla = new TableView<>();
        agregarColumna(tabla, "Solicitud", Visita::getNumeroSolicitud);
        agregarColumna(tabla, "Persona", visita -> visita.getVisitante().getNombreCompleto());
        agregarColumna(tabla, "Documento", visita -> visita.getVisitante().getNumeroDocumento());
        agregarColumna(tabla, "Empresa", visita -> visita.getAnfitrion().getEmpresaNombre());
        agregarColumna(tabla, "Anfitrion", visita -> visita.getAnfitrion().getNombreCompleto());
        agregarColumna(tabla, "Tipo", visita -> visita.getTipoSolicitud().getNombreVisible());
        agregarColumna(tabla, "Estado", visita -> visita.getEstado().getNombre());
        agregarColumna(tabla, "Programada", visita -> visita.getFechaProgramada() == null
                ? "" : visita.getFechaProgramada().toString().replace('T', ' '));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(360);
        return tabla;
    }

    private void agregarColumna(TableView<Visita> tabla, String titulo,
            java.util.function.Function<Visita, String> valor) {
        TableColumn<Visita, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(dato -> new SimpleStringProperty(
                valor.apply(dato.getValue()) == null ? "" : valor.apply(dato.getValue())));
        tabla.getColumns().add(columna);
    }

    private void estilizarAcciones(Button... botones) {
        for (Button boton : botones) boton.getStyleClass().add("boton-neutro");
    }

    public void mostrarPersona(Persona persona) {
        personaLabel.setText("Persona: " + persona.getNombreCompleto()
                + " (" + persona.getTipoDocumento() + " " + persona.getNumeroDocumento() + ")");
        accesoLabel.setText("Estado de acceso: " + persona.getEstadoAcceso().getNombre());
        fotoView.setImage(null);
        if (persona.getFotoUrl() == null || persona.getFotoUrl().isBlank()) {
            fotoLabel.setText("Sin fotografia");
        } else {
            fotoLabel.setText(persona.getFotoUrl());
            try {
                fotoView.setImage(new Image(persona.getFotoUrl(), true));
            } catch (IllegalArgumentException excepcion) {
                fotoLabel.setText("URL de fotografia no disponible");
            }
        }
    }

    public void mostrarVisitas(List<Visita> visitas) {
        visitasTable.setItems(FXCollections.observableArrayList(visitas));
    }
    public void mostrarPendientes(List<Visita> visitas) {
        pendientesTable.setItems(FXCollections.observableArrayList(visitas));
    }
    public void mostrarHistorial(List<Visita> visitas) {
        historialTable.setItems(FXCollections.observableArrayList(visitas));
    }
    public void mostrarEmpresas(List<Empresa> empresas) {
        empresaCombo.setItems(FXCollections.observableArrayList(empresas));
    }
    public void mostrarAnfitriones(List<Usuario> usuarios) {
        anfitrionCombo.setItems(FXCollections.observableArrayList(usuarios));
    }

    public Parent getRoot() { return root; }
    public String getTipoDocumento() { return tipoDocumentoField.getText(); }
    public String getDocumento() { return documentoField.getText(); }
    public Empresa getEmpresa() { return empresaCombo.getValue(); }
    public Usuario getAnfitrion() { return anfitrionCombo.getValue(); }
    public String getMotivoGuarda() { return motivoGuardaArea.getText(); }
    public String getPlacaGuarda() { return placaGuardaField.getText(); }
    public Visita getVisitaSeleccionada() { return visitasTable.getSelectionModel().getSelectedItem(); }
    public Visita getPendienteSeleccionada() { return pendientesTable.getSelectionModel().getSelectedItem(); }
    public LocalDate getFechaProgramada() { return fechaProgramadaPicker.getValue(); }
    public String getHoraProgramada() { return horaProgramadaField.getText(); }
    public String getMotivoFuncionario() { return motivoFuncionarioArea.getText(); }
    public String getPlacaFuncionario() { return placaFuncionarioField.getText(); }
    public String getHistorialDocumento() { return historialDocumentoField.getText(); }
    public LocalDate getHistorialDesde() { return historialDesdePicker.getValue(); }
    public LocalDate getHistorialHasta() { return historialHastaPicker.getValue(); }
    public String getHistorialEstado() { return historialEstadoField.getText(); }

    public void configurarPermisos(boolean guarda, boolean funcionario, boolean historial) {
        guardaTab.setDisable(!guarda);
        funcionarioTab.setDisable(!funcionario);
        historialTab.setDisable(!historial);
    }

    private Runnable onBuscarPersona = () -> { };
    private Runnable onEmpresaSeleccionada = () -> { };
    private Runnable onNoAnunciada = () -> { };
    private Runnable onPase = () -> { };
    private Runnable onCheckIn = () -> { };
    private Runnable onCheckOut = () -> { };
    private Runnable onRegularizar = () -> { };
    private Runnable onPrerregistrar = () -> { };
    private Runnable onAprobar = () -> { };
    private Runnable onRechazar = () -> { };
    private Runnable onHistorial = () -> { };
    public void alBuscarPersona(Runnable a) { onBuscarPersona = a; }
    public void alSeleccionarEmpresa(Runnable a) { onEmpresaSeleccionada = a; }
    public void alNoAnunciada(Runnable a) { onNoAnunciada = a; }
    public void alPase(Runnable a) { onPase = a; }
    public void alCheckIn(Runnable a) { onCheckIn = a; }
    public void alCheckOut(Runnable a) { onCheckOut = a; }
    public void alRegularizar(Runnable a) { onRegularizar = a; }
    public void alPrerregistrar(Runnable a) { onPrerregistrar = a; }
    public void alAprobar(Runnable a) { onAprobar = a; }
    public void alRechazar(Runnable a) { onRechazar = a; }
    public void alConsultarHistorial(Runnable a) { onHistorial = a; }

    public void mostrarError(String mensaje) { alerta(Alert.AlertType.ERROR, mensaje); }
    public void mostrarInformacion(String mensaje) { alerta(Alert.AlertType.INFORMATION, mensaje); }
    private void alerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setTitle("SICA");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
