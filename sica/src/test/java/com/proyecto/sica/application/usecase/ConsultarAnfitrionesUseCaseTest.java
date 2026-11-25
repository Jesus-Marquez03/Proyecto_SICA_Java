package com.proyecto.sica.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.Permiso;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.support.RepositoriosMemoria.AuditoriaMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.UsuariosMemoria;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ConsultarAnfitrionesUseCaseTest {

    @Test
    void devuelveSoloFuncionariosActivosDeLaEmpresaConPermisoDeAprobar() {
        UsuariosMemoria repositorio = new UsuariosMemoria(new AuditoriaMemoria());
        Rol funcionario = rol(4, true, PermisosSistema.APROBAR_SOLICITUD);
        Rol empresarialSinAprobar = rol(5, true, PermisosSistema.CONSULTAR_VISITA);
        repositorio.agregar(usuario(10, funcionario, 2L, true));
        repositorio.agregar(usuario(11, funcionario, 3L, true));
        repositorio.agregar(usuario(12, funcionario, 2L, false));
        repositorio.agregar(usuario(13, empresarialSinAprobar, 2L, true));

        Usuario actor = usuario(1, rol(1, false, PermisosSistema.CONSULTAR_VISITA),
                null, true);
        ConsultarAnfitrionesUseCase casoUso = new ConsultarAnfitrionesUseCase(
                repositorio, new ServicioAutorizacion());

        assertEquals(java.util.List.of(10L), casoUso.ejecutar(actor, 2L).stream()
                .map(Usuario::getId).toList());
    }

    private Rol rol(int id, boolean requiereEmpresa, String... permisos) {
        int permisoId = 1;
        java.util.Set<Permiso> asignados = new java.util.HashSet<>();
        for (String codigo : permisos) {
            asignados.add(new Permiso(permisoId++, codigo, codigo, "Prueba"));
        }
        return new Rol(id, "Rol " + id, "Prueba", true, requiereEmpresa,
                Set.copyOf(asignados));
    }

    private Usuario usuario(long id, Rol rol, Long empresaId, boolean activo) {
        return new Usuario(id, rol, empresaId,
                empresaId == null ? null : "Empresa " + empresaId,
                "usuario" + id, "hash", "Nombre", String.valueOf(id),
                "usuario" + id + "@example.test", activo, null);
    }
}
