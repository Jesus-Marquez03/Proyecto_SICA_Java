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

/** Registra una unica salida para una visita que esta dentro. */
public final class RegistrarCheckOutUseCase {
    private final VisitaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorVisita validador;

    public RegistrarCheckOutUseCase(VisitaRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorVisita validador) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Visita ejecutar(Usuario actor, long visitaId) {
        autorizacion.verificarPermiso(actor, PermisosSistema.REGISTRAR_CHECK_OUT);
        Visita visita = repositorio.buscarPorId(visitaId)
                .orElseThrow(() -> new ValidacionException("La visita no existe"));
        if (!"DENTRO".equals(visita.getEstado().getCodigo())) {
            throw new ValidacionException("La visita no esta dentro o ya registro su salida");
        }
        EstadoVisita fuera = validador.obtenerEstado("FUERA");
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "REGISTRAR_CHECK_OUT", "visitas", String.valueOf(visitaId), "EXITOSO",
                "Salida de " + visita.getNumeroSolicitud());
        return repositorio.registrarSalidaConAuditoria(visitaId, actor.getId(),
                visita.getEstado().getId(), fuera.getId(), evento);
    }
}
