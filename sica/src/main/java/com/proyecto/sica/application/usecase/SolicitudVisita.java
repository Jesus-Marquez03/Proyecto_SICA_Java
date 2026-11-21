package com.proyecto.sica.application.usecase;

import java.time.LocalDateTime;

/** Datos comunes para prerregistros, solicitudes y pases temporales. */
public final class SolicitudVisita {
    private final long personaId;
    private final long empresaDestinoId;
    private final long anfitrionUsuarioId;
    private final LocalDateTime fechaProgramada;
    private final String motivo;
    private final String placaVehiculo;
    private final String observaciones;

    public SolicitudVisita(long personaId, long empresaDestinoId, long anfitrionUsuarioId,
            LocalDateTime fechaProgramada, String motivo, String placaVehiculo,
            String observaciones) {
        this.personaId = personaId;
        this.empresaDestinoId = empresaDestinoId;
        this.anfitrionUsuarioId = anfitrionUsuarioId;
        this.fechaProgramada = fechaProgramada;
        this.motivo = motivo;
        this.placaVehiculo = placaVehiculo;
        this.observaciones = observaciones;
    }

    public long getPersonaId() { return personaId; }
    public long getEmpresaDestinoId() { return empresaDestinoId; }
    public long getAnfitrionUsuarioId() { return anfitrionUsuarioId; }
    public LocalDateTime getFechaProgramada() { return fechaProgramada; }
    public String getMotivo() { return motivo; }
    public String getPlacaVehiculo() { return placaVehiculo; }
    public String getObservaciones() { return observaciones; }
}
