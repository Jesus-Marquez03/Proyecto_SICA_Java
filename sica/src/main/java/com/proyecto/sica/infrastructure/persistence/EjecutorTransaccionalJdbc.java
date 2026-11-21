package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

/**
 * Reutiliza el manejo de commit, rollback y auditoria en adaptadores JDBC.
 */
public final class EjecutorTransaccionalJdbc {

    private final ConnectionFactory connectionFactory;
    private final AuditoriaJdbcRepository auditoriaRepository;

    public EjecutorTransaccionalJdbc(ConnectionFactory connectionFactory,
            AuditoriaJdbcRepository auditoriaRepository) {
        this.connectionFactory = connectionFactory;
        this.auditoriaRepository = auditoriaRepository;
    }

    public <T> T ejecutar(String mensajeError, OperacionJdbc<T> operacion,
            Function<T, EventoAuditoria> crearEvento) {
        return ejecutarConAuditorias(mensajeError, operacion,
                resultado -> List.of(crearEvento.apply(resultado)));
    }

    public <T> T ejecutarConAuditorias(String mensajeError, OperacionJdbc<T> operacion,
            Function<T, List<EventoAuditoria>> crearEventos) {
        try (Connection conexion = connectionFactory.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                for (EventoAuditoria evento : crearEventos.apply(resultado)) {
                    auditoriaRepository.registrar(conexion, evento);
                }
                conexion.commit();
                return resultado;
            } catch (SQLException excepcion) {
                rollback(conexion, excepcion);
                throw excepcion;
            } catch (RuntimeException excepcion) {
                rollback(conexion, excepcion);
                throw excepcion;
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException(mensajeError, excepcion);
        }
    }

    private void rollback(Connection conexion, Throwable original) {
        try {
            conexion.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    @FunctionalInterface
    public interface OperacionJdbc<T> {
        T ejecutar(Connection conexion) throws SQLException;
    }
}
