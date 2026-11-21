package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.application.service.PermisosSistema;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Contenedor principal con navegacion lateral para los modulos disponibles.
 */
public final class DashboardView {

    private final BorderPane root = new BorderPane();

    public DashboardView(Usuario usuario, Runnable alCerrarSesion,
            Runnable alAbrirEmpresas, Runnable alAbrirPersonas,
            Runnable alAbrirEstados, Runnable alAbrirVisitas,
            Runnable alAbrirIncidentes, Runnable alAbrirAuditoria) {
        construir(usuario, alCerrarSesion, alAbrirEmpresas, alAbrirPersonas,
                alAbrirEstados, alAbrirVisitas, alAbrirIncidentes, alAbrirAuditoria);
    }

    private void construir(Usuario usuario, Runnable alCerrarSesion,
            Runnable alAbrirEmpresas, Runnable alAbrirPersonas,
            Runnable alAbrirEstados, Runnable alAbrirVisitas,
            Runnable alAbrirIncidentes, Runnable alAbrirAuditoria) {
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
        Label mensaje = new Label("Selecciona un modulo en el menu lateral para comenzar.");
        mensaje.setWrapText(true);
        mensaje.getStyleClass().add("texto-secundario");

        VBox contenido = new VBox(14, bienvenida, rol, empresa, mensaje);
        contenido.setMaxWidth(640);
        contenido.setPadding(new Insets(38));
        contenido.getStyleClass().add("tarjeta-dashboard");

        VBox centro = new VBox(contenido);
        centro.setAlignment(Pos.CENTER);
        centro.setPadding(new Insets(40));

        Button empresasButton = botonMenu("Empresas", alAbrirEmpresas);
        Button personasButton = botonMenu("Personas", alAbrirPersonas);
        Button estadosButton = botonMenu("Estados de acceso", alAbrirEstados);
        Button visitasButton = botonMenu("Control de visitas", alAbrirVisitas);
        Button incidentesButton = botonMenu("Incidentes", alAbrirIncidentes);
        Button auditoriaButton = botonMenu("Auditoria", alAbrirAuditoria);
        empresasButton.setDisable(!usuario.getRol().tienePermiso(PermisosSistema.CONSULTAR_EMPRESA));
        personasButton.setDisable(!usuario.getRol().tienePermiso(PermisosSistema.CONSULTAR_PERSONA));
        estadosButton.setDisable(!usuario.getRol().tienePermiso(PermisosSistema.CONSULTAR_PERSONA));
        visitasButton.setDisable(!usuario.getRol().tienePermiso(PermisosSistema.CONSULTAR_VISITA));
        incidentesButton.setDisable(!usuario.getRol().tienePermiso(
                PermisosSistema.CONSULTAR_INCIDENTE));
        auditoriaButton.setDisable(!usuario.getRol().tienePermiso(
                PermisosSistema.CONSULTAR_AUDITORIA));
        VBox menu = new VBox(8, new Label("MODULOS"), empresasButton,
                personasButton, estadosButton, visitasButton, incidentesButton, auditoriaButton);
        menu.getStyleClass().add("menu-lateral");
        menu.setPadding(new Insets(24, 14, 24, 14));
        menu.setPrefWidth(190);

        root.setTop(encabezado);
        root.setLeft(menu);
        root.setCenter(centro);
    }

    private Button botonMenu(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("boton-menu");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(evento -> accion.run());
        return boton;
    }

    public void mostrarContenido(Parent contenido) {
        root.setCenter(contenido);
    }

    public Parent getRoot() {
        return root;
    }
}
