package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.EstadoVisita;
import java.util.List;
import java.util.Optional;

/** Puerto para consultar estados de visita sin codificar sus IDs. */
public interface EstadoVisitaRepository {
    Optional<EstadoVisita> buscarPorId(int id);
    Optional<EstadoVisita> buscarPorCodigo(String codigo);
    List<EstadoVisita> listar();
}
