package com.proyecto.sica.domain.entity;

import java.util.Objects;

/**
 * Estado administrativo o fisico de acceso asignado a una persona.
 */
public final class EstadoAcceso {

    private final int id;
    private final String codigo;
    private final String nombre;
    private final String descripcion;

    public EstadoAcceso(int id, String codigo, String nombre, String descripcion) {
        this.id = id;
        this.codigo = Objects.requireNonNull(codigo);
        this.nombre = Objects.requireNonNull(nombre);
        this.descripcion = descripcion;
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
