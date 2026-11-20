package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.PersonaRepository;

/** Consulta una persona por su identificador. */
public final class ConsultarPersonaPorIdUseCase {
    private final PersonaRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarPersonaPorIdUseCase(PersonaRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public Persona ejecutar(Usuario actor, long id) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_PERSONA);
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new ValidacionException("La persona no existe"));
    }
}
