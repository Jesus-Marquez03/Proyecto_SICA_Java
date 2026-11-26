package com.proyecto.sica.infrastructure.ui.components;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/** Huella vectorial completa cuyas crestas se dibujan progresivamente. */
public final class HuellaBiometrica extends Pane {
    private static final double ANCHO = 60;
    private static final double ALTO = 70;
    private static final double LONGITUD_TRAZO = 190;

    /*
     * Las rutas comparten un sistema de coordenadas de 60 x 70. Esto evita
     * escalados y desplazamientos externos que alteren los limites del nodo.
     */
    private static final String[] CRESTAS = {
        "M8 60C3 49 4 31 10 19C15 9 22 4 30 4C44 4 55 17 56 32C57 46 52 58 45 65",
        "M13 62C8 52 9 36 13 23C17 14 23 9 31 9C42 9 50 19 51 32C52 43 48 54 41 61",
        "M18 64C13 55 14 40 18 27C21 18 25 14 32 14C41 14 46 22 47 33C48 41 45 49 39 56",
        "M23 65C18 57 19 44 22 31C24 23 28 19 33 19C40 19 43 25 43 33C44 38 42 44 38 48",
        "M33 24C29 24 27 27 27 31C27 35 30 37 34 37C40 37 44 42 44 49C44 57 39 64 33 67",
        "M34 29C32 29 32 31 34 32C43 33 49 40 49 49C49 59 43 67 36 69",
        "M22 36C21 44 24 49 30 51C35 53 37 57 36 61",
        "M17 34C16 46 20 54 27 57C31 59 32 62 31 66",
        "M12 31C11 45 16 56 24 61",
        "M18 10C21 7 25 6 29 6",
        "M22 15C25 12 29 11 33 12"
    };

    private final List<SVGPath> segmentos = new ArrayList<>();
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
            getChildren().add(segmento);
        }
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
