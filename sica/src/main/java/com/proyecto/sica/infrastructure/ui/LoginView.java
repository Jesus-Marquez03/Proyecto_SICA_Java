package com.proyecto.sica.infrastructure.ui;

import java.util.function.BiConsumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Vista JavaFX del login. Delega la autenticacion mediante un callback.
 */
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
        Label marca = new Label("SICA");
        marca.getStyleClass().add("marca");
        Label subtitulo = new Label("Sistema Integrado de Control de Acceso");
        subtitulo.getStyleClass().add("subtitulo");
        Label titulo = new Label("Bienvenido");
        titulo.getStyleClass().add("titulo-formulario");
        Label indicacion = new Label("Ingresa con tu correo institucional");
        indicacion.getStyleClass().add("texto-secundario");

        correoField.setPromptText("Correo electronico");
        correoField.getStyleClass().add("campo");
        passwordField.setPromptText("Contrasena");
        passwordField.getStyleClass().add("campo");
        mensajeError.getStyleClass().add("mensaje-error");
        mensajeError.setVisible(false);
        mensajeError.setManaged(false);
        mensajeError.setWrapText(true);

        iniciarButton.getStyleClass().add("boton-principal");
        iniciarButton.setMaxWidth(Double.MAX_VALUE);
        iniciarButton.setDefaultButton(true);
        iniciarButton.setOnAction(evento -> {
            ocultarError();
            alIniciarSesion.accept(correoField.getText(), passwordField.getText().toCharArray());
            passwordField.clear();
        });

        VBox tarjeta = new VBox(12, marca, subtitulo, titulo, indicacion,
                correoField, passwordField, mensajeError, iniciarButton);
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setPadding(new Insets(38));
        tarjeta.setMaxWidth(420);
        tarjeta.getStyleClass().add("tarjeta");
        root.getChildren().add(tarjeta);
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

    public Parent getRoot() {
        return root;
    }
}
