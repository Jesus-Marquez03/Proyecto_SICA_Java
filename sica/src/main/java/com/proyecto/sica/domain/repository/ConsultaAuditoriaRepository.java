package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.RegistroAuditoria;
import java.util.List;

/** Puerto de solo lectura para consultar la trazabilidad historica. */
public interface ConsultaAuditoriaRepository {
    List<RegistroAuditoria> consultar(FiltroAuditoria filtro);
}
