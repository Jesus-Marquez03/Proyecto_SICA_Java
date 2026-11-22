package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.EstadisticasIncidentes;
import com.proyecto.sica.domain.entity.PersonaDentroReporte;
import com.proyecto.sica.domain.entity.ReporteAccesos;
import com.proyecto.sica.domain.entity.ResumenDashboard;
import com.proyecto.sica.domain.entity.ResumenEstadosVisita;
import com.proyecto.sica.domain.entity.ResumenTrazabilidad;
import java.time.LocalDate;
import java.util.List;

/** Puerto de lectura para reportes agregados por MySQL. */
public interface ReporteRepository {
    ReporteAccesos consultarAccesos(LocalDate desde, LocalDate hasta, Long empresaId);
    List<PersonaDentroReporte> consultarPersonasDentro(Long empresaId);
    ResumenEstadosVisita consultarEstadosVisita(LocalDate desde, LocalDate hasta, Long empresaId);
    EstadisticasIncidentes consultarIncidentes(LocalDate desde, LocalDate hasta, Long empresaId);
    ResumenTrazabilidad consultarTrazabilidad(LocalDate desde, LocalDate hasta, Long empresaId);
    ResumenDashboard consultarDashboard(LocalDate hoy, Long empresaId, boolean incluirIncidentes);
}
