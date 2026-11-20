package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.PersonaRepository;
import java.util.List;

/** Consulta el listado general de personas. */
public final class ConsultarPersonasUseCase {
    private final PersonaRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarPersonasUseCase(PersonaRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public List<Persona> ejecutar(Usuario actor) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_PERSONA);
        return repositorio.listar();
    }

    public List<Persona> buscarPorEmpresa(Usuario actor, long empresaId) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_PERSONA);
        return repositorio.buscarPorEmpresa(empresaId);
    }
}
