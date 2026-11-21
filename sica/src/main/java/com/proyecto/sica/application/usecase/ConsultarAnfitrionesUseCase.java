package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.UsuarioRepository;
import java.util.List;

/** Lista funcionarios activos disponibles en una empresa. */
public final class ConsultarAnfitrionesUseCase {
    private final UsuarioRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarAnfitrionesUseCase(UsuarioRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public List<Usuario> ejecutar(Usuario actor, long empresaId) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_VISITA);
        return repositorio.listarPorEmpresa(empresaId).stream()
                .filter(usuario -> usuario.getRol().isRequiereEmpresa())
                .toList();
    }
}
