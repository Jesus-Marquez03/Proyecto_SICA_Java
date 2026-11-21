package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorIncidente;
import com.proyecto.sica.domain.entity.EstadoIncidente;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.IncidenteRepository;
import com.proyecto.sica.domain.repository.VisitaRepository;

/** Registra un acontecimiento de seguridad y su auditoria en una transaccion. */
public final class CrearIncidenteUseCase {
    private final IncidenteRepository incidentes;
    private final VisitaRepository visitas;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorIncidente validador;

    public CrearIncidenteUseCase(IncidenteRepository incidentes, VisitaRepository visitas,
            ServicioAutorizacion autorizacion, ValidadorIncidente validador) {
        this.incidentes = incidentes;
        this.visitas = visitas;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Incidente ejecutar(Usuario actor, SolicitudIncidente solicitud) {
        autorizacion.verificarPermiso(actor, PermisosSistema.REGISTRAR_INCIDENTE);
        if (solicitud == null) throw new ValidacionException("Los datos son obligatorios");
        Visita visita = visitas.buscarPorId(solicitud.visitaId())
                .orElseThrow(() -> new ValidacionException("La visita no existe"));
        String tipo = validador.textoObligatorio(
                solicitud.tipo(), "El tipo es obligatorio", 60);
        String descripcion = validador.textoObligatorio(
                solicitud.descripcion(), "La descripcion es obligatoria", 1000);
        if (solicitud.gravedad() == null) {
            throw new ValidacionException("La gravedad es obligatoria");
        }
        Incidente incidente = new Incidente(null, visita, visita.getVisitante(), actor,
                tipo, solicitud.gravedad(), EstadoIncidente.ABIERTO, descripcion,
                validador.textoOpcional(solicitud.accionesTomadas(), 1000),
                validador.fechaValida(solicitud.fechaIncidente()), null, null);
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "CREACION_INCIDENTE", "incidentes", null, "EXITOSO",
                "Incidente " + tipo + " asociado a visita " + visita.getNumeroSolicitud());
        return incidentes.crearConAuditoria(incidente, evento);
    }
}
