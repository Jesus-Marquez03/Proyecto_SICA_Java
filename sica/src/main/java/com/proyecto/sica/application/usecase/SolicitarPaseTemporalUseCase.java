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
import java.time.LocalTime;

/** Solicita un pase de un solo ingreso para un trabajador sin carnet. */
public final class SolicitarPaseTemporalUseCase {
    private final VisitaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorVisita validador;
    private final FabricaVisita fabrica;

    public SolicitarPaseTemporalUseCase(VisitaRepository repositorio,
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
        Persona trabajador = validador.obtenerPersonaHabilitada(
                solicitud.getPersonaId(), TipoPersona.TRABAJADOR);
        if (trabajador.getEmpresa() == null
                || trabajador.getEmpresa().getId() != solicitud.getEmpresaDestinoId()) {
            throw new ValidacionException("El pase debe dirigirse a la empresa del trabajador");
        }
        validador.obtenerEmpresaActiva(solicitud.getEmpresaDestinoId());
        Usuario anfitrion = validador.obtenerAnfitrion(
                solicitud.getAnfitrionUsuarioId(), solicitud.getEmpresaDestinoId());
        EstadoVisita estado = validador.obtenerEstado("PENDIENTE_APROBACION_OLVIDO");
        LocalDateTime ahora = LocalDateTime.now();
        SolicitudVisita normalizada = new SolicitudVisita(solicitud.getPersonaId(),
                solicitud.getEmpresaDestinoId(), solicitud.getAnfitrionUsuarioId(), ahora,
                solicitud.getMotivo(), solicitud.getPlacaVehiculo(), solicitud.getObservaciones());
        Visita visita = fabrica.crear(trabajador, anfitrion, estado, actor,
                TipoSolicitudVisita.PASE_TEMPORAL, normalizada, false,
                fabrica.generarCodigoPase(), ahora.toLocalDate().atTime(LocalTime.MAX));
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "SOLICITAR_PASE_TEMPORAL", "visitas", null, "EXITOSO",
                "Pase temporal " + visita.getNumeroSolicitud());
        return repositorio.crearConAuditoria(visita, evento);
    }
}
