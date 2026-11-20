package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorPersona;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.PersonaRepository;

/** Actualiza datos basicos y conserva el estado de acceso actual. */
public final class ActualizarPersonaUseCase {
    private final PersonaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorPersona validador;

    public ActualizarPersonaUseCase(PersonaRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorPersona validador) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Persona ejecutar(Usuario actor, long id, SolicitudPersona solicitud) {
        autorizacion.verificarPermiso(actor, PermisosSistema.ACTUALIZAR_PERSONA);
        Persona actual = repositorio.buscarPorId(id)
                .orElseThrow(() -> new ValidacionException("La persona no existe"));
        if (solicitud.getTipoPersona() == null) {
            throw new ValidacionException("El tipo de persona es obligatorio");
        }
        validador.validarTextos(solicitud.getTipoDocumento(),
                solicitud.getNumeroDocumento(), solicitud.getNombres(),
                solicitud.getApellidos(), solicitud.getCorreo(), solicitud.getFotoUrl());
        validador.validarDocumentoUnico(solicitud.getTipoDocumento(),
                solicitud.getNumeroDocumento(), id);
        Empresa empresa = validador.obtenerEmpresaValida(
                solicitud.getEmpresaId(), solicitud.getTipoPersona());
        Persona persona = new Persona(id, empresa, actual.getEstadoAcceso(),
                validador.normalizarTipoDocumento(solicitud.getTipoDocumento()),
                solicitud.getNumeroDocumento().trim(), solicitud.getNombres().trim(),
                solicitud.getApellidos().trim(), opcional(solicitud.getTelefono()),
                validador.normalizarCorreo(solicitud.getCorreo()),
                solicitud.getTipoPersona(), actual.isActiva(), opcional(solicitud.getFotoUrl()));
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "ACTUALIZAR_PERSONA", "personas", String.valueOf(id), "EXITOSO",
                "Actualizacion de persona con documento " + persona.getNumeroDocumento());
        repositorio.actualizarConAuditoria(persona, evento);
        return persona;
    }

    private String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
