package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.repository.AuditoriaRepository;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Adaptador JDBC para la bitacora de auditoria.
 */
public final class AuditoriaJdbcRepository implements AuditoriaRepository {

    private static final String INSERTAR = """
            INSERT INTO bitacora_auditoria
                (usuario_id, nombre_usuario_intentado, accion, entidad,
                 entidad_id, resultado, descripcion)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final ConnectionFactory connectionFactory;

    public AuditoriaJdbcRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void registrar(EventoAuditoria evento) {
        try (Connection conexion = connectionFactory.obtenerConexion()) {
            registrar(conexion, evento);
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible registrar la auditoria", excepcion);
        }
    }

    void registrar(Connection conexion, EventoAuditoria evento) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
            sentencia.setObject(1, evento.getUsuarioId());
            sentencia.setString(2, evento.getIdentificadorIntentado());
            sentencia.setString(3, evento.getAccion());
            sentencia.setString(4, evento.getEntidad());
            sentencia.setString(5, evento.getEntidadId());
            sentencia.setString(6, evento.getResultado());
            sentencia.setString(7, evento.getDescripcion());
            sentencia.executeUpdate();
        }
    }
}
