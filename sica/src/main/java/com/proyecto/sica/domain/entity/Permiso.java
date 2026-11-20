package com.proyecto.sica.domain.entity;

import java.util.Objects;

/**
 * Operacion que puede ser autorizada a traves de un rol.
 */
public final class Permiso {

    private final int id;
    private final String codigo;
    private final String nombre;
    private final String descripcion;

    public Permiso(int id, String codigo, String nombre, String descripcion) {
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
}
