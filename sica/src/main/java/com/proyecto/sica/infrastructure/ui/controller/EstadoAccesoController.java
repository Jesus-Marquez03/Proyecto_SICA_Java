package com.proyecto.sica.infrastructure.ui.controller;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.usecase.CambiarEstadoAccesoUseCase;
import com.proyecto.sica.application.usecase.ConsultarEstadosAccesoUseCase;
import com.proyecto.sica.application.usecase.ConsultarPersonasUseCase;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.EjecutorTareasUi;
import com.proyecto.sica.infrastructure.ui.EstadoAccesoView;

/** Controlador MVC para consultar y cambiar restricciones de acceso. */
public final class EstadoAccesoController {
    private final EstadoAccesoView vista;
    private final Usuario usuario;
    private final ConsultarPersonasUseCase consultarPersonas;
    private final ConsultarEstadosAccesoUseCase consultarEstados;
    private final CambiarEstadoAccesoUseCase cambiarEstado;

    public EstadoAccesoController(EstadoAccesoView vista, Usuario usuario,
            ConsultarPersonasUseCase consultarPersonas,
            ConsultarEstadosAccesoUseCase consultarEstados,
            CambiarEstadoAccesoUseCase cambiarEstado) {
        this.vista = vista;
        this.usuario = usuario;
        this.consultarPersonas = consultarPersonas;
        this.consultarEstados = consultarEstados;
        this.cambiarEstado = cambiarEstado;
        vista.alCambiar(this::cambiar);
        vista.permitirCambio(
                usuario.getRol().tienePermiso(PermisosSistema.CAMBIAR_ESTADO_ACCESO));
    }

    public void cargar() {
        EjecutorTareasUi.ejecutar(() -> consultarPersonas.ejecutar(usuario),
                vista::mostrarPersonas, this::mostrarError);
        EjecutorTareasUi.ejecutar(() -> consultarEstados.ejecutar(usuario),
                vista::mostrarEstados, this::mostrarError);
    }

    private void cambiar() {
        Persona persona = vista.getPersonaSeleccionada();
        EstadoAcceso estado = vista.getEstadoSeleccionado();
        if (persona == null || estado == null) {
            vista.mostrarError("Seleccione una persona y el nuevo estado");
            return;
        }
        EjecutorTareasUi.ejecutar(
                () -> cambiarEstado.ejecutar(usuario, persona.getId(), estado.getId()),
                cambioRealizado -> {
                    vista.mostrarInformacion(cambioRealizado
                            ? "Estado de acceso actualizado"
                            : "La persona ya tenia ese estado; no se realizaron cambios");
                    cargar();
                }, this::mostrarError);
    }

    private void mostrarError(Throwable error) {
        vista.mostrarError(error.getMessage() == null
                ? "No fue posible completar la operacion" : error.getMessage());
    }
}
