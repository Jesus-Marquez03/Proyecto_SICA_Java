package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.PersonaRepository;

/** Busca una persona mediante la clave natural tipo y numero de documento. */
public final class BuscarPersonaPorDocumentoUseCase {
    private final PersonaRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public BuscarPersonaPorDocumentoUseCase(PersonaRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public Persona ejecutar(Usuario actor, String tipoDocumento, String numeroDocumento) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_PERSONA);
        if (tipoDocumento == null || tipoDocumento.isBlank()
                || numeroDocumento == null || numeroDocumento.isBlank()) {
            throw new ValidacionException("Indique el tipo y numero de documento");
        }
        return repositorio.buscarPorDocumento(tipoDocumento.trim().toUpperCase(),
                numeroDocumento.trim()).orElseThrow(
                        () -> new ValidacionException("No existe una persona con ese documento"));
    }
}
