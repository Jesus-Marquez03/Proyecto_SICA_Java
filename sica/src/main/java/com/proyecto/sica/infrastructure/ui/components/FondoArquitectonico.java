package com.proyecto.sica.infrastructure.ui.components;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/** Fondo abstracto construido con formas JavaFX; no depende de imagenes externas. */
public final class FondoArquitectonico extends Pane {
    public FondoArquitectonico() {
        setMouseTransparent(true);
        getStyleClass().add("fondo-arquitectonico");
        for (int i = 0; i < 8; i++) {
            Circle aro = new Circle(160 + i * 18);
            aro.setFill(Color.TRANSPARENT);
            aro.setStroke(Color.rgb(255, 255, 255, 0.32));
            aro.setStrokeWidth(38);
            aro.setLayoutX(100 + (i % 4) * 360);
            aro.setLayoutY(i < 4 ? 40 : 820);
            getChildren().add(aro);
        }
        Rectangle velo = new Rectangle();
        velo.widthProperty().bind(widthProperty());
        velo.heightProperty().bind(heightProperty());
        velo.setFill(Color.rgb(232, 232, 232, 0.48));
        getChildren().add(velo);
    }
}
