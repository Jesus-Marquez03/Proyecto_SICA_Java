package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorIncidente;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.IncidenteRepository;

/** Recupera un incidente y comprueba su alcance antes de devolverlo. */
public final class ConsultarIncidentePorIdUseCase {
    private final IncidenteRepository repositorio;
    private final ServicioAutorizacion autorizacion;
    private final ValidadorIncidente validador;

    public ConsultarIncidentePorIdUseCase(IncidenteRepository repositorio,
            ServicioAutorizacion autorizacion, ValidadorIncidente validador) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
        this.validador = validador;
    }

    public Incidente ejecutar(Usuario actor, long id) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_INCIDENTE);
        Incidente incidente = repositorio.buscarPorId(id)
                .orElseThrow(() -> new ValidacionException("El incidente no existe"));
        validador.verificarAmbito(actor, incidente);
        return incidente;
    }
}
