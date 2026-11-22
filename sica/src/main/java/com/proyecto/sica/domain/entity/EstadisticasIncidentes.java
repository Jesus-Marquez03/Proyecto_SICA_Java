package com.proyecto.sica.domain.entity;

import java.util.List;

/** Conteos verificables de incidentes para un periodo. */
public record EstadisticasIncidentes(long total, long abiertos, long enRevision,
        long cerrados, List<ConteoCategoria> porGravedad,
        List<DatoSerieFecha> porFecha) {
    public EstadisticasIncidentes {
        porGravedad = List.copyOf(porGravedad);
        porFecha = List.copyOf(porFecha);
    }
}
