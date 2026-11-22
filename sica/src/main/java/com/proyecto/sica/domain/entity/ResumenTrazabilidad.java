package com.proyecto.sica.domain.entity;

/** Indicadores operativos derivados de visitas, sin duplicar eventos. */
public record ResumenTrazabilidad(long accesosRechazados, long regularizaciones,
        long pasesTemporales, long solicitudesPendientes) { }
