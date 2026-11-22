package com.proyecto.sica.infrastructure.ui.controller;

import com.proyecto.sica.application.usecase.ConsultarResumenDashboardUseCase;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.DashboardInicioView;
import com.proyecto.sica.infrastructure.ui.EjecutorTareasUi;

/** Carga el resumen sin bloquear el hilo grafico de JavaFX. */
public final class DashboardController {
    private final DashboardInicioView vista;
    private final Usuario actor;
    private final ConsultarResumenDashboardUseCase casoUso;

    public DashboardController(DashboardInicioView vista, Usuario actor,
            ConsultarResumenDashboardUseCase casoUso) {
        this.vista = vista;
        this.actor = actor;
        this.casoUso = casoUso;
        vista.alActualizar(this::cargar);
    }

    public void cargar() {
        vista.cargando();
        EjecutorTareasUi.ejecutar(() -> casoUso.ejecutar(actor), vista::mostrar,
                error -> vista.error("No fue posible actualizar el resumen"));
    }
}
