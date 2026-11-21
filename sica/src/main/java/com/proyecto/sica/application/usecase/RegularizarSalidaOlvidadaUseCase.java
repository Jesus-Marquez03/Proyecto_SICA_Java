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

/** Cierra manualmente una visita abierta con responsable y justificacion. */
public final class RegularizarSalidaOlvidadaUseCase {
    private final VisitaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorVisita validador;

    public RegularizarSalidaOlvidadaUseCase(VisitaRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorVisita validador) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Visita ejecutar(Usuario actor, long visitaId, String motivo) {
        autorizacion.verificarPermiso(actor,
                PermisosSistema.REGULARIZAR_SALIDA_OLVIDADA);
        String motivoValido = validador.textoObligatorio(
                motivo, "El motivo de regularizacion es obligatorio");
        Visita visita = repositorio.buscarPorId(visitaId)
                .orElseThrow(() -> new ValidacionException("La visita no existe"));
        if (!"DENTRO".equals(visita.getEstado().getCodigo())) {
            throw new ValidacionException("Solo se puede regularizar una visita abierta");
        }
        EstadoVisita cerrada = validador.obtenerEstado("CERRADA_POR_SISTEMA");
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "REGULARIZAR_SALIDA_OLVIDADA", "visitas", String.valueOf(visitaId),
                "EXITOSO", motivoValido);
        return repositorio.regularizarSalidaConAuditoria(visitaId, actor.getId(),
                visita.getEstado().getId(), cerrada.getId(), motivoValido, evento);
    }
}
