package com.proyecto.sica.infrastructure.ui.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AnimacionNotificacionTest {

    @BeforeAll
    static void iniciarJavaFx() throws InterruptedException {
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                inicio.countDown();
            });
            inicio.await();
        } catch (IllegalStateException yaIniciado) {
            // El toolkit puede estar activo por otra prueba JavaFX.
            Platform.setImplicitExit(false);
        }
    }

    @AfterAll
    static void cerrarJavaFxEnCapturaAislada() {
        if (Boolean.getBoolean("sica.capturarNotificacion")) {
            Platform.exit();
        }
    }

    @Test
    void conservaTamanoCompactoYSustituyeUnaConfirmacionActiva() throws Exception {
        FutureTask<Void> prueba = new FutureTask<>(() -> {
            NotificacionOperacion notificacion = new NotificacionOperacion();
            StackPane root = new StackPane(notificacion);
            Scene escena = new Scene(root, 520, 180);
            escena.getStylesheets().add(
                    getClass().getResource("/styles/sica.css").toExternalForm());
            root.applyCss();
            root.layout();

            assertEquals(344, notificacion.getMinWidth());
            assertEquals(344, notificacion.getPrefWidth());
            assertEquals(344, notificacion.getMaxWidth());
            assertEquals(104, notificacion.getMinHeight());
            assertEquals(104, notificacion.getPrefHeight());
            assertEquals(104, notificacion.getMaxHeight());

            notificacion.mostrar("Persona registrada correctamente");
            assertTrue(notificacion.isVisible());
            assertEquals("Persona registrada correctamente",
                    notificacion.getDetalleTexto());

            notificacion.mostrar("Registro actualizado");
            assertTrue(notificacion.isVisible());
            assertEquals("Registro actualizado", notificacion.getDetalleTexto());

            notificacion.detener();
            assertFalse(notificacion.isVisible());
            return null;
        });
        Platform.runLater(prueba);
        prueba.get();
    }

    @Test
    void huellaPermaneceCompletaDentroDeLaRegionYComparteCentroConCheck() throws Exception {
        FutureTask<Void> prueba = new FutureTask<>(() -> {
            HuellaBiometrica huella = HuellaBiometrica.compacta();
            huella.mostrarCompleta();
            StackPane region = new StackPane(huella);
            region.setMinSize(68, 68);
            region.setPrefSize(68, 68);
            region.setMaxSize(68, 68);
            StackPane rootHuella = new StackPane(region);
            Scene escenaHuella = new Scene(rootHuella, 120, 120);
            escenaHuella.getStylesheets().add(
                    getClass().getResource("/styles/sica.css").toExternalForm());
            rootHuella.applyCss();
            rootHuella.layout();

            Group dibujo = (Group) huella.getChildren().get(0);
            assertTrue(dibujo.getChildren().size() >= 15);
            Bounds limites = dibujo.getBoundsInParent();
            assertTrue(limites.getMinX() >= 0 && limites.getMaxX() <= 60,
                    "La huella excede el ancho asignado");
            assertTrue(limites.getMinY() >= 0 && limites.getMaxY() <= 70,
                    "La huella excede el alto asignado");

            NotificacionOperacion notificacion = new NotificacionOperacion();
            StackPane rootNotificacion = new StackPane(notificacion);
            Scene escenaNotificacion = new Scene(rootNotificacion, 520, 180);
            escenaNotificacion.getStylesheets().add(
                    getClass().getResource("/styles/sica.css").toExternalForm());
            rootNotificacion.applyCss();
            rootNotificacion.layout();

            HBox tarjeta = notificacion;
            StackPane icono = (StackPane) tarjeta.getChildren().get(0);
            HuellaBiometrica huellaTarjeta = (HuellaBiometrica) icono.getChildren().get(0);
            SVGPath check = (SVGPath) icono.getChildren().get(1);
            Bounds limitesHuella = huellaTarjeta.getBoundsInParent();
            Bounds limitesCheck = check.getBoundsInParent();
            assertEquals(limitesHuella.getCenterX(), limitesCheck.getCenterX(), 0.2);
            assertEquals(limitesHuella.getCenterY(), limitesCheck.getCenterY(), 0.2);
            return null;
        });
        Platform.runLater(prueba);
        prueba.get();
    }

    @Test
    void generaEvidenciaVisualOpcionalDelCiclo() throws Exception {
        if (!Boolean.getBoolean("sica.capturarNotificacion")) {
            return;
        }

        CountDownLatch capturas = new CountDownLatch(6);
        Path destino = Path.of("target", "notificacion-evidencia");
        Files.createDirectories(destino);
        Platform.runLater(() -> {
            NotificacionOperacion notificacion = new NotificacionOperacion();
            StackPane root = new StackPane(notificacion);
            root.setStyle("-fx-background-color: #F4F4F4;");
            StackPane.setAlignment(notificacion, Pos.TOP_RIGHT);
            StackPane.setMargin(notificacion, new Insets(22));
            Scene escena = new Scene(root, 720, 340);
            escena.getStylesheets().add(
                    getClass().getResource("/styles/sica.css").toExternalForm());
            root.applyCss();
            root.layout();
            notificacion.mostrar("Persona registrada correctamente");

            Timeline evidencia = new Timeline(
                    fotograma(100, "01-entrada.png", root, destino, capturas),
                    fotograma(500, "02-huella-parcial.png", root, destino, capturas),
                    fotograma(940, "03-huella-completa.png", root, destino, capturas),
                    fotograma(1180, "04-transformacion.png", root, destino, capturas),
                    fotograma(1500, "05-check.png", root, destino, capturas),
                    fotograma(3150, "06-salida.png", root, destino, capturas));
            evidencia.playFromStart();
        });

        assertTrue(capturas.await(5, TimeUnit.SECONDS));
    }

    private KeyFrame fotograma(double milisegundos, String nombre, StackPane root,
            Path destino, CountDownLatch capturas) {
        return new KeyFrame(Duration.millis(milisegundos), evento -> {
            try {
                root.applyCss();
                root.layout();
                WritableImage imagen = root.snapshot(null, null);
                ImageIO.write(convertir(imagen), "png", destino.resolve(nombre).toFile());
            } catch (Exception error) {
                throw new IllegalStateException("No fue posible guardar " + nombre, error);
            } finally {
                capturas.countDown();
            }
        });
    }

    private BufferedImage convertir(WritableImage imagen) {
        int ancho = (int) imagen.getWidth();
        int alto = (int) imagen.getHeight();
        BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        PixelReader pixeles = imagen.getPixelReader();
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                resultado.setRGB(x, y, pixeles.getArgb(x, y));
            }
        }
        return resultado;
    }
}
