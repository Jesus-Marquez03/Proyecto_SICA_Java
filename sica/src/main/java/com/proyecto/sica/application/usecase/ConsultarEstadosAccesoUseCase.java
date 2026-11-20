package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.EstadoAccesoRepository;
import java.util.List;

/** Consulta el catalogo de estados de acceso. */
public final class ConsultarEstadosAccesoUseCase {
    private final EstadoAccesoRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarEstadosAccesoUseCase(EstadoAccesoRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public List<EstadoAcceso> ejecutar(Usuario actor) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_PERSONA);
        return repositorio.listar();
    }
}
