package com.proyecto.sica.infrastructure.ui.controller;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.usecase.ActualizarPersonaUseCase;
import com.proyecto.sica.application.usecase.BuscarPersonaPorDocumentoUseCase;
import com.proyecto.sica.application.usecase.ConsultarEmpresasUseCase;
import com.proyecto.sica.application.usecase.ConsultarPersonasUseCase;
import com.proyecto.sica.application.usecase.CrearPersonaUseCase;
import com.proyecto.sica.application.usecase.SolicitudPersona;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.EjecutorTareasUi;
import com.proyecto.sica.infrastructure.ui.PersonaView;

/** Controlador MVC de trabajadores e invitados. */
public final class PersonaController {
    private final PersonaView vista;
    private final Usuario usuario;
    private final CrearPersonaUseCase crear;
    private final ActualizarPersonaUseCase actualizar;
    private final ConsultarPersonasUseCase consultar;
    private final BuscarPersonaPorDocumentoUseCase buscar;
    private final ConsultarEmpresasUseCase consultarEmpresas;

    public PersonaController(PersonaView vista, Usuario usuario,
            CrearPersonaUseCase crear, ActualizarPersonaUseCase actualizar,
            ConsultarPersonasUseCase consultar, BuscarPersonaPorDocumentoUseCase buscar,
            ConsultarEmpresasUseCase consultarEmpresas) {
        this.vista = vista;
        this.usuario = usuario;
        this.crear = crear;
        this.actualizar = actualizar;
        this.consultar = consultar;
        this.buscar = buscar;
        this.consultarEmpresas = consultarEmpresas;
        vista.alCrear(this::crear);
        vista.alActualizar(this::actualizar);
        vista.alBuscar(this::buscar);
        vista.permitirCrear(usuario.getRol().tienePermiso(PermisosSistema.CREAR_PERSONA));
        vista.permitirActualizar(usuario.getRol().tienePermiso(PermisosSistema.ACTUALIZAR_PERSONA));
    }

    public void cargar() {
        EjecutorTareasUi.ejecutar(() -> consultar.ejecutar(usuario),
                vista::mostrarPersonas, this::mostrarError);
        EjecutorTareasUi.ejecutar(() -> consultarEmpresas.ejecutar(usuario),
                vista::mostrarEmpresas, this::mostrarError);
    }

    private void buscar() {
        String tipoDocumento = vista.getTipoDocumentoBusqueda();
        String numeroDocumento = vista.getDocumentoBusqueda();
        EjecutorTareasUi.ejecutar(() -> buscar.ejecutar(usuario,
                tipoDocumento, numeroDocumento),
                vista::seleccionar, this::mostrarError);
    }

    private void crear() {
        SolicitudPersona solicitud = vista.getSolicitud();
        EjecutorTareasUi.ejecutar(() -> crear.ejecutar(usuario, solicitud), persona -> {
            vista.mostrarInformacion("Persona registrada correctamente");
            cargar();
        }, this::mostrarError);
    }

    private void actualizar() {
        Persona persona = vista.getSeleccionada();
        if (persona == null) {
            vista.mostrarError("Seleccione una persona para editar");
            return;
        }
        SolicitudPersona solicitud = vista.getSolicitud();
        EjecutorTareasUi.ejecutar(
                () -> actualizar.ejecutar(usuario, persona.getId(), solicitud),
                editada -> {
                    vista.mostrarInformacion("Persona actualizada correctamente");
                    cargar();
                }, this::mostrarError);
    }

    private void mostrarError(Throwable error) {
        vista.mostrarError(error.getMessage() == null
                ? "No fue posible completar la operacion" : error.getMessage());
    }
}
