package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.application.usecase.SolicitudPersona;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.TipoPersona;
import java.util.List;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Vista JavaFX para registrar, buscar y editar personas. */
public final class PersonaView {
    private final BorderPane root = new BorderPane();
    private final TableView<Persona> tabla = new TableView<>();
    private final TextField buscarDocumentoField = new TextField();
    private final ComboBox<TipoPersona> tipoPersonaCombo = new ComboBox<>();
    private final ComboBox<Empresa> empresaCombo = new ComboBox<>();
    private final TextField tipoDocumentoField = new TextField("CC");
    private final TextField numeroDocumentoField = new TextField();
    private final TextField nombresField = new TextField();
    private final TextField apellidosField = new TextField();
    private final TextField telefonoField = new TextField();
    private final TextField correoField = new TextField();
    private final TextField fotoUrlField = new TextField();
    private final Label estadoLabel = new Label("Estado: nuevo registro");
    private final Button crearButton = new Button("Registrar");
    private final Button actualizarButton = new Button("Guardar cambios");

    public PersonaView() {
        construir();
    }

    private void construir() {
        root.getStyleClass().add("modulo-root");
        Label titulo = new Label("Gestion de personas");
        titulo.getStyleClass().add("titulo-dashboard");
        buscarDocumentoField.setPromptText("Numero de documento");
        Button buscarButton = new Button("Buscar documento");
        buscarButton.getStyleClass().add("boton-neutro");
        buscarButton.setOnAction(evento -> onBuscar.run());
        HBox cabecera = new HBox(10, titulo, buscarDocumentoField, buscarButton);
        cabecera.getStyleClass().add("barra-modulo");

        agregarColumna("Documento", persona -> persona.getTipoDocumento()
                + " " + persona.getNumeroDocumento(), 145);
        agregarColumna("Nombre", Persona::getNombreCompleto, 220);
        agregarColumna("Tipo", persona -> persona.getTipoPersona().getNombreVisible(), 100);
        agregarColumna("Empresa", persona -> persona.getEmpresa() == null
                ? "Sin empresa" : persona.getEmpresa().getNombre(), 170);
        agregarColumna("Estado", persona -> persona.getEstadoAcceso().getNombre(), 180);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, persona) -> cargarFormulario(persona));

        tipoPersonaCombo.setItems(FXCollections.observableArrayList(TipoPersona.values()));
        tipoPersonaCombo.setValue(TipoPersona.VISITANTE);
        empresaCombo.setPromptText("Sin empresa");
        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(9);
        formulario.addRow(0, new Label("Tipo"), tipoPersonaCombo);
        formulario.addRow(1, new Label("Empresa"), empresaCombo);
        formulario.addRow(2, new Label("Tipo documento"), tipoDocumentoField);
        formulario.addRow(3, new Label("Numero"), numeroDocumentoField);
        formulario.addRow(4, new Label("Nombres"), nombresField);
        formulario.addRow(5, new Label("Apellidos"), apellidosField);
        formulario.addRow(6, new Label("Telefono"), telefonoField);
        formulario.addRow(7, new Label("Correo"), correoField);
        formulario.addRow(8, new Label("Foto URL"), fotoUrlField);
        estadoLabel.getStyleClass().add("texto-secundario");
        crearButton.getStyleClass().add("boton-principal");
        actualizarButton.getStyleClass().add("boton-neutro");
        crearButton.setOnAction(evento -> onCrear.run());
        actualizarButton.setOnAction(evento -> onActualizar.run());
        VBox tarjeta = new VBox(12, new Label("Datos de la persona"), formulario,
                estadoLabel, new HBox(10, crearButton, actualizarButton));
        tarjeta.getStyleClass().add("tarjeta-modulo");
        tarjeta.setPadding(new Insets(18));
        tarjeta.setPrefWidth(390);

        root.setTop(cabecera);
        root.setCenter(tabla);
        root.setRight(tarjeta);
        BorderPane.setMargin(tabla, new Insets(16));
        BorderPane.setMargin(tarjeta, new Insets(16, 16, 16, 0));
    }

    private void agregarColumna(String titulo,
            java.util.function.Function<Persona, String> valor, double ancho) {
        TableColumn<Persona, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(dato -> new SimpleStringProperty(valor.apply(dato.getValue())));
        columna.setPrefWidth(ancho);
        tabla.getColumns().add(columna);
    }

    private void cargarFormulario(Persona persona) {
        if (persona == null) return;
        tipoPersonaCombo.setValue(persona.getTipoPersona());
        empresaCombo.setValue(persona.getEmpresa());
        tipoDocumentoField.setText(persona.getTipoDocumento());
        numeroDocumentoField.setText(persona.getNumeroDocumento());
        nombresField.setText(persona.getNombres());
        apellidosField.setText(persona.getApellidos());
        telefonoField.setText(valor(persona.getTelefono()));
        correoField.setText(valor(persona.getCorreo()));
        fotoUrlField.setText(valor(persona.getFotoUrl()));
        estadoLabel.setText("Estado: " + persona.getEstadoAcceso().getNombre());
    }

    private String valor(String texto) { return texto == null ? "" : texto; }
    private Runnable onCrear = () -> { };
    private Runnable onActualizar = () -> { };
    private Runnable onBuscar = () -> { };

    public void alCrear(Runnable accion) { onCrear = accion; }
    public void alActualizar(Runnable accion) { onActualizar = accion; }
    public void alBuscar(Runnable accion) { onBuscar = accion; }
    public Parent getRoot() { return root; }
    public Persona getSeleccionada() { return tabla.getSelectionModel().getSelectedItem(); }
    public String getDocumentoBusqueda() { return buscarDocumentoField.getText(); }
    public String getTipoDocumentoBusqueda() { return tipoDocumentoField.getText(); }
    public void mostrarPersonas(List<Persona> personas) {
        tabla.setItems(FXCollections.observableArrayList(personas));
    }
    public void mostrarEmpresas(List<Empresa> empresas) {
        Empresa seleccionada = empresaCombo.getValue();
        empresaCombo.setItems(FXCollections.observableArrayList(empresas));
        empresaCombo.setValue(seleccionada);
    }
    public void seleccionar(Persona persona) {
        tabla.getSelectionModel().select(persona);
        tabla.scrollTo(persona);
        cargarFormulario(persona);
    }
    public SolicitudPersona getSolicitud() {
        Empresa empresa = empresaCombo.getValue();
        return new SolicitudPersona(empresa == null ? null : empresa.getId(),
                tipoDocumentoField.getText(), numeroDocumentoField.getText(),
                nombresField.getText(), apellidosField.getText(), telefonoField.getText(),
                correoField.getText(), tipoPersonaCombo.getValue(), fotoUrlField.getText());
    }
    public void permitirCrear(boolean permitido) { crearButton.setDisable(!permitido); }
    public void permitirActualizar(boolean permitido) { actualizarButton.setDisable(!permitido); }
    public void mostrarError(String mensaje) { alerta(Alert.AlertType.ERROR, mensaje); }
    public void mostrarInformacion(String mensaje) { alerta(Alert.AlertType.INFORMATION, mensaje); }
    private void alerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.setTitle("SICA");
        alerta.showAndWait();
    }
}
