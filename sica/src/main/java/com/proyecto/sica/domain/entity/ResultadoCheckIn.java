package com.proyecto.sica.domain.entity;

/** Resultado del ingreso, incluyendo una salida olvidada regularizada. */
public final class ResultadoCheckIn {
    private final Visita visitaIngresada;
    private final Long visitaRegularizadaId;

    public ResultadoCheckIn(Visita visitaIngresada, Long visitaRegularizadaId) {
        this.visitaIngresada = visitaIngresada;
        this.visitaRegularizadaId = visitaRegularizadaId;
    }

    public Visita getVisitaIngresada() { return visitaIngresada; }
    public Long getVisitaRegularizadaId() { return visitaRegularizadaId; }
    public boolean huboRegularizacion() { return visitaRegularizadaId != null; }
}
