package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.ResultadoCheckIn;
import com.proyecto.sica.domain.entity.Visita;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Puerto de persistencia para solicitudes, ingresos y salidas. */
public interface VisitaRepository {
    Optional<Visita> buscarPorId(long id);
    Optional<Visita> buscarPorNumero(String numeroSolicitud);
    List<Visita> buscarPorDocumento(String tipoDocumento, String numeroDocumento);
    List<Visita> listarPendientesPorAnfitrion(long anfitrionId);
    List<Visita> listarHistorial(String numeroDocumento, LocalDate desde,
            LocalDate hasta, String estadoCodigo);
    Visita crearConAuditoria(Visita visita, EventoAuditoria evento);
    Visita responderConAuditoria(long visitaId, long usuarioId, int estadoPendienteId,
            int nuevoEstadoId, EventoAuditoria evento);
    ResultadoCheckIn registrarIngresoConAuditoria(long visitaId, long usuarioId,
            int estadoAprobadoId, int estadoDentroId, int estadoCerradaSistemaId,
            String motivoRegularizacion, EventoAuditoria ingreso,
            EventoAuditoria regularizacion);
    Visita registrarSalidaConAuditoria(long visitaId, long usuarioId,
            int estadoDentroId, int estadoFueraId, EventoAuditoria evento);
    Visita regularizarSalidaConAuditoria(long visitaId, long usuarioId,
            int estadoDentroId, int estadoCerradaSistemaId, String motivo,
            EventoAuditoria evento);
}
