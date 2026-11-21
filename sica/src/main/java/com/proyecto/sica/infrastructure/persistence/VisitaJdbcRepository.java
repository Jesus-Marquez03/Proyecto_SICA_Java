package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.ResultadoCheckIn;
import com.proyecto.sica.domain.entity.TipoSolicitudVisita;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.ConflictoPersistenciaException;
import com.proyecto.sica.domain.repository.PersonaRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adaptador JDBC con bloqueos cortos para las transiciones de visitas. */
public final class VisitaJdbcRepository implements VisitaRepository {
    private static final String COLUMNAS = """
            v.id,v.numero_solicitud,v.visitante_persona_id,v.anfitrion_usuario_id,
            v.estado_visita_id,ev.codigo AS estado_codigo,ev.nombre AS estado_nombre,
            ev.descripcion AS estado_descripcion,v.registrado_por_usuario_id,
            v.respondido_por_usuario_id,v.regularizada_por_usuario_id,v.tipo_solicitud,
            v.motivo,v.placa_vehiculo,v.fecha_solicitud,v.fecha_programada,
            v.fecha_respuesta,v.fecha_ingreso,v.fecha_salida,v.fecha_cierre_sistema,
            v.motivo_regularizacion,v.codigo_pase,v.fecha_vencimiento_pase,v.observaciones
            """;
    private static final String BASE = "SELECT " + COLUMNAS + " FROM visitas v "
            + "JOIN visita_estados ev ON ev.id=v.estado_visita_id ";

    private final ConnectionFactory connectionFactory;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EjecutorTransaccionalJdbc transacciones;

    public VisitaJdbcRepository(ConnectionFactory connectionFactory,
            PersonaRepository personaRepository, UsuarioRepository usuarioRepository,
            EjecutorTransaccionalJdbc transacciones) {
        this.connectionFactory = connectionFactory;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
        this.transacciones = transacciones;
    }

    @Override
    public Optional<Visita> buscarPorId(long id) {
        return buscarUno(BASE + " WHERE v.id=?", sentencia -> sentencia.setLong(1, id));
    }

    @Override
    public Optional<Visita> buscarPorNumero(String numeroSolicitud) {
        return buscarUno(BASE + " WHERE v.numero_solicitud=?",
                sentencia -> sentencia.setString(1, numeroSolicitud));
    }

    @Override
    public List<Visita> buscarPorDocumento(String tipoDocumento, String numeroDocumento) {
        String sql = BASE + " JOIN personas p ON p.id=v.visitante_persona_id "
                + "WHERE p.tipo_documento=? AND p.numero_documento=? "
                + "ORDER BY v.fecha_solicitud DESC";
        return buscarLista(sql, sentencia -> {
            sentencia.setString(1, tipoDocumento);
            sentencia.setString(2, numeroDocumento);
        });
    }

    @Override
    public List<Visita> listarPendientesPorAnfitrion(long anfitrionId) {
        String sql = BASE + " WHERE v.anfitrion_usuario_id=? "
                + "AND ev.codigo IN ('PENDIENTE_APROBACION','PENDIENTE_APROBACION_OLVIDO') "
                + "ORDER BY v.fecha_solicitud";
        return buscarLista(sql, sentencia -> sentencia.setLong(1, anfitrionId));
    }

    @Override
    public List<Visita> listarHistorial(String numeroDocumento, LocalDate desde,
            LocalDate hasta, String estadoCodigo, Long anfitrionId) {
        StringBuilder sql = new StringBuilder(BASE)
                .append(" JOIN personas p ON p.id=v.visitante_persona_id WHERE 1=1");
        List<Object> parametros = new ArrayList<>();
        if (numeroDocumento != null && !numeroDocumento.isBlank()) {
            sql.append(" AND p.numero_documento=?");
            parametros.add(numeroDocumento.trim());
        }
        if (desde != null) {
            sql.append(" AND v.fecha_solicitud>=?");
            parametros.add(desde.atStartOfDay());
        }
        if (hasta != null) {
            sql.append(" AND v.fecha_solicitud<?");
            parametros.add(hasta.plusDays(1).atStartOfDay());
        }
        if (estadoCodigo != null && !estadoCodigo.isBlank()) {
            sql.append(" AND ev.codigo=?");
            parametros.add(estadoCodigo);
        }
        if (anfitrionId != null) {
            sql.append(" AND v.anfitrion_usuario_id=?");
            parametros.add(anfitrionId);
        }
        sql.append(" ORDER BY v.fecha_solicitud DESC");
        return buscarLista(sql.toString(), sentencia -> {
            for (int i = 0; i < parametros.size(); i++) {
                Object valor = parametros.get(i);
                if (valor instanceof LocalDateTime fecha) {
                    sentencia.setTimestamp(i + 1, Timestamp.valueOf(fecha));
                } else {
                    sentencia.setObject(i + 1, valor);
                }
            }
        });
    }

    @Override
    public Visita crearConAuditoria(Visita visita, EventoAuditoria evento) {
        long id = transacciones.ejecutar("No fue posible crear la visita", conexion -> {
            String sql = """
                    INSERT INTO visitas (numero_solicitud,visitante_persona_id,
                        anfitrion_usuario_id,estado_visita_id,registrado_por_usuario_id,
                        respondido_por_usuario_id,regularizada_por_usuario_id,tipo_solicitud,
                        motivo,placa_vehiculo,fecha_solicitud,fecha_programada,fecha_respuesta,
                        fecha_ingreso,fecha_salida,fecha_cierre_sistema,motivo_regularizacion,
                        codigo_pase,fecha_vencimiento_pase,observaciones)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """;
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
                asignarVisita(sentencia, visita);
                sentencia.executeUpdate();
                try (ResultSet claves = sentencia.getGeneratedKeys()) {
                    if (!claves.next()) throw new SQLException("MySQL no devolvio el ID de visita");
                    return claves.getLong(1);
                }
            }
        }, creadoId -> evento.conEntidadId(String.valueOf(creadoId)));
        return buscarPorId(id).orElseThrow(
                () -> new RepositorioException("No fue posible recuperar la visita creada"));
    }

    @Override
    public Visita responderConAuditoria(long visitaId, long usuarioId, int estadoPendienteId,
            int nuevoEstadoId, EventoAuditoria evento) {
        transacciones.ejecutar("No fue posible responder la solicitud", conexion -> {
            try (PreparedStatement bloqueo = conexion.prepareStatement(
                    "SELECT estado_visita_id,anfitrion_usuario_id FROM visitas WHERE id=? FOR UPDATE")) {
                bloqueo.setLong(1, visitaId);
                try (ResultSet fila = bloqueo.executeQuery()) {
                    if (!fila.next()) throw new ConflictoPersistenciaException("La visita no existe");
                    if (fila.getLong("anfitrion_usuario_id") != usuarioId) {
                        throw new ConflictoPersistenciaException(
                                "La solicitud pertenece a otro funcionario");
                    }
                    verificarEstado(fila.getInt("estado_visita_id"), estadoPendienteId,
                            "La solicitud ya fue respondida por otra sesion");
                }
            }
            try (PreparedStatement actualizacion = conexion.prepareStatement(
                    "UPDATE visitas SET estado_visita_id=?,respondido_por_usuario_id=?,"
                            + "fecha_respuesta=GREATEST(CURRENT_TIMESTAMP,fecha_solicitud) "
                            + "WHERE id=?")) {
                actualizacion.setInt(1, nuevoEstadoId);
                actualizacion.setLong(2, usuarioId);
                actualizacion.setLong(3, visitaId);
                verificarFila(actualizacion.executeUpdate());
            }
            return visitaId;
        }, resultado -> evento);
        return recuperar(visitaId);
    }

    @Override
    public ResultadoCheckIn registrarIngresoConAuditoria(long visitaId, long usuarioId,
            int estadoAprobadoId, int estadoDentroId, int estadoCerradaSistemaId,
            String motivoRegularizacion, EventoAuditoria ingreso,
            EventoAuditoria regularizacion) {
        CheckInPersistido persistido = transacciones.ejecutarConAuditorias(
                "No fue posible registrar el ingreso", conexion -> {
                    long personaId;
                    String tipo;
                    LocalDateTime vencimiento;
                    try (PreparedStatement bloqueo = conexion.prepareStatement(
                            "SELECT visitante_persona_id,estado_visita_id,tipo_solicitud,"
                                    + "fecha_vencimiento_pase,fecha_ingreso "
                                    + "FROM visitas WHERE id=? FOR UPDATE")) {
                        bloqueo.setLong(1, visitaId);
                        try (ResultSet fila = bloqueo.executeQuery()) {
                            if (!fila.next()) throw new ConflictoPersistenciaException("La visita no existe");
                            verificarEstado(fila.getInt("estado_visita_id"), estadoAprobadoId,
                                    "La visita no esta aprobada o ya fue utilizada");
                            if (fila.getTimestamp("fecha_ingreso") != null) {
                                throw new ConflictoPersistenciaException("El ingreso ya fue registrado");
                            }
                            personaId = fila.getLong("visitante_persona_id");
                            tipo = fila.getString("tipo_solicitud");
                            vencimiento = obtenerFecha(fila, "fecha_vencimiento_pase");
                        }
                    }
                    bloquearPersona(conexion, personaId);
                    if ("PASE_TEMPORAL".equals(tipo)
                            && (vencimiento == null || vencimiento.isBefore(LocalDateTime.now()))) {
                        throw new ConflictoPersistenciaException("El pase temporal esta vencido");
                    }
                    Long anteriorId = buscarVisitaAbierta(conexion, personaId,
                            visitaId, estadoDentroId);
                    if (anteriorId != null) {
                        cerrarPorSistema(conexion, anteriorId, usuarioId,
                                estadoCerradaSistemaId, motivoRegularizacion);
                    }
                    try (PreparedStatement actualizacion = conexion.prepareStatement(
                            "UPDATE visitas SET estado_visita_id=?,fecha_ingreso=CURRENT_TIMESTAMP "
                                    + "WHERE id=?")) {
                        actualizacion.setInt(1, estadoDentroId);
                        actualizacion.setLong(2, visitaId);
                        verificarFila(actualizacion.executeUpdate());
                    }
                    return new CheckInPersistido(visitaId, anteriorId);
                }, resultado -> {
                    List<EventoAuditoria> eventos = new ArrayList<>();
                    if (resultado.regularizadaId() != null) {
                        eventos.add(regularizacion.conEntidadId(
                                String.valueOf(resultado.regularizadaId())));
                    }
                    eventos.add(ingreso);
                    return eventos;
                });
        return new ResultadoCheckIn(recuperar(visitaId), persistido.regularizadaId());
    }

    @Override
    public Visita registrarSalidaConAuditoria(long visitaId, long usuarioId,
            int estadoDentroId, int estadoFueraId, EventoAuditoria evento) {
        cambiarEstadoBloqueado(visitaId, estadoDentroId, estadoFueraId,
                "fecha_salida=CURRENT_TIMESTAMP", null, usuarioId, evento,
                "La visita no esta dentro o la salida ya fue registrada");
        return recuperar(visitaId);
    }

    @Override
    public Visita regularizarSalidaConAuditoria(long visitaId, long usuarioId,
            int estadoDentroId, int estadoCerradaSistemaId, String motivo,
            EventoAuditoria evento) {
        transacciones.ejecutar("No fue posible regularizar la salida", conexion -> {
            bloquearYVerificarEstado(conexion, visitaId, estadoDentroId,
                    "La visita no se encuentra abierta");
            cerrarPorSistema(conexion, visitaId, usuarioId, estadoCerradaSistemaId, motivo);
            return visitaId;
        }, resultado -> evento);
        return recuperar(visitaId);
    }

    private void cambiarEstadoBloqueado(long visitaId, int esperado, int nuevo,
            String asignacionFecha, String motivo, long usuarioId, EventoAuditoria evento,
            String mensajeConflicto) {
        transacciones.ejecutar("No fue posible cambiar el estado de visita", conexion -> {
            bloquearYVerificarEstado(conexion, visitaId, esperado, mensajeConflicto);
            String sql = "UPDATE visitas SET estado_visita_id=?," + asignacionFecha + " WHERE id=?";
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setInt(1, nuevo);
                sentencia.setLong(2, visitaId);
                verificarFila(sentencia.executeUpdate());
            }
            return visitaId;
        }, resultado -> evento);
    }

    private void bloquearYVerificarEstado(Connection conexion, long visitaId,
            int esperado, String mensaje) throws SQLException {
        try (PreparedStatement bloqueo = conexion.prepareStatement(
                "SELECT estado_visita_id FROM visitas WHERE id=? FOR UPDATE")) {
            bloqueo.setLong(1, visitaId);
            try (ResultSet fila = bloqueo.executeQuery()) {
                if (!fila.next()) throw new ConflictoPersistenciaException("La visita no existe");
                verificarEstado(fila.getInt(1), esperado, mensaje);
            }
        }
    }

    private void bloquearPersona(Connection conexion, long personaId) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "SELECT id FROM personas WHERE id=? FOR UPDATE")) {
            sentencia.setLong(1, personaId);
            try (ResultSet fila = sentencia.executeQuery()) {
                if (!fila.next()) throw new ConflictoPersistenciaException("La persona no existe");
            }
        }
    }

    private Long buscarVisitaAbierta(Connection conexion, long personaId,
            long visitaActualId, int estadoDentroId) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "SELECT id FROM visitas WHERE visitante_persona_id=? "
                        + "AND estado_visita_id=? AND id<>? "
                        + "ORDER BY fecha_ingreso DESC LIMIT 1 FOR UPDATE")) {
            sentencia.setLong(1, personaId);
            sentencia.setInt(2, estadoDentroId);
            sentencia.setLong(3, visitaActualId);
            try (ResultSet fila = sentencia.executeQuery()) {
                return fila.next() ? fila.getLong(1) : null;
            }
        }
    }

    private void cerrarPorSistema(Connection conexion, long visitaId, long usuarioId,
            int estadoCerradaId, String motivo) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(
                "UPDATE visitas SET estado_visita_id=?,fecha_cierre_sistema=CURRENT_TIMESTAMP,"
                        + "fecha_salida=CURRENT_TIMESTAMP,regularizada_por_usuario_id=?,"
                        + "motivo_regularizacion=? WHERE id=?")) {
            sentencia.setInt(1, estadoCerradaId);
            sentencia.setLong(2, usuarioId);
            sentencia.setString(3, motivo);
            sentencia.setLong(4, visitaId);
            verificarFila(sentencia.executeUpdate());
        }
    }

    private Optional<Visita> buscarUno(String sql, Configurador configurador) {
        List<Visita> visitas = buscarLista(sql, configurador);
        return visitas.isEmpty() ? Optional.empty() : Optional.of(visitas.get(0));
    }

    private List<Visita> buscarLista(String sql, Configurador configurador) {
        List<Visita> visitas = new ArrayList<>();
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            configurador.configurar(sentencia);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) visitas.add(mapear(resultado));
            }
            return visitas;
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar visitas", excepcion);
        }
    }

    private Visita mapear(ResultSet resultado) throws SQLException {
        Persona persona = personaRepository.buscarPorId(resultado.getLong("visitante_persona_id"))
                .orElseThrow(() -> new SQLException("La visita tiene una persona inexistente"));
        Usuario anfitrion = usuarioRepository.buscarPorId(resultado.getLong("anfitrion_usuario_id"))
                .orElseThrow(() -> new SQLException("La visita tiene un anfitrion inexistente"));
        EstadoVisita estado = new EstadoVisita(resultado.getInt("estado_visita_id"),
                resultado.getString("estado_codigo"), resultado.getString("estado_nombre"),
                resultado.getString("estado_descripcion"));
        return new Visita(resultado.getLong("id"), resultado.getString("numero_solicitud"),
                persona, anfitrion, estado, resultado.getLong("registrado_por_usuario_id"),
                obtenerLong(resultado, "respondido_por_usuario_id"),
                obtenerLong(resultado, "regularizada_por_usuario_id"),
                TipoSolicitudVisita.valueOf(resultado.getString("tipo_solicitud")),
                resultado.getString("motivo"), resultado.getString("placa_vehiculo"),
                obtenerFecha(resultado, "fecha_solicitud"),
                obtenerFecha(resultado, "fecha_programada"),
                obtenerFecha(resultado, "fecha_respuesta"),
                obtenerFecha(resultado, "fecha_ingreso"),
                obtenerFecha(resultado, "fecha_salida"),
                obtenerFecha(resultado, "fecha_cierre_sistema"),
                resultado.getString("motivo_regularizacion"), resultado.getString("codigo_pase"),
                obtenerFecha(resultado, "fecha_vencimiento_pase"),
                resultado.getString("observaciones"));
    }

    private void asignarVisita(PreparedStatement s, Visita v) throws SQLException {
        s.setString(1, v.getNumeroSolicitud());
        s.setLong(2, v.getVisitante().getId());
        s.setLong(3, v.getAnfitrion().getId());
        s.setInt(4, v.getEstado().getId());
        s.setLong(5, v.getRegistradoPorUsuarioId());
        asignarLong(s, 6, v.getRespondidoPorUsuarioId());
        asignarLong(s, 7, v.getRegularizadaPorUsuarioId());
        s.setString(8, v.getTipoSolicitud().name());
        s.setString(9, v.getMotivo());
        s.setString(10, v.getPlacaVehiculo());
        asignarFecha(s, 11, v.getFechaSolicitud());
        asignarFecha(s, 12, v.getFechaProgramada());
        asignarFecha(s, 13, v.getFechaRespuesta());
        asignarFecha(s, 14, v.getFechaIngreso());
        asignarFecha(s, 15, v.getFechaSalida());
        asignarFecha(s, 16, v.getFechaCierreSistema());
        s.setString(17, v.getMotivoRegularizacion());
        s.setString(18, v.getCodigoPase());
        asignarFecha(s, 19, v.getFechaVencimientoPase());
        s.setString(20, v.getObservaciones());
    }

    private Visita recuperar(long id) {
        return buscarPorId(id).orElseThrow(
                () -> new RepositorioException("No fue posible recuperar la visita actualizada"));
    }

    private void verificarEstado(int actual, int esperado, String mensaje) {
        if (actual != esperado) throw new ConflictoPersistenciaException(mensaje);
    }

    private void verificarFila(int filas) throws SQLException {
        if (filas != 1) throw new SQLException("La operacion no modifico exactamente una visita");
    }

    private void asignarFecha(PreparedStatement s, int indice, LocalDateTime fecha)
            throws SQLException {
        if (fecha == null) s.setNull(indice, Types.TIMESTAMP);
        else s.setTimestamp(indice, Timestamp.valueOf(fecha));
    }

    private void asignarLong(PreparedStatement s, int indice, Long valor) throws SQLException {
        if (valor == null) s.setNull(indice, Types.BIGINT);
        else s.setLong(indice, valor);
    }

    private LocalDateTime obtenerFecha(ResultSet r, String columna) throws SQLException {
        Timestamp valor = r.getTimestamp(columna);
        return valor == null ? null : valor.toLocalDateTime();
    }

    private Long obtenerLong(ResultSet r, String columna) throws SQLException {
        long valor = r.getLong(columna);
        return r.wasNull() ? null : valor;
    }

    private record CheckInPersistido(long visitaId, Long regularizadaId) { }

    @FunctionalInterface
    private interface Configurador {
        void configurar(PreparedStatement sentencia) throws SQLException;
    }
}
