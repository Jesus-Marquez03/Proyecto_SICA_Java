package com.proyecto.sica.application.usecase;

import com.proyecto.sica.domain.entity.EstadoIncidente;
import com.proyecto.sica.domain.entity.GravedadIncidente;

/** Correccion o seguimiento controlado de un incidente existente. */
public record SolicitudSeguimientoIncidente(GravedadIncidente gravedad,
        EstadoIncidente estado, String descripcion, String accionesTomadas,
        String justificacion) {
}
