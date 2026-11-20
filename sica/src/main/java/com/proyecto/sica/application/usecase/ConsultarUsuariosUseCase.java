package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.UsuarioRepository;
import java.util.List;

/**
 * Caso de uso para consultar la lista de usuarios.
 */
public final class ConsultarUsuariosUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ServicioAutorizacion autorizacion;

    public ConsultarUsuariosUseCase(UsuarioRepository usuarioRepository,
            ServicioAutorizacion autorizacion) {
        this.usuarioRepository = usuarioRepository;
        this.autorizacion = autorizacion;
    }

    public List<Usuario> ejecutar(Usuario actor) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_USUARIO);
        return usuarioRepository.listar();
    }
}
