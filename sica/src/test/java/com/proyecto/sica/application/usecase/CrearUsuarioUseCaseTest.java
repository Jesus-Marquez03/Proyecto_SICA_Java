package com.proyecto.sica.application.usecase;

import static com.proyecto.sica.support.DatosPrueba.rolConPermiso;
import static com.proyecto.sica.support.DatosPrueba.usuario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.proyecto.sica.application.service.ProtectorPassword;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorUsuario;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.support.RepositoriosMemoria.AuditoriaMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.EmpresasMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.RolesMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.UsuariosMemoria;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearUsuarioUseCaseTest {

    private AuditoriaMemoria auditoria;
    private UsuariosMemoria usuarios;
    private RolesMemoria roles;
    private EmpresasMemoria empresas;
    private CrearUsuarioUseCase casoUso;
    private Usuario actor;

    @BeforeEach
    void preparar() {
        auditoria = new AuditoriaMemoria();
        usuarios = new UsuariosMemoria(auditoria);
        roles = new RolesMemoria();
        empresas = new EmpresasMemoria();
        Rol administrador = rolConPermiso("CREAR_USUARIO");
        Rol funcionario = new Rol(4, "Funcionario de Empresa", "Prueba",
                true, true, Set.of());
        roles.agregar(administrador);
        roles.agregar(funcionario);
        empresas.agregar(2, "Empresa Demo");
        actor = usuario(99, administrador, "admin@sica.example", "hash", true);
        ValidadorUsuario validador = new ValidadorUsuario(usuarios, roles, empresas);
        casoUso = new CrearUsuarioUseCase(usuarios, new ServicioAutorizacion(),
                validador, new ProtectorPassword());
    }

    @Test
    void creaFuncionarioConEmpresaYAuditoria() {
        SolicitudCrearUsuario solicitud = new SolicitudCrearUsuario(
                "funcionario", "ClaveSegura2026!".toCharArray(), "Ana", "Prueba",
                "ANA@SICA.EXAMPLE", 4, 2L);

        Usuario creado = casoUso.ejecutar(actor, solicitud);

        assertEquals("ana@sica.example", creado.getCorreo());
        assertEquals("Empresa Demo", creado.getEmpresaNombre());
        assertEquals("CREAR_USUARIO", auditoria.getEventos().get(0).getAccion());
    }

    @Test
    void rechazaCorreoInvalido() {
        SolicitudCrearUsuario solicitud = new SolicitudCrearUsuario(
                "persona", "ClaveSegura2026!".toCharArray(), "Ana", "Prueba",
                "correo-invalido", 1, null);

        assertThrows(ValidacionException.class, () -> casoUso.ejecutar(actor, solicitud));
    }

    @Test
    void exigeEmpresaParaFuncionario() {
        SolicitudCrearUsuario solicitud = new SolicitudCrearUsuario(
                "funcionario", "ClaveSegura2026!".toCharArray(), "Ana", "Prueba",
                "ana@sica.example", 4, null);

        assertThrows(ValidacionException.class, () -> casoUso.ejecutar(actor, solicitud));
    }

    @Test
    void rechazaCorreoDuplicado() {
        usuarios.agregar(usuario(5, rolConPermiso("CREAR_USUARIO"),
                "repetido@sica.example", "hash", true));
        SolicitudCrearUsuario solicitud = new SolicitudCrearUsuario(
                "persona", "ClaveSegura2026!".toCharArray(), "Ana", "Prueba",
                "repetido@sica.example", 1, null);

        assertThrows(ValidacionException.class, () -> casoUso.ejecutar(actor, solicitud));
    }
}
