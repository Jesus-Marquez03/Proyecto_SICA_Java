package com.proyecto.sica.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProtectorPasswordTest {

    private final ProtectorPassword protector = new ProtectorPassword();

    @Test
    void aceptaPasswordCorrectoYRechazaIncorrecto() {
        String hash = protector.generarHash("ClaveSegura2026!".toCharArray());

        assertTrue(protector.verificar("ClaveSegura2026!".toCharArray(), hash));
        assertFalse(protector.verificar("ClaveIncorrecta".toCharArray(), hash));
        assertTrue(hash.startsWith("pbkdf2-sha256$600000$"));
    }

    @Test
    void generaSalesDiferentes() {
        String primero = protector.generarHash("MismaClave2026!".toCharArray());
        String segundo = protector.generarHash("MismaClave2026!".toCharArray());

        assertNotEquals(primero, segundo);
    }
}
