package com.proyecto.sica.infrastructure.ui.controller;

import static com.proyecto.sica.support.DatosPrueba.rolConPermisos;
import static com.proyecto.sica.support.DatosPrueba.rolSinPermisos;
import static com.proyecto.sica.support.DatosPrueba.usuario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.UsuariosView;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Verifica que el controlador solo confirme operaciones de usuarios exitosas. */
class UsuariosControllerNotificacionTest {

    @BeforeAll
    static void iniciarJavaFx() throws InterruptedException {
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                inicio.countDown();
            });
            inicio.await();
        } catch (IllegalStateException yaIniciado) {
            // El toolkit ya fue iniciado por otra prueba JavaFX.
            Platform.setImplicitExit(false);
        }
    }

    @Test
    void creacionExitosaGeneraUnaConfirmacion() throws Exception {
        probarExito("Usuario creado correctamente");
    }

    @Test
    void actualizacionExitosaGeneraUnaConfirmacion() throws Exception {
        probarExito("Usuario actualizado correctamente");
    }

    @Test
    void errorDeOperacionNoGeneraConfirmacion() throws Exception {
        Contexto contexto = crearContexto(true);
        CountDownLatch ejecutada = new CountDownLatch(1);
        invocar(contexto.controlador(), () -> {
            ejecutada.countDown();
            throw new IllegalArgumentException("Datos invalidos");
        }, "Usuario creado correctamente");

        assertTrue(ejecutada.await(1, TimeUnit.SECONDS));
        assertFalse(contexto.notificada().await(350, TimeUnit.MILLISECONDS));
        assertEquals(0, contexto.cantidad().get());
    }

    @Test
    void denegacionRbacNoGeneraConfirmacion() throws Exception {
        Contexto contexto = crearContexto(false);
        CountDownLatch ejecutada = new CountDownLatch(1);
        invocar(contexto.controlador(), () -> {
            ejecutada.countDown();
            new ServicioAutorizacion().verificarPermiso(
                    contexto.actor(), PermisosSistema.CREAR_USUARIO);
            return contexto.actor();
        }, "Usuario creado correctamente");

        assertTrue(ejecutada.await(1, TimeUnit.SECONDS));
        assertFalse(contexto.notificada().await(350, TimeUnit.MILLISECONDS));
        assertEquals(0, contexto.cantidad().get());
    }

    private void probarExito(String mensaje) throws Exception {
        Contexto contexto = crearContexto(true);
        invocar(contexto.controlador(), () -> contexto.actor(), mensaje);

        assertTrue(contexto.notificada().await(1, TimeUnit.SECONDS));
        assertEquals(1, contexto.cantidad().get());
        assertEquals(mensaje, contexto.mensaje().get());
    }

    private Contexto crearContexto(boolean autorizado) throws Exception {
        FutureTask<Contexto> tarea = new FutureTask<>(() -> {
            Usuario actor = usuario(1,
                    autorizado
                            ? rolConPermisos(PermisosSistema.CREAR_USUARIO,
                                    PermisosSistema.ACTUALIZAR_USUARIO,
                                    PermisosSistema.CAMBIAR_ESTADO_USUARIO)
                            : rolSinPermisos(),
                    "actor@sica.example", "hash", true);
            UsuariosView vista = new UsuariosView();
            CountDownLatch notificada = new CountDownLatch(1);
            AtomicInteger cantidad = new AtomicInteger();
            AtomicReference<String> mensaje = new AtomicReference<>();
            vista.alConfirmar(texto -> {
                mensaje.set(texto);
                cantidad.incrementAndGet();
                notificada.countDown();
            });
            UsuariosController controlador = new UsuariosController(vista, actor,
                    null, null, null, null, null, null);
            return new Contexto(controlador, actor, notificada, cantidad, mensaje);
        });
        Platform.runLater(tarea);
        return tarea.get(1, TimeUnit.SECONDS);
    }

    private void invocar(UsuariosController controlador, Callable<Usuario> operacion,
            String mensaje) throws Exception {
        Method ejecutar = UsuariosController.class.getDeclaredMethod(
                "ejecutar", Callable.class, String.class);
        ejecutar.setAccessible(true);
        FutureTask<Void> tarea = new FutureTask<>(() -> {
            ejecutar.invoke(controlador, operacion, mensaje);
            return null;
        });
        Platform.runLater(tarea);
        tarea.get(1, TimeUnit.SECONDS);
    }

    private record Contexto(UsuariosController controlador, Usuario actor,
            CountDownLatch notificada, AtomicInteger cantidad,
            AtomicReference<String> mensaje) { }
}
