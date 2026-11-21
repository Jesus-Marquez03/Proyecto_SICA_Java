package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.VisitaRepository;
import java.time.LocalDate;
import java.util.List;

/** Consulta entradas, salidas, autorizaciones y regularizaciones. */
public final class ConsultarHistorialVisitasUseCase {
    private final VisitaRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarHistorialVisitasUseCase(VisitaRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public List<Visita> ejecutar(Usuario actor, String documento, LocalDate desde,
            LocalDate hasta, String estadoCodigo) {
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_HISTORIAL_VISITAS);
        Long anfitrionId = actor.getRol().isRequiereEmpresa() ? actor.getId() : null;
        return repositorio.listarHistorial(documento, desde, hasta, estadoCodigo, anfitrionId);
    }
}
