package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.infrastructure.ui.components.EncabezadoModulo;
import com.proyecto.sica.infrastructure.ui.components.IconosSica;
import com.proyecto.sica.infrastructure.ui.components.HuellaBiometrica;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

/** Demostracion visual local; no identifica personas ni guarda datos biometricos. */
public final class AnimacionBiometricaView {
    private final VBox root = new VBox(22);
    private final StackPane lector = new StackPane();
    private final Label estado = new Label("En reposo");
    private final Label detalle = new Label("Coloca el dedo para iniciar la demostracion");
    private final Rectangle linea = new Rectangle(170, 3, Color.web("#111111"));
    private final HuellaBiometrica huella = new HuellaBiometrica();
    private final SVGPath check = new SVGPath();
    private final VBox notificacion = new VBox(3);
    private Timeline secuencia;
    private Timeline escaneo;
    private Timeline notificacionAnimacion;

    public AnimacionBiometricaView() { construir(); }

    private void construir() {
        root.getStyleClass().add("contenido-modulo-final");
        root.setPadding(new Insets(24, 28, 30, 28));
        root.getChildren().add(new EncabezadoModulo("Animacion biometrica",
                "Prototipo visual sin captura, comparacion ni almacenamiento de huellas.",
                TipoIcono.BIOMETRIA));

        Circle halo = new Circle(130, Color.rgb(255, 255, 255, 0.58));
        halo.setStroke(Color.rgb(30, 30, 30, 0.12));
        check.setContent("M4 12L9 17L20 6");
        check.getStyleClass().add("check-biometrico");
        check.setScaleX(2.2);
        check.setScaleY(2.2);
        check.setVisible(false);
        Label tituloNotificacion = new Label("Demostracion completada");
        tituloNotificacion.getStyleClass().add("notificacion-titulo");
        Label detalleNotificacion = new Label("La animacion visual finalizo; no se capturaron datos.");
        detalleNotificacion.getStyleClass().add("notificacion-detalle");
        notificacion.getChildren().addAll(tituloNotificacion, detalleNotificacion);
        notificacion.getStyleClass().add("notificacion-flotante");
        notificacion.setVisible(false);
        notificacion.setTranslateY(115);
        linea.setOpacity(0);
        lector.getChildren().addAll(halo, huella, linea, check, notificacion);
        lector.setMinSize(360, 320);
        lector.getStyleClass().add("lector-biometrico");

        estado.getStyleClass().add("estado-biometrico");
        detalle.getStyleClass().add("subtitulo-pagina");
        Label aviso = new Label("DEMOSTRACION VISUAL · NO ES AUTENTICACION REAL");
        aviso.getStyleClass().add("aviso-demostracion");
        Button exito = new Button("Simular lectura correcta");
        exito.setGraphic(IconosSica.crear(TipoIcono.BIOMETRIA));
        exito.getStyleClass().add("boton-principal");
        Button error = new Button("Simular error");
        error.getStyleClass().add("boton-neutro");
        exito.setOnAction(e -> iniciar(true));
        error.setOnAction(e -> iniciar(false));
        HBox acciones = new HBox(10, exito, error);
        acciones.setAlignment(Pos.CENTER);
        VBox tarjeta = new VBox(14, aviso, lector, estado, detalle, acciones);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.getStyleClass().addAll("tarjeta-bento", "tarjeta-biometria");
        root.getChildren().add(tarjeta);
    }

    private void iniciar(boolean terminarConExito) {
        detener();
        lector.getStyleClass().removeAll("biometria-exito", "biometria-error");
        check.setVisible(false);
        notificacion.setVisible(false);
        huella.reiniciar();
        if (Boolean.getBoolean("sica.movimientoReducido")) {
            finalizar(terminarConExito);
            return;
        }
        secuencia = new Timeline(
                new KeyFrame(Duration.ZERO, e -> fase("Detectando huella", "Mantenga el dedo sobre el lector")),
                new KeyFrame(Duration.seconds(0.8), e -> procesar()),
                new KeyFrame(Duration.seconds(2.7), e -> finalizar(terminarConExito)));
        secuencia.play();
        ScaleTransition pulso = new ScaleTransition(Duration.millis(420), huella);
        pulso.setFromX(1); pulso.setFromY(1);
        pulso.setToX(1.06); pulso.setToY(1.06);
        pulso.setAutoReverse(true); pulso.setCycleCount(2); pulso.play();
    }

    private void procesar() {
        fase("Procesando", "Analizando el patron de demostracion...");
        huella.revelar();
        linea.setOpacity(1);
        escaneo = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(linea.translateYProperty(), -88)),
                new KeyFrame(Duration.millis(650), new KeyValue(linea.translateYProperty(), 88)));
        escaneo.setAutoReverse(true);
        escaneo.setCycleCount(4);
        escaneo.play();
    }

    private void finalizar(boolean exito) {
        if (escaneo != null) escaneo.stop();
        linea.setOpacity(0);
        lector.getStyleClass().add(exito ? "biometria-exito" : "biometria-error");
        huella.mostrarCompleta();
        check.setVisible(exito);
        fase(exito ? "Demostracion completada" : "Error de demostracion",
                exito ? "Demostracion completada correctamente" : "Intenta nuevamente en el prototipo");
        FadeTransition destello = new FadeTransition(Duration.millis(320), lector);
        destello.setFromValue(0.55); destello.setToValue(1); destello.play();
        if (exito) {
            notificacion.setVisible(true);
            notificacionAnimacion = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(notificacion.opacityProperty(), 0),
                            new KeyValue(notificacion.translateYProperty(), 130)),
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(notificacion.opacityProperty(), 1),
                            new KeyValue(notificacion.translateYProperty(), 112)),
                    new KeyFrame(Duration.seconds(2.3),
                            new KeyValue(notificacion.opacityProperty(), 1)),
                    new KeyFrame(Duration.seconds(2.6),
                            new KeyValue(notificacion.opacityProperty(), 0),
                            new KeyValue(notificacion.translateYProperty(), 105)));
            notificacionAnimacion.setOnFinished(e -> notificacion.setVisible(false));
            notificacionAnimacion.play();
        }
    }

    private void fase(String titulo, String mensaje) {
        estado.setText(titulo);
        detalle.setText(mensaje);
    }

    public void detener() {
        if (secuencia != null) secuencia.stop();
        if (escaneo != null) escaneo.stop();
        if (notificacionAnimacion != null) notificacionAnimacion.stop();
        huella.detener();
        linea.setOpacity(0);
        notificacion.setVisible(false);
    }

    public Parent getRoot() { return root; }
}
