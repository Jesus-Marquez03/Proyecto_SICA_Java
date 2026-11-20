package com.proyecto.sica.config;

import com.proyecto.sica.application.service.InicializadorUsuariosDemo;
import com.proyecto.sica.application.service.ProtectorPassword;
import com.proyecto.sica.application.service.ServicioAutenticacion;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.SesionUsuario;
import com.proyecto.sica.application.service.ValidadorUsuario;
import com.proyecto.sica.application.usecase.ActualizarUsuarioUseCase;
import com.proyecto.sica.application.usecase.CambiarEstadoUsuarioUseCase;
import com.proyecto.sica.application.usecase.ConsultarUsuarioPorIdUseCase;
import com.proyecto.sica.application.usecase.ConsultarUsuariosUseCase;
import com.proyecto.sica.application.usecase.CrearUsuarioUseCase;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import com.proyecto.sica.infrastructure.persistence.AuditoriaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.EmpresaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.RolJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.UsuarioJdbcRepository;

/**
 * Ensambla manualmente los adaptadores y casos de uso, sin framework externo.
 */
public final class ConfiguracionAplicacion {

    private final ServicioAutenticacion servicioAutenticacion;
    private final SesionUsuario sesionUsuario;
    private final InicializadorUsuariosDemo inicializadorUsuariosDemo;
    private final CrearUsuarioUseCase crearUsuarioUseCase;
    private final ConsultarUsuariosUseCase consultarUsuariosUseCase;
    private final ConsultarUsuarioPorIdUseCase consultarUsuarioPorIdUseCase;
    private final ActualizarUsuarioUseCase actualizarUsuarioUseCase;
    private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;

    public ConfiguracionAplicacion() {
        ConnectionFactory connectionFactory = new ConnectionFactory(ConfiguracionBaseDatos.cargar());
        AuditoriaJdbcRepository auditoriaRepository = new AuditoriaJdbcRepository(connectionFactory);
        RolJdbcRepository rolRepository = new RolJdbcRepository(connectionFactory);
        EmpresaJdbcRepository empresaRepository = new EmpresaJdbcRepository(connectionFactory);
        UsuarioJdbcRepository usuarioRepository = new UsuarioJdbcRepository(
                connectionFactory, rolRepository, auditoriaRepository);

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
}
