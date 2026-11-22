package com.proyecto.sica.domain.entity;

import java.time.LocalDateTime;

/** Fila del reporte de presencia fisica actual. */
public record PersonaDentroReporte(long visitaId, String solicitud, String persona,
        String documento, String empresa, String tipoPersona, LocalDateTime fechaIngreso) { }
