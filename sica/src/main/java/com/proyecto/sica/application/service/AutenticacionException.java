package com.proyecto.sica.application.service;

/**
 * Error esperado durante el inicio de sesion.
 */
public final class AutenticacionException extends RuntimeException {

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
