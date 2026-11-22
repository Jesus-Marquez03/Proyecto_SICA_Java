package com.proyecto.sica.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.sica.application.usecase.ResultadoReportes;
import com.proyecto.sica.config.ConfiguracionAplicacion;
import com.proyecto.sica.config.ConfiguracionBaseDatos;
import com.proyecto.sica.domain.entity.ResumenDashboard;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import com.proyecto.sica.infrastructure.persistence.ReporteJdbcRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Comprueba agregaciones de reportes contra el MySQL de desarrollo. */
class ReportesMysqlIT {

    @Test
    void coincideConLosConteosSqlYGeneraDashboard() throws Exception {
        ConfiguracionAplicacion aplicacion = new ConfiguracionAplicacion();
        aplicacion.getInicializadorUsuariosDemo().ejecutar();
        Usuario supervisor = aplicacion.getServicioAutenticacion().iniciarSesion(
                "supervisor@sica.example", "SicaSupervisor2026!".toCharArray());
        LocalDate desde = LocalDate.now().minusMonths(2);
        LocalDate hasta = LocalDate.now();

        ResultadoReportes reportes = aplicacion.getConsultarReportesUseCase()
                .ejecutar(supervisor, desde, hasta);
        ResumenDashboard dashboard = aplicacion.getConsultarResumenDashboardUseCase()
                .ejecutar(supervisor);

        assertEquals(contarIngresos(desde, hasta), reportes.accesos().ingresos());
        assertEquals(7, dashboard.accesosUltimosSieteDias().size());
        assertTrue(dashboard.progresoFinalizadas() >= 0);
        assertTrue(dashboard.progresoFinalizadas() <= 100);
        assertFalse(reportes.accesos().ingresosPorDia().isEmpty());

        Usuario funcionario = new ConfiguracionAplicacion().getServicioAutenticacion()
                .iniciarSesion("funcionario@sica.example", "SicaFuncionario2026!".toCharArray());
        ReporteJdbcRepository repositorio = new ReporteJdbcRepository(
                new ConnectionFactory(ConfiguracionBaseDatos.cargar()));
        assertEquals(contarIncidentesEmpresa(desde, hasta, funcionario.getEmpresaId()),
                repositorio.consultarIncidentes(
                        desde, hasta, funcionario.getEmpresaId()).total());
    }

    private long contarIngresos(LocalDate desde, LocalDate hasta) throws Exception {
        String sql = "SELECT COUNT(*) FROM visitas WHERE fecha_ingreso>=? AND fecha_ingreso<?";
        ConnectionFactory factory = new ConnectionFactory(ConfiguracionBaseDatos.cargar());
        try (Connection conexion = factory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setObject(1, desde.atStartOfDay());
            sentencia.setObject(2, hasta.plusDays(1).atStartOfDay());
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getLong(1);
            }
        }
    }

    private long contarIncidentesEmpresa(LocalDate desde, LocalDate hasta,
            long empresaId) throws Exception {
        String sql = """
                SELECT COUNT(*) FROM incidentes i
                JOIN visitas v ON v.id=i.visita_id
                JOIN usuarios u ON u.id=v.anfitrion_usuario_id
                WHERE i.fecha_incidente>=? AND i.fecha_incidente<? AND u.empresa_id=?
                """;
        ConnectionFactory factory = new ConnectionFactory(ConfiguracionBaseDatos.cargar());
        try (Connection conexion = factory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setObject(1, desde.atStartOfDay());
            sentencia.setObject(2, hasta.plusDays(1).atStartOfDay());
            sentencia.setLong(3, empresaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getLong(1);
            }
        }
    }
}
