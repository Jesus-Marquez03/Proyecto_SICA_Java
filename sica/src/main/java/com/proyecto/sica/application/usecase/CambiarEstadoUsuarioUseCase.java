package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.UsuarioRepository;

/**
 * Activa o desactiva usuarios conservando su historial.
 */
public final class CambiarEstadoUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ServicioAutorizacion autorizacion;

    public CambiarEstadoUsuarioUseCase(UsuarioRepository usuarioRepository,
            ServicioAutorizacion autorizacion) {
        this.usuarioRepository = usuarioRepository;
        this.autorizacion = autorizacion;
    }

    public void ejecutar(Usuario actor, long usuarioId, boolean activo) {
        autorizacion.verificarPermiso(actor, PermisosSistema.GESTIONAR_USUARIOS);
        usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new ValidacionException("El usuario no existe"));
        String accion = activo ? "ACTIVAR_USUARIO" : "DESACTIVAR_USUARIO";
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                accion, "usuarios", String.valueOf(usuarioId), "EXITOSO",
                activo ? "Activacion de usuario" : "Desactivacion de usuario");
        usuarioRepository.cambiarEstadoConAuditoria(usuarioId, activo, evento);
    }
}
