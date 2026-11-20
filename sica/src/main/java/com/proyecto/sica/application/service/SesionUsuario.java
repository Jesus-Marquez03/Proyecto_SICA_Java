package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.entity.Usuario;
import java.util.Optional;

/**
 * Conserva solamente al usuario autenticado durante la ejecucion actual.
 */
public final class SesionUsuario {

    private Usuario usuario;

    public void iniciar(Usuario usuario) {
        this.usuario = usuario;
    }

    public Optional<Usuario> obtenerUsuario() {
        return Optional.ofNullable(usuario);
    }

    public boolean estaIniciada() {
        return usuario != null;
    }

    public void cerrar() {
        usuario = null;
    }
}
