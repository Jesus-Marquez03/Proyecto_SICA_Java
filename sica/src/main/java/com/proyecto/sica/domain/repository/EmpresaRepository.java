package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import java.util.List;
import java.util.Optional;

/**
 * Puerto minimo para validar la empresa asociada a un usuario.
 */
public interface EmpresaRepository {

    boolean existeActiva(long id);

    Optional<String> buscarNombre(long id);

    Optional<Empresa> buscarPorId(long id);

    List<Empresa> listar();

    List<Empresa> buscarPorNombre(String nombre);

    boolean existeNombre(String nombre, Long idExcluido);

    boolean existeNit(String nit, Long idExcluido);

    Empresa crearConAuditoria(Empresa empresa, EventoAuditoria evento);

    void actualizarConAuditoria(Empresa empresa, EventoAuditoria evento);
}
