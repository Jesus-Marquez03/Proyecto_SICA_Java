package com.proyecto.sica.domain.entity;

/** Tipos admitidos por la restriccion de la tabla visitas. */
public enum TipoSolicitudVisita {
    PRERREGISTRADA("Prerregistrada"),
    NO_ANUNCIADA("No anunciada"),
    PASE_TEMPORAL("Pase temporal");

    private final String nombreVisible;

    TipoSolicitudVisita(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() { return nombreVisible; }

    @Override
    public String toString() { return nombreVisible; }
}
