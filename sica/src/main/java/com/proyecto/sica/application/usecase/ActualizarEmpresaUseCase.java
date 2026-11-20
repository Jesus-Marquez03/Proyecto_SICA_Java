package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorEmpresa;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.EmpresaRepository;

/** Actualiza una empresa existente sin eliminar sus relaciones. */
public final class ActualizarEmpresaUseCase {
    private final EmpresaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorEmpresa validador;

    public ActualizarEmpresaUseCase(EmpresaRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorEmpresa validador) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Empresa ejecutar(Usuario actor, long id, SolicitudEmpresa solicitud) {
        autorizacion.verificarPermiso(actor, PermisosSistema.ACTUALIZAR_EMPRESA);
        Empresa actual = repositorio.buscarPorId(id)
                .orElseThrow(() -> new ValidacionException("La empresa no existe"));
        validador.validar(solicitud.getNit(), solicitud.getNombre(),
                solicitud.getCorreo(), id);
        Empresa empresa = new Empresa(id, validador.normalizarNit(solicitud.getNit()),
                solicitud.getNombre().trim(), textoOpcional(solicitud.getDireccion()),
                textoOpcional(solicitud.getTelefono()),
                validador.normalizarCorreo(solicitud.getCorreo()), actual.isActiva());
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "ACTUALIZAR_EMPRESA", "empresas", String.valueOf(id), "EXITOSO",
                "Actualizacion de la empresa " + empresa.getNombre());
        repositorio.actualizarConAuditoria(empresa, evento);
        return empresa;
    }

    private String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
