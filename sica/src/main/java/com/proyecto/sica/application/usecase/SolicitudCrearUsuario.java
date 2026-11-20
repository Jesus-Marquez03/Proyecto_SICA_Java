package com.proyecto.sica.application.usecase;

import java.util.Arrays;

/**
 * Datos necesarios para crear un usuario sin acoplar el caso de uso a JavaFX.
 */
public final class SolicitudCrearUsuario {

    private final String nombreUsuario;
    private final char[] password;
    private final String nombres;
    private final String apellidos;
    private final String correo;
    private final int rolId;
    private final Long empresaId;

    public SolicitudCrearUsuario(String nombreUsuario, char[] password, String nombres,
            String apellidos, String correo, int rolId, Long empresaId) {
        this.nombreUsuario = nombreUsuario;
        this.password = password == null ? null : password.clone();
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.rolId = rolId;
        this.empresaId = empresaId;
    }

    public char[] copiarPassword() {
        return password == null ? null : password.clone();
    }

    public void limpiarPassword() {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
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
