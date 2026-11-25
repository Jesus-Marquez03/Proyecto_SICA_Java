package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.application.usecase.SolicitudEmpresa;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.infrastructure.ui.components.EncabezadoModulo;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Vista de empresas. Solo captura datos y muestra resultados. */
public final class EmpresaView {
    private final BorderPane root = new BorderPane();
    private final TableView<Empresa> tabla = new TableView<>();
    private final TextField buscarField = new TextField();
    private final TextField nitField = new TextField();
    private final TextField nombreField = new TextField();
    private final TextField direccionField = new TextField();
    private final TextField telefonoField = new TextField();
    private final TextField correoField = new TextField();
    private final Button crearButton = new Button("Registrar");
    private final Button actualizarButton = new Button("Guardar cambios");

    public EmpresaView() {
        construir();
    }

    private void construir() {
        root.getStyleClass().add("modulo-root");
        buscarField.setPromptText("Buscar por nombre");
        Button buscarButton = new Button("Buscar");
        buscarButton.getStyleClass().add("boton-neutro");
        buscarButton.setOnAction(evento -> onBuscar.run());
        HBox filtros = new HBox(10, buscarField, buscarButton);
        filtros.getStyleClass().add("panel-filtros");
        VBox cabecera = new VBox(16,
                new EncabezadoModulo("Gestion de empresas",
                        "Administra las empresas registradas en Zona Acme.", TipoIcono.EMPRESAS),
                filtros);

        agregarColumna("NIT", empresa -> empresa.getNit(), 120);
        agregarColumna("Nombre", empresa -> empresa.getNombre(), 220);
        agregarColumna("Telefono", empresa -> valor(empresa.getTelefono()), 130);
        agregarColumna("Correo", empresa -> valor(empresa.getCorreo()), 210);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, empresa) -> cargarFormulario(empresa));

        GridPane formulario = new GridPane();
        formulario.getStyleClass().add("formulario-administrativo");
        formulario.setHgap(12);
        formulario.setVgap(13);
        agregarCampo(formulario, 0, "NIT", nitField);
        agregarCampo(formulario, 1, "Nombre", nombreField);
        agregarCampo(formulario, 2, "Direccion", direccionField);
        agregarCampo(formulario, 3, "Telefono", telefonoField);
        agregarCampo(formulario, 4, "Correo", correoField);
        crearButton.getStyleClass().add("boton-principal");
        actualizarButton.getStyleClass().add("boton-neutro");
        crearButton.setOnAction(evento -> onCrear.run());
        actualizarButton.setOnAction(evento -> onActualizar.run());
        HBox acciones = new HBox(10, crearButton, actualizarButton);
        Label tituloFormulario = new Label("Datos de la empresa");
        tituloFormulario.getStyleClass().add("titulo-formulario-interno");
        VBox tarjeta = new VBox(17, tituloFormulario, formulario, acciones);
        tarjeta.getStyleClass().add("tarjeta-formulario");
        tarjeta.setPrefWidth(360);
        tarjeta.setMinWidth(320);

        root.setTop(cabecera);
        root.setCenter(tabla);
        root.setRight(tarjeta);
        BorderPane.setMargin(tabla, new Insets(16, 14, 0, 0));
        BorderPane.setMargin(tarjeta, new Insets(16, 0, 0, 0));
    }

    private void agregarCampo(GridPane formulario, int fila, String texto, TextField campo) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("etiqueta-campo");
        campo.getStyleClass().add("campo-formulario");
        campo.setMaxWidth(Double.MAX_VALUE);
        formulario.addRow(fila, etiqueta, campo);
        GridPane.setHgrow(campo, javafx.scene.layout.Priority.ALWAYS);
    }

    private void agregarColumna(String titulo,
            java.util.function.Function<Empresa, String> valor, double ancho) {
        TableColumn<Empresa, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(dato -> new SimpleStringProperty(valor.apply(dato.getValue())));
        columna.setPrefWidth(ancho);
        tabla.getColumns().add(columna);
    }

    private String valor(String texto) { return texto == null ? "" : texto; }

    private void cargarFormulario(Empresa empresa) {
        if (empresa == null) return;
        nitField.setText(empresa.getNit());
        nombreField.setText(empresa.getNombre());
        direccionField.setText(valor(empresa.getDireccion()));
        telefonoField.setText(valor(empresa.getTelefono()));
        correoField.setText(valor(empresa.getCorreo()));
    }

    private Runnable onCrear = () -> { };
    private Runnable onActualizar = () -> { };
    private Runnable onBuscar = () -> { };
    private Consumer<String> onConfirmacion = mensaje -> { };

    public void alCrear(Runnable accion) { onCrear = accion; }
    public void alActualizar(Runnable accion) { onActualizar = accion; }
    public void alBuscar(Runnable accion) { onBuscar = accion; }
    public void alConfirmar(Consumer<String> accion) { onConfirmacion = accion; }
    public Parent getRoot() { return root; }
    public Empresa getSeleccionada() { return tabla.getSelectionModel().getSelectedItem(); }
    public String getTextoBusqueda() { return buscarField.getText(); }
    public SolicitudEmpresa getSolicitud() {
        return new SolicitudEmpresa(nitField.getText(), nombreField.getText(),
                direccionField.getText(), telefonoField.getText(), correoField.getText());
    }
    public void mostrarEmpresas(List<Empresa> empresas) {
        tabla.setItems(FXCollections.observableArrayList(empresas));
    }
    public void permitirCrear(boolean permitido) { crearButton.setDisable(!permitido); }
    public void permitirActualizar(boolean permitido) { actualizarButton.setDisable(!permitido); }
    public void mostrarError(String mensaje) { mostrarAlerta(Alert.AlertType.ERROR, "Error", mensaje); }
    public void mostrarInformacion(String mensaje) {
        onConfirmacion.accept(mensaje);
    }
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.showAndWait();
    }
}
