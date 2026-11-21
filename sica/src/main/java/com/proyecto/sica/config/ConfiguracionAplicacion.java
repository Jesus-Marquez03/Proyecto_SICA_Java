package com.proyecto.sica.config;

import com.proyecto.sica.application.service.InicializadorUsuariosDemo;
import com.proyecto.sica.application.service.InicializadorVisitasDemo;
import com.proyecto.sica.application.service.InicializadorIncidentesDemo;
import com.proyecto.sica.application.service.FabricaVisita;
import com.proyecto.sica.application.service.GeneradorNumeroVisita;
import com.proyecto.sica.application.service.ProtectorPassword;
import com.proyecto.sica.application.service.ServicioAutenticacion;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.SesionUsuario;
import com.proyecto.sica.application.service.ValidadorEmpresa;
import com.proyecto.sica.application.service.ValidadorPersona;
import com.proyecto.sica.application.service.ValidadorUsuario;
import com.proyecto.sica.application.service.ValidadorVisita;
import com.proyecto.sica.application.service.ValidadorIncidente;
import com.proyecto.sica.application.usecase.ActualizarEmpresaUseCase;
import com.proyecto.sica.application.usecase.ActualizarPersonaUseCase;
import com.proyecto.sica.application.usecase.ActualizarUsuarioUseCase;
import com.proyecto.sica.application.usecase.BuscarPersonaPorDocumentoUseCase;
import com.proyecto.sica.application.usecase.CambiarEstadoAccesoUseCase;
import com.proyecto.sica.application.usecase.CambiarEstadoUsuarioUseCase;
import com.proyecto.sica.application.usecase.ConsultarEmpresaPorIdUseCase;
import com.proyecto.sica.application.usecase.ConsultarEmpresasUseCase;
import com.proyecto.sica.application.usecase.ConsultarEstadosAccesoUseCase;
import com.proyecto.sica.application.usecase.ConsultarPersonaPorIdUseCase;
import com.proyecto.sica.application.usecase.ConsultarPersonasUseCase;
import com.proyecto.sica.application.usecase.ConsultarUsuarioPorIdUseCase;
import com.proyecto.sica.application.usecase.ConsultarUsuariosUseCase;
import com.proyecto.sica.application.usecase.CrearEmpresaUseCase;
import com.proyecto.sica.application.usecase.CrearPersonaUseCase;
import com.proyecto.sica.application.usecase.CrearUsuarioUseCase;
import com.proyecto.sica.application.usecase.ConsultarAnfitrionesUseCase;
import com.proyecto.sica.application.usecase.ConsultarHistorialVisitasUseCase;
import com.proyecto.sica.application.usecase.ConsultarVisitasUseCase;
import com.proyecto.sica.application.usecase.PrerregistrarVisitaUseCase;
import com.proyecto.sica.application.usecase.RegistrarCheckInUseCase;
import com.proyecto.sica.application.usecase.RegistrarCheckOutUseCase;
import com.proyecto.sica.application.usecase.RegularizarSalidaOlvidadaUseCase;
import com.proyecto.sica.application.usecase.ResponderSolicitudVisitaUseCase;
import com.proyecto.sica.application.usecase.SolicitarPaseTemporalUseCase;
import com.proyecto.sica.application.usecase.SolicitarVisitaNoAnunciadaUseCase;
import com.proyecto.sica.application.usecase.ActualizarIncidenteUseCase;
import com.proyecto.sica.application.usecase.ConsultarAuditoriaUseCase;
import com.proyecto.sica.application.usecase.ConsultarIncidentePorIdUseCase;
import com.proyecto.sica.application.usecase.ConsultarIncidentesUseCase;
import com.proyecto.sica.application.usecase.CrearIncidenteUseCase;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import com.proyecto.sica.infrastructure.persistence.AuditoriaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.EmpresaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.EjecutorTransaccionalJdbc;
import com.proyecto.sica.infrastructure.persistence.EstadoAccesoJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.EstadoVisitaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.PersonaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.RolJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.UsuarioJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.VisitaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.IncidenteJdbcRepository;

/**
 * Ensambla manualmente los adaptadores y casos de uso, sin framework externo.
 */
public final class ConfiguracionAplicacion {

    private final ServicioAutenticacion servicioAutenticacion;
    private final SesionUsuario sesionUsuario;
    private final InicializadorUsuariosDemo inicializadorUsuariosDemo;
    private final InicializadorVisitasDemo inicializadorVisitasDemo;
    private final InicializadorIncidentesDemo inicializadorIncidentesDemo;
    private final CrearUsuarioUseCase crearUsuarioUseCase;
    private final ConsultarUsuariosUseCase consultarUsuariosUseCase;
    private final ConsultarUsuarioPorIdUseCase consultarUsuarioPorIdUseCase;
    private final ActualizarUsuarioUseCase actualizarUsuarioUseCase;
    private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;
    private final CrearEmpresaUseCase crearEmpresaUseCase;
    private final ActualizarEmpresaUseCase actualizarEmpresaUseCase;
    private final ConsultarEmpresasUseCase consultarEmpresasUseCase;
    private final ConsultarEmpresaPorIdUseCase consultarEmpresaPorIdUseCase;
    private final CrearPersonaUseCase crearPersonaUseCase;
    private final ActualizarPersonaUseCase actualizarPersonaUseCase;
    private final ConsultarPersonasUseCase consultarPersonasUseCase;
    private final ConsultarPersonaPorIdUseCase consultarPersonaPorIdUseCase;
    private final BuscarPersonaPorDocumentoUseCase buscarPersonaPorDocumentoUseCase;
    private final ConsultarEstadosAccesoUseCase consultarEstadosAccesoUseCase;
    private final CambiarEstadoAccesoUseCase cambiarEstadoAccesoUseCase;
    private final PrerregistrarVisitaUseCase prerregistrarVisitaUseCase;
    private final SolicitarVisitaNoAnunciadaUseCase solicitarVisitaNoAnunciadaUseCase;
    private final SolicitarPaseTemporalUseCase solicitarPaseTemporalUseCase;
    private final ResponderSolicitudVisitaUseCase responderSolicitudVisitaUseCase;
    private final ConsultarVisitasUseCase consultarVisitasUseCase;
    private final ConsultarHistorialVisitasUseCase consultarHistorialVisitasUseCase;
    private final ConsultarAnfitrionesUseCase consultarAnfitrionesUseCase;
    private final RegistrarCheckInUseCase registrarCheckInUseCase;
    private final RegistrarCheckOutUseCase registrarCheckOutUseCase;
    private final RegularizarSalidaOlvidadaUseCase regularizarSalidaOlvidadaUseCase;
    private final CrearIncidenteUseCase crearIncidenteUseCase;
    private final ConsultarIncidentesUseCase consultarIncidentesUseCase;
    private final ConsultarIncidentePorIdUseCase consultarIncidentePorIdUseCase;
    private final ActualizarIncidenteUseCase actualizarIncidenteUseCase;
    private final ConsultarAuditoriaUseCase consultarAuditoriaUseCase;

    public ConfiguracionAplicacion() {
        ConnectionFactory connectionFactory = new ConnectionFactory(ConfiguracionBaseDatos.cargar());
        AuditoriaJdbcRepository auditoriaRepository = new AuditoriaJdbcRepository(connectionFactory);
        EjecutorTransaccionalJdbc transacciones = new EjecutorTransaccionalJdbc(
                connectionFactory, auditoriaRepository);
        RolJdbcRepository rolRepository = new RolJdbcRepository(connectionFactory);
        EmpresaJdbcRepository empresaRepository = new EmpresaJdbcRepository(
                connectionFactory, transacciones);
        EstadoAccesoJdbcRepository estadoRepository = new EstadoAccesoJdbcRepository(
                connectionFactory);
        PersonaJdbcRepository personaRepository = new PersonaJdbcRepository(
                connectionFactory, transacciones);
        UsuarioJdbcRepository usuarioRepository = new UsuarioJdbcRepository(
                connectionFactory, rolRepository, auditoriaRepository);
        EstadoVisitaJdbcRepository estadoVisitaRepository =
                new EstadoVisitaJdbcRepository(connectionFactory);
        VisitaJdbcRepository visitaRepository = new VisitaJdbcRepository(
                connectionFactory, personaRepository, usuarioRepository, transacciones);
        IncidenteJdbcRepository incidenteRepository = new IncidenteJdbcRepository(
                connectionFactory, visitaRepository, usuarioRepository, transacciones);
        inicializadorVisitasDemo = new InicializadorVisitasDemo(visitaRepository,
                estadoVisitaRepository, personaRepository, usuarioRepository);
        inicializadorIncidentesDemo = new InicializadorIncidentesDemo(incidenteRepository,
                visitaRepository, estadoVisitaRepository, personaRepository, usuarioRepository);

        ProtectorPassword protectorPassword = new ProtectorPassword();
        sesionUsuario = new SesionUsuario();
        ServicioAutorizacion autorizacion = new ServicioAutorizacion();
        ValidadorUsuario validador = new ValidadorUsuario(
                usuarioRepository, rolRepository, empresaRepository);

        servicioAutenticacion = new ServicioAutenticacion(
                usuarioRepository, auditoriaRepository, protectorPassword, sesionUsuario);
        inicializadorUsuariosDemo = new InicializadorUsuariosDemo(
                usuarioRepository, rolRepository, empresaRepository, protectorPassword);
        crearUsuarioUseCase = new CrearUsuarioUseCase(
                usuarioRepository, autorizacion, validador, protectorPassword);
        consultarUsuariosUseCase = new ConsultarUsuariosUseCase(usuarioRepository, autorizacion);
        consultarUsuarioPorIdUseCase = new ConsultarUsuarioPorIdUseCase(
                usuarioRepository, autorizacion);
        actualizarUsuarioUseCase = new ActualizarUsuarioUseCase(
                usuarioRepository, autorizacion, validador);
        cambiarEstadoUsuarioUseCase = new CambiarEstadoUsuarioUseCase(
                usuarioRepository, autorizacion);

        ValidadorEmpresa validadorEmpresa = new ValidadorEmpresa(empresaRepository);
        crearEmpresaUseCase = new CrearEmpresaUseCase(
                empresaRepository, autorizacion, validadorEmpresa);
        actualizarEmpresaUseCase = new ActualizarEmpresaUseCase(
                empresaRepository, autorizacion, validadorEmpresa);
        consultarEmpresasUseCase = new ConsultarEmpresasUseCase(
                empresaRepository, autorizacion);
        consultarEmpresaPorIdUseCase = new ConsultarEmpresaPorIdUseCase(
                empresaRepository, autorizacion);

        ValidadorPersona validadorPersona = new ValidadorPersona(
                personaRepository, empresaRepository, estadoRepository);
        crearPersonaUseCase = new CrearPersonaUseCase(
                personaRepository, autorizacion, validadorPersona);
        actualizarPersonaUseCase = new ActualizarPersonaUseCase(
                personaRepository, autorizacion, validadorPersona);
        consultarPersonasUseCase = new ConsultarPersonasUseCase(
                personaRepository, autorizacion);
        consultarPersonaPorIdUseCase = new ConsultarPersonaPorIdUseCase(
                personaRepository, autorizacion);
        buscarPersonaPorDocumentoUseCase = new BuscarPersonaPorDocumentoUseCase(
                personaRepository, autorizacion);
        consultarEstadosAccesoUseCase = new ConsultarEstadosAccesoUseCase(
                estadoRepository, autorizacion);
        cambiarEstadoAccesoUseCase = new CambiarEstadoAccesoUseCase(
                personaRepository, estadoRepository, autorizacion);

        ValidadorVisita validadorVisita = new ValidadorVisita(personaRepository,
                empresaRepository, usuarioRepository, estadoVisitaRepository);
        FabricaVisita fabricaVisita = new FabricaVisita(new GeneradorNumeroVisita());
        prerregistrarVisitaUseCase = new PrerregistrarVisitaUseCase(
                visitaRepository, autorizacion, validadorVisita, fabricaVisita);
        solicitarVisitaNoAnunciadaUseCase = new SolicitarVisitaNoAnunciadaUseCase(
                visitaRepository, autorizacion, validadorVisita, fabricaVisita);
        solicitarPaseTemporalUseCase = new SolicitarPaseTemporalUseCase(
                visitaRepository, autorizacion, validadorVisita, fabricaVisita);
        responderSolicitudVisitaUseCase = new ResponderSolicitudVisitaUseCase(
                visitaRepository, autorizacion, validadorVisita);
        consultarVisitasUseCase = new ConsultarVisitasUseCase(
                visitaRepository, autorizacion);
        consultarHistorialVisitasUseCase = new ConsultarHistorialVisitasUseCase(
                visitaRepository, autorizacion);
        consultarAnfitrionesUseCase = new ConsultarAnfitrionesUseCase(
                usuarioRepository, autorizacion);
        registrarCheckInUseCase = new RegistrarCheckInUseCase(
                visitaRepository, autorizacion, validadorVisita);
        registrarCheckOutUseCase = new RegistrarCheckOutUseCase(
                visitaRepository, autorizacion, validadorVisita);
        regularizarSalidaOlvidadaUseCase = new RegularizarSalidaOlvidadaUseCase(
                visitaRepository, autorizacion, validadorVisita);

        ValidadorIncidente validadorIncidente = new ValidadorIncidente();
        crearIncidenteUseCase = new CrearIncidenteUseCase(
                incidenteRepository, visitaRepository, autorizacion, validadorIncidente);
        consultarIncidentesUseCase = new ConsultarIncidentesUseCase(
                incidenteRepository, autorizacion);
        consultarIncidentePorIdUseCase = new ConsultarIncidentePorIdUseCase(
                incidenteRepository, autorizacion, validadorIncidente);
        actualizarIncidenteUseCase = new ActualizarIncidenteUseCase(
                incidenteRepository, autorizacion, validadorIncidente);
        consultarAuditoriaUseCase = new ConsultarAuditoriaUseCase(
                auditoriaRepository, autorizacion);
    }

    public ServicioAutenticacion getServicioAutenticacion() {
        return servicioAutenticacion;
    }

    public SesionUsuario getSesionUsuario() {
        return sesionUsuario;
    }

    public InicializadorUsuariosDemo getInicializadorUsuariosDemo() {
        return inicializadorUsuariosDemo;
    }

    public InicializadorVisitasDemo getInicializadorVisitasDemo() {
        return inicializadorVisitasDemo;
    }

    public InicializadorIncidentesDemo getInicializadorIncidentesDemo() {
        return inicializadorIncidentesDemo;
    }

    public CrearUsuarioUseCase getCrearUsuarioUseCase() {
        return crearUsuarioUseCase;
    }

    public ConsultarUsuariosUseCase getConsultarUsuariosUseCase() {
        return consultarUsuariosUseCase;
    }

    public ConsultarUsuarioPorIdUseCase getConsultarUsuarioPorIdUseCase() {
        return consultarUsuarioPorIdUseCase;
    }

    public ActualizarUsuarioUseCase getActualizarUsuarioUseCase() {
        return actualizarUsuarioUseCase;
    }

    public CambiarEstadoUsuarioUseCase getCambiarEstadoUsuarioUseCase() {
        return cambiarEstadoUsuarioUseCase;
    }

    public CrearEmpresaUseCase getCrearEmpresaUseCase() { return crearEmpresaUseCase; }

    public ActualizarEmpresaUseCase getActualizarEmpresaUseCase() {
        return actualizarEmpresaUseCase;
    }

    public ConsultarEmpresasUseCase getConsultarEmpresasUseCase() {
        return consultarEmpresasUseCase;
    }

    public ConsultarEmpresaPorIdUseCase getConsultarEmpresaPorIdUseCase() {
        return consultarEmpresaPorIdUseCase;
    }

    public CrearPersonaUseCase getCrearPersonaUseCase() { return crearPersonaUseCase; }

    public ActualizarPersonaUseCase getActualizarPersonaUseCase() {
        return actualizarPersonaUseCase;
    }

    public ConsultarPersonasUseCase getConsultarPersonasUseCase() {
        return consultarPersonasUseCase;
    }

    public ConsultarPersonaPorIdUseCase getConsultarPersonaPorIdUseCase() {
        return consultarPersonaPorIdUseCase;
    }

    public BuscarPersonaPorDocumentoUseCase getBuscarPersonaPorDocumentoUseCase() {
        return buscarPersonaPorDocumentoUseCase;
    }

    public ConsultarEstadosAccesoUseCase getConsultarEstadosAccesoUseCase() {
        return consultarEstadosAccesoUseCase;
    }

    public CambiarEstadoAccesoUseCase getCambiarEstadoAccesoUseCase() {
        return cambiarEstadoAccesoUseCase;
    }

    public PrerregistrarVisitaUseCase getPrerregistrarVisitaUseCase() {
        return prerregistrarVisitaUseCase;
    }

    public SolicitarVisitaNoAnunciadaUseCase getSolicitarVisitaNoAnunciadaUseCase() {
        return solicitarVisitaNoAnunciadaUseCase;
    }

    public SolicitarPaseTemporalUseCase getSolicitarPaseTemporalUseCase() {
        return solicitarPaseTemporalUseCase;
    }

    public ResponderSolicitudVisitaUseCase getResponderSolicitudVisitaUseCase() {
        return responderSolicitudVisitaUseCase;
    }

    public ConsultarVisitasUseCase getConsultarVisitasUseCase() {
        return consultarVisitasUseCase;
    }

    public ConsultarHistorialVisitasUseCase getConsultarHistorialVisitasUseCase() {
        return consultarHistorialVisitasUseCase;
    }

    public ConsultarAnfitrionesUseCase getConsultarAnfitrionesUseCase() {
        return consultarAnfitrionesUseCase;
    }

    public RegistrarCheckInUseCase getRegistrarCheckInUseCase() {
        return registrarCheckInUseCase;
    }

    public RegistrarCheckOutUseCase getRegistrarCheckOutUseCase() {
        return registrarCheckOutUseCase;
    }

    public RegularizarSalidaOlvidadaUseCase getRegularizarSalidaOlvidadaUseCase() {
        return regularizarSalidaOlvidadaUseCase;
    }

    public CrearIncidenteUseCase getCrearIncidenteUseCase() { return crearIncidenteUseCase; }

    public ConsultarIncidentesUseCase getConsultarIncidentesUseCase() {
        return consultarIncidentesUseCase;
    }

    public ConsultarIncidentePorIdUseCase getConsultarIncidentePorIdUseCase() {
        return consultarIncidentePorIdUseCase;
    }

    public ActualizarIncidenteUseCase getActualizarIncidenteUseCase() {
        return actualizarIncidenteUseCase;
    }

    public ConsultarAuditoriaUseCase getConsultarAuditoriaUseCase() {
        return consultarAuditoriaUseCase;
    }
}
