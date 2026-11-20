package com.proyecto.sica.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.sica.application.service.AccesoDenegadoException;
import com.proyecto.sica.config.ConfiguracionAplicacion;
import com.proyecto.sica.config.ConfiguracionBaseDatos;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;

/** Prueba explicita de relaciones, permisos y auditoria contra MySQL. */
class PersonasEmpresasMysqlIT {

    @Test
    void verificaRelacionesCambioEstadoYPermisos() throws Exception {
        ConfiguracionAplicacion configuracion = new ConfiguracionAplicacion();
        configuracion.getInicializadorUsuariosDemo().ejecutar();
        Usuario supervisor = configuracion.getServicioAutenticacion().iniciarSesion(
                "supervisor@sica.example", "SicaSupervisor2026!".toCharArray());

        Persona persona = configuracion.getBuscarPersonaPorDocumentoUseCase()
                .ejecutar(supervisor, "CC", "10000002");
        assertNotNull(persona.getEmpresa());
        assertEquals("PROHIBICION_INGRESO", persona.getEstadoAcceso().getCodigo());
        assertTrue(configuracion.getConsultarEmpresasUseCase()
                .buscarPorNombre(supervisor, "Acme").size() >= 1);

        int estadoOriginal = persona.getEstadoAcceso().getId();
        long auditoriasAntes = contarAuditorias(persona.getId());
        try {
            assertTrue(configuracion.getCambiarEstadoAccesoUseCase()
                    .ejecutar(supervisor, persona.getId(), 5));
            Persona actualizada = configuracion.getConsultarPersonaPorIdUseCase()
                    .ejecutar(supervisor, persona.getId());
            assertEquals("ACTIVO", actualizada.getEstadoAcceso().getCodigo());
            assertTrue(contarAuditorias(persona.getId()) > auditoriasAntes);
        } finally {
            configuracion.getCambiarEstadoAccesoUseCase()
                    .ejecutar(supervisor, persona.getId(), estadoOriginal);
            configuracion.getServicioAutenticacion().cerrarSesion();
        }

        Usuario guarda = configuracion.getServicioAutenticacion().iniciarSesion(
                "guarda@sica.example", "SicaGuarda2026!".toCharArray());
        assertThrows(AccesoDenegadoException.class,
                () -> configuracion.getCambiarEstadoAccesoUseCase()
                        .ejecutar(guarda, persona.getId(), 5));
        configuracion.getServicioAutenticacion().cerrarSesion();
    }

    private long contarAuditorias(long personaId) throws Exception {
        ConnectionFactory factory = new ConnectionFactory(ConfiguracionBaseDatos.cargar());
        String sql = "SELECT COUNT(*) FROM bitacora_auditoria "
                + "WHERE accion = 'CAMBIAR_ESTADO_ACCESO' AND entidad_id = ?";
        try (Connection conexion = factory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, String.valueOf(personaId));
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getLong(1);
            }
        }
    }
}
