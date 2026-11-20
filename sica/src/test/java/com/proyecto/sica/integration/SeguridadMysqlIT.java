package com.proyecto.sica.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.sica.application.service.AccesoDenegadoException;
import com.proyecto.sica.application.service.AutenticacionException;
import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutenticacion;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.config.ConfiguracionAplicacion;
import com.proyecto.sica.config.ConfiguracionBaseDatos;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;

/**
 * Verificacion de integracion invocada expresamente contra MySQL local.
 */
class SeguridadMysqlIT {

    @Test
    void verificaLoginRbacYAuditoriaEnMySql() throws Exception {
        ConfiguracionAplicacion configuracion = new ConfiguracionAplicacion();
        configuracion.getInicializadorUsuariosDemo().ejecutar();
        ServicioAutenticacion autenticacion = configuracion.getServicioAutenticacion();
        ServicioAutorizacion autorizacion = new ServicioAutorizacion();

        Usuario superusuario = autenticacion.iniciarSesion(
                "superusuario@sica.example", "SicaSuper2026!".toCharArray());
        assertDoesNotThrow(() -> autorizacion.verificarPermiso(
                superusuario, PermisosSistema.GESTIONAR_USUARIOS));
        autenticacion.cerrarSesion();

        assertThrows(AutenticacionException.class, () -> autenticacion.iniciarSesion(
                "guarda@sica.example", "ClaveIncorrecta".toCharArray()));
        Usuario guarda = autenticacion.iniciarSesion(
                "guarda@sica.example", "SicaGuarda2026!".toCharArray());
        assertThrows(AccesoDenegadoException.class, () -> autorizacion.verificarPermiso(
                guarda, PermisosSistema.GESTIONAR_USUARIOS));
        autenticacion.cerrarSesion();

        assertTrue(contarEventosLogin() >= 3);
    }

    private long contarEventosLogin() throws Exception {
        ConnectionFactory factory = new ConnectionFactory(ConfiguracionBaseDatos.cargar());
        try (Connection conexion = factory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        "SELECT COUNT(*) FROM bitacora_auditoria WHERE accion = ?")) {
            sentencia.setString(1, "LOGIN");
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getLong(1);
            }
        }
    }
}
