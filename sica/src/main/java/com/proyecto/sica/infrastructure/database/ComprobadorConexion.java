package com.proyecto.sica.infrastructure.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Comprueba la conectividad sin mezclarla con casos de uso del negocio.
 */
public final class ComprobadorConexion {

    private static final String CONSULTA_PRUEBA = "SELECT 1";

    private final ConnectionFactory connectionFactory;

    public ComprobadorConexion(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String verificar() throws SQLException {
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(CONSULTA_PRUEBA);
                ResultSet resultado = sentencia.executeQuery()) {

            if (!resultado.next() || resultado.getInt(1) != 1) {
                throw new SQLException("MySQL no devolvio el resultado esperado");
            }

            return "Conexion exitosa con "
                    + conexion.getMetaData().getDatabaseProductName()
                    + " "
                    + conexion.getMetaData().getDatabaseProductVersion();
        }
    }
}
