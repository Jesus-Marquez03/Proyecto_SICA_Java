package com.proyecto.sica.infrastructure.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ActualizadorPeriodicoTest {

    @Test
    void dejaDeConsultarAlCerrarSesion() throws Exception {
        AtomicInteger consultas = new AtomicInteger();
        CountDownLatch primeraConsulta = new CountDownLatch(1);
        ActualizadorPeriodico<Integer> polling = new ActualizadorPeriodico<>(() -> {
            primeraConsulta.countDown();
            return consultas.incrementAndGet();
        }, resultado -> { }, error -> { }, Duration.ofSeconds(1), Runnable::run);

        polling.iniciar();
        assertTrue(primeraConsulta.await(2, TimeUnit.SECONDS));
        polling.close();
        int consultasAlCerrar = consultas.get();
        Thread.sleep(1200);

        assertFalse(polling.isActivo());
        assertEquals(consultasAlCerrar, consultas.get());
    }
}
