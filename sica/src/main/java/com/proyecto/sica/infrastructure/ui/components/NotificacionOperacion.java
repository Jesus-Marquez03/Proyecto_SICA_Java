package com.proyecto.sica.infrastructure.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
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
    static final double ANCHO = 344;
    static final double ALTO = 104;

    private final HuellaBiometrica huella = HuellaBiometrica.compacta();
    private final SVGPath check = new SVGPath();
    private final Label detalle = new Label();
    private SequentialTransition ciclo;

    public NotificacionOperacion() {
        getStyleClass().add("notificacion-operacion");
        setAlignment(Pos.CENTER_LEFT);
        setFillHeight(false);
        setSpacing(13);
        setMinSize(ANCHO, ALTO);
        setPrefSize(ANCHO, ALTO);
        setMaxSize(ANCHO, ALTO);
        setVisible(false);
        setMouseTransparent(true);

        check.setContent("M7 17L13 23L27 9");
        check.getStyleClass().add("check-operacion");
        check.setOpacity(0);
        check.setScaleX(0.72);
        check.setScaleY(0.72);

        StackPane icono = new StackPane(huella, check);
        icono.setAlignment(Pos.CENTER);
        icono.setMinSize(68, 68);
        icono.setPrefSize(68, 68);
        icono.setMaxSize(68, 68);
        icono.getStyleClass().add("icono-notificacion-operacion");

        Label titulo = new Label("Operación completada");
        titulo.getStyleClass().add("notificacion-titulo");
        detalle.getStyleClass().add("notificacion-detalle");
        detalle.setWrapText(true);
        detalle.setMaxWidth(232);
        VBox textos = new VBox(4, titulo, detalle);
        textos.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(icono, textos);
    }

    /** Sustituye cualquier confirmacion activa y reproduce una secuencia completa. */
    public void mostrar(String mensaje) {
        detener();
        detalle.setText(mensaje == null ? "" : mensaje);
        prepararEstadoInicial();
        setVisible(true);

        ParallelTransition entrada = crearEntrada();
        PauseTransition dibujo = new PauseTransition(Duration.millis(790));
        dibujo.setOnFinished(evento -> huella.mostrarCompleta());
        ParallelTransition transformacion = crearTransformacion();
        PauseTransition permanencia = new PauseTransition(Duration.millis(1650));
        ParallelTransition salida = crearSalida();

        entrada.setOnFinished(evento -> huella.revelar());
        ciclo = new SequentialTransition(entrada, dibujo, transformacion, permanencia, salida);
        ciclo.setOnFinished(evento -> {
            setVisible(false);
            huella.detener();
            ciclo = null;
        });
        ciclo.playFromStart();
    }

    private void prepararEstadoInicial() {
        huella.reiniciar();
        huella.setOpacity(1);
        huella.setScaleX(1);
        huella.setScaleY(1);
        check.setOpacity(0);
        check.setScaleX(0.72);
        check.setScaleY(0.72);
        setOpacity(0);
        setTranslateY(-9);
        setScaleX(0.98);
        setScaleY(0.98);
    }

    private ParallelTransition crearEntrada() {
        FadeTransition fade = new FadeTransition(Duration.millis(220), this);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition movimiento = new TranslateTransition(Duration.millis(220), this);
        movimiento.setFromY(-9);
        movimiento.setToY(0);
        ScaleTransition escala = new ScaleTransition(Duration.millis(220), this);
        escala.setFromX(0.98);
        escala.setFromY(0.98);
        escala.setToX(1);
        escala.setToY(1);
        configurarSuavizado(fade, movimiento, escala);
        return new ParallelTransition(fade, movimiento, escala);
    }

    private ParallelTransition crearTransformacion() {
        FadeTransition ocultarHuella = new FadeTransition(Duration.millis(360), huella);
        ocultarHuella.setFromValue(1);
        ocultarHuella.setToValue(0);
        ScaleTransition contraerHuella = new ScaleTransition(Duration.millis(360), huella);
        contraerHuella.setFromX(1);
        contraerHuella.setFromY(1);
        contraerHuella.setToX(0.78);
        contraerHuella.setToY(0.78);
        FadeTransition mostrarCheck = new FadeTransition(Duration.millis(360), check);
        mostrarCheck.setFromValue(0);
        mostrarCheck.setToValue(1);
        ScaleTransition expandirCheck = new ScaleTransition(Duration.millis(360), check);
        expandirCheck.setFromX(0.72);
        expandirCheck.setFromY(0.72);
        expandirCheck.setToX(1);
        expandirCheck.setToY(1);
        configurarSuavizado(ocultarHuella, contraerHuella, mostrarCheck, expandirCheck);
        return new ParallelTransition(
                ocultarHuella, contraerHuella, mostrarCheck, expandirCheck);
    }

    private ParallelTransition crearSalida() {
        FadeTransition fade = new FadeTransition(Duration.millis(250), this);
        fade.setFromValue(1);
        fade.setToValue(0);
        TranslateTransition movimiento = new TranslateTransition(Duration.millis(250), this);
        movimiento.setFromY(0);
        movimiento.setToY(-7);
        configurarSuavizado(fade, movimiento);
        return new ParallelTransition(fade, movimiento);
    }

    private void configurarSuavizado(javafx.animation.Transition... transiciones) {
        for (javafx.animation.Transition transicion : transiciones) {
            transicion.setInterpolator(Interpolator.EASE_BOTH);
        }
    }

    /** Detiene y limpia la animacion activa, por ejemplo al cerrar sesion. */
    public void detener() {
        if (ciclo != null) {
            ciclo.stop();
            ciclo = null;
        }
        huella.detener();
        setVisible(false);
        setOpacity(0);
        setTranslateY(0);
        setScaleX(1);
        setScaleY(1);
    }

    String getDetalleTexto() {
        return detalle.getText();
    }
}
