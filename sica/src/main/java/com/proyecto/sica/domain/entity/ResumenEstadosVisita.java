package com.proyecto.sica.domain.entity;

import java.util.List;

/** Cantidades de solicitudes agrupadas por estado de visita. */
public record ResumenEstadosVisita(List<ConteoCategoria> estados) {
    public ResumenEstadosVisita { estados = List.copyOf(estados); }

    public long cantidad(String codigo) {
        return estados.stream().filter(e -> e.categoria().equals(codigo))
                .mapToLong(ConteoCategoria::cantidad).findFirst().orElse(0);
    }
}
