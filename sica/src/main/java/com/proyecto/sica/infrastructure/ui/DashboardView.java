package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.domain.entity.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Pantalla provisional reutilizable como base del futuro dashboard.
 */
public final class DashboardView {

    private final BorderPane root = new BorderPane();

    public DashboardView(Usuario usuario, Runnable alCerrarSesion) {
        construir(usuario, alCerrarSesion);
    }

    private void construir(Usuario usuario, Runnable alCerrarSesion) {
        root.getStyleClass().add("dashboard-root");
        Label marca = new Label("SICA");
        marca.getStyleClass().add("marca-dashboard");
        Button cerrarButton = new Button("Cerrar sesion");
        cerrarButton.getStyleClass().add("boton-secundario");
        cerrarButton.setOnAction(evento -> alCerrarSesion.run());

        HBox encabezado = new HBox(marca, cerrarButton);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.setSpacing(20);
        HBox.setHgrow(marca, javafx.scene.layout.Priority.ALWAYS);
        encabezado.setPadding(new Insets(20, 32, 20, 32));
        encabezado.getStyleClass().add("encabezado");

        Label bienvenida = new Label("Bienvenido, " + usuario.getNombreCompleto());
        bienvenida.getStyleClass().add("titulo-dashboard");
        Label rol = new Label("Rol: " + usuario.getRol().getNombre());
        rol.getStyleClass().add("dato-dashboard");
        Label empresa = new Label(usuario.getEmpresaNombre() == null
                ? "Empresa: Administracion general"
                : "Empresa: " + usuario.getEmpresaNombre());
        empresa.getStyleClass().add("dato-dashboard");
        Label mensaje = new Label("El modulo de seguridad esta listo. Los paneles operativos se agregaran en las siguientes etapas.");
        mensaje.setWrapText(true);
        mensaje.getStyleClass().add("texto-secundario");

        VBox contenido = new VBox(14, bienvenida, rol, empresa, mensaje);
        contenido.setMaxWidth(640);
        contenido.setPadding(new Insets(38));
        contenido.getStyleClass().add("tarjeta-dashboard");

        VBox centro = new VBox(contenido);
        centro.setAlignment(Pos.CENTER);
        centro.setPadding(new Insets(40));
        root.setTop(encabezado);
        root.setCenter(centro);
    }

    public Parent getRoot() {
        return root;
    }
}
