package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.EmpresaRepository;

/** Obtiene una empresa por su identificador. */
public final class ConsultarEmpresaPorIdUseCase {
    private final EmpresaRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarEmpresaPorIdUseCase(EmpresaRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public Empresa ejecutar(Usuario actor, long id) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_EMPRESA);
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ValidacionException("La empresa no existe"));
    }
}
