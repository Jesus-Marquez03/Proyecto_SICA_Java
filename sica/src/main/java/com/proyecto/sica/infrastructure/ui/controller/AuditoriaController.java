package com.proyecto.sica.infrastructure.ui.controller;

import com.proyecto.sica.application.usecase.ConsultarAuditoriaUseCase;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.FiltroAuditoria;
import com.proyecto.sica.infrastructure.ui.AuditoriaView;
import com.proyecto.sica.infrastructure.ui.EjecutorTareasUi;
import java.time.LocalDate;
import javafx.collections.FXCollections;

/** Controlador de consultas de bitacora; no expone escritura ni eliminacion. */
public final class AuditoriaController {
    private final AuditoriaView vista;
    private final Usuario actor;
    private final ConsultarAuditoriaUseCase consultar;

    public AuditoriaController(AuditoriaView vista, Usuario actor,
            ConsultarAuditoriaUseCase consultar) {
        this.vista = vista; this.actor = actor; this.consultar = consultar;
        vista.getBuscar().setOnAction(e -> cargar());
    }

    public void cargar() {
        FiltroAuditoria filtro;
        try {
            filtro = new FiltroAuditoria(largo(vista.getUsuario().getText()),
                    texto(vista.getAccion().getText()), fecha(vista.getDesde().getText()),
                    fecha(vista.getHasta().getText()), texto(vista.getEntidad().getText()),
                    texto(vista.getRegistro().getText()), largo(vista.getPersona().getText()),
                    largo(vista.getVisita().getText()), 200);
        } catch (RuntimeException error) {
            vista.mostrarError("Revise IDs y fechas de los filtros"); return;
        }
        EjecutorTareasUi.ejecutar(() -> consultar.ejecutar(actor, filtro),
                datos -> vista.getTabla().setItems(FXCollections.observableArrayList(datos)),
                error -> vista.mostrarError(error.getMessage() == null
                        ? "No fue posible consultar" : error.getMessage()));
    }

    private String texto(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private Long largo(String valor) { String limpio = texto(valor); return limpio == null ? null : Long.valueOf(limpio); }
    private LocalDate fecha(String valor) { String limpio = texto(valor); return limpio == null ? null : LocalDate.parse(limpio); }
}
