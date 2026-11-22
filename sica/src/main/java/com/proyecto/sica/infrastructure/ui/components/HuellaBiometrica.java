package com.proyecto.sica.infrastructure.ui.components;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/** Huella formada por crestas SVG independientes para revelar cada segmento. */
public final class HuellaBiometrica extends Pane {
    private static final String[] CRESTAS = {
        "M12 2C6.5 2 2.5 6.3 2.5 11.5C2.5 15.8 1.7 18.5 0.5 21",
        "M12 4.2C16.5 4.2 20 7.7 20 12C20 16.4 18.6 19.3 16.4 22",
        "M12 6.2C8.7 6.2 6 8.8 6 12C6 15.7 5 18.2 3.5 20.2",
        "M12 8.2C14.2 8.2 16 10 16 12.2C16 16.3 14.8 19.2 12.4 21.8",
        "M9.2 11.2C9.2 15 8.4 17.4 6.8 19.4",
        "M12 10.4V13.2C12 16.2 11.4 18 10 19.8"
    };

    private final List<SVGPath> segmentos = new ArrayList<>();
    private SequentialTransition revelado;

    public HuellaBiometrica() {
        setMinSize(190, 215);
        setPrefSize(190, 215);
        for (String cresta : CRESTAS) {
            SVGPath segmento = new SVGPath();
            segmento.setContent(cresta);
            segmento.getStyleClass().add("segmento-huella");
            segmento.setScaleX(7.2);
            segmento.setScaleY(7.2);
            segmento.setLayoutX(10);
            segmento.setLayoutY(15);
            segmentos.add(segmento);
            getChildren().add(segmento);
        }
    }

    public void reiniciar() {
        detener();
        segmentos.forEach(segmento -> segmento.setOpacity(0.22));
    }

    public void mostrarCompleta() {
        segmentos.forEach(segmento -> segmento.setOpacity(1));
    }

    public void revelar() {
        detener();
        revelado = new SequentialTransition();
        for (SVGPath segmento : segmentos) {
            FadeTransition transicion = new FadeTransition(Duration.millis(220), segmento);
            transicion.setFromValue(0.22);
            transicion.setToValue(1);
            revelado.getChildren().add(transicion);
        }
        revelado.play();
    }

    public void detener() {
        if (revelado != null) revelado.stop();
    }
}
