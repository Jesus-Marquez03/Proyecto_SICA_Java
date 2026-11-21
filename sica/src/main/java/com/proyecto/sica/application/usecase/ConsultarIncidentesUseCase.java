package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.FiltroIncidentes;
import com.proyecto.sica.domain.repository.IncidenteRepository;
import java.util.List;

/** Consulta incidentes respetando el ambito empresarial del actor. */
public final class ConsultarIncidentesUseCase {
    private final IncidenteRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarIncidentesUseCase(IncidenteRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public List<Incidente> ejecutar(Usuario actor, FiltroIncidentes filtro) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_INCIDENTE);
        Long empresaId = actor.getRol().isRequiereEmpresa() ? actor.getEmpresaId() : null;
        return repositorio.consultar(filtro == null ? FiltroIncidentes.todos() : filtro,
                empresaId);
    }
}
