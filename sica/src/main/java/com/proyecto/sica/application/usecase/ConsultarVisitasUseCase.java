package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.VisitaRepository;
import java.util.List;

/** Consultas operativas usadas por el guarda y el funcionario. */
public final class ConsultarVisitasUseCase {
    private final VisitaRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarVisitasUseCase(VisitaRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public List<Visita> buscarPorDocumento(Usuario actor,
            String tipoDocumento, String numeroDocumento) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_VISITA);
        if (tipoDocumento == null || tipoDocumento.isBlank()
                || numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new ValidacionException("Indique tipo y numero de documento");
        }
        return repositorio.buscarPorDocumento(tipoDocumento.trim().toUpperCase(),
                numeroDocumento.trim());
    }

    public List<Visita> pendientesAsignadas(Usuario actor) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_VISITA);
        return repositorio.listarPendientesPorAnfitrion(actor.getId());
    }

    public Visita buscarPorId(Usuario actor, long id) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_VISITA);
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ValidacionException("La visita no existe"));
    }
}
