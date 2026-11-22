package com.proyecto.sica.application.usecase;

import com.proyecto.sica.domain.entity.EstadisticasIncidentes;
import com.proyecto.sica.domain.entity.PersonaDentroReporte;
import com.proyecto.sica.domain.entity.ReporteAccesos;
import com.proyecto.sica.domain.entity.ResumenEstadosVisita;
import com.proyecto.sica.domain.entity.ResumenTrazabilidad;
import java.util.List;

/** Resultado completo de la pantalla de reportes. */
public record ResultadoReportes(ReporteAccesos accesos,
        List<PersonaDentroReporte> personasDentro, ResumenEstadosVisita visitas,
        EstadisticasIncidentes incidentes, ResumenTrazabilidad trazabilidad) {
    public ResultadoReportes { personasDentro = List.copyOf(personasDentro); }
}
