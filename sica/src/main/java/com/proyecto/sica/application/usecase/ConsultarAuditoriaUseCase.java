package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.RegistroAuditoria;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.ConsultaAuditoriaRepository;
import com.proyecto.sica.domain.repository.FiltroAuditoria;
import java.util.List;

/** Expone consultas de bitacora sin ofrecer edicion ni eliminacion. */
public final class ConsultarAuditoriaUseCase {
    private final ConsultaAuditoriaRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarAuditoriaUseCase(ConsultaAuditoriaRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public List<RegistroAuditoria> ejecutar(Usuario actor, FiltroAuditoria filtro) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_AUDITORIA);
        return repositorio.consultar(filtro == null ? FiltroAuditoria.recientes() : filtro);
    }
}
