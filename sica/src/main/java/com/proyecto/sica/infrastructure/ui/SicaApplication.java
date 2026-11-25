package com.proyecto.sica.infrastructure.ui;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.proyecto.sica.application.service.AutenticacionException;
import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutenticacion;
import com.proyecto.sica.config.ConfiguracionAplicacion;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import com.proyecto.sica.infrastructure.ui.controller.AuditoriaController;
import com.proyecto.sica.infrastructure.ui.controller.DashboardController;
import com.proyecto.sica.infrastructure.ui.controller.EmpresaController;
import com.proyecto.sica.infrastructure.ui.controller.EstadoAccesoController;
import com.proyecto.sica.infrastructure.ui.controller.IncidentesController;
import com.proyecto.sica.infrastructure.ui.controller.PersonaController;
import com.proyecto.sica.infrastructure.ui.controller.ReportesController;
import com.proyecto.sica.infrastructure.ui.controller.UsuariosController;
import com.proyecto.sica.infrastructure.ui.controller.VisitasController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Punto de composicion JavaFX y navegacion entre controladores MVC. */
public final class SicaApplication extends Application {
    private Stage stage;
    private ServicioAutenticacion autenticacion;
    private ConfiguracionAplicacion configuracion;
    private LoginView loginView;
    private VisitasController visitasController;
    private DashboardView dashboardView;

    public static void iniciar(String... args) { launch(args); }

    @Override
    public void start(Stage stagePrincipal) {
        stage = stagePrincipal;
        configuracion = new ConfiguracionAplicacion();
        autenticacion = configuracion.getServicioAutenticacion();
        stage.setTitle("SICA Zona Acme");
        stage.setMinWidth(1050);
        stage.setMinHeight(680);
        mostrarLogin();
        stage.show();
    }

    private void mostrarLogin() {
        detenerVistasActivas();
        loginView = new LoginView(this::autenticar);
        aplicarEscena(new Scene(loginView.getRoot(), 1100, 720));
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
        DashboardInicioView inicioView = new DashboardInicioView();
        EmpresaView empresaView = new EmpresaView();
        PersonaView personaView = new PersonaView();
        EstadoAccesoView estadoView = new EstadoAccesoView();
        VisitasView visitasView = new VisitasView();
        IncidentesView incidentesView = new IncidentesView();
        ReportesView reportesView = new ReportesView();
        UsuariosView usuariosView = new UsuariosView();
        AuditoriaView auditoriaView = new AuditoriaView();

        DashboardController inicioController = new DashboardController(inicioView, usuario,
                configuracion.getConsultarResumenDashboardUseCase());
        EmpresaController empresaController = new EmpresaController(empresaView, usuario,
                configuracion.getCrearEmpresaUseCase(), configuracion.getActualizarEmpresaUseCase(),
                configuracion.getConsultarEmpresasUseCase());
        PersonaController personaController = new PersonaController(personaView, usuario,
                configuracion.getCrearPersonaUseCase(), configuracion.getActualizarPersonaUseCase(),
                configuracion.getConsultarPersonasUseCase(),
                configuracion.getBuscarPersonaPorDocumentoUseCase(),
                configuracion.getConsultarEmpresasUseCase());
        EstadoAccesoController estadoController = new EstadoAccesoController(estadoView, usuario,
                configuracion.getConsultarPersonasUseCase(),
                configuracion.getConsultarEstadosAccesoUseCase(),
                configuracion.getCambiarEstadoAccesoUseCase());
        visitasController = new VisitasController(visitasView, usuario,
                configuracion.getBuscarPersonaPorDocumentoUseCase(),
                configuracion.getConsultarEmpresasUseCase(),
                configuracion.getConsultarAnfitrionesUseCase(),
                configuracion.getPrerregistrarVisitaUseCase(),
                configuracion.getSolicitarVisitaNoAnunciadaUseCase(),
                configuracion.getSolicitarPaseTemporalUseCase(),
                configuracion.getResponderSolicitudVisitaUseCase(),
                configuracion.getConsultarVisitasUseCase(),
                configuracion.getConsultarHistorialVisitasUseCase(),
                configuracion.getRegistrarCheckInUseCase(),
                configuracion.getRegistrarCheckOutUseCase(),
                configuracion.getRegularizarSalidaOlvidadaUseCase());
        IncidentesController incidentesController = new IncidentesController(incidentesView,
                usuario, configuracion.getCrearIncidenteUseCase(),
                configuracion.getConsultarIncidentesUseCase(),
                configuracion.getActualizarIncidenteUseCase(),
                configuracion.getConsultarEstadosAccesoUseCase(),
                configuracion.getCambiarEstadoAccesoUseCase());
        ReportesController reportesController = new ReportesController(reportesView, usuario,
                configuracion.getConsultarReportesUseCase());
        UsuariosController usuariosController = new UsuariosController(usuariosView, usuario,
                configuracion.getCrearUsuarioUseCase(), configuracion.getActualizarUsuarioUseCase(),
                configuracion.getCambiarEstadoUsuarioUseCase(),
                configuracion.getConsultarUsuariosUseCase(), configuracion.getConsultarRolesUseCase(),
                configuracion.getConsultarEmpresasUseCase());
        AuditoriaController auditoriaController = new AuditoriaController(auditoriaView, usuario,
                configuracion.getConsultarAuditoriaUseCase());

        boolean puedeEstados = tiene(usuario, PermisosSistema.CONSULTAR_PERSONA);
        visitasView.integrarEstadosAcceso(estadoView.getRoot(), puedeEstados);
        dashboardView = new DashboardView(usuario, this::cerrarSesion);
        empresaView.alConfirmar(dashboardView::mostrarConfirmacion);
        personaView.alConfirmar(dashboardView::mostrarConfirmacion);
        visitasView.alConfirmar(dashboardView::mostrarConfirmacion);
        incidentesView.alConfirmar(dashboardView::mostrarConfirmacion);
        registrarNavegacion(usuario, inicioView, inicioController, empresaView, empresaController,
                personaView, personaController, visitasView, estadoController, incidentesView,
                incidentesController, reportesView, reportesController, usuariosView,
                usuariosController, auditoriaView, auditoriaController);

        dashboardView.activar("Dashboard");
        abrir(inicioView.getRoot(), inicioController::cargar);
        aplicarEscena(new Scene(dashboardView.getRoot(), 1366, 820));
    }

    private void registrarNavegacion(Usuario usuario, DashboardInicioView inicioView,
            DashboardController inicioController, EmpresaView empresaView,
            EmpresaController empresaController, PersonaView personaView,
            PersonaController personaController, VisitasView visitasView,
            EstadoAccesoController estadoController, IncidentesView incidentesView,
            IncidentesController incidentesController, ReportesView reportesView,
            ReportesController reportesController, UsuariosView usuariosView,
            UsuariosController usuariosController, AuditoriaView auditoriaView,
            AuditoriaController auditoriaController) {
        boolean consultarVisita = tiene(usuario, PermisosSistema.CONSULTAR_VISITA);
        boolean control = tieneAlguno(usuario, PermisosSistema.REGISTRAR_CHECK_IN,
                PermisosSistema.REGISTRAR_CHECK_OUT, PermisosSistema.SOLICITAR_INGRESO,
                PermisosSistema.CAMBIAR_ESTADO_ACCESO);
        dashboardView.registrarOpcion("Dashboard", TipoIcono.DASHBOARD, consultarVisita,
                () -> abrir(inicioView.getRoot(), inicioController::cargar));
        dashboardView.registrarOpcion("Control de acceso", TipoIcono.CONTROL_ACCESO, control,
                () -> abrir(visitasView.getRoot(), () -> {
                    visitasView.mostrarPanelControl();
                    visitasController.cargar();
                    if (tiene(usuario, PermisosSistema.CONSULTAR_PERSONA)) estadoController.cargar();
                }));
        dashboardView.registrarOpcion("Visitas", TipoIcono.VISITAS, consultarVisita,
                () -> abrir(visitasView.getRoot(), () -> {
                    visitasView.mostrarPanelVisitas();
                    visitasController.cargar();
                }));
        dashboardView.registrarOpcion("Personas", TipoIcono.PERSONAS,
                tiene(usuario, PermisosSistema.CONSULTAR_PERSONA),
                () -> abrir(personaView.getRoot(), personaController::cargar));
        dashboardView.registrarOpcion("Empresas", TipoIcono.EMPRESAS,
                tiene(usuario, PermisosSistema.CONSULTAR_EMPRESA),
                () -> abrir(empresaView.getRoot(), empresaController::cargar));
        dashboardView.registrarOpcion("Incidentes", TipoIcono.INCIDENTES,
                tiene(usuario, PermisosSistema.CONSULTAR_INCIDENTE),
                () -> abrir(incidentesView.getRoot(), incidentesController::cargar));
        dashboardView.registrarOpcion("Reportes", TipoIcono.REPORTES,
                tiene(usuario, PermisosSistema.GENERAR_REPORTES)
                        && tiene(usuario, PermisosSistema.CONSULTAR_INCIDENTE),
                () -> abrir(reportesView.getRoot(), reportesController::cargar));
        dashboardView.registrarOpcion("Usuarios y roles", TipoIcono.USUARIOS,
                tiene(usuario, PermisosSistema.CONSULTAR_USUARIO),
                () -> abrir(usuariosView.getRoot(), usuariosController::cargar));
        dashboardView.registrarOpcion("Bitacora", TipoIcono.BITACORA,
                tiene(usuario, PermisosSistema.CONSULTAR_AUDITORIA),
                () -> abrir(auditoriaView.getRoot(), auditoriaController::cargar));
    }

    private void abrir(Parent contenido, Runnable alAbrir) {
        dashboardView.mostrarContenido(contenido);
        alAbrir.run();
    }

    private boolean tiene(Usuario usuario, String permiso) {
        return usuario.getRol().tienePermiso(permiso);
    }

    private boolean tieneAlguno(Usuario usuario, String... permisos) {
        for (String permiso : permisos) if (tiene(usuario, permiso)) return true;
        return false;
    }

    private void cerrarSesion() {
        detenerVistasActivas();
        autenticacion.cerrarSesion();
        mostrarLogin();
    }

    private void aplicarEscena(Scene escena) {
        escena.getStylesheets().add(getClass().getResource("/styles/sica.css").toExternalForm());
        stage.setScene(escena);
    }

    @Override
    public void stop() {
        detenerVistasActivas();
        if (autenticacion != null) autenticacion.cerrarSesion();
        AbandonedConnectionCleanupThread.checkedShutdown();
    }

    private void detenerVistasActivas() {
        if (visitasController != null) { visitasController.close(); visitasController = null; }
        if (dashboardView != null) dashboardView.detenerNotificaciones();
    }
}
