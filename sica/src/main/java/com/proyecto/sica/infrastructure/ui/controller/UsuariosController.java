package com.proyecto.sica.infrastructure.ui.controller;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.usecase.ActualizarUsuarioUseCase;
import com.proyecto.sica.application.usecase.CambiarEstadoUsuarioUseCase;
import com.proyecto.sica.application.usecase.ConsultarEmpresasUseCase;
import com.proyecto.sica.application.usecase.ConsultarRolesUseCase;
import com.proyecto.sica.application.usecase.ConsultarUsuariosUseCase;
import com.proyecto.sica.application.usecase.CrearUsuarioUseCase;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.EjecutorTareasUi;
import com.proyecto.sica.infrastructure.ui.UsuariosView;
import java.util.List;

/** Controlador MVC de la administracion de usuarios. */
public final class UsuariosController {
    private final UsuariosView vista;
    private final Usuario actor;
    private final CrearUsuarioUseCase crear;
    private final ActualizarUsuarioUseCase actualizar;
    private final CambiarEstadoUsuarioUseCase cambiarEstado;
    private final ConsultarUsuariosUseCase consultar;
    private final ConsultarRolesUseCase consultarRoles;
    private final ConsultarEmpresasUseCase consultarEmpresas;

    public UsuariosController(UsuariosView vista, Usuario actor,
            CrearUsuarioUseCase crear, ActualizarUsuarioUseCase actualizar,
            CambiarEstadoUsuarioUseCase cambiarEstado, ConsultarUsuariosUseCase consultar,
            ConsultarRolesUseCase consultarRoles, ConsultarEmpresasUseCase consultarEmpresas) {
        this.vista = vista;
        this.actor = actor;
        this.crear = crear;
        this.actualizar = actualizar;
        this.cambiarEstado = cambiarEstado;
        this.consultar = consultar;
        this.consultarRoles = consultarRoles;
        this.consultarEmpresas = consultarEmpresas;
        vista.alCrear(this::crear);
        vista.alActualizar(this::actualizar);
        vista.alCambiarEstado(this::cambiarEstado);
        vista.permitir(tiene(PermisosSistema.CREAR_USUARIO),
                tiene(PermisosSistema.ACTUALIZAR_USUARIO),
                tiene(PermisosSistema.CAMBIAR_ESTADO_USUARIO));
    }

    public void cargar() {
        vista.cargando();
        EjecutorTareasUi.ejecutar(() -> new Datos(
                consultar.ejecutar(actor), consultarRoles.ejecutar(actor),
                consultarEmpresas.ejecutar(actor)),
                d -> vista.mostrarDatos(d.usuarios(), d.roles(), d.empresas()), this::error);
    }

    private void crear() {
        ejecutar(() -> crear.ejecutar(actor, vista.solicitudCrear()));
    }

    private void actualizar() {
        if (vista.seleccionado() == null) { vista.error("Selecciona un usuario"); return; }
        ejecutar(() -> actualizar.ejecutar(actor, vista.solicitudActualizar()));
    }

    private void cambiarEstado() {
        Usuario seleccionado = vista.seleccionado();
        if (seleccionado == null) { vista.error("Selecciona un usuario"); return; }
        ejecutar(() -> {
            cambiarEstado.ejecutar(actor, seleccionado.getId(), !seleccionado.isActivo());
            return seleccionado;
        });
    }

    private void ejecutar(java.util.concurrent.Callable<Usuario> operacion) {
        vista.cargando();
        EjecutorTareasUi.ejecutar(operacion, resultado -> cargar(), this::error);
    }

    private boolean tiene(String permiso) { return actor.getRol().tienePermiso(permiso); }
    private void error(Throwable error) {
        vista.error(error.getMessage() == null ? "No fue posible completar la operacion" : error.getMessage());
    }

    private record Datos(List<Usuario> usuarios, List<Rol> roles, List<Empresa> empresas) { }
}
