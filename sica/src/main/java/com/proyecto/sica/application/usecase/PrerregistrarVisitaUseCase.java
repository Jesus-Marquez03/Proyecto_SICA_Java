package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.FabricaVisita;
import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorVisita;
import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.TipoPersona;
import com.proyecto.sica.domain.entity.TipoSolicitudVisita;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.VisitaRepository;
import java.time.LocalDateTime;

/** Prerregistra un invitado y deja la visita aprobada, pero no dentro. */
public final class PrerregistrarVisitaUseCase {
    private final VisitaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorVisita validador;
    private final FabricaVisita fabrica;

    public PrerregistrarVisitaUseCase(VisitaRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorVisita validador,
            FabricaVisita fabrica) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
        this.fabrica = fabrica;
    }

    public Visita ejecutar(Usuario actor, SolicitudVisita solicitud) {
        autorizacion.verificarPermiso(actor, PermisosSistema.PRERREGISTRAR_VISITA);
        validador.verificarActorEmpresa(actor, solicitud.getEmpresaDestinoId());
        if (solicitud.getFechaProgramada() == null
                || solicitud.getFechaProgramada().isBefore(LocalDateTime.now())) {
            throw new ValidacionException("La fecha programada debe ser futura");
        }
        validador.textoObligatorio(solicitud.getMotivo(), "El motivo es obligatorio");
        validador.obtenerEmpresaActiva(solicitud.getEmpresaDestinoId());
        Persona persona = validador.obtenerPersonaHabilitada(
                solicitud.getPersonaId(), TipoPersona.VISITANTE);
        Usuario anfitrion = validador.obtenerAnfitrion(
                solicitud.getAnfitrionUsuarioId(), solicitud.getEmpresaDestinoId());
        EstadoVisita estado = validador.obtenerEstado("APROBADA");
        Visita visita = fabrica.crear(persona, anfitrion, estado, actor,
                TipoSolicitudVisita.PRERREGISTRADA, solicitud, true, null, null);
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "PRERREGISTRAR_VISITA", "visitas", null, "EXITOSO",
                "Prerregistro " + visita.getNumeroSolicitud());
        return repositorio.crearConAuditoria(visita, evento);
    }
}
