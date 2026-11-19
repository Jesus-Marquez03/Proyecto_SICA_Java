package com.proyecto.sica;

import com.proyecto.sica.config.ConfiguracionBaseDatos;
import com.proyecto.sica.infrastructure.database.ComprobadorConexion;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.SQLException;

/**
 * Punto de entrada unico de SICA.
 */
public final class Main {

    private Main() {
        // Evita crear instancias de la clase de inicio.
    }

    public static void main(String[] args) {
        if (args.length == 1 && "--verificar-conexion".equals(args[0])) {
            verificarConexion();
            return;
        }

        System.out.println("SICA - configuracion inicial");
        System.out.println("Use --verificar-conexion para comprobar MySQL.");
    }

    private static void verificarConexion() {
        try {
            ConfiguracionBaseDatos configuracion = ConfiguracionBaseDatos.cargar();
            ConnectionFactory connectionFactory = new ConnectionFactory(configuracion);
            ComprobadorConexion comprobador = new ComprobadorConexion(connectionFactory);
            System.out.println(comprobador.verificar());
        } catch (IllegalStateException | SQLException excepcion) {
            System.err.println("No fue posible conectar con MySQL: " + excepcion.getMessage());
            System.exit(1);
        }
    }
}
