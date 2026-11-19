package com.proyecto.sica.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ConfiguracionBaseDatosTest {

    @Test
    void construyeUrlConPropiedades() {
        Properties propiedades = new Properties();
        propiedades.setProperty("db.host", "servidor-local");
        propiedades.setProperty("db.port", "3308");
        propiedades.setProperty("db.name", "sica_pruebas");
        propiedades.setProperty("db.user", "usuario_pruebas");

        ConfiguracionBaseDatos configuracion = ConfiguracionBaseDatos.crear(
                propiedades, Map.of());

        assertEquals("jdbc:mysql://servidor-local:3308/sica_pruebas", configuracion.getUrl());
        assertEquals("usuario_pruebas", configuracion.getUsuario());
    }

    @Test
    void priorizaVariablesDeEntorno() {
        Properties propiedades = new Properties();
        propiedades.setProperty("db.user", "usuario_archivo");

        Map<String, String> entorno = Map.of(
                "SICA_DB_URL", "jdbc:mysql://mysql:3306/sica_db",
                "SICA_DB_USER", "usuario_entorno",
                "SICA_DB_PASSWORD", "valor-solo-para-prueba");

        ConfiguracionBaseDatos configuracion = ConfiguracionBaseDatos.crear(
                propiedades, entorno);

        assertEquals("jdbc:mysql://mysql:3306/sica_db", configuracion.getUrl());
        assertEquals("usuario_entorno", configuracion.getUsuario());
        assertEquals("valor-solo-para-prueba", configuracion.getPassword());
    }
}
