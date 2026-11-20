package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.UsuarioRepository;

/**
 * Caso de uso para buscar un usuario por identificador.
 */
public final class ConsultarUsuarioPorIdUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ServicioAutorizacion autorizacion;

    public ConsultarUsuarioPorIdUseCase(UsuarioRepository usuarioRepository,
            ServicioAutorizacion autorizacion) {
        this.usuarioRepository = usuarioRepository;
        this.autorizacion = autorizacion;
    }

    public Usuario ejecutar(Usuario actor, long id) {
        autorizacion.verificarPermiso(actor, PermisosSistema.GESTIONAR_USUARIOS);
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new ValidacionException("El usuario no existe"));
    }
}
