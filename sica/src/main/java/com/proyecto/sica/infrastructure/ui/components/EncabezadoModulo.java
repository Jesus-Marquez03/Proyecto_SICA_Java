package com.proyecto.sica.infrastructure.ui.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Encabezado consistente para las pantallas internas. */
public final class EncabezadoModulo extends HBox {
    public EncabezadoModulo(String titulo, String descripcion, TipoIcono icono) {
        getStyleClass().add("encabezado-modulo-final");
        setAlignment(Pos.CENTER_LEFT);
        Label tituloLabel = new Label(titulo);
        tituloLabel.getStyleClass().add("titulo-pagina");
        Label descripcionLabel = new Label(descripcion);
        descripcionLabel.getStyleClass().add("subtitulo-pagina");
        descripcionLabel.setWrapText(true);
        VBox textos = new VBox(4, tituloLabel, descripcionLabel);
        getChildren().addAll(IconosSica.crear(icono), textos);
    }
}
