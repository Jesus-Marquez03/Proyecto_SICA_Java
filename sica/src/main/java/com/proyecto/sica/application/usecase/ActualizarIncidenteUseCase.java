package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorIncidente;
import com.proyecto.sica.domain.entity.EstadoIncidente;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.IncidenteRepository;
import java.time.LocalDateTime;

/** Actualiza seguimiento sin borrar el hecho original y deja el cambio auditado. */
public final class ActualizarIncidenteUseCase {
    private final IncidenteRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorIncidente validador;

    public ActualizarIncidenteUseCase(IncidenteRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorIncidente validador) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Incidente ejecutar(Usuario actor, long id, SolicitudSeguimientoIncidente solicitud) {
        autorizacion.verificarPermiso(actor, PermisosSistema.ACTUALIZAR_INCIDENTE);
        if (solicitud == null || solicitud.gravedad() == null || solicitud.estado() == null) {
            throw new ValidacionException("Gravedad y estado son obligatorios");
        }
        Incidente anterior = repositorio.buscarPorId(id)
                .orElseThrow(() -> new ValidacionException("El incidente no existe"));
        validador.verificarAmbito(actor, anterior);
        validador.verificarTransicion(anterior.getEstado(), solicitud.estado());
        String descripcion = validador.textoObligatorio(
                solicitud.descripcion(), "La descripcion es obligatoria", 1000);
        String justificacion = validador.textoObligatorio(
                solicitud.justificacion(), "La justificacion es obligatoria", 300);
        LocalDateTime cierre = solicitud.estado() == EstadoIncidente.CERRADO
                ? (anterior.getFechaCierre() == null ? LocalDateTime.now() : anterior.getFechaCierre())
                : null;
        Incidente actualizado = new Incidente(anterior.getId(), anterior.getVisita(),
                anterior.getPersona(), anterior.getReportadoPor(), anterior.getTipo(),
                solicitud.gravedad(), solicitud.estado(), descripcion,
                validador.textoOpcional(solicitud.accionesTomadas(), 1000),
                anterior.getFechaIncidente(), anterior.getFechaReporte(), cierre);
        String detalle = "Justificacion: " + justificacion + "; estado: "
                + anterior.getEstado() + " -> " + actualizado.getEstado()
                + "; gravedad: " + anterior.getGravedad() + " -> "
                + actualizado.getGravedad() + "; descripcion anterior: "
                + resumir(anterior.getDescripcion()) + "; nueva: "
                + resumir(actualizado.getDescripcion());
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "ACTUALIZACION_INCIDENTE", "incidentes", String.valueOf(id),
                "EXITOSO", detalle);
        return repositorio.actualizarConAuditoria(anterior, actualizado, evento);
    }

    private String resumir(String texto) {
        return texto.length() <= 160 ? texto : texto.substring(0, 157) + "...";
    }
}
