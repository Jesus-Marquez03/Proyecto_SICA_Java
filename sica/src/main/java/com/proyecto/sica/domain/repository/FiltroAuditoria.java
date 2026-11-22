package com.proyecto.sica.domain.repository;

import java.time.LocalDate;

/** Filtros autorizados de la bitacora; todos son opcionales. */
public record FiltroAuditoria(Long usuarioId, String accion, LocalDate desde,
        LocalDate hasta, String entidad, String entidadId, Long personaId, Long visitaId,
        int limite) {

    public FiltroAuditoria {
        limite = limite <= 0 ? 200 : Math.min(limite, 500);
    }

    public static FiltroAuditoria recientes() {
        return new FiltroAuditoria(null, null, null, null, null, null,
                null, null, 200);
    }
}
