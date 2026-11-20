package com.proyecto.sica.domain.repository;

import java.util.Optional;

/**
 * Puerto minimo para validar la empresa asociada a un usuario.
 */
public interface EmpresaRepository {

    boolean existeActiva(long id);

    Optional<String> buscarNombre(long id);
}
