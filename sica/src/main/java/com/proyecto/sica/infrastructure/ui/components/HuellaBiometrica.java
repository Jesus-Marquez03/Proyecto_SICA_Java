package com.proyecto.sica.infrastructure.ui.components;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/** Huella vectorial completa cuyas crestas se dibujan progresivamente. */
public final class HuellaBiometrica extends Pane {
    private static final double ANCHO = 60;
    private static final double ALTO = 70;
    private static final double LONGITUD_TRAZO = 190;

    /*
     * Las rutas comparten un sistema de coordenadas de 60 x 70. El grupo aplica
     * solo la proporcion horizontal medida en la referencia y conserva el centro.
     */
    private static final String[] CRESTAS = {
        // Arcos superiores cortos y separados de la referencia.
        "M22 4C27 2.5 33 2.5 38 4",
        "M17 7.5C24 4.5 36 4.5 43 8",
        "M13 12C22 7.5 38 7.5 47 13",
        "M10 17C20 10.5 39 10.5 50 18",

        // Retornos inferiores concentricos que completan la silueta alargada.
        "M8 22C4 34 5 48 12 58C20 68 35 70 46 63C53 58 56 50 55 42",
        "M12 24C8 34 9 46 15 54C22 63 35 66 44 60C51 55 53 46 50 38",
        "M16 26C12 35 13 44 18 50C23 57 33 60 41 55C47 51 48 44 45 38",

        // Crestas superiores que rodean el primer giro de la S.
        "M51 31C47 20 36 15 25 17C15 19 10 27 11 37",
        "M47 30C43 22 35 18 27 20C18 22 14 29 16 36C18 43 25 47 33 46",
        "M43 31C40 25 33 22 27 23C21 24 18 28 19 33C20 38 25 41 31 40C37 39 42 42 43 47C44 52 40 57 35 59",

        // Nucleo en S y sus retornos paralelos, como en el fotograma original.
        "M40 29C37 26 31 25 27 27C23 29 23 33 26 35C29 37 35 36 39 38C45 40 47 46 45 52C42 59 34 63 26 61C19 60 14 55 12 49",
        "M37 31C34 29 29 29 27 30C25 31 26 33 28 33C34 33 40 34 43 39C47 45 45 52 40 57C34 62 24 61 18 55",
        "M34 36C40 36 43 39 44 43",
        "M39 43C41 47 39 51 35 53C30 56 23 54 20 50",
        "M34 47C31 50 26 50 23 48"
    };

    private final List<SVGPath> segmentos = new ArrayList<>();
    private final Group dibujo = new Group();
    private Timeline revelado;

    public HuellaBiometrica() {
        setMinSize(ANCHO, ALTO);
        setPrefSize(ANCHO, ALTO);
        setMaxSize(ANCHO, ALTO);
        setPickOnBounds(false);

        for (String cresta : CRESTAS) {
            SVGPath segmento = new SVGPath();
            segmento.setContent(cresta);
            segmento.getStyleClass().add("segmento-huella");
            segmento.getStrokeDashArray().setAll(LONGITUD_TRAZO, LONGITUD_TRAZO);
            segmentos.add(segmento);
            dibujo.getChildren().add(segmento);
        }
        // La referencia mide aproximadamente 0,62 veces su alto.
        dibujo.setScaleX(0.82);
        getChildren().add(dibujo);
        reiniciar();
    }

    /** Crea la huella de la notificacion con limites fijos y sin escalado externo. */
    public static HuellaBiometrica compacta() {
        return new HuellaBiometrica();
    }

    public void reiniciar() {
        detener();
        setOpacity(1);
        setScaleX(1);
        setScaleY(1);
        segmentos.forEach(segmento -> {
            segmento.setOpacity(1);
            segmento.setStrokeDashOffset(LONGITUD_TRAZO);
        });
    }

    public void mostrarCompleta() {
        segmentos.forEach(segmento -> segmento.setStrokeDashOffset(0));
    }

    /** Dibuja las crestas con un pequeno desfase para imitar el escaneo. */
    public void revelar() {
        detener();
        revelado = new Timeline();
        for (int indice = 0; indice < segmentos.size(); indice++) {
            SVGPath segmento = segmentos.get(indice);
            double inicio = indice * 24.0;
            revelado.getKeyFrames().addAll(
                    new KeyFrame(Duration.millis(inicio),
                            new KeyValue(segmento.strokeDashOffsetProperty(), LONGITUD_TRAZO)),
                    new KeyFrame(Duration.millis(inicio + 480),
                            new KeyValue(segmento.strokeDashOffsetProperty(), 0,
                                    Interpolator.EASE_BOTH)));
        }
        revelado.playFromStart();
    }

    public void detener() {
        if (revelado != null) {
            revelado.stop();
            revelado = null;
        }
    }
}
