package com.proyecto.sica.domain.entity;

import java.util.Objects;
import java.util.Set;

/**
 * Rol de seguridad con los permisos asignados desde la base de datos.
 */
public final class Rol {

    private final int id;
    private final String nombre;
    private final String descripcion;
    private final boolean activo;
    private final boolean requiereEmpresa;
    private final Set<Permiso> permisos;

    public Rol(int id, String nombre, String descripcion, boolean activo,
            boolean requiereEmpresa, Set<Permiso> permisos) {
        this.id = id;
        this.nombre = Objects.requireNonNull(nombre);
        this.descripcion = descripcion;
        this.activo = activo;
        this.requiereEmpresa = requiereEmpresa;
        this.permisos = Set.copyOf(permisos);
    }

    public boolean tienePermiso(String codigoPermiso) {
        return permisos.stream()
                .anyMatch(permiso -> permiso.getCodigo().equals(codigoPermiso));
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public boolean isRequiereEmpresa() {
        return requiereEmpresa;
    }

    public Set<Permiso> getPermisos() {
        return permisos;
    }
}
