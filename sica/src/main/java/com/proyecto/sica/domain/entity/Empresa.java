package com.proyecto.sica.domain.entity;

import java.util.Objects;

/**
 * Empresa vinculada con usuarios, personas y futuras visitas.
 */
public final class Empresa {

    private final Long id;
    private final String nit;
    private final String nombre;
    private final String direccion;
    private final String telefono;
    private final String correo;
    private final boolean activa;

    public Empresa(Long id, String nit, String nombre, String direccion,
            String telefono, String correo, boolean activa) {
        this.id = id;
        this.nit = Objects.requireNonNull(nit);
        this.nombre = Objects.requireNonNull(nombre);
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
        this.activa = activa;
    }

    public Long getId() {
        return id;
    }

    public String getNit() {
        return nit;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public boolean isActiva() {
        return activa;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
