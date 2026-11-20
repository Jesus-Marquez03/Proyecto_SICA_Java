package com.proyecto.sica.application.service;

/**
 * Error de datos de entrada que puede mostrarse al usuario.
 */
public final class ValidacionException extends RuntimeException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
