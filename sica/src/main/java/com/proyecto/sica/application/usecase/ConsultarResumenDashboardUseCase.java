package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.ResumenDashboard;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.ReporteRepository;
import java.time.LocalDate;

/** Obtiene el dashboard respetando permisos y empresa del usuario activo. */
public final class ConsultarResumenDashboardUseCase {
    private final ReporteRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarResumenDashboardUseCase(ReporteRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public ResumenDashboard ejecutar(Usuario actor) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_VISITA);
        Long empresaId = actor.getRol().isRequiereEmpresa() ? actor.getEmpresaId() : null;
        boolean verIncidentes = actor.getRol().tienePermiso(PermisosSistema.CONSULTAR_INCIDENTE);
        return repositorio.consultarDashboard(LocalDate.now(), empresaId, verIncidentes);
    }
}
