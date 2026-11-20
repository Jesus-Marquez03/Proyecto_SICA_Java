package com.proyecto.sica.support;

import com.proyecto.sica.domain.entity.Permiso;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Objetos reutilizables en las pruebas unitarias.
 */
public final class DatosPrueba {

    private DatosPrueba() {
    }

    public static Rol rolConPermiso(String codigo) {
        return new Rol(1, "Rol autorizado", "Prueba", true, false,
                Set.of(new Permiso(1, codigo, codigo, "Prueba")));
    }

    public static Rol rolConPermisos(String... codigos) {
        AtomicInteger id = new AtomicInteger(1);
        Set<Permiso> permisos = java.util.Arrays.stream(codigos)
                .map(codigo -> new Permiso(id.getAndIncrement(), codigo, codigo, "Prueba"))
                .collect(Collectors.toUnmodifiableSet());
        return new Rol(1, "Rol autorizado", "Prueba", true, false, permisos);
    }

    public static Rol rolSinPermisos() {
        return new Rol(3, "Rol sin permiso", "Prueba", true, false, Set.of());
    }

    public static Usuario usuario(long id, Rol rol, String correo,
            String hash, boolean activo) {
        return new Usuario(id, rol, null, null, "usuario" + id, hash,
                "Nombre", "Apellido", correo, activo, null);
    }
}
