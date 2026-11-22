package com.proyecto.sica.infrastructure.ui.controller;

import com.proyecto.sica.application.usecase.ConsultarReportesUseCase;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.EjecutorTareasUi;
import com.proyecto.sica.infrastructure.ui.ReportesView;

/** Coordina filtros de reportes y ejecuta JDBC fuera del hilo JavaFX. */
public final class ReportesController {
    private final ReportesView vista;
    private final Usuario actor;
    private final ConsultarReportesUseCase casoUso;

    public ReportesController(ReportesView vista, Usuario actor,
            ConsultarReportesUseCase casoUso) {
        this.vista = vista;
        this.actor = actor;
        this.casoUso = casoUso;
        vista.alConsultar(this::cargar);
    }

    public void cargar() {
        vista.cargando();
        EjecutorTareasUi.ejecutar(
                () -> casoUso.ejecutar(actor, vista.getDesde(), vista.getHasta()),
                vista::mostrar,
                error -> vista.error(error.getMessage() == null
                        ? "No fue posible generar el reporte" : error.getMessage()));
    }
}
