package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidadorEmpresa;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.EmpresaRepository;

/** Crea una empresa y su auditoria dentro de la misma transaccion. */
public final class CrearEmpresaUseCase {
    private final EmpresaRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorEmpresa validador;

    public CrearEmpresaUseCase(EmpresaRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorEmpresa validador) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Empresa ejecutar(Usuario actor, SolicitudEmpresa solicitud) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CREAR_EMPRESA);
        validador.validar(solicitud.getNit(), solicitud.getNombre(),
                solicitud.getCorreo(), null);
        Empresa empresa = construir(null, solicitud, true);
        EventoAuditoria evento = new EventoAuditoria(actor.getId(), null,
                "CREAR_EMPRESA", "empresas", null, "EXITOSO",
                "Creacion de la empresa " + empresa.getNombre());
        return repositorio.crearConAuditoria(empresa, evento);
    }

    private Empresa construir(Long id, SolicitudEmpresa solicitud, boolean activa) {
        return new Empresa(id, validador.normalizarNit(solicitud.getNit()),
                solicitud.getNombre().trim(), textoOpcional(solicitud.getDireccion()),
                textoOpcional(solicitud.getTelefono()),
                validador.normalizarCorreo(solicitud.getCorreo()), activa);
    }

    private String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
