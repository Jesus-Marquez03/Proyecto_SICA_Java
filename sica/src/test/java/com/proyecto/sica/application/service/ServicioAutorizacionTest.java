package com.proyecto.sica.application.service;

import static com.proyecto.sica.support.DatosPrueba.rolConPermiso;
import static com.proyecto.sica.support.DatosPrueba.rolSinPermisos;
import static com.proyecto.sica.support.DatosPrueba.usuario;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ServicioAutorizacionTest {

    private final ServicioAutorizacion servicio = new ServicioAutorizacion();

    @Test
    void permiteOperacionAsignadaAlRol() {
        assertDoesNotThrow(() -> servicio.verificarPermiso(
                usuario(1, rolConPermiso("GESTIONAR_USUARIOS"),
                        "admin@sica.example", "hash", true),
                "GESTIONAR_USUARIOS"));
    }

    @Test
    void deniegaOperacionNoAsignadaAlRol() {
        assertThrows(AccesoDenegadoException.class, () -> servicio.verificarPermiso(
                usuario(2, rolSinPermisos(), "guarda@sica.example", "hash", true),
                "GESTIONAR_USUARIOS"));
    }
}
