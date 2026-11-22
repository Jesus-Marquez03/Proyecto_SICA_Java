package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.EstadoIncidente;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.GravedadIncidente;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.ConflictoPersistenciaException;
import com.proyecto.sica.domain.repository.FiltroIncidentes;
import com.proyecto.sica.domain.repository.IncidenteRepository;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.domain.repository.UsuarioRepository;
import com.proyecto.sica.domain.repository.VisitaRepository;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptador JDBC de incidentes con auditoria transaccional. */
public final class IncidenteJdbcRepository implements IncidenteRepository {
    private static final String COLUMNAS = "i.id,i.visita_id,i.persona_id,"
            + "i.reportado_por_usuario_id,i.tipo,i.gravedad,i.estado,i.descripcion,"
            + "i.acciones_tomadas,i.fecha_incidente,i.fecha_reporte,i.fecha_cierre";
    private static final String BASE = "SELECT " + COLUMNAS + " FROM incidentes i ";

    private final ConnectionFactory connectionFactory;
    private final VisitaRepository visitaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EjecutorTransaccionalJdbc transacciones;

    public IncidenteJdbcRepository(ConnectionFactory connectionFactory,
            VisitaRepository visitaRepository, UsuarioRepository usuarioRepository,
            EjecutorTransaccionalJdbc transacciones) {
        this.connectionFactory = connectionFactory;
        this.visitaRepository = visitaRepository;
        this.usuarioRepository = usuarioRepository;
        this.transacciones = transacciones;
    }

    @Override
    public Optional<Incidente> buscarPorId(long id) {
        List<Incidente> lista = consultarSql(BASE + " WHERE i.id=?",
                sentencia -> sentencia.setLong(1, id));
        return lista.isEmpty() ? Optional.empty() : Optional.of(lista.get(0));
    }

    @Override
    public List<Incidente> consultar(FiltroIncidentes filtro, Long empresaId) {
        StringBuilder sql = new StringBuilder(BASE)
                .append(" JOIN personas p ON p.id=i.persona_id WHERE 1=1");
        List<Object> parametros = new ArrayList<>();
        agregar(sql, parametros, " AND i.visita_id=?", filtro.visitaId());
        agregar(sql, parametros, " AND i.persona_id=?", filtro.personaId());
        agregar(sql, parametros, " AND i.reportado_por_usuario_id=?", filtro.reportadoPorId());
        if (filtro.desde() != null) {
            sql.append(" AND i.fecha_incidente>=?");
            parametros.add(filtro.desde().atStartOfDay());
        }
        if (filtro.hasta() != null) {
            sql.append(" AND i.fecha_incidente<?");
            parametros.add(filtro.hasta().plusDays(1).atStartOfDay());
        }
        if (filtro.estado() != null && !filtro.estado().isBlank()) {
            sql.append(" AND i.estado=?");
            parametros.add(filtro.estado().trim());
        }
        agregar(sql, parametros, " AND p.empresa_id=?", empresaId);
        sql.append(" ORDER BY i.fecha_incidente DESC,i.id DESC");
        return consultarSql(sql.toString(), sentencia -> asignarParametros(sentencia, parametros));
    }

    @Override
    public Incidente crearConAuditoria(Incidente incidente, EventoAuditoria evento) {
        long id = transacciones.ejecutar("No fue posible registrar el incidente", conexion -> {
            String sql = """
                    INSERT INTO incidentes (visita_id,persona_id,reportado_por_usuario_id,
                        tipo,gravedad,estado,descripcion,acciones_tomadas,fecha_incidente,
                        fecha_reporte,fecha_cierre)
                    VALUES (?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,?)
                    """;
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
                asignar(sentencia, incidente, false);
                sentencia.executeUpdate();
                try (ResultSet claves = sentencia.getGeneratedKeys()) {
                    if (!claves.next()) throw new SQLException("MySQL no devolvio el ID");
                    return claves.getLong(1);
                }
            }
        }, creadoId -> evento.conEntidadId(String.valueOf(creadoId)));
        return recuperar(id);
    }

    @Override
    public Incidente actualizarConAuditoria(Incidente anterior, Incidente actualizado,
            EventoAuditoria evento) {
        transacciones.ejecutar("No fue posible actualizar el incidente", conexion -> {
            try (PreparedStatement bloqueo = conexion.prepareStatement(
                    "SELECT estado,descripcion FROM incidentes WHERE id=? FOR UPDATE")) {
                bloqueo.setLong(1, anterior.getId());
                try (ResultSet fila = bloqueo.executeQuery()) {
                    if (!fila.next()) throw new ConflictoPersistenciaException("El incidente no existe");
                    if (!fila.getString("estado").equals(anterior.getEstado().name())
                            || !fila.getString("descripcion").equals(anterior.getDescripcion())) {
                        throw new ConflictoPersistenciaException(
                                "El incidente fue modificado por otra sesion");
                    }
                }
            }
            String sql = "UPDATE incidentes SET gravedad=?,estado=?,descripcion=?,"
                    + "acciones_tomadas=?,fecha_cierre=? WHERE id=?";
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setString(1, actualizado.getGravedad().name());
                sentencia.setString(2, actualizado.getEstado().name());
                sentencia.setString(3, actualizado.getDescripcion());
                sentencia.setString(4, actualizado.getAccionesTomadas());
                asignarFecha(sentencia, 5, actualizado.getFechaCierre());
                sentencia.setLong(6, actualizado.getId());
                if (sentencia.executeUpdate() != 1) throw new SQLException("Actualizacion incompleta");
            }
            return actualizado.getId();
        }, resultado -> evento);
        return recuperar(actualizado.getId());
    }

    @Override
    public boolean existeTipoEnVisita(String tipo, long visitaId) {
        String sql = "SELECT COUNT(*) FROM incidentes WHERE tipo=? AND visita_id=?";
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, tipo);
            sentencia.setLong(2, visitaId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible validar el incidente", excepcion);
        }
    }

    private Incidente recuperar(long id) {
        return buscarPorId(id).orElseThrow(
                () -> new RepositorioException("No fue posible recuperar el incidente"));
    }

    private List<Incidente> consultarSql(String sql, Configurador configurador) {
        List<Incidente> resultados = new ArrayList<>();
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            configurador.configurar(sentencia);
            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) resultados.add(mapear(filas));
            }
            return resultados;
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar incidentes", excepcion);
        }
    }

    private Incidente mapear(ResultSet fila) throws SQLException {
        Visita visita = visitaRepository.buscarPorId(fila.getLong("visita_id"))
                .orElseThrow(() -> new SQLException("El incidente tiene una visita inexistente"));
        Usuario usuario = usuarioRepository.buscarPorId(fila.getLong("reportado_por_usuario_id"))
                .orElseThrow(() -> new SQLException("El incidente tiene un usuario inexistente"));
        return new Incidente(fila.getLong("id"), visita, visita.getVisitante(), usuario,
                fila.getString("tipo"), GravedadIncidente.valueOf(fila.getString("gravedad")),
                EstadoIncidente.valueOf(fila.getString("estado")),
                fila.getString("descripcion"), fila.getString("acciones_tomadas"),
                fecha(fila, "fecha_incidente"), fecha(fila, "fecha_reporte"),
                fecha(fila, "fecha_cierre"));
    }

    private void asignar(PreparedStatement s, Incidente i, boolean actualizar) throws SQLException {
        s.setLong(1, i.getVisita().getId());
        s.setLong(2, i.getPersona().getId());
        s.setLong(3, i.getReportadoPor().getId());
        s.setString(4, i.getTipo());
        s.setString(5, i.getGravedad().name());
        s.setString(6, i.getEstado().name());
        s.setString(7, i.getDescripcion());
        s.setString(8, i.getAccionesTomadas());
        asignarFecha(s, 9, i.getFechaIncidente());
        asignarFecha(s, 10, i.getFechaCierre());
    }

    private void agregar(StringBuilder sql, List<Object> parametros,
            String fragmento, Object valor) {
        if (valor != null) {
            sql.append(fragmento);
            parametros.add(valor);
        }
    }

    private void asignarParametros(PreparedStatement sentencia, List<Object> parametros)
            throws SQLException {
        for (int i = 0; i < parametros.size(); i++) {
            Object valor = parametros.get(i);
            if (valor instanceof LocalDateTime fecha) {
                sentencia.setTimestamp(i + 1, Timestamp.valueOf(fecha));
            } else {
                sentencia.setObject(i + 1, valor);
            }
        }
    }

    private void asignarFecha(PreparedStatement sentencia, int indice, LocalDateTime fecha)
            throws SQLException {
        if (fecha == null) sentencia.setNull(indice, Types.TIMESTAMP);
        else sentencia.setTimestamp(indice, Timestamp.valueOf(fecha));
    }

    private LocalDateTime fecha(ResultSet fila, String columna) throws SQLException {
        Timestamp valor = fila.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    @FunctionalInterface
    private interface Configurador {
        void configurar(PreparedStatement sentencia) throws SQLException;
    }
}
