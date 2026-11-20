package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.entity.Usuario;

/**
 * Verifica permisos cargados desde la relacion rol_permisos.
 */
public final class ServicioAutorizacion {

    public void verificarPermiso(Usuario usuario, String codigoPermiso) {
        if (usuario == null || !usuario.isActivo()) {
            throw new AccesoDenegadoException("Se requiere un usuario activo");
        }
        if (!usuario.getRol().isActivo()
                || !usuario.getRol().tienePermiso(codigoPermiso)) {
            throw new AccesoDenegadoException(
                    "No tiene permiso para ejecutar esta operacion: " + codigoPermiso);
        }
    }
}
