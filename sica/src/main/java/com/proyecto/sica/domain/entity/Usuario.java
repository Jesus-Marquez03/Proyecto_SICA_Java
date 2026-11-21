package com.proyecto.sica.domain.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Usuario autenticable de SICA. No contiene consultas ni codigo de interfaz.
 */
public final class Usuario {

    private final Long id;
    private final Rol rol;
    private final Long empresaId;
    private final String empresaNombre;
    private final String nombreUsuario;
    private final String passwordHash;
    private final String nombres;
    private final String apellidos;
    private final String correo;
    private final boolean activo;
    private final LocalDateTime ultimoAcceso;

    public Usuario(Long id, Rol rol, Long empresaId, String empresaNombre,
            String nombreUsuario, String passwordHash, String nombres,
            String apellidos, String correo, boolean activo, LocalDateTime ultimoAcceso) {
        this.id = id;
        this.rol = Objects.requireNonNull(rol);
        this.empresaId = empresaId;
        this.empresaNombre = empresaNombre;
        this.nombreUsuario = Objects.requireNonNull(nombreUsuario);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.nombres = Objects.requireNonNull(nombres);
        this.apellidos = Objects.requireNonNull(apellidos);
        this.correo = Objects.requireNonNull(correo);
        this.activo = activo;
        this.ultimoAcceso = ultimoAcceso;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public Long getId() {
        return id;
    }

    public Rol getRol() {
        return rol;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getEmpresaNombre() {
        return empresaNombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
