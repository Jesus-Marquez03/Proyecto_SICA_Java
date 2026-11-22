package com.proyecto.sica.infrastructure.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.proyecto.sica.infrastructure.ui.components.IconosSica;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import org.junit.jupiter.api.Test;

/** Verifica que cada opcion visual tenga un SVG valido y no un emoji. */
class IconosSicaTest {
    @Test
    void todosLosIconosTienenRutaVectorial() {
        for (TipoIcono tipo : TipoIcono.values()) {
            Node icono = IconosSica.crear(tipo);
            StackPane contenedor = (StackPane) icono;
            SVGPath ruta = (SVGPath) contenedor.getChildren().get(0);
            assertFalse(ruta.getContent().isBlank(), tipo.name());
            assertEquals(24, contenedor.getPrefWidth());
            assertEquals(24, contenedor.getPrefHeight());
        }
    }
}
