package com.proyecto.sica.infrastructure.ui.controller;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.usecase.ActualizarEmpresaUseCase;
import com.proyecto.sica.application.usecase.ConsultarEmpresasUseCase;
import com.proyecto.sica.application.usecase.CrearEmpresaUseCase;
import com.proyecto.sica.application.usecase.SolicitudEmpresa;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.EjecutorTareasUi;
import com.proyecto.sica.infrastructure.ui.EmpresaView;

/** Controlador MVC que conecta la vista de empresas con sus casos de uso. */
public final class EmpresaController {
    private final EmpresaView vista;
    private final Usuario usuario;
    private final CrearEmpresaUseCase crear;
    private final ActualizarEmpresaUseCase actualizar;
    private final ConsultarEmpresasUseCase consultar;

    public EmpresaController(EmpresaView vista, Usuario usuario,
            CrearEmpresaUseCase crear, ActualizarEmpresaUseCase actualizar,
            ConsultarEmpresasUseCase consultar) {
        this.vista = vista;
        this.usuario = usuario;
        this.crear = crear;
        this.actualizar = actualizar;
        this.consultar = consultar;
        vista.alCrear(this::crear);
        vista.alActualizar(this::actualizar);
        vista.alBuscar(this::buscar);
        vista.permitirCrear(usuario.getRol().tienePermiso(PermisosSistema.CREAR_EMPRESA));
        vista.permitirActualizar(usuario.getRol().tienePermiso(PermisosSistema.ACTUALIZAR_EMPRESA));
    }

    public void cargar() {
        EjecutorTareasUi.ejecutar(() -> consultar.ejecutar(usuario),
                vista::mostrarEmpresas, this::mostrarError);
    }

    private void buscar() {
        String texto = vista.getTextoBusqueda();
        EjecutorTareasUi.ejecutar(
                () -> consultar.buscarPorNombre(usuario, texto),
                vista::mostrarEmpresas, this::mostrarError);
    }

    private void crear() {
        SolicitudEmpresa solicitud = vista.getSolicitud();
        EjecutorTareasUi.ejecutar(() -> crear.ejecutar(usuario, solicitud), empresa -> {
            vista.mostrarInformacion("Empresa registrada correctamente");
            cargar();
        }, this::mostrarError);
    }

    private void actualizar() {
        Empresa empresa = vista.getSeleccionada();
        if (empresa == null) {
            vista.mostrarError("Seleccione una empresa para editar");
            return;
        }
        SolicitudEmpresa solicitud = vista.getSolicitud();
        EjecutorTareasUi.ejecutar(
                () -> actualizar.ejecutar(usuario, empresa.getId(), solicitud),
                editada -> {
                    vista.mostrarInformacion("Empresa actualizada correctamente");
                    cargar();
                }, this::mostrarError);
    }

    private void mostrarError(Throwable error) {
        vista.mostrarError(error.getMessage() == null
                ? "No fue posible completar la operacion" : error.getMessage());
    }
}
