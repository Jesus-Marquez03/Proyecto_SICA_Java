package com.proyecto.sica.infrastructure.ui;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.proyecto.sica.application.service.AutenticacionException;
import com.proyecto.sica.application.service.ServicioAutenticacion;
import com.proyecto.sica.config.ConfiguracionAplicacion;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.controller.EmpresaController;
import com.proyecto.sica.infrastructure.ui.controller.EstadoAccesoController;
import com.proyecto.sica.infrastructure.ui.controller.PersonaController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Aplicacion JavaFX que coordina las vistas con los servicios de aplicacion.
 */
public final class SicaApplication extends Application {

    private Stage stage;
    private ServicioAutenticacion autenticacion;
    private ConfiguracionAplicacion configuracion;
    private LoginView loginView;

    public static void iniciar(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage stagePrincipal) {
        this.stage = stagePrincipal;
        configuracion = new ConfiguracionAplicacion();
        this.autenticacion = configuracion.getServicioAutenticacion();

        stage.setTitle("SICA - Control de acceso");
        stage.setMinWidth(760);
        stage.setMinHeight(520);
        mostrarLogin();
        stage.show();
    }

    private void mostrarLogin() {
        loginView = new LoginView(this::autenticar);
        aplicarEscena(new Scene(loginView.getRoot(), 900, 600));
    }

    private void autenticar(String correo, char[] password) {
        try {
            Usuario usuario = autenticacion.iniciarSesion(correo, password);
            mostrarDashboard(usuario);
        } catch (AutenticacionException excepcion) {
            loginView.mostrarError(excepcion.getMessage());
        } catch (RuntimeException excepcion) {
            loginView.mostrarError("No fue posible iniciar sesion. Verifica la conexion con MySQL.");
        }
    }

    private void mostrarDashboard(Usuario usuario) {
        EmpresaView empresaView = new EmpresaView();
        PersonaView personaView = new PersonaView();
        EstadoAccesoView estadoView = new EstadoAccesoView();
        EmpresaController empresaController = new EmpresaController(empresaView, usuario,
                configuracion.getCrearEmpresaUseCase(),
                configuracion.getActualizarEmpresaUseCase(),
                configuracion.getConsultarEmpresasUseCase());
        PersonaController personaController = new PersonaController(personaView, usuario,
                configuracion.getCrearPersonaUseCase(),
                configuracion.getActualizarPersonaUseCase(),
                configuracion.getConsultarPersonasUseCase(),
                configuracion.getBuscarPersonaPorDocumentoUseCase(),
                configuracion.getConsultarEmpresasUseCase());
        EstadoAccesoController estadoController = new EstadoAccesoController(estadoView, usuario,
                configuracion.getConsultarPersonasUseCase(),
                configuracion.getConsultarEstadosAccesoUseCase(),
                configuracion.getCambiarEstadoAccesoUseCase());

        final DashboardView[] referencia = new DashboardView[1];
        DashboardView dashboard = new DashboardView(usuario, this::cerrarSesion,
                () -> {
                    referencia[0].mostrarContenido(empresaView.getRoot());
                    empresaController.cargar();
                },
                () -> {
                    referencia[0].mostrarContenido(personaView.getRoot());
                    personaController.cargar();
                },
                () -> {
                    referencia[0].mostrarContenido(estadoView.getRoot());
                    estadoController.cargar();
                });
        referencia[0] = dashboard;
        aplicarEscena(new Scene(dashboard.getRoot(), 1180, 720));
    }

    private void cerrarSesion() {
        autenticacion.cerrarSesion();
        mostrarLogin();
    }

    private void aplicarEscena(Scene escena) {
        escena.getStylesheets().add(getClass().getResource("/styles/sica.css").toExternalForm());
        stage.setScene(escena);
    }

    @Override
    public void stop() {
        if (autenticacion != null) {
            autenticacion.cerrarSesion();
        }
        AbandonedConnectionCleanupThread.checkedShutdown();
    }
}
