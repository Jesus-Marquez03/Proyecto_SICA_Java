package com.proyecto.sica.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/** Acontecimiento de seguridad asociado con una visita y su persona. */
public final class Incidente {
    private final Long id;
    private final Visita visita;
    private final Persona persona;
    private final Usuario reportadoPor;
    private final String tipo;
    private final GravedadIncidente gravedad;
    private final EstadoIncidente estado;
    private final String descripcion;
    private final String accionesTomadas;
    private final LocalDateTime fechaIncidente;
    private final LocalDateTime fechaReporte;
    private final LocalDateTime fechaCierre;

    public Incidente(Long id, Visita visita, Persona persona, Usuario reportadoPor,
            String tipo, GravedadIncidente gravedad, EstadoIncidente estado,
            String descripcion, String accionesTomadas, LocalDateTime fechaIncidente,
            LocalDateTime fechaReporte, LocalDateTime fechaCierre) {
        this.id = id;
        this.visita = Objects.requireNonNull(visita);
        this.persona = Objects.requireNonNull(persona);
        this.reportadoPor = Objects.requireNonNull(reportadoPor);
        this.tipo = Objects.requireNonNull(tipo);
        this.gravedad = Objects.requireNonNull(gravedad);
        this.estado = Objects.requireNonNull(estado);
        this.descripcion = Objects.requireNonNull(descripcion);
        this.accionesTomadas = accionesTomadas;
        this.fechaIncidente = Objects.requireNonNull(fechaIncidente);
        this.fechaReporte = fechaReporte;
        this.fechaCierre = fechaCierre;
    }

    public Long getId() { return id; }
    public Visita getVisita() { return visita; }
    public Persona getPersona() { return persona; }
    public Usuario getReportadoPor() { return reportadoPor; }
    public String getTipo() { return tipo; }
    public GravedadIncidente getGravedad() { return gravedad; }
    public EstadoIncidente getEstado() { return estado; }
    public String getDescripcion() { return descripcion; }
    public String getAccionesTomadas() { return accionesTomadas; }
    public LocalDateTime getFechaIncidente() { return fechaIncidente; }
    public LocalDateTime getFechaReporte() { return fechaReporte; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
}
