package com.proyecto.sica.infrastructure.ui.controller;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.usecase.BuscarPersonaPorDocumentoUseCase;
import com.proyecto.sica.application.usecase.ConsultarAnfitrionesUseCase;
import com.proyecto.sica.application.usecase.ConsultarEmpresasUseCase;
import com.proyecto.sica.application.usecase.ConsultarHistorialVisitasUseCase;
import com.proyecto.sica.application.usecase.ConsultarVisitasUseCase;
import com.proyecto.sica.application.usecase.PrerregistrarVisitaUseCase;
import com.proyecto.sica.application.usecase.RegistrarCheckInUseCase;
import com.proyecto.sica.application.usecase.RegistrarCheckOutUseCase;
import com.proyecto.sica.application.usecase.RegularizarSalidaOlvidadaUseCase;
import com.proyecto.sica.application.usecase.ResponderSolicitudVisitaUseCase;
import com.proyecto.sica.application.usecase.SolicitarPaseTemporalUseCase;
import com.proyecto.sica.application.usecase.SolicitarVisitaNoAnunciadaUseCase;
import com.proyecto.sica.application.usecase.SolicitudVisita;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.infrastructure.ui.ActualizadorPeriodico;
import com.proyecto.sica.infrastructure.ui.EjecutorTareasUi;
import com.proyecto.sica.infrastructure.ui.VisitasView;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Controlador MVC de los cuatro escenarios de acceso. */
public final class VisitasController implements AutoCloseable {
    private final VisitasView vista;
    private final Usuario actor;
    private final BuscarPersonaPorDocumentoUseCase buscarPersona;
    private final ConsultarEmpresasUseCase consultarEmpresas;
    private final ConsultarAnfitrionesUseCase consultarAnfitriones;
    private final PrerregistrarVisitaUseCase prerregistrar;
    private final SolicitarVisitaNoAnunciadaUseCase solicitarNoAnunciada;
    private final SolicitarPaseTemporalUseCase solicitarPase;
    private final ResponderSolicitudVisitaUseCase responder;
    private final ConsultarVisitasUseCase consultarVisitas;
    private final ConsultarHistorialVisitasUseCase consultarHistorial;
    private final RegistrarCheckInUseCase checkIn;
    private final RegistrarCheckOutUseCase checkOut;
    private final RegularizarSalidaOlvidadaUseCase regularizar;
    private final ActualizadorPeriodico<Actualizacion> polling;
    private final AtomicBoolean errorPollingMostrado = new AtomicBoolean();
    private final AtomicLong versionConsultaAnfitriones = new AtomicLong();
    private volatile String tipoDocumentoPolling;
    private volatile String documentoPolling;
    private Persona personaActual;

    public VisitasController(VisitasView vista, Usuario actor,
            BuscarPersonaPorDocumentoUseCase buscarPersona,
            ConsultarEmpresasUseCase consultarEmpresas,
            ConsultarAnfitrionesUseCase consultarAnfitriones,
            PrerregistrarVisitaUseCase prerregistrar,
            SolicitarVisitaNoAnunciadaUseCase solicitarNoAnunciada,
            SolicitarPaseTemporalUseCase solicitarPase,
            ResponderSolicitudVisitaUseCase responder,
            ConsultarVisitasUseCase consultarVisitas,
            ConsultarHistorialVisitasUseCase consultarHistorial,
            RegistrarCheckInUseCase checkIn, RegistrarCheckOutUseCase checkOut,
            RegularizarSalidaOlvidadaUseCase regularizar) {
        this.vista = vista;
        this.actor = actor;
        this.buscarPersona = buscarPersona;
        this.consultarEmpresas = consultarEmpresas;
        this.consultarAnfitriones = consultarAnfitriones;
        this.prerregistrar = prerregistrar;
        this.solicitarNoAnunciada = solicitarNoAnunciada;
        this.solicitarPase = solicitarPase;
        this.responder = responder;
        this.consultarVisitas = consultarVisitas;
        this.consultarHistorial = consultarHistorial;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.regularizar = regularizar;
        enlazarVista();
        boolean panelGuarda = tiene(PermisosSistema.SOLICITAR_INGRESO)
                || tiene(PermisosSistema.REGISTRAR_CHECK_IN);
        boolean panelFuncionario = tiene(PermisosSistema.PRERREGISTRAR_VISITA)
                || tiene(PermisosSistema.APROBAR_SOLICITUD);
        vista.configurarPermisos(panelGuarda, panelFuncionario,
                tiene(PermisosSistema.CONSULTAR_HISTORIAL_VISITAS));
        polling = new ActualizadorPeriodico<>(this::consultarActualizacion,
                this::aplicarActualizacion, this::manejarErrorPolling, Duration.ofSeconds(4));
    }

    public void cargar() {
        EjecutorTareasUi.ejecutar(() -> consultarEmpresas.ejecutar(actor),
                vista::mostrarEmpresas, this::mostrarError);
        polling.iniciar();
    }

    private void enlazarVista() {
        vista.alBuscarPersona(this::buscarPersona);
        vista.alSeleccionarEmpresa(this::cargarAnfitriones);
        vista.alNoAnunciada(() -> crearSolicitud(false));
        vista.alPase(() -> crearSolicitud(true));
        vista.alCheckIn(this::registrarIngreso);
        vista.alCheckOut(this::registrarSalida);
        vista.alRegularizar(this::regularizarSalida);
        vista.alPrerregistrar(this::prerregistrar);
        vista.alAprobar(() -> responder(true));
        vista.alRechazar(() -> responder(false));
        vista.alConsultarHistorial(this::consultarHistorial);
    }

    private void buscarPersona() {
        String tipo = vista.getTipoDocumento();
        String numero = vista.getDocumento();
        EjecutorTareasUi.ejecutar(() -> {
            Persona persona = buscarPersona.ejecutar(actor, tipo, numero);
            List<Visita> visitas = consultarVisitas.buscarPorDocumento(actor, tipo, numero);
            return new PersonaYVisitas(persona, visitas);
        }, resultado -> {
            personaActual = resultado.persona();
            tipoDocumentoPolling = tipo;
            documentoPolling = numero;
            vista.mostrarPersona(resultado.persona());
            vista.mostrarVisitas(resultado.visitas());
        }, error -> {
            personaActual = null;
            vista.mostrarError((error.getMessage() == null ? "Persona no encontrada" : error.getMessage())
                    + ". Si es un invitado nuevo, registrelo primero en el modulo Personas.");
        });
    }

    private void cargarAnfitriones() {
        Empresa empresa = vista.getEmpresa();
        long version = versionConsultaAnfitriones.incrementAndGet();
        if (empresa == null) {
            vista.mostrarAnfitriones(List.of());
            return;
        }
        long empresaId = empresa.getId();
        vista.preparandoAnfitriones();
        EjecutorTareasUi.ejecutar(() -> consultarAnfitriones.ejecutar(actor, empresaId),
                anfitriones -> {
                    Empresa actual = vista.getEmpresa();
                    if (version == versionConsultaAnfitriones.get()
                            && actual != null && actual.getId() == empresaId) {
                        vista.mostrarAnfitriones(anfitriones);
                    }
                }, error -> {
                    if (version == versionConsultaAnfitriones.get()) {
                        vista.mostrarAnfitriones(List.of());
                        mostrarError(error);
                    }
                });
    }

    private void crearSolicitud(boolean pase) {
        Persona persona = exigirPersona();
        if (persona == null) return;
        Empresa empresa = vista.getEmpresa();
        Usuario anfitrion = vista.getAnfitrion();
        if (empresa == null || anfitrion == null) {
            vista.mostrarError("Seleccione empresa y funcionario anfitrion");
            return;
        }
        SolicitudVisita solicitud = new SolicitudVisita(persona.getId(), empresa.getId(),
                anfitrion.getId(), LocalDateTime.now(), vista.getMotivoGuarda(),
                vista.getPlacaGuarda(), null);
        EjecutorTareasUi.ejecutar(() -> pase
                ? solicitarPase.ejecutar(actor, solicitud)
                : solicitarNoAnunciada.ejecutar(actor, solicitud), creada -> {
                    vista.mostrarInformacion(pase
                            ? "Pase temporal enviado para aprobacion"
                            : "Solicitud enviada al funcionario");
                    refrescarDocumento();
                }, this::mostrarError);
    }

    private void prerregistrar() {
        Persona persona = exigirPersona();
        if (persona == null) return;
        if (actor.getEmpresaId() == null) {
            vista.mostrarError("El usuario no tiene empresa asociada");
            return;
        }
        LocalDateTime fecha;
        try {
            fecha = LocalDateTime.of(vista.getFechaProgramada(),
                    LocalTime.parse(vista.getHoraProgramada()));
        } catch (RuntimeException error) {
            vista.mostrarError("Use una fecha valida y hora con formato HH:mm");
            return;
        }
        SolicitudVisita solicitud = new SolicitudVisita(persona.getId(), actor.getEmpresaId(),
                actor.getId(), fecha, vista.getMotivoFuncionario(),
                vista.getPlacaFuncionario(), null);
        EjecutorTareasUi.ejecutar(() -> prerregistrar.ejecutar(actor, solicitud), creada -> {
            vista.mostrarInformacion("Visita prerregistrada y aprobada");
            refrescarDocumento();
        }, this::mostrarError);
    }

    private void responder(boolean aprobar) {
        Visita visita = vista.getPendienteSeleccionada();
        if (visita == null) {
            vista.mostrarError("Seleccione una solicitud pendiente");
            return;
        }
        EjecutorTareasUi.ejecutar(() -> aprobar
                ? responder.aprobar(actor, visita.getId())
                : responder.rechazar(actor, visita.getId()), actualizada -> {
                    vista.mostrarInformacion(aprobar ? "Solicitud aprobada" : "Solicitud rechazada");
                    refrescarPendientes();
                }, this::mostrarError);
    }

    private void registrarIngreso() {
        Visita visita = exigirVisitaSeleccionada();
        if (visita == null) return;
        EjecutorTareasUi.ejecutar(() -> checkIn.ejecutar(actor, visita.getId()), resultado -> {
            vista.mostrarInformacion(resultado.huboRegularizacion()
                    ? "Ingreso registrado y salida anterior regularizada"
                    : "Ingreso registrado");
            refrescarDocumento();
        }, this::mostrarError);
    }

    private void registrarSalida() {
        Visita visita = exigirVisitaSeleccionada();
        if (visita == null) return;
        EjecutorTareasUi.ejecutar(() -> checkOut.ejecutar(actor, visita.getId()), resultado -> {
            vista.mostrarInformacion("Salida registrada");
            refrescarDocumento();
        }, this::mostrarError);
    }

    private void regularizarSalida() {
        Visita visita = exigirVisitaSeleccionada();
        if (visita == null) return;
        String motivo = vista.getMotivoGuarda();
        EjecutorTareasUi.ejecutar(() -> regularizar.ejecutar(actor, visita.getId(), motivo),
                resultado -> {
                    vista.mostrarInformacion("Salida olvidada regularizada");
                    refrescarDocumento();
                }, this::mostrarError);
    }

    private void consultarHistorial() {
        String documento = vista.getHistorialDocumento();
        String estado = vista.getHistorialEstado();
        java.time.LocalDate desde = vista.getHistorialDesde();
        java.time.LocalDate hasta = vista.getHistorialHasta();
        EjecutorTareasUi.ejecutar(() -> consultarHistorial.ejecutar(actor, documento,
                desde, hasta, estado),
                vista::mostrarHistorial, this::mostrarError);
    }

    private Actualizacion consultarActualizacion() {
        List<Visita> pendientes = tiene(PermisosSistema.APROBAR_SOLICITUD)
                ? consultarVisitas.pendientesAsignadas(actor) : List.of();
        String tipo = tipoDocumentoPolling;
        String documento = documentoPolling;
        List<Visita> visitas = tipo == null || documento == null || documento.isBlank()
                ? List.of() : consultarVisitas.buscarPorDocumento(actor, tipo, documento);
        return new Actualizacion(pendientes, visitas);
    }

    private void aplicarActualizacion(Actualizacion actualizacion) {
        errorPollingMostrado.set(false);
        if (tiene(PermisosSistema.APROBAR_SOLICITUD)) {
            vista.mostrarPendientes(actualizacion.pendientes());
        }
        if (documentoPolling != null) vista.mostrarVisitas(actualizacion.visitasDocumento());
    }

    private void manejarErrorPolling(Throwable error) {
        if (errorPollingMostrado.compareAndSet(false, true)) {
            mostrarError(error);
        }
    }

    private void refrescarDocumento() {
        String tipo = tipoDocumentoPolling;
        String documento = documentoPolling;
        if (tipo == null || documento == null) return;
        EjecutorTareasUi.ejecutar(() -> consultarVisitas.buscarPorDocumento(actor, tipo, documento),
                vista::mostrarVisitas, this::mostrarError);
    }

    private void refrescarPendientes() {
        EjecutorTareasUi.ejecutar(() -> consultarVisitas.pendientesAsignadas(actor),
                vista::mostrarPendientes, this::mostrarError);
    }

    private Persona exigirPersona() {
        if (personaActual == null) {
            vista.mostrarError("Busque y seleccione una persona");
            return null;
        }
        return personaActual;
    }

    private Visita exigirVisitaSeleccionada() {
        Visita visita = vista.getVisitaSeleccionada();
        if (visita == null) {
            vista.mostrarError("Seleccione una visita");
            return null;
        }
        return visita;
    }

    private boolean tiene(String permiso) { return actor.getRol().tienePermiso(permiso); }
    private void mostrarError(Throwable error) {
        vista.mostrarError(error.getMessage() == null
                ? "No fue posible completar la operacion" : error.getMessage());
    }

    @Override
    public void close() { polling.close(); }

    private record PersonaYVisitas(Persona persona, List<Visita> visitas) { }
    private record Actualizacion(List<Visita> pendientes, List<Visita> visitasDocumento) { }
}
