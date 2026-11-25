package com.proyecto.sica.infrastructure.ui.components;

import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/** Agrega copia de celdas y filas visibles sin consultar nuevamente los datos. */
public final class TablaCopiable {
    private TablaCopiable() { }

    public static <S> void habilitar(TableView<S> tabla) {
        tabla.getSelectionModel().setCellSelectionEnabled(true);
        tabla.addEventFilter(KeyEvent.KEY_PRESSED, evento -> {
            if (evento.isShortcutDown() && evento.getCode() == KeyCode.C) {
                copiarCeldaSeleccionada(tabla);
                evento.consume();
            }
        });
        tabla.getColumns().forEach(columna -> configurarColumna(tabla, columna));
    }

    private static <S, T> void configurarColumna(TableView<S> tabla,
            TableColumn<S, T> columna) {
        columna.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(T valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null : String.valueOf(valor));
            }

            {
                setOnContextMenuRequested(evento -> {
                    if (isEmpty() || getIndex() < 0 || getIndex() >= tabla.getItems().size()) {
                        return;
                    }
                    tabla.getSelectionModel().clearAndSelect(getIndex(), columna);
                    crearMenu(tabla, getIndex()).show(this,
                            evento.getScreenX(), evento.getScreenY());
                    evento.consume();
                });
            }
        });
    }

    private static <S> ContextMenu crearMenu(TableView<S> tabla, int fila) {
        MenuItem copiarCelda = new MenuItem("Copiar celda");
        copiarCelda.setOnAction(evento -> copiarCeldaSeleccionada(tabla));
        MenuItem copiarFila = new MenuItem("Copiar fila");
        copiarFila.setOnAction(evento -> copiarFila(tabla, fila));
        ContextMenu menu = new ContextMenu(copiarCelda, copiarFila);
        menu.getStyleClass().add("menu-copia-tabla");
        return menu;
    }

    private static void copiarCeldaSeleccionada(TableView<?> tabla) {
        copiar(textoCeldaSeleccionada(tabla));
    }

    static String textoCeldaSeleccionada(TableView<?> tabla) {
        @SuppressWarnings("rawtypes")
        List<TablePosition> celdas = tabla.getSelectionModel().getSelectedCells();
        if (celdas.isEmpty()) return "";
        @SuppressWarnings("rawtypes")
        TablePosition posicion = celdas.get(0);
        Object valor = posicion.getTableColumn().getCellData(posicion.getRow());
        return valor == null ? "" : String.valueOf(valor);
    }

    private static void copiarFila(TableView<?> tabla, int fila) {
        copiar(textoFila(tabla, fila));
    }

    static String textoFila(TableView<?> tabla, int fila) {
        if (fila < 0 || fila >= tabla.getItems().size()) return "";
        return tabla.getVisibleLeafColumns().stream()
                .map(columna -> columna.getCellData(fila))
                .map(valor -> valor == null ? "" : String.valueOf(valor))
                .collect(Collectors.joining("\t"));
    }

    private static void copiar(String texto) {
        if (texto == null || texto.isEmpty()) return;
        ClipboardContent contenido = new ClipboardContent();
        contenido.putString(texto);
        Clipboard.getSystemClipboard().setContent(contenido);
    }
}
