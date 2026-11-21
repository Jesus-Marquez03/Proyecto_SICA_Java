package com.proyecto.sica.domain.repository;

/** Indica que otra operacion cambio el registro antes de confirmar la actual. */
public final class ConflictoPersistenciaException extends RuntimeException {
    public ConflictoPersistenciaException(String mensaje) {
        super(mensaje);
    }
}
