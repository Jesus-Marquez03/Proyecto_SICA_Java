package com.proyecto.sica.infrastructure.ui.controller;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.usecase.ActualizarIncidenteUseCase;
import com.proyecto.sica.application.usecase.CambiarEstadoAccesoUseCase;
import com.proyecto.sica.application.usecase.ConsultarEstadosAccesoUseCase;
import com.proyecto.sica.application.usecase.ConsultarIncidentesUseCase;
import com.proyecto.sica.application.usecase.CrearIncidenteUseCase;
import com.proyecto.sica.application.usecase.SolicitudIncidente;
import com.proyecto.sica.application.usecase.SolicitudSeguimientoIncidente;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.FiltroIncidentes;
import com.proyecto.sica.infrastructure.ui.EjecutorTareasUi;
import com.proyecto.sica.infrastructure.ui.IncidentesView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javafx.collections.FXCollections;

/** Controlador MVC de incidentes; todas las operaciones JDBC son asincronas. */
public final class IncidentesController {
    private final IncidentesView vista;
    private final Usuario actor;
    private final CrearIncidenteUseCase crear;
    private final ConsultarIncidentesUseCase consultar;
    private final ActualizarIncidenteUseCase actualizar;
    private final ConsultarEstadosAccesoUseCase consultarEstados;
    private final CambiarEstadoAccesoUseCase cambiarEstado;

    public IncidentesController(IncidentesView vista, Usuario actor,
            CrearIncidenteUseCase crear, ConsultarIncidentesUseCase consultar,
            ActualizarIncidenteUseCase actualizar,
            ConsultarEstadosAccesoUseCase consultarEstados,
            CambiarEstadoAccesoUseCase cambiarEstado) {
        this.vista = vista; this.actor = actor; this.crear = crear;
        this.consultar = consultar; this.actualizar = actualizar;
        this.consultarEstados = consultarEstados; this.cambiarEstado = cambiarEstado;
        configurar();
    }

    private void configurar() {
        vista.getBuscar().setOnAction(e -> cargar());
        vista.getRegistrar().setOnAction(e -> registrar());
        vista.getActualizar().setOnAction(e -> actualizar());
        vista.getRestringir().setOnAction(e -> restringir());
        vista.getRegistrar().setDisable(!permiso(PermisosSistema.REGISTRAR_INCIDENTE));
        vista.getActualizar().setDisable(!permiso(PermisosSistema.ACTUALIZAR_INCIDENTE));
        vista.getRestringir().setDisable(!permiso(PermisosSistema.CAMBIAR_ESTADO_ACCESO));
    }

    public void cargar() {
        ejecutar(() -> consultar.ejecutar(actor, new FiltroIncidentes(
                largo(vista.getFiltroVisita().getText()), largo(vista.getFiltroPersona().getText()),
                largo(vista.getFiltroResponsable().getText()), fecha(vista.getFiltroDesde().getText()),
                fecha(vista.getFiltroHasta().getText()), vista.getFiltroEstado().getValue())),
                datos -> vista.getTabla().setItems(FXCollections.observableArrayList(datos)));
    }

    private void registrar() {
        ejecutar(() -> crear.ejecutar(actor, new SolicitudIncidente(
                requerido(vista.getVisitaId().getText(), "La visita es obligatoria"),
                vista.getTipo().getText(), vista.getGravedad().getValue(),
                vista.getDescripcion().getText(), vista.getAcciones().getText(),
                fechaHora(vista.getFecha().getText()))), resultado -> {
                    vista.mostrarInfo("Incidente registrado con ID " + resultado.getId()); cargar();
                });
    }

    private void actualizar() {
        Incidente seleccionado = vista.seleccionado();
        if (seleccionado == null) { vista.mostrarError("Seleccione un incidente"); return; }
        ejecutar(() -> actualizar.ejecutar(actor, seleccionado.getId(),
                new SolicitudSeguimientoIncidente(vista.getGravedad().getValue(),
                        vista.getEstado().getValue(), vista.getDescripcion().getText(),
                        vista.getAcciones().getText(), vista.getJustificacion().getText())), r -> {
                            vista.mostrarInfo("Seguimiento guardado"); cargar();
                        });
    }

    private void restringir() {
        Incidente seleccionado = vista.seleccionado();
        if (seleccionado == null) { vista.mostrarError("Seleccione un incidente"); return; }
        ejecutar(() -> {
            EstadoAcceso prohibicion = consultarEstados.ejecutar(actor).stream()
                    .filter(e -> "PROHIBICION_INGRESO".equals(e.getCodigo())).findFirst()
                    .orElseThrow(() -> new IllegalStateException("No existe PROHIBICION_INGRESO"));
            return cambiarEstado.ejecutar(actor, seleccionado.getPersona().getId(), prohibicion.getId());
        }, cambio -> vista.mostrarInfo(cambio
                ? "Prohibicion aplicada y auditada" : "La persona ya tenia la prohibicion"));
    }

    private boolean permiso(String codigo) { return actor.getRol().tienePermiso(codigo); }
    private Long largo(String texto) { return texto == null || texto.isBlank() ? null : Long.valueOf(texto.trim()); }
    private long requerido(String texto, String mensaje) {
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException(mensaje);
        return Long.parseLong(texto.trim());
    }
    private LocalDate fecha(String texto) { return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim()); }
    private LocalDateTime fechaHora(String texto) {
        return texto == null || texto.isBlank() ? LocalDateTime.now() : LocalDateTime.parse(texto.trim());
    }
    private <T> void ejecutar(java.util.concurrent.Callable<T> operacion,
            java.util.function.Consumer<T> exito) {
        EjecutorTareasUi.ejecutar(operacion, exito,
                error -> vista.mostrarError(error.getMessage() == null ? "Operacion fallida" : error.getMessage()));
    }
}
