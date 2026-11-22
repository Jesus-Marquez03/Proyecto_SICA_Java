package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.infrastructure.ui.components.FondoArquitectonico;
import com.proyecto.sica.infrastructure.ui.components.IconosSica;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import java.util.function.BiConsumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Login JavaFX con identidad visual de Zona Acme y mensajes accesibles. */
public final class LoginView {
    private final StackPane root = new StackPane();
    private final TextField correoField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label mensajeError = new Label();
    private final Button iniciarButton = new Button("Iniciar sesion");

    public LoginView(BiConsumer<String, char[]> alIniciarSesion) {
        construir(alIniciarSesion);
    }

    private void construir(BiConsumer<String, char[]> alIniciarSesion) {
        root.getStyleClass().add("login-root");
        FondoArquitectonico fondo = new FondoArquitectonico();
        fondo.prefWidthProperty().bind(root.widthProperty());
        fondo.prefHeightProperty().bind(root.heightProperty());

        Label sigla = new Label("SICA");
        sigla.getStyleClass().add("marca-login-grande");
        Label nombre = new Label("Sistema Integrado de Control de Acceso");
        nombre.getStyleClass().add("subtitulo-login-grande");
        nombre.setWrapText(true);
        Label zona = new Label("ZONA ACME");
        zona.getStyleClass().add("etiqueta-zona");
        Label mensaje = new Label("Seguridad, trazabilidad y control en un solo lugar.");
        mensaje.getStyleClass().add("frase-login");
        mensaje.setWrapText(true);
        VBox presentacion = new VBox(14, zona, sigla, nombre, mensaje);
        presentacion.setAlignment(Pos.CENTER_LEFT);
        presentacion.setMaxWidth(430);
        HBox.setHgrow(presentacion, Priority.ALWAYS);

        StackPane simbolo = new StackPane(IconosSica.crear(TipoIcono.ESCUDO));
        simbolo.getStyleClass().add("simbolo-login");
        Label titulo = new Label("Bienvenido de nuevo");
        titulo.getStyleClass().add("titulo-login");
        Label indicacion = new Label("Ingresa con tu correo institucional");
        indicacion.getStyleClass().add("texto-secundario");
        correoField.setPromptText("Correo electronico");
        passwordField.setPromptText("Contrasena");
        correoField.getStyleClass().add("campo");
        passwordField.getStyleClass().add("campo");
        mensajeError.getStyleClass().add("mensaje-error");
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        mensajeError.setWrapText(true);
        iniciarButton.setGraphic(IconosSica.crear(TipoIcono.SALIR));
        iniciarButton.getStyleClass().add("boton-principal");
        iniciarButton.setMaxWidth(Double.MAX_VALUE);
        iniciarButton.setDefaultButton(true);
        iniciarButton.setOnAction(evento -> {
            ocultarError();
            alIniciarSesion.accept(correoField.getText(), passwordField.getText().toCharArray());
            passwordField.clear();
        });
        VBox tarjeta = new VBox(14, simbolo, titulo, indicacion,
                new Label("Correo"), correoField, new Label("Contrasena"), passwordField,
                mensajeError, iniciarButton);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setPadding(new Insets(36));
        tarjeta.setMaxWidth(420);
        tarjeta.getStyleClass().add("tarjeta-login");

        HBox panel = new HBox(50, presentacion, tarjeta);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(55));
        panel.getStyleClass().add("panel-login-cristal");
        StackPane.setMargin(panel, new Insets(30));
        root.getChildren().addAll(fondo, panel);
    }

    public void mostrarError(String mensaje) {
        mensajeError.setText(mensaje);
        mensajeError.setManaged(true);
        mensajeError.setVisible(true);
    }

    public void ocultarError() {
        mensajeError.setManaged(false);
        mensajeError.setVisible(false);
    }

    public Parent getRoot() { return root; }
}
