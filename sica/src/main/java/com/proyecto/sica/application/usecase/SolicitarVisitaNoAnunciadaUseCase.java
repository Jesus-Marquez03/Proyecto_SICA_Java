package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.FabricaVisita;
import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
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

/** Crea la solicitud pendiente para un invitado que llego sin anuncio. */
public final class SolicitarVisitaNoAnunciadaUseCase {
    private final VisitaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorVisita validador;
    private final FabricaVisita fabrica;

    public SolicitarVisitaNoAnunciadaUseCase(VisitaRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorVisita validador,
            FabricaVisita fabrica) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
        this.fabrica = fabrica;
    }

    public Visita ejecutar(Usuario actor, SolicitudVisita solicitud) {
        autorizacion.verificarPermiso(actor, PermisosSistema.SOLICITAR_INGRESO);
        validador.textoObligatorio(solicitud.getMotivo(), "El motivo es obligatorio");
        validador.obtenerEmpresaActiva(solicitud.getEmpresaDestinoId());
        Persona persona = validador.obtenerPersonaHabilitada(
                solicitud.getPersonaId(), TipoPersona.VISITANTE);
        Usuario anfitrion = validador.obtenerAnfitrion(
                solicitud.getAnfitrionUsuarioId(), solicitud.getEmpresaDestinoId());
        EstadoVisita estado = validador.obtenerEstado("PENDIENTE_APROBACION");
        SolicitudVisita normalizada = new SolicitudVisita(solicitud.getPersonaId(),
                solicitud.getEmpresaDestinoId(), solicitud.getAnfitrionUsuarioId(),
                LocalDateTime.now(), solicitud.getMotivo(), solicitud.getPlacaVehiculo(),
                solicitud.getObservaciones());
        Visita visita = fabrica.crear(persona, anfitrion, estado, actor,
                TipoSolicitudVisita.NO_ANUNCIADA, normalizada, false, null, null);
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "SOLICITAR_VISITA_NO_ANUNCIADA", "visitas", null, "EXITOSO",
                "Solicitud " + visita.getNumeroSolicitud());
        return repositorio.crearConAuditoria(visita, evento);
    }
}
