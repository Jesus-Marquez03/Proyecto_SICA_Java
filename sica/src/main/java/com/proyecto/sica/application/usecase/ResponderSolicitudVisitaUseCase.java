package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorVisita;
import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.VisitaRepository;

/** Aprueba o rechaza solamente solicitudes asignadas al funcionario. */
public final class ResponderSolicitudVisitaUseCase {
    private final VisitaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorVisita validador;

    public ResponderSolicitudVisitaUseCase(VisitaRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorVisita validador) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Visita aprobar(Usuario actor, long visitaId) {
        autorizacion.verificarPermiso(actor, PermisosSistema.APROBAR_SOLICITUD);
        Visita visita = obtenerAsignada(actor, visitaId);
        validador.verificarAcceso(visita.getVisitante());
        EstadoVisita aprobado = validador.obtenerEstado("APROBADA");
        EventoAuditoria evento = evento(actor, visita, "APROBAR_SOLICITUD",
                "Aprobacion de " + visita.getNumeroSolicitud());
        return repositorio.responderConAuditoria(visitaId, actor.getId(),
                visita.getEstado().getId(), aprobado.getId(), evento);
    }

    public Visita rechazar(Usuario actor, long visitaId) {
        autorizacion.verificarPermiso(actor, PermisosSistema.RECHAZAR_SOLICITUD);
        Visita visita = obtenerAsignada(actor, visitaId);
        EstadoVisita rechazado = validador.obtenerEstado("RECHAZADA");
        EventoAuditoria evento = evento(actor, visita, "RECHAZAR_SOLICITUD",
                "Rechazo de " + visita.getNumeroSolicitud());
        return repositorio.responderConAuditoria(visitaId, actor.getId(),
                visita.getEstado().getId(), rechazado.getId(), evento);
    }

    private Visita obtenerAsignada(Usuario actor, long visitaId) {
        Visita visita = repositorio.buscarPorId(visitaId)
                .orElseThrow(() -> new ValidacionException("La visita no existe"));
        if (visita.getAnfitrion().getId().longValue() != actor.getId().longValue()) {
            throw new ValidacionException("La solicitud esta asignada a otro funcionario");
        }
        if (actor.getEmpresaId() == null || visita.getAnfitrion().getEmpresaId() == null
                || actor.getEmpresaId().longValue()
                        != visita.getAnfitrion().getEmpresaId().longValue()) {
            throw new ValidacionException("La solicitud pertenece a otra empresa");
        }
        String estado = visita.getEstado().getCodigo();
        if (!"PENDIENTE_APROBACION".equals(estado)
                && !"PENDIENTE_APROBACION_OLVIDO".equals(estado)) {
            throw new ValidacionException("La solicitud ya fue respondida");
        }
        return visita;
    }

    private EventoAuditoria evento(Usuario actor, Visita visita,
            String accion, String descripcion) {
        return new EventoAuditoria(actor.getId(), null, accion, "visitas",
                String.valueOf(visita.getId()), "EXITOSO", descripcion);
    }
}
