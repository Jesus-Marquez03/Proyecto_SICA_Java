package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Incidente;
import java.util.List;
import java.util.Optional;

/** Puerto para crear, consultar y dar seguimiento a incidentes. */
public interface IncidenteRepository {
    Optional<Incidente> buscarPorId(long id);
    List<Incidente> consultar(FiltroIncidentes filtro, Long empresaId);
    Incidente crearConAuditoria(Incidente incidente, EventoAuditoria evento);
    Incidente actualizarConAuditoria(Incidente anterior, Incidente actualizado,
            EventoAuditoria evento);
    boolean existeTipoEnVisita(String tipo, long visitaId);
}
