package com.proyecto.sica;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import com.proyecto.sica.config.ConfiguracionBaseDatos;
import com.proyecto.sica.config.ConfiguracionAplicacion;
import com.proyecto.sica.infrastructure.database.ComprobadorConexion;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import com.proyecto.sica.infrastructure.ui.SicaApplication;
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
        if (args.length == 1 && "--inicializar-demo".equals(args[0])) {
            inicializarUsuariosDemo();
            return;
        }
        if (args.length == 0 || (args.length == 1 && "--javafx".equals(args[0]))) {
            SicaApplication.iniciar(args);
            return;
        }

        System.out.println("SICA - usuarios y seguridad");
        System.out.println("Use --verificar-conexion para comprobar MySQL.");
        System.out.println("Use --inicializar-demo para crear los usuarios academicos.");
        System.out.println("Use --javafx o ningun argumento para abrir la interfaz.");
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
        } finally {
            // Detiene el hilo auxiliar del driver al finalizar esta utilidad de consola.
            AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }

    private static void inicializarUsuariosDemo() {
        try {
            ConfiguracionAplicacion configuracion = new ConfiguracionAplicacion();
            configuracion.getInicializadorUsuariosDemo().ejecutar()
                    .forEach(System.out::println);
            configuracion.getInicializadorVisitasDemo().ejecutar()
                    .forEach(System.out::println);
            configuracion.getInicializadorIncidentesDemo().ejecutar()
                    .forEach(System.out::println);
        } catch (RuntimeException excepcion) {
            System.err.println("No fue posible inicializar usuarios demo: " + excepcion.getMessage());
            System.exit(1);
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }
    }
}
