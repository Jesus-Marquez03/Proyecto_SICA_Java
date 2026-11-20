package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.EstadoAccesoRepository;
import com.proyecto.sica.domain.repository.PersonaRepository;

/** Cambia una restriccion de acceso sin borrar la persona ni sus visitas. */
public final class CambiarEstadoAccesoUseCase {
    private final PersonaRepository personaRepository;
    private final EstadoAccesoRepository estadoRepository;
    private final ServicioAutorizacion autorizacion;

    public CambiarEstadoAccesoUseCase(PersonaRepository personaRepository,
            EstadoAccesoRepository estadoRepository, ServicioAutorizacion autorizacion) {
        this.personaRepository = personaRepository;
        this.estadoRepository = estadoRepository;
        this.autorizacion = autorizacion;
    }

    /** Retorna false cuando la persona ya tenia el estado solicitado. */
    public boolean ejecutar(Usuario actor, long personaId, int estadoId) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CAMBIAR_ESTADO_ACCESO);
        Persona persona = personaRepository.buscarPorId(personaId)
                .orElseThrow(() -> new ValidacionException("La persona no existe"));
        EstadoAcceso nuevoEstado = estadoRepository.buscarPorId(estadoId)
                .orElseThrow(() -> new ValidacionException("El estado de acceso no existe"));
        if (persona.getEstadoAcceso().getId() == nuevoEstado.getId()) {
            return false;
        }
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "CAMBIAR_ESTADO_ACCESO", "personas", String.valueOf(personaId),
                "EXITOSO", "Cambio de " + persona.getEstadoAcceso().getCodigo()
                        + " a " + nuevoEstado.getCodigo());
        personaRepository.cambiarEstadoConAuditoria(personaId, nuevoEstado.getId(), evento);
        return true;
    }
}
