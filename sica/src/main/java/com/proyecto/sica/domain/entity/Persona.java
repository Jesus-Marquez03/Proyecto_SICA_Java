package com.proyecto.sica.domain.entity;

import java.util.Objects;

/**
 * Trabajador o invitado identificado dentro de SICA.
 */
public final class Persona {

    private final Long id;
    private final Empresa empresa;
    private final EstadoAcceso estadoAcceso;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String nombres;
    private final String apellidos;
    private final String telefono;
    private final String correo;
    private final TipoPersona tipoPersona;
    private final boolean activa;
    private final String fotoUrl;

    public Persona(Long id, Empresa empresa, EstadoAcceso estadoAcceso,
            String tipoDocumento, String numeroDocumento, String nombres,
            String apellidos, String telefono, String correo,
            TipoPersona tipoPersona, boolean activa, String fotoUrl) {
        this.id = id;
        this.empresa = empresa;
        this.estadoAcceso = Objects.requireNonNull(estadoAcceso);
        this.tipoDocumento = Objects.requireNonNull(tipoDocumento);
        this.numeroDocumento = Objects.requireNonNull(numeroDocumento);
        this.nombres = Objects.requireNonNull(nombres);
        this.apellidos = Objects.requireNonNull(apellidos);
        this.telefono = telefono;
        this.correo = correo;
        this.tipoPersona = Objects.requireNonNull(tipoPersona);
        this.activa = activa;
        this.fotoUrl = fotoUrl;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public EstadoAcceso getEstadoAcceso() {
        return estadoAcceso;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public TipoPersona getTipoPersona() {
        return tipoPersona;
    }

    public boolean isActiva() {
        return activa;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }
}
