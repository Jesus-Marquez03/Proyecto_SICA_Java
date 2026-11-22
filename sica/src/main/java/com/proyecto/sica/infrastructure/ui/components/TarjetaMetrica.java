package com.proyecto.sica.infrastructure.ui.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Tarjeta reutilizable para un indicador numerico y su contexto. */
public final class TarjetaMetrica extends VBox {
    private final Label valor = new Label("--");
    private final Label detalle = new Label();

    public TarjetaMetrica(String titulo, TipoIcono icono) {
        getStyleClass().addAll("tarjeta-bento", "tarjeta-metrica");
        Label tituloLabel = new Label(titulo);
        tituloLabel.getStyleClass().add("titulo-tarjeta");
        Node grafico = IconosSica.crear(icono);
        HBox encabezado = new HBox(10, tituloLabel, grafico);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tituloLabel, Priority.ALWAYS);
        valor.getStyleClass().add("valor-metrica");
        detalle.getStyleClass().add("detalle-metrica");
        detalle.setWrapText(true);
        getChildren().addAll(encabezado, valor, detalle);
    }

    public void actualizar(long nuevoValor, String nuevoDetalle) {
        valor.setText(String.valueOf(nuevoValor));
        detalle.setText(nuevoDetalle == null ? "" : nuevoDetalle);
    }
}
