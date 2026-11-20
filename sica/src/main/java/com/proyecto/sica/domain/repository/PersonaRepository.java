package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Persona;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia para trabajadores e invitados.
 */
public interface PersonaRepository {

    Optional<Persona> buscarPorId(long id);

    Optional<Persona> buscarPorDocumento(String tipoDocumento, String numeroDocumento);

    List<Persona> listar();

    List<Persona> buscarPorEmpresa(long empresaId);

    boolean existeDocumento(String tipoDocumento, String numeroDocumento, Long idExcluido);

    Persona crearConAuditoria(Persona persona, EventoAuditoria evento);

    void actualizarConAuditoria(Persona persona, EventoAuditoria evento);

    void cambiarEstadoConAuditoria(long personaId, int estadoId, EventoAuditoria evento);
}
