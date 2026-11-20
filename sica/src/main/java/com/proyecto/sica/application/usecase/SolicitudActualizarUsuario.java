package com.proyecto.sica.application.usecase;

/**
 * Datos basicos editables de un usuario existente.
 */
public final class SolicitudActualizarUsuario {

    private final long id;
    private final String nombreUsuario;
    private final String nombres;
    private final String apellidos;
    private final String correo;
    private final int rolId;
    private final Long empresaId;

    public SolicitudActualizarUsuario(long id, String nombreUsuario, String nombres,
            String apellidos, String correo, int rolId, Long empresaId) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.rolId = rolId;
        this.empresaId = empresaId;
    }

    public long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
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

    public int getRolId() {
        return rolId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }
}
