package com.proyecto.sica.application.service;

/**
 * Indica que un usuario no puede ejecutar una operacion.
 */
public final class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
