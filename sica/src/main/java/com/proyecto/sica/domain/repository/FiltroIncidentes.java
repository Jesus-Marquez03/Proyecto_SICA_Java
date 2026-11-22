package com.proyecto.sica.domain.repository;

import java.time.LocalDate;

/** Criterios opcionales para buscar incidentes sin construir SQL en la interfaz. */
public record FiltroIncidentes(Long visitaId, Long personaId, Long reportadoPorId,
        LocalDate desde, LocalDate hasta, String estado) {

    public static FiltroIncidentes todos() {
        return new FiltroIncidentes(null, null, null, null, null, null);
    }
}
