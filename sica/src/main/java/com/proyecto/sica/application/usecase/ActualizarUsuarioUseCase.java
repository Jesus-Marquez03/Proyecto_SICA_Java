package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorUsuario;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.UsuarioRepository;

/**
 * Caso de uso para actualizar datos basicos sin cambiar la contrasena.
 */
public final class ActualizarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorUsuario validador;

    public ActualizarUsuarioUseCase(UsuarioRepository usuarioRepository,
            ServicioAutorizacion autorizacion, ValidadorUsuario validador) {
        this.usuarioRepository = usuarioRepository;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Usuario ejecutar(Usuario actor, SolicitudActualizarUsuario solicitud) {
        autorizacion.verificarPermiso(actor, PermisosSistema.GESTIONAR_USUARIOS);
        Usuario actual = usuarioRepository.buscarPorId(solicitud.getId())
                .orElseThrow(() -> new ValidacionException("El usuario no existe"));
        validador.validarTextos(solicitud.getNombreUsuario(), solicitud.getNombres(),
                solicitud.getApellidos(), solicitud.getCorreo());
        validador.validarCorreoUnico(solicitud.getCorreo(), solicitud.getId());
        Rol rol = validador.obtenerRolValido(solicitud.getRolId());
        String empresaNombre = validador.validarYObtenerEmpresa(rol, solicitud.getEmpresaId());
        String correo = validador.normalizarCorreo(solicitud.getCorreo());

        Usuario actualizado = new Usuario(actual.getId(), rol, solicitud.getEmpresaId(),
                empresaNombre, solicitud.getNombreUsuario().trim(), actual.getPasswordHash(),
                solicitud.getNombres().trim(), solicitud.getApellidos().trim(), correo,
                actual.isActivo(), actual.getUltimoAcceso());
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "ACTUALIZAR_USUARIO", "usuarios", String.valueOf(actual.getId()),
                "EXITOSO", "Actualizacion del usuario " + correo);
        usuarioRepository.actualizarConAuditoria(actualizado, evento);
        return actualizado;
    }
}
