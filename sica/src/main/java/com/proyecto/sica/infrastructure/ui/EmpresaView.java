package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.application.usecase.SolicitudEmpresa;
import com.proyecto.sica.domain.entity.Empresa;
import java.util.List;
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
        Label titulo = new Label("Gestion de empresas");
        titulo.getStyleClass().add("titulo-dashboard");
        buscarField.setPromptText("Buscar por nombre");
        Button buscarButton = new Button("Buscar");
        buscarButton.getStyleClass().add("boton-neutro");
        buscarButton.setOnAction(evento -> onBuscar.run());
        HBox cabecera = new HBox(10, titulo, buscarField, buscarButton);
        cabecera.getStyleClass().add("barra-modulo");

        agregarColumna("NIT", empresa -> empresa.getNit(), 120);
        agregarColumna("Nombre", empresa -> empresa.getNombre(), 220);
        agregarColumna("Telefono", empresa -> valor(empresa.getTelefono()), 130);
        agregarColumna("Correo", empresa -> valor(empresa.getCorreo()), 210);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, empresa) -> cargarFormulario(empresa));

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.addRow(0, new Label("NIT"), nitField);
        formulario.addRow(1, new Label("Nombre"), nombreField);
        formulario.addRow(2, new Label("Direccion"), direccionField);
        formulario.addRow(3, new Label("Telefono"), telefonoField);
        formulario.addRow(4, new Label("Correo"), correoField);
        crearButton.getStyleClass().add("boton-principal");
        actualizarButton.getStyleClass().add("boton-neutro");
        crearButton.setOnAction(evento -> onCrear.run());
        actualizarButton.setOnAction(evento -> onActualizar.run());
        HBox acciones = new HBox(10, crearButton, actualizarButton);
        VBox tarjeta = new VBox(14, new Label("Datos de la empresa"), formulario, acciones);
        tarjeta.getStyleClass().add("tarjeta-modulo");
        tarjeta.setPadding(new Insets(20));
        tarjeta.setPrefWidth(360);

        root.setTop(cabecera);
        root.setCenter(tabla);
        root.setRight(tarjeta);
        BorderPane.setMargin(tabla, new Insets(16));
        BorderPane.setMargin(tarjeta, new Insets(16, 16, 16, 0));
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

    public void alCrear(Runnable accion) { onCrear = accion; }
    public void alActualizar(Runnable accion) { onActualizar = accion; }
    public void alBuscar(Runnable accion) { onBuscar = accion; }
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
        mostrarAlerta(Alert.AlertType.INFORMATION, "SICA", mensaje);
    }
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.setTitle(titulo);
        alerta.showAndWait();
    }
}
