package com.proyecto.sica.domain.repository;

/**
 * Error de persistencia presentado a la capa de aplicacion sin exponer JDBC.
 */
public final class RepositorioException extends RuntimeException {

    public RepositorioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
