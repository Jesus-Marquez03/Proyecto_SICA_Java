package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.infrastructure.ui.components.ComboBoxLegible;
import com.proyecto.sica.infrastructure.ui.components.TablaCopiable;
import java.util.List;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/** Vista de consulta y cambio del estado administrativo de acceso. */
public final class EstadoAccesoView {
    private final BorderPane root = new BorderPane();
    private final TableView<Persona> tabla = new TableView<>();
    private final ComboBox<EstadoAcceso> estadoCombo = new ComboBox<>();
    private final Button cambiarButton = new Button("Cambiar estado");
    private Consumer<String> onConfirmacion = mensaje -> { };

    public EstadoAccesoView() {
        Label titulo = new Label("Estados de acceso");
        titulo.getStyleClass().add("titulo-dashboard");
        estadoCombo.setPromptText("Nuevo estado");
        ComboBoxLegible.configurar(estadoCombo, EstadoAcceso::getNombre);
        cambiarButton.getStyleClass().add("boton-principal");
        cambiarButton.setOnAction(evento -> onCambiar.run());
        HBox cabecera = new HBox(12, titulo, estadoCombo, cambiarButton);
        cabecera.getStyleClass().add("barra-modulo");

        agregarColumna("Documento", persona -> persona.getTipoDocumento()
                + " " + persona.getNumeroDocumento());
        agregarColumna("Persona", Persona::getNombreCompleto);
        agregarColumna("Empresa", persona -> persona.getEmpresa() == null
                ? "Sin empresa" : persona.getEmpresa().getNombre());
        agregarColumna("Estado actual", persona -> persona.getEstadoAcceso().getNombre());
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TablaCopiable.habilitar(tabla);
        root.getStyleClass().add("modulo-root");
        root.setTop(cabecera);
        root.setCenter(tabla);
        BorderPane.setMargin(tabla, new Insets(16));
    }

    private void agregarColumna(String titulo,
            java.util.function.Function<Persona, String> valor) {
        TableColumn<Persona, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(dato -> new SimpleStringProperty(valor.apply(dato.getValue())));
        tabla.getColumns().add(columna);
    }

    private Runnable onCambiar = () -> { };
    public void alCambiar(Runnable accion) { onCambiar = accion; }
    public void alConfirmar(Consumer<String> accion) { onConfirmacion = accion; }
    public Parent getRoot() { return root; }
    public Persona getPersonaSeleccionada() { return tabla.getSelectionModel().getSelectedItem(); }
    public EstadoAcceso getEstadoSeleccionado() { return estadoCombo.getValue(); }
    public void mostrarPersonas(List<Persona> personas) {
        tabla.setItems(FXCollections.observableArrayList(personas));
    }
    public void mostrarEstados(List<EstadoAcceso> estados) {
        estadoCombo.setItems(FXCollections.observableArrayList(estados));
    }
    public void permitirCambio(boolean permitido) { cambiarButton.setDisable(!permitido); }
    public void mostrarError(String mensaje) { alerta(Alert.AlertType.ERROR, mensaje); }
    public void mostrarInformacion(String mensaje) { alerta(Alert.AlertType.INFORMATION, mensaje); }
    public void mostrarConfirmacion(String mensaje) { onConfirmacion.accept(mensaje); }
    private void alerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.setTitle("SICA");
        alerta.showAndWait();
    }
}
