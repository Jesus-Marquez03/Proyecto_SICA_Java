package com.proyecto.sica.infrastructure.ui;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/** Ejecuta JDBC fuera del hilo grafico y entrega el resultado en JavaFX. */
public final class EjecutorTareasUi {
    private EjecutorTareasUi() {
    }

    public static <T> void ejecutar(Callable<T> operacion,
            Consumer<T> alCompletar, Consumer<Throwable> alFallar) {
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operacion.call();
            }
        };
        tarea.setOnSucceeded(evento -> alCompletar.accept(tarea.getValue()));
        tarea.setOnFailed(evento -> alFallar.accept(tarea.getException()));
        Thread hilo = new Thread(tarea, "sica-tarea-jdbc");
        hilo.setDaemon(true);
        hilo.start();
    }
}
