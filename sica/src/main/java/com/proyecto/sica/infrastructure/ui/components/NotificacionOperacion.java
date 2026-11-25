package com.proyecto.sica.infrastructure.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/** Confirmacion visual superpuesta para operaciones que terminaron correctamente. */
public final class NotificacionOperacion extends HBox {
    private final HuellaBiometrica huella = HuellaBiometrica.compacta();
    private final SVGPath check = new SVGPath();
    private final Label detalle = new Label();
    private Timeline secuencia;
    private FadeTransition entrada;
    private FadeTransition salida;
    private ScaleTransition pulso;
    private TranslateTransition desplazamiento;

    public NotificacionOperacion() {
        getStyleClass().add("notificacion-operacion");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(12);
        setVisible(false);
        setMouseTransparent(true);

        check.setContent("M4 12L9 17L20 6");
        check.getStyleClass().add("check-operacion");
        check.setScaleX(1.45);
        check.setScaleY(1.45);
        check.setVisible(false);
        StackPane icono = new StackPane(huella, check);
        icono.getStyleClass().add("icono-notificacion-operacion");

        Label titulo = new Label("Operacion completada");
        titulo.getStyleClass().add("notificacion-titulo");
        detalle.getStyleClass().add("notificacion-detalle");
        detalle.setWrapText(true);
        VBox textos = new VBox(3, titulo, detalle);
        getChildren().addAll(icono, textos);
    }

    /** Sustituye cualquier confirmacion activa y reproduce una secuencia breve. */
    public void mostrar(String mensaje) {
        detener();
        detalle.setText(mensaje);
        huella.setVisible(true);
        check.setVisible(false);
        huella.reiniciar();
        setOpacity(0);
        setTranslateY(-12);
        setVisible(true);

        entrada = new FadeTransition(Duration.millis(220), this);
        entrada.setFromValue(0);
        entrada.setToValue(1);
        desplazamiento = new TranslateTransition(Duration.millis(240), this);
        desplazamiento.setFromY(-12);
        desplazamiento.setToY(0);
        entrada.play();
        desplazamiento.play();
        huella.revelar();

        secuencia = new Timeline(
                new KeyFrame(Duration.millis(950), e -> mostrarCheck()),
                new KeyFrame(Duration.seconds(3.0), e -> ocultar()));
        secuencia.play();
    }

    private void mostrarCheck() {
        huella.setVisible(false);
        check.setVisible(true);
        pulso = new ScaleTransition(Duration.millis(260), check);
        pulso.setFromX(0.72);
        pulso.setFromY(0.72);
        pulso.setToX(1);
        pulso.setToY(1);
        pulso.play();
    }

    private void ocultar() {
        salida = new FadeTransition(Duration.millis(260), this);
        salida.setFromValue(1);
        salida.setToValue(0);
        salida.setOnFinished(e -> {
            setVisible(false);
            huella.detener();
        });
        salida.play();
    }

    /** Detiene transiciones para que no queden animaciones tras cerrar sesion. */
    public void detener() {
        if (secuencia != null) secuencia.stop();
        if (entrada != null) entrada.stop();
        if (salida != null) salida.stop();
        if (pulso != null) pulso.stop();
        if (desplazamiento != null) desplazamiento.stop();
        huella.detener();
        setVisible(false);
    }
}
