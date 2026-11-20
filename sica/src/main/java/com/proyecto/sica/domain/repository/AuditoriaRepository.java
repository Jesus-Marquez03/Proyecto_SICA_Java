package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.EventoAuditoria;

/**
 * Puerto para registrar eventos en la bitacora.
 */
public interface AuditoriaRepository {

    void registrar(EventoAuditoria evento);
}
