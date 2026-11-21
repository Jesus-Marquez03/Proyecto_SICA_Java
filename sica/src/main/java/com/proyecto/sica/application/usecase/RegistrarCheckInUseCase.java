package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorVisita;
import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.ResultadoCheckIn;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.VisitaRepository;

/** Confirma el ingreso y regulariza una salida anterior dentro de la misma transaccion. */
public final class RegistrarCheckInUseCase {
    private static final String MOTIVO_AUTOMATICO =
            "Salida anterior olvidada detectada durante un nuevo check-in";

    private final VisitaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorVisita validador;

    public RegistrarCheckInUseCase(VisitaRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorVisita validador) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public ResultadoCheckIn ejecutar(Usuario actor, long visitaId) {
        autorizacion.verificarPermiso(actor, PermisosSistema.REGISTRAR_CHECK_IN);
        Visita visita = repositorio.buscarPorId(visitaId)
                .orElseThrow(() -> new ValidacionException("La visita no existe"));
        if (!"APROBADA".equals(visita.getEstado().getCodigo())) {
            throw new ValidacionException("Solo una visita aprobada puede registrar ingreso");
        }
        validador.verificarAcceso(visita.getVisitante());
        EstadoVisita dentro = validador.obtenerEstado("DENTRO");
        EstadoVisita cerrada = validador.obtenerEstado("CERRADA_POR_SISTEMA");
        EventoAuditoria ingreso = new EventoAuditoria(actor.getId(), null,
                "REGISTRAR_CHECK_IN", "visitas", String.valueOf(visitaId), "EXITOSO",
                "Ingreso de " + visita.getNumeroSolicitud());
        EventoAuditoria regularizacion = new EventoAuditoria(actor.getId(), null,
                "REGULARIZAR_SALIDA_OLVIDADA", "visitas", null, "EXITOSO",
                MOTIVO_AUTOMATICO);
        return repositorio.registrarIngresoConAuditoria(visitaId, actor.getId(),
                visita.getEstado().getId(), dentro.getId(), cerrada.getId(),
                MOTIVO_AUTOMATICO, ingreso, regularizacion);
    }
}
