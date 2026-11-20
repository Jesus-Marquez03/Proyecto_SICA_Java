package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.EmpresaRepository;
import java.util.List;

/** Consulta empresas para listados y formularios. */
public final class ConsultarEmpresasUseCase {
    private final EmpresaRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarEmpresasUseCase(EmpresaRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public List<Empresa> ejecutar(Usuario actor) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_EMPRESA);
        return repositorio.listar();
    }

    public List<Empresa> buscarPorNombre(Usuario actor, String nombre) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_EMPRESA);
        return nombre == null || nombre.isBlank()
                ? repositorio.listar() : repositorio.buscarPorNombre(nombre.trim());
    }
}
