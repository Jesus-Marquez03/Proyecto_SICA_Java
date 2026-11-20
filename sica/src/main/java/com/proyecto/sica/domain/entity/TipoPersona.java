package com.proyecto.sica.domain.entity;

/**
 * Tipos admitidos por la restriccion CHECK de la tabla personas.
 */
public enum TipoPersona {
    TRABAJADOR("Trabajador"),
    VISITANTE("Invitado");

    private final String nombreVisible;

    TipoPersona(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    @Override
    public String toString() {
        return nombreVisible;
    }
}
