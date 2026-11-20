package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorPersona;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.PersonaRepository;

/** Registra un trabajador o invitado con auditoria atomica. */
public final class CrearPersonaUseCase {
    private final PersonaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorPersona validador;

    public CrearPersonaUseCase(PersonaRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorPersona validador) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Persona ejecutar(Usuario actor, SolicitudPersona solicitud) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CREAR_PERSONA);
        if (solicitud.getTipoPersona() == null) {
            throw new ValidacionException("El tipo de persona es obligatorio");
        }
        validador.validarTextos(solicitud.getTipoDocumento(),
                solicitud.getNumeroDocumento(), solicitud.getNombres(),
                solicitud.getApellidos(), solicitud.getCorreo(), solicitud.getFotoUrl());
        validador.validarDocumentoUnico(solicitud.getTipoDocumento(),
                solicitud.getNumeroDocumento(), null);
        Empresa empresa = validador.obtenerEmpresaValida(
                solicitud.getEmpresaId(), solicitud.getTipoPersona());
        EstadoAcceso estado = validador.obtenerEstadoInicial();
        Persona persona = construir(null, empresa, estado, solicitud, true);
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "CREAR_PERSONA", "personas", null, "EXITOSO",
                "Creacion de persona con documento " + persona.getNumeroDocumento());
        return repositorio.crearConAuditoria(persona, evento);
    }

    private Persona construir(Long id, Empresa empresa, EstadoAcceso estado,
            SolicitudPersona solicitud, boolean activa) {
        return new Persona(id, empresa, estado,
                validador.normalizarTipoDocumento(solicitud.getTipoDocumento()),
                solicitud.getNumeroDocumento().trim(), solicitud.getNombres().trim(),
                solicitud.getApellidos().trim(), opcional(solicitud.getTelefono()),
                validador.normalizarCorreo(solicitud.getCorreo()),
                solicitud.getTipoPersona(), activa, opcional(solicitud.getFotoUrl()));
    }

    private String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
