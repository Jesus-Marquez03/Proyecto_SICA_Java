package com.proyecto.sica.application.usecase;

import com.proyecto.sica.domain.entity.GravedadIncidente;
import java.time.LocalDateTime;

/** Datos capturados al registrar un incidente. */
public record SolicitudIncidente(long visitaId, String tipo, GravedadIncidente gravedad,
        String descripcion, String accionesTomadas, LocalDateTime fechaIncidente) {
}
