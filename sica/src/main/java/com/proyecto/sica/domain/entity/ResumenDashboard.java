package com.proyecto.sica.domain.entity;

import java.util.List;

/** Datos reales y ya agregados para el dashboard Bento. */
public record ResumenDashboard(long accesosHoy, long personasDentro,
        long visitasPendientes, long incidentesAbiertos, double progresoFinalizadas,
        long totalElegibles, List<DatoSerieFecha> accesosUltimosSieteDias,
        List<ActividadReciente> actividadReciente) {
    public ResumenDashboard {
        accesosUltimosSieteDias = List.copyOf(accesosUltimosSieteDias);
        actividadReciente = List.copyOf(actividadReciente);
    }

    public boolean tieneProgreso() { return totalElegibles > 0; }
}
