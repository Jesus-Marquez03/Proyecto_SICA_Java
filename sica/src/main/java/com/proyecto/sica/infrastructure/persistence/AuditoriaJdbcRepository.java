package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.RegistroAuditoria;
import com.proyecto.sica.domain.repository.AuditoriaRepository;
import com.proyecto.sica.domain.repository.ConsultaAuditoriaRepository;
import com.proyecto.sica.domain.repository.FiltroAuditoria;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC para la bitacora de auditoria.
 */
public final class AuditoriaJdbcRepository
        implements AuditoriaRepository, ConsultaAuditoriaRepository {

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

    @Override
    public List<RegistroAuditoria> consultar(FiltroAuditoria filtro) {
        StringBuilder sql = new StringBuilder("""
                SELECT b.id,b.usuario_id,b.nombre_usuario_intentado,b.accion,b.entidad,
                    b.entidad_id,b.resultado,b.descripcion,b.ip_origen,b.fecha_evento,
                    CONCAT(u.nombres,' ',u.apellidos) AS nombre_usuario
                FROM bitacora_auditoria b
                LEFT JOIN usuarios u ON u.id=b.usuario_id
                WHERE 1=1
                """);
        List<Object> parametros = new ArrayList<>();
        agregar(sql, parametros, " AND b.usuario_id=?", filtro.usuarioId());
        if (filtro.accion() != null && !filtro.accion().isBlank()) {
            sql.append(" AND b.accion=?");
            parametros.add(filtro.accion().trim());
        }
        if (filtro.desde() != null) {
            sql.append(" AND b.fecha_evento>=?");
            parametros.add(filtro.desde().atStartOfDay());
        }
        if (filtro.hasta() != null) {
            sql.append(" AND b.fecha_evento<?");
            parametros.add(filtro.hasta().plusDays(1).atStartOfDay());
        }
        if (filtro.entidad() != null && !filtro.entidad().isBlank()) {
            sql.append(" AND b.entidad=?");
            parametros.add(filtro.entidad().trim());
        }
        if (filtro.entidadId() != null && !filtro.entidadId().isBlank()) {
            sql.append(" AND b.entidad_id=?");
            parametros.add(filtro.entidadId().trim());
        }
        if (filtro.visitaId() != null) {
            sql.append(" AND ((b.entidad='visitas' AND b.entidad_id=CAST(? AS CHAR))")
                    .append(" OR (b.entidad='incidentes' AND EXISTS (")
                    .append("SELECT 1 FROM incidentes i WHERE i.id=CAST(b.entidad_id AS UNSIGNED)")
                    .append(" AND i.visita_id=?)))");
            parametros.add(filtro.visitaId());
            parametros.add(filtro.visitaId());
        }
        if (filtro.personaId() != null) {
            sql.append(" AND ((b.entidad='personas' AND b.entidad_id=CAST(? AS CHAR))")
                    .append(" OR (b.entidad='visitas' AND EXISTS (")
                    .append("SELECT 1 FROM visitas v WHERE v.id=CAST(b.entidad_id AS UNSIGNED)")
                    .append(" AND v.visitante_persona_id=?))")
                    .append(" OR (b.entidad='incidentes' AND EXISTS (")
                    .append("SELECT 1 FROM incidentes i WHERE i.id=CAST(b.entidad_id AS UNSIGNED)")
                    .append(" AND i.persona_id=?)))");
            parametros.add(filtro.personaId());
            parametros.add(filtro.personaId());
            parametros.add(filtro.personaId());
        }
        sql.append(" ORDER BY b.fecha_evento DESC,b.id DESC LIMIT ?");
        parametros.add(filtro.limite());

        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                Object valor = parametros.get(i);
                if (valor instanceof LocalDateTime fecha) {
                    sentencia.setTimestamp(i + 1, Timestamp.valueOf(fecha));
                } else {
                    sentencia.setObject(i + 1, valor);
                }
            }
            List<RegistroAuditoria> registros = new ArrayList<>();
            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    long usuarioValor = filas.getLong("usuario_id");
                    Long usuario = filas.wasNull() ? null : usuarioValor;
                    registros.add(new RegistroAuditoria(filas.getLong("id"),
                            usuario, filas.getString("nombre_usuario"),
                            filas.getString("nombre_usuario_intentado"),
                            filas.getString("accion"), filas.getString("entidad"),
                            filas.getString("entidad_id"), filas.getString("resultado"),
                            filas.getString("descripcion"), filas.getString("ip_origen"),
                            filas.getTimestamp("fecha_evento").toLocalDateTime()));
                }
            }
            return registros;
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar la auditoria", excepcion);
        }
    }

    private void agregar(StringBuilder sql, List<Object> parametros,
            String fragmento, Object valor) {
        if (valor != null) {
            sql.append(fragmento);
            parametros.add(valor);
        }
    }
}
