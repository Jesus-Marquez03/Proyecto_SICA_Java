package com.proyecto.sica.domain.entity;

import java.time.LocalDateTime;

/** Evento operativo reciente mostrado en el dashboard. */
public record ActividadReciente(String titulo, String detalle, String estado,
        LocalDateTime fecha) { }
