package com.proyecto.sica.domain.entity;

import java.util.List;

/** Ingresos y salidas fisicas de un periodo. */
public record ReporteAccesos(long ingresos, long salidas,
        List<DatoSerieFecha> ingresosPorDia) {
    public ReporteAccesos {
        ingresosPorDia = List.copyOf(ingresosPorDia);
    }
}
