package com.proyecto.sica.infrastructure.database;

import com.proyecto.sica.config.ConfiguracionBaseDatos;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Crea una conexion nueva cuando se solicita; no conserva conexiones compartidas.
 */
public final class ConnectionFactory {

    private final ConfiguracionBaseDatos configuracion;

    public ConnectionFactory(ConfiguracionBaseDatos configuracion) {
        this.configuracion = configuracion;
    }

    public Connection obtenerConexion() throws SQLException {
        if (configuracion.getPassword().isBlank()) {
            throw new IllegalStateException(
                    "Configure SICA_DB_PASSWORD antes de conectarse a MySQL");
        }

        return DriverManager.getConnection(
                configuracion.getUrl(),
                configuracion.getUsuario(),
                configuracion.getPassword());
    }
}
