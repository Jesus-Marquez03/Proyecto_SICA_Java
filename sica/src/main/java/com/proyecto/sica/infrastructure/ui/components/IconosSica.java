package com.proyecto.sica.infrastructure.ui.components;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/** Fabrica pequena de iconos SVG para mantener una sola identidad visual. */
public final class IconosSica {
    private IconosSica() { }

    public static Node crear(TipoIcono tipo) {
        SVGPath svg = new SVGPath();
        svg.setContent(ruta(tipo));
        svg.getStyleClass().add("icono-svg");
        svg.setScaleX(0.82);
        svg.setScaleY(0.82);
        StackPane contenedor = new StackPane(svg);
        contenedor.setMinSize(24, 24);
        contenedor.setPrefSize(24, 24);
        contenedor.setMaxSize(24, 24);
        contenedor.getStyleClass().add("icono-contenedor");
        return contenedor;
    }

    private static String ruta(TipoIcono tipo) {
        return switch (tipo) {
            case DASHBOARD -> "M3 3H10V10H3ZM14 3H21V10H14ZM3 14H10V21H3ZM14 14H21V21H14Z";
            case CONTROL_ACCESO -> "M4 2H16V4H6V20H16V22H4ZM14 11H22V13H14ZM18 7L22 12L18 17V14H12V10H18Z";
            case VISITAS -> "M4 3H20V21H4ZM6 7V19H18V7ZM8 1H10V5H8ZM14 1H16V5H14ZM8 10H16V12H8ZM8 14H14V16H8Z";
            case PERSONAS -> "M12 12C14.2 12 16 10.2 16 8S14.2 4 12 4 8 5.8 8 8 9.8 12 12 12ZM5 21V18C5 14.7 8 13 12 13S19 14.7 19 18V21Z";
            case EMPRESAS -> "M3 21V5H13V9H21V21ZM6 8V10H9V8ZM6 12V14H9V12ZM6 16V18H9V16ZM14 12V14H18V12ZM14 16V18H18V16Z";
            case INCIDENTES -> "M12 2L23 21H1ZM12 7L11 14H13ZM11 17V19H13V17Z";
            case REPORTES -> "M4 20L5 15L15 5L19 9L9 19ZM16 4L18 2L22 6L20 8Z";
            case USUARIOS -> "M9 11C11.2 11 13 9.2 13 7S11.2 3 9 3 5 4.8 5 7 6.8 11 9 11ZM2 21V18C2 14.7 5 13 9 13 10.2 13 11.3 13.2 12.3 13.5 10.8 14.7 10 16.2 10 18V21ZM17 12C19 12 20.5 10.5 20.5 8.5S19 5 17 5C16.2 5 15.5 5.2 15 5.7 15.3 6.4 15.5 7.2 15.5 8 15.5 9.3 15.1 10.5 14.3 11.5 15.1 11.8 16 12 17 12ZM13 21V18C13 15.5 15 14 18 14S23 15.5 23 18V21Z";
            case BITACORA -> "M12 2C17.5 2 22 6.5 22 12S17.5 22 12 22 2 17.5 2 12 6.5 2 12 2ZM11 6V13L16 16L17 14.3L13 12V6Z";
            case BIOMETRIA -> "M12 2C7 2 3 6 3 11H5C5 7.1 8.1 4 12 4S19 7.1 19 11C19 15 17.5 18.2 15 21L16.5 22.3C19.3 19.2 21 15.5 21 11 21 6 17 2 12 2ZM12 6C9.2 6 7 8.2 7 11 7 14.5 5.8 17.2 4 19L5.5 20.5C7.7 18.2 9 15 9 11 9 9.3 10.3 8 12 8S15 9.3 15 11C15 15.5 13.5 18.3 11 20.8L12.5 22.2C15.5 19.3 17 15.8 17 11 17 8.2 14.8 6 12 6ZM11 10V12C11 15.2 10.2 17.2 8.5 19L10 20.4C12 18.2 13 15.6 13 12V10Z";
            case CONFIGURACION -> "M12 8C9.8 8 8 9.8 8 12S9.8 16 12 16 16 14.2 16 12 14.2 8 12 8ZM4 10L2 8L5 3L8 4 10 2H14L16 4 19 3 22 8 20 10V14L22 16 19 21 16 20 14 22H10L8 20 5 21 2 16 4 14Z";
            case SALIR -> "M4 3H14V5H6V19H14V21H4ZM13 8L17 11H9V13H17L13 16V19L22 12L13 5Z";
            case ACTUALIZAR -> "M12 4C8.5 4 5.5 6.2 4.4 9.3L2 8V14H8L5.9 11.8C6 8.6 8.7 6 12 6 15.3 6 18 8.7 18 12S15.3 18 12 18C9.8 18 7.9 16.8 6.8 15H4.5C5.7 18 8.6 20 12 20 16.4 20 20 16.4 20 12S16.4 4 12 4Z";
            case BUSCAR -> "M10 3C6.1 3 3 6.1 3 10S6.1 17 10 17C11.6 17 13 16.5 14.2 15.6L20 21L21.5 19.5L15.8 13.8C16.6 12.7 17 11.4 17 10 17 6.1 13.9 3 10 3ZM10 5C12.8 5 15 7.2 15 10S12.8 15 10 15 5 12.8 5 10 7.2 5 10 5Z";
            case SUMAR -> "M11 4H13V11H20V13H13V20H11V13H4V11H11Z";
            case ESCUDO -> "M12 2L21 6V12C21 17.5 17.2 21.7 12 23 6.8 21.7 3 17.5 3 12V6ZM12 5L6 7.6V12C6 15.8 8.4 18.8 12 20 15.6 18.8 18 15.8 18 12V7.6Z";
        };
    }
}
