package com.proyecto.sica.infrastructure.ui;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.application.Platform;

/** Polling controlado: evita solapamientos y se detiene al cerrar la sesion. */
public final class ActualizadorPeriodico<T> implements AutoCloseable {
    private final Callable<T> consulta;
    private final Consumer<T> alActualizar;
    private final Consumer<Throwable> alFallar;
    private final Consumer<Runnable> despachadorUi;
    private final long intervaloSegundos;
    private final AtomicBoolean consultaEnCurso = new AtomicBoolean();
    private final AtomicBoolean activo = new AtomicBoolean();
    private ScheduledExecutorService ejecutor;

    public ActualizadorPeriodico(Callable<T> consulta, Consumer<T> alActualizar,
            Consumer<Throwable> alFallar, Duration intervalo) {
        this(consulta, alActualizar, alFallar, intervalo, Platform::runLater);
    }

    /** Constructor con despachador inyectable para probar sin iniciar JavaFX. */
    public ActualizadorPeriodico(Callable<T> consulta, Consumer<T> alActualizar,
            Consumer<Throwable> alFallar, Duration intervalo,
            Consumer<Runnable> despachadorUi) {
        this.consulta = consulta;
        this.alActualizar = alActualizar;
        this.alFallar = alFallar;
        this.intervaloSegundos = Math.max(1, intervalo.toSeconds());
        this.despachadorUi = despachadorUi;
    }

    public synchronized void iniciar() {
        if (!activo.compareAndSet(false, true)) return;
        ejecutor = Executors.newSingleThreadScheduledExecutor(tarea -> {
            Thread hilo = new Thread(tarea, "sica-polling-visitas");
            hilo.setDaemon(true);
            return hilo;
        });
        ejecutor.scheduleWithFixedDelay(this::consultar,
                0, intervaloSegundos, TimeUnit.SECONDS);
    }

    private void consultar() {
        if (!activo.get() || !consultaEnCurso.compareAndSet(false, true)) return;
        try {
            T resultado = consulta.call();
            if (activo.get()) despachadorUi.accept(() -> alActualizar.accept(resultado));
        } catch (Throwable error) {
            if (activo.get()) despachadorUi.accept(() -> alFallar.accept(error));
        } finally {
            consultaEnCurso.set(false);
        }
    }

    public boolean isActivo() { return activo.get(); }

    @Override
    public synchronized void close() {
        activo.set(false);
        if (ejecutor != null) {
            ejecutor.shutdownNow();
            ejecutor = null;
        }
    }
}
