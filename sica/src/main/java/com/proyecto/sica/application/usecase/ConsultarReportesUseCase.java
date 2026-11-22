package com.proyecto.sica.application.usecase;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.ReporteRepository;
import java.time.LocalDate;

/** Coordina los reportes del periodo con autorizacion y alcance empresarial. */
public final class ConsultarReportesUseCase {
    private final ReporteRepository repositorio;
    private final ServicioAutorizacion autorizacion;

    public ConsultarReportesUseCase(ReporteRepository repositorio,
            ServicioAutorizacion autorizacion) {
        this.repositorio = repositorio;
        this.autorizacion = autorizacion;
    }

    public ResultadoReportes ejecutar(Usuario actor, LocalDate desde, LocalDate hasta) {
        autorizacion.verificarPermiso(actor, PermisosSistema.GENERAR_REPORTES);
        autorizacion.verificarPermiso(actor, PermisosSistema.CONSULTAR_INCIDENTE);
        validarPeriodo(desde, hasta);
        Long empresaId = actor.getRol().isRequiereEmpresa() ? actor.getEmpresaId() : null;
        return new ResultadoReportes(
                repositorio.consultarAccesos(desde, hasta, empresaId),
                repositorio.consultarPersonasDentro(empresaId),
                repositorio.consultarEstadosVisita(desde, hasta, empresaId),
                repositorio.consultarIncidentes(desde, hasta, empresaId),
                repositorio.consultarTrazabilidad(desde, hasta, empresaId));
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new ValidacionException("Las fechas del reporte son obligatorias");
        }
        if (hasta.isBefore(desde)) {
            throw new ValidacionException("La fecha final no puede ser anterior a la inicial");
        }
        if (desde.plusYears(2).isBefore(hasta)) {
            throw new ValidacionException("El periodo maximo de consulta es de dos anos");
        }
    }
}
