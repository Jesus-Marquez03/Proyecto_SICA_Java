package com.proyecto.sica.infrastructure.ui.components;

import java.util.function.Function;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;

/** Configura selectores con texto completo, celdas legibles y ayuda contextual. */
public final class ComboBoxLegible {
    private ComboBoxLegible() { }

    public static <T> void configurar(ComboBox<T> combo, Function<T, String> texto) {
        if (!combo.getStyleClass().contains("combo-legible")) {
            combo.getStyleClass().add("combo-legible");
        }
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(T valor) {
                return valor == null ? "" : texto.apply(valor);
            }

            @Override
            public T fromString(String valor) {
                return null;
            }
        });
        combo.setCellFactory(lista -> new CeldaLegible<>(texto));
        combo.valueProperty().addListener((observable, anterior, actual) ->
                combo.setTooltip(tooltip(actual == null ? null : texto.apply(actual))));
        T actual = combo.getValue();
        combo.setTooltip(tooltip(actual == null ? null : texto.apply(actual)));
    }

    private static Tooltip tooltip(String texto) {
        return texto == null || texto.isBlank() ? null : new Tooltip(texto);
    }

    private static final class CeldaLegible<T> extends ListCell<T> {
        private final Function<T, String> texto;

        private CeldaLegible(Function<T, String> texto) {
            this.texto = texto;
        }

        @Override
        protected void updateItem(T valor, boolean vacia) {
            super.updateItem(valor, vacia);
            String contenido = vacia || valor == null ? null : texto.apply(valor);
            setText(contenido);
            setTooltip(tooltip(contenido));
        }
    }
}
