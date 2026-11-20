package com.proyecto.sica.application.service;

import static com.proyecto.sica.support.DatosPrueba.rolSinPermisos;
import static com.proyecto.sica.support.DatosPrueba.usuario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.support.RepositoriosMemoria.AuditoriaMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.UsuariosMemoria;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServicioAutenticacionTest {

    private static String hashValido;
    private AuditoriaMemoria auditoria;
    private UsuariosMemoria usuarios;
    private SesionUsuario sesion;
    private ServicioAutenticacion servicio;

    @BeforeAll
    static void crearHash() {
        hashValido = new ProtectorPassword().generarHash("ClaveSegura2026!".toCharArray());
    }

    @BeforeEach
    void preparar() {
        auditoria = new AuditoriaMemoria();
        usuarios = new UsuariosMemoria(auditoria);
        sesion = new SesionUsuario();
        servicio = new ServicioAutenticacion(
                usuarios, auditoria, new ProtectorPassword(), sesion);
    }

    @Test
    void iniciaSesionConCredencialesCorrectas() {
        Usuario esperado = usuario(1, rolSinPermisos(), "persona@sica.example", hashValido, true);
        usuarios.agregar(esperado);
        char[] password = "ClaveSegura2026!".toCharArray();

        Usuario autenticado = servicio.iniciarSesion("PERSONA@sica.example", password);

        assertEquals(esperado.getId(), autenticado.getId());
        assertTrue(sesion.estaIniciada());
        assertEquals("EXITOSO", auditoria.getEventos().get(0).getResultado());
        assertTrue(String.valueOf(password).chars().allMatch(caracter -> caracter == 0));
    }

    @Test
    void rechazaPasswordIncorrectoYAudita() {
        usuarios.agregar(usuario(1, rolSinPermisos(),
                "persona@sica.example", hashValido, true));

        assertThrows(AutenticacionException.class,
                () -> servicio.iniciarSesion("persona@sica.example",
                        "Incorrecta".toCharArray()));

        assertFalse(sesion.estaIniciada());
        assertEquals("FALLIDO", auditoria.getEventos().get(0).getResultado());
    }

    @Test
    void rechazaUsuarioInactivo() {
        usuarios.agregar(usuario(1, rolSinPermisos(),
                "inactivo@sica.example", hashValido, false));

        assertThrows(AutenticacionException.class,
                () -> servicio.iniciarSesion("inactivo@sica.example",
                        "ClaveSegura2026!".toCharArray()));
        assertEquals("Usuario inactivo", auditoria.getEventos().get(0).getDescripcion());
    }

    @Test
    void rechazaUsuarioInexistenteYConservaCorreoIntentado() {
        assertThrows(AutenticacionException.class,
                () -> servicio.iniciarSesion("nadie@sica.example", "CualquierClave".toCharArray()));

        assertEquals("nadie@sica.example",
                auditoria.getEventos().get(0).getIdentificadorIntentado());
    }
}
