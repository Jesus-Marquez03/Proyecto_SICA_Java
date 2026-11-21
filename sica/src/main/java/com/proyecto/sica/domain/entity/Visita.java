package com.proyecto.sica.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Solicitud y registro fisico de una visita o pase temporal. */
public final class Visita {
    private final Long id;
    private final String numeroSolicitud;
    private final Persona visitante;
    private final Usuario anfitrion;
    private final EstadoVisita estado;
    private final long registradoPorUsuarioId;
    private final Long respondidoPorUsuarioId;
    private final Long regularizadaPorUsuarioId;
    private final TipoSolicitudVisita tipoSolicitud;
    private final String motivo;
    private final String placaVehiculo;
    private final LocalDateTime fechaSolicitud;
    private final LocalDateTime fechaProgramada;
    private final LocalDateTime fechaRespuesta;
    private final LocalDateTime fechaIngreso;
    private final LocalDateTime fechaSalida;
    private final LocalDateTime fechaCierreSistema;
    private final String motivoRegularizacion;
    private final String codigoPase;
    private final LocalDateTime fechaVencimientoPase;
    private final String observaciones;

    public Visita(Long id, String numeroSolicitud, Persona visitante, Usuario anfitrion,
            EstadoVisita estado, long registradoPorUsuarioId, Long respondidoPorUsuarioId,
            Long regularizadaPorUsuarioId, TipoSolicitudVisita tipoSolicitud, String motivo,
            String placaVehiculo, LocalDateTime fechaSolicitud, LocalDateTime fechaProgramada,
            LocalDateTime fechaRespuesta, LocalDateTime fechaIngreso, LocalDateTime fechaSalida,
            LocalDateTime fechaCierreSistema, String motivoRegularizacion, String codigoPase,
            LocalDateTime fechaVencimientoPase, String observaciones) {
        this.id = id;
        this.numeroSolicitud = Objects.requireNonNull(numeroSolicitud);
        this.visitante = Objects.requireNonNull(visitante);
        this.anfitrion = Objects.requireNonNull(anfitrion);
        this.estado = Objects.requireNonNull(estado);
        this.registradoPorUsuarioId = registradoPorUsuarioId;
        this.respondidoPorUsuarioId = respondidoPorUsuarioId;
        this.regularizadaPorUsuarioId = regularizadaPorUsuarioId;
        this.tipoSolicitud = Objects.requireNonNull(tipoSolicitud);
        this.motivo = Objects.requireNonNull(motivo);
        this.placaVehiculo = placaVehiculo;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaProgramada = fechaProgramada;
        this.fechaRespuesta = fechaRespuesta;
        this.fechaIngreso = fechaIngreso;
        this.fechaSalida = fechaSalida;
        this.fechaCierreSistema = fechaCierreSistema;
        this.motivoRegularizacion = motivoRegularizacion;
        this.codigoPase = codigoPase;
        this.fechaVencimientoPase = fechaVencimientoPase;
        this.observaciones = observaciones;
    }

    public Long getId() { return id; }
    public String getNumeroSolicitud() { return numeroSolicitud; }
    public Persona getVisitante() { return visitante; }
    public Usuario getAnfitrion() { return anfitrion; }
    public EstadoVisita getEstado() { return estado; }
    public long getRegistradoPorUsuarioId() { return registradoPorUsuarioId; }
    public Long getRespondidoPorUsuarioId() { return respondidoPorUsuarioId; }
    public Long getRegularizadaPorUsuarioId() { return regularizadaPorUsuarioId; }
    public TipoSolicitudVisita getTipoSolicitud() { return tipoSolicitud; }
    public String getMotivo() { return motivo; }
    public String getPlacaVehiculo() { return placaVehiculo; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public LocalDateTime getFechaProgramada() { return fechaProgramada; }
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public LocalDateTime getFechaCierreSistema() { return fechaCierreSistema; }
    public String getMotivoRegularizacion() { return motivoRegularizacion; }
    public String getCodigoPase() { return codigoPase; }
    public LocalDateTime getFechaVencimientoPase() { return fechaVencimientoPase; }
    public String getObservaciones() { return observaciones; }
}
