package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.components.FondoArquitectonico;
import com.proyecto.sica.infrastructure.ui.components.IconosSica;
import com.proyecto.sica.infrastructure.ui.components.NotificacionOperacion;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Marco principal reutilizable con menu lateral y contenido por modulos. */
public final class DashboardView {
    private final StackPane root = new StackPane();
    private final VBox opciones = new VBox(3);
    private final ScrollPane contenido = new ScrollPane();
    private final Label seccionActual = new Label("Dashboard");
    private final Map<String, Button> botones = new LinkedHashMap<>();
    private final NotificacionOperacion notificacion = new NotificacionOperacion();

    public DashboardView(Usuario usuario, Runnable alCerrarSesion) {
        construir(usuario, alCerrarSesion);
    }

    private void construir(Usuario usuario, Runnable alCerrarSesion) {
        root.getStyleClass().add("dashboard-root");
        FondoArquitectonico fondo = new FondoArquitectonico();
        fondo.prefWidthProperty().bind(root.widthProperty());
        fondo.prefHeightProperty().bind(root.heightProperty());

        BorderPane aplicacion = new BorderPane();
        aplicacion.getStyleClass().add("contenedor-cristal");
        aplicacion.setLeft(crearBarraLateral(usuario, alCerrarSesion));
        aplicacion.setCenter(crearAreaPrincipal(usuario));
        StackPane.setMargin(aplicacion, new Insets(22));
        root.getChildren().addAll(fondo, aplicacion);
    }

    private Node crearBarraLateral(Usuario usuario, Runnable alCerrarSesion) {
        Label marca = new Label("SICA");
        marca.getStyleClass().add("marca-sidebar");
        Label zona = new Label("ZONA ACME");
        zona.getStyleClass().add("zona-sidebar");
        VBox textosMarca = new VBox(-2, marca, zona);
        StackPane simbolo = new StackPane(IconosSica.crear(TipoIcono.ESCUDO));
        simbolo.getStyleClass().add("simbolo-marca");
        HBox identidad = new HBox(10, simbolo, textosMarca);
        identidad.setAlignment(Pos.CENTER_LEFT);
        identidad.getStyleClass().add("identidad-sidebar");

        opciones.getStyleClass().add("opciones-sidebar");
        ScrollPane desplazamiento = new ScrollPane(opciones);
        desplazamiento.setFitToWidth(true);
        desplazamiento.getStyleClass().add("scroll-sidebar");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);

        String inicial = usuario.getNombreCompleto().isBlank()
                ? "U" : usuario.getNombreCompleto().substring(0, 1).toUpperCase(Locale.ROOT);
        Label avatar = new Label(inicial);
        avatar.getStyleClass().add("avatar-usuario");
        Label nombre = new Label(usuario.getNombreCompleto());
        nombre.getStyleClass().add("nombre-sidebar");
        Label rol = new Label(usuario.getRol().getNombre());
        rol.getStyleClass().add("rol-sidebar");
        VBox datos = new VBox(2, nombre, rol);
        HBox.setHgrow(datos, Priority.ALWAYS);
        Button salir = new Button();
        salir.setGraphic(IconosSica.crear(TipoIcono.SALIR));
        salir.getStyleClass().add("boton-icono");
        salir.setAccessibleText("Cerrar sesion");
        salir.setOnAction(evento -> alCerrarSesion.run());
        HBox usuarioCaja = new HBox(9, avatar, datos, salir);
        usuarioCaja.setAlignment(Pos.CENTER_LEFT);
        usuarioCaja.getStyleClass().add("usuario-sidebar");

        VBox barra = new VBox(12, identidad, desplazamiento, usuarioCaja);
        barra.getStyleClass().add("sidebar-final");
        barra.setPrefWidth(244);
        barra.setMinWidth(220);
        return barra;
    }

    private Node crearAreaPrincipal(Usuario usuario) {
        Label saludo = new Label("Hola, " + usuario.getNombres() + "!");
        saludo.getStyleClass().add("saludo-usuario");
        DateTimeFormatter formato = DateTimeFormatter.ofPattern(
                "EEEE, d 'de' MMMM", new Locale("es", "CO"));
        Label fecha = new Label(LocalDate.now().format(formato));
        fecha.getStyleClass().add("fecha-encabezado");
        VBox saludoCaja = new VBox(2, saludo, fecha);
        Region separador = new Region();
        HBox.setHgrow(separador, Priority.ALWAYS);
        seccionActual.getStyleClass().add("seccion-actual");
        HBox encabezado = new HBox(12, saludoCaja, separador, seccionActual);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.getStyleClass().add("encabezado-principal");

        contenido.setFitToWidth(true);
        contenido.setFitToHeight(true);
        contenido.setPannable(true);
        contenido.getStyleClass().add("scroll-contenido");

        StackPane contenidoSuperpuesto = new StackPane(contenido, notificacion);
        StackPane.setAlignment(notificacion, Pos.TOP_RIGHT);
        StackPane.setMargin(notificacion, new Insets(22, 22, 0, 0));
        VBox.setVgrow(contenidoSuperpuesto, Priority.ALWAYS);

        VBox area = new VBox(encabezado, contenidoSuperpuesto);
        area.getStyleClass().add("area-principal");
        return area;
    }

    public void registrarOpcion(String texto, TipoIcono icono,
            boolean permitido, Runnable accion) {
        Button boton = new Button(texto);
        boton.setGraphic(IconosSica.crear(icono));
        boton.getStyleClass().add("boton-menu-final");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setDisable(!permitido);
        boton.setOnAction(evento -> {
            activar(texto);
            accion.run();
        });
        botones.put(texto, boton);
        opciones.getChildren().add(boton);
    }

    public void activar(String texto) {
        botones.forEach((nombre, boton) -> {
            boton.getStyleClass().remove("activo");
            if (nombre.equals(texto)) {
                boton.getStyleClass().add("activo");
            }
        });
        seccionActual.setText(texto);
    }

    public void mostrarContenido(Parent nuevoContenido) {
        contenido.setContent(nuevoContenido);
    }

    public void mostrarConfirmacion(String mensaje) {
        notificacion.mostrar(mensaje);
    }

    public void detenerNotificaciones() {
        notificacion.detener();
    }

    public Parent getRoot() {
        return root;
    }
}
