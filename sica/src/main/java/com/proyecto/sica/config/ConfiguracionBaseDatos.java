package com.proyecto.sica.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Lee la configuracion publica y permite sobrescribirla con variables de entorno.
 */
public final class ConfiguracionBaseDatos {

    private static final String ARCHIVO_PROPIEDADES = "/application.properties";

    private final String url;
    private final String usuario;
    private final String password;

    private ConfiguracionBaseDatos(String url, String usuario, String password) {
        this.url = url;
        this.usuario = usuario;
        this.password = password;
    }

    public static ConfiguracionBaseDatos cargar() {
        Properties propiedades = cargarPropiedades();
        return crear(propiedades, System.getenv());
    }

    static ConfiguracionBaseDatos crear(Properties propiedades, Map<String, String> entorno) {
        Objects.requireNonNull(propiedades, "Las propiedades son obligatorias");
        Objects.requireNonNull(entorno, "El entorno es obligatorio");

        String host = obtener(entorno, "SICA_DB_HOST", propiedades, "db.host", "localhost");
        String puerto = obtener(entorno, "SICA_DB_PORT", propiedades, "db.port", "3307");
        String baseDatos = obtener(entorno, "SICA_DB_NAME", propiedades, "db.name", "sica_db");
        String opciones = propiedades.getProperty("db.options", "").trim();
        String urlPredeterminada = construirUrl(host, puerto, baseDatos, opciones);

        String url = valorNoVacio(entorno.get("SICA_DB_URL"),
                propiedades.getProperty("db.url", urlPredeterminada));
        String usuario = obtener(entorno, "SICA_DB_USER", propiedades, "db.user", "sica_app");
        String password = valorNoVacio(entorno.get("SICA_DB_PASSWORD"),
                propiedades.getProperty("db.password", ""));

        return new ConfiguracionBaseDatos(url, usuario, password);
    }

    private static Properties cargarPropiedades() {
        Properties propiedades = new Properties();

        try (InputStream entrada = ConfiguracionBaseDatos.class.getResourceAsStream(ARCHIVO_PROPIEDADES)) {
            if (entrada == null) {
                throw new IllegalStateException("No se encontro " + ARCHIVO_PROPIEDADES);
            }
            propiedades.load(entrada);
            return propiedades;
        } catch (IOException excepcion) {
            throw new IllegalStateException("No fue posible leer la configuracion de base de datos", excepcion);
        }
    }

    private static String obtener(Map<String, String> entorno, String variable,
            Properties propiedades, String propiedad, String valorPredeterminado) {
        return valorNoVacio(entorno.get(variable),
                propiedades.getProperty(propiedad, valorPredeterminado));
    }

    private static String valorNoVacio(String preferido, String alternativo) {
        return preferido == null || preferido.isBlank() ? alternativo.trim() : preferido.trim();
    }

    private static String construirUrl(String host, String puerto, String baseDatos, String opciones) {
        String urlBase = "jdbc:mysql://" + host + ":" + puerto + "/" + baseDatos;
        return opciones.isBlank() ? urlBase : urlBase + "?" + opciones;
    }

    public String getUrl() {
        return url;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getPassword() {
        return password;
    }
}
