package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.Rol;
import java.util.List;
import java.util.Optional;

/**
 * Puerto para consultar roles y sus permisos.
 */
public interface RolRepository {

    Optional<Rol> buscarPorId(int id);

    List<Rol> listar();
}
