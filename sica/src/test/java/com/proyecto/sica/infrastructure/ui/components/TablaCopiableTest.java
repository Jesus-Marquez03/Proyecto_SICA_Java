package com.proyecto.sica.infrastructure.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TablaCopiableTest {

    @BeforeAll
    static void iniciarJavaFx() throws InterruptedException {
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                inicio.countDown();
            });
            inicio.await();
        } catch (IllegalStateException yaIniciado) {
            // Otra prueba puede haber iniciado JavaFX en la misma JVM.
            Platform.setImplicitExit(false);
        }
    }

    @Test
    void construyeTextoCompletoDeCeldaYFilaUsandoSoloColumnasVisibles() throws Exception {
        FutureTask<Void> prueba = new FutureTask<>(() -> {
            TableView<Fila> tabla = new TableView<>();
            TableColumn<Fila, String> documento = columna("Documento", Fila::documento);
            TableColumn<Fila, String> nombre = columna("Nombre", Fila::nombre);
            TableColumn<Fila, String> interno = columna("Interno", Fila::interno);
            interno.setVisible(false);
            tabla.getColumns().add(documento);
            tabla.getColumns().add(nombre);
            tabla.getColumns().add(interno);
            tabla.getItems().add(new Fila("CC 123456789", "Nombre completo", "secreto"));
            TablaCopiable.habilitar(tabla);
            tabla.getSelectionModel().clearAndSelect(0, documento);

            assertEquals("CC 123456789", TablaCopiable.textoCeldaSeleccionada(tabla));
            assertEquals("CC 123456789\tNombre completo", TablaCopiable.textoFila(tabla, 0));
            return null;
        });
        Platform.runLater(prueba);
        prueba.get();
    }

    @Test
    void conservaTextoCompletoYAlturaLegibleEnComboBox() throws Exception {
        FutureTask<Void> prueba = new FutureTask<>(() -> {
            ComboBox<String> combo = new ComboBox<>();
            ComboBoxLegible.configurar(combo, valor -> valor);
            combo.getItems().add("Funcionario de Empresa con nombre completo");
            combo.setValue(combo.getItems().get(0));
            VBox root = new VBox(combo);
            Scene escena = new Scene(root, 360, 100);
            escena.getStylesheets().add(getClass().getResource("/styles/sica.css").toExternalForm());
            root.applyCss();
            root.layout();

            assertEquals("Funcionario de Empresa con nombre completo",
                    combo.getConverter().toString(combo.getValue()));
            assertEquals(combo.getValue(), combo.getTooltip().getText());
            assertTrue(combo.getHeight() >= 38);
            return null;
        });
        Platform.runLater(prueba);
        prueba.get();
    }

    private static TableColumn<Fila, String> columna(String titulo,
            java.util.function.Function<Fila, String> valor) {
        TableColumn<Fila, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(dato -> new SimpleStringProperty(valor.apply(dato.getValue())));
        return columna;
    }

    private record Fila(String documento, String nombre, String interno) { }
}
