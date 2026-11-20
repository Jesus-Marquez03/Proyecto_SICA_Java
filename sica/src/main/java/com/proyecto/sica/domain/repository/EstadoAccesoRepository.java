package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.EstadoAcceso;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de consulta del catalogo persona_estados_acceso.
 */
public interface EstadoAccesoRepository {

    Optional<EstadoAcceso> buscarPorId(int id);

    Optional<EstadoAcceso> buscarPorCodigo(String codigo);

    List<EstadoAcceso> listar();
}
