package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.RolRepository;
import java.util.List;

/** Lista roles y sus permisos para la administracion de usuarios. */
public final class ConsultarRolesUseCase {
    private final RolRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarRolesUseCase(RolRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public List<Rol> ejecutar(Usuario actor) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_USUARIO);
        return repositorio.listar();
    }
}
