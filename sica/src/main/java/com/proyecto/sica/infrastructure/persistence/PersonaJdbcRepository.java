package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.TipoPersona;
import com.proyecto.sica.domain.repository.PersonaRepository;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia JDBC de trabajadores e invitados.
 */
public final class PersonaJdbcRepository implements PersonaRepository {

    private static final String COLUMNAS = """
            p.id, p.empresa_id, e.nit AS empresa_nit, e.nombre AS empresa_nombre,
            e.direccion AS empresa_direccion, e.telefono AS empresa_telefono,
            e.correo AS empresa_correo, e.activa AS empresa_activa,
            p.estado_acceso_id, ea.codigo AS estado_codigo, ea.nombre AS estado_nombre,
            ea.descripcion AS estado_descripcion, p.tipo_documento, p.numero_documento,
            p.nombres, p.apellidos, p.telefono, p.correo, p.tipo_persona,
            p.activa, p.foto_url
            """;
    private static final String BASE = "SELECT " + COLUMNAS + """
            FROM personas p
            LEFT JOIN empresas e ON e.id = p.empresa_id
            INNER JOIN persona_estados_acceso ea ON ea.id = p.estado_acceso_id
            """;
    private static final String INSERTAR = """
            INSERT INTO personas (empresa_id, estado_acceso_id, tipo_documento,
                numero_documento, nombres, apellidos, telefono, correo,
                tipo_persona, activa, foto_url)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String ACTUALIZAR = """
            UPDATE personas SET empresa_id = ?, tipo_documento = ?, numero_documento = ?,
                nombres = ?, apellidos = ?, telefono = ?, correo = ?,
                tipo_persona = ?, activa = ?, foto_url = ?
            WHERE id = ?
            """;

    private final ConnectionFactory connectionFactory;
    private final EjecutorTransaccionalJdbc transacciones;

    public PersonaJdbcRepository(ConnectionFactory connectionFactory,
            EjecutorTransaccionalJdbc transacciones) {
        this.connectionFactory = connectionFactory;
        this.transacciones = transacciones;
    }

    @Override
    public Optional<Persona> buscarPorId(long id) {
        return buscarUno(BASE + " WHERE p.id = ?", sentencia -> sentencia.setLong(1, id));
    }

    @Override
    public Optional<Persona> buscarPorDocumento(String tipoDocumento, String numeroDocumento) {
        return buscarUno(BASE + " WHERE p.tipo_documento = ? AND p.numero_documento = ?",
                sentencia -> {
                    sentencia.setString(1, tipoDocumento);
                    sentencia.setString(2, numeroDocumento);
                });
    }

    @Override
    public List<Persona> listar() {
        return buscarLista(BASE + " ORDER BY p.apellidos, p.nombres", null);
    }

    @Override
    public List<Persona> buscarPorEmpresa(long empresaId) {
        return buscarLista(BASE + " WHERE p.empresa_id = ? ORDER BY p.apellidos, p.nombres",
                sentencia -> sentencia.setLong(1, empresaId));
    }

    @Override
    public boolean existeDocumento(String tipoDocumento, String numeroDocumento, Long idExcluido) {
        String sql = idExcluido == null
                ? "SELECT COUNT(*) FROM personas WHERE tipo_documento = ? AND numero_documento = ?"
                : "SELECT COUNT(*) FROM personas WHERE tipo_documento = ? AND numero_documento = ? AND id <> ?";
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, tipoDocumento);
            sentencia.setString(2, numeroDocumento);
            if (idExcluido != null) {
                sentencia.setLong(3, idExcluido);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible validar el documento", excepcion);
        }
    }

    @Override
    public Persona crearConAuditoria(Persona persona, EventoAuditoria evento) {
        return transacciones.ejecutar("No fue posible crear la persona", conexion -> {
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
                asignarPersona(sentencia, persona, false);
                sentencia.executeUpdate();
                try (ResultSet claves = sentencia.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new SQLException("MySQL no devolvio el ID de la persona");
                    }
                    return copiarConId(persona, claves.getLong(1));
                }
            }
        }, creada -> evento.conEntidadId(String.valueOf(creada.getId())));
    }

    @Override
    public void actualizarConAuditoria(Persona persona, EventoAuditoria evento) {
        transacciones.ejecutar("No fue posible actualizar la persona", conexion -> {
            try (PreparedStatement sentencia = conexion.prepareStatement(ACTUALIZAR)) {
                asignarPersona(sentencia, persona, true);
                sentencia.setLong(11, persona.getId());
                verificarFila(sentencia.executeUpdate());
                return persona;
            }
        }, resultado -> evento);
    }

    @Override
    public void cambiarEstadoConAuditoria(long personaId, int estadoId, EventoAuditoria evento) {
        transacciones.ejecutar("No fue posible cambiar el estado de acceso", conexion -> {
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    "UPDATE personas SET estado_acceso_id = ? WHERE id = ?")) {
                sentencia.setInt(1, estadoId);
                sentencia.setLong(2, personaId);
                verificarFila(sentencia.executeUpdate());
                return personaId;
            }
        }, resultado -> evento);
    }

    private Optional<Persona> buscarUno(String sql, Configurador configurador) {
        List<Persona> resultados = buscarLista(sql, configurador);
        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }

    private List<Persona> buscarLista(String sql, Configurador configurador) {
        List<Persona> personas = new ArrayList<>();
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (configurador != null) {
                configurador.configurar(sentencia);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    personas.add(mapear(resultado));
                }
            }
            return personas;
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar personas", excepcion);
        }
    }

    private Persona mapear(ResultSet resultado) throws SQLException {
        long empresaValor = resultado.getLong("empresa_id");
        Empresa empresa = resultado.wasNull() ? null : new Empresa(
                empresaValor, resultado.getString("empresa_nit"),
                resultado.getString("empresa_nombre"), resultado.getString("empresa_direccion"),
                resultado.getString("empresa_telefono"), resultado.getString("empresa_correo"),
                resultado.getBoolean("empresa_activa"));
        EstadoAcceso estado = new EstadoAcceso(resultado.getInt("estado_acceso_id"),
                resultado.getString("estado_codigo"), resultado.getString("estado_nombre"),
                resultado.getString("estado_descripcion"));
        return new Persona(resultado.getLong("id"), empresa, estado,
                resultado.getString("tipo_documento"), resultado.getString("numero_documento"),
                resultado.getString("nombres"), resultado.getString("apellidos"),
                resultado.getString("telefono"), resultado.getString("correo"),
                TipoPersona.valueOf(resultado.getString("tipo_persona")),
                resultado.getBoolean("activa"), resultado.getString("foto_url"));
    }

    private void asignarPersona(PreparedStatement sentencia, Persona persona, boolean actualizar)
            throws SQLException {
        if (persona.getEmpresa() == null) {
            sentencia.setNull(1, Types.BIGINT);
        } else {
            sentencia.setLong(1, persona.getEmpresa().getId());
        }
        if (!actualizar) {
            sentencia.setInt(2, persona.getEstadoAcceso().getId());
            sentencia.setString(3, persona.getTipoDocumento());
            sentencia.setString(4, persona.getNumeroDocumento());
            sentencia.setString(5, persona.getNombres());
            sentencia.setString(6, persona.getApellidos());
            sentencia.setString(7, persona.getTelefono());
            sentencia.setString(8, persona.getCorreo());
            sentencia.setString(9, persona.getTipoPersona().name());
            sentencia.setBoolean(10, persona.isActiva());
            sentencia.setString(11, persona.getFotoUrl());
        } else {
            sentencia.setString(2, persona.getTipoDocumento());
            sentencia.setString(3, persona.getNumeroDocumento());
            sentencia.setString(4, persona.getNombres());
            sentencia.setString(5, persona.getApellidos());
            sentencia.setString(6, persona.getTelefono());
            sentencia.setString(7, persona.getCorreo());
            sentencia.setString(8, persona.getTipoPersona().name());
            sentencia.setBoolean(9, persona.isActiva());
            sentencia.setString(10, persona.getFotoUrl());
        }
    }

    private Persona copiarConId(Persona persona, long id) {
        return new Persona(id, persona.getEmpresa(), persona.getEstadoAcceso(),
                persona.getTipoDocumento(), persona.getNumeroDocumento(), persona.getNombres(),
                persona.getApellidos(), persona.getTelefono(), persona.getCorreo(),
                persona.getTipoPersona(), persona.isActiva(), persona.getFotoUrl());
    }

    private void verificarFila(int filas) throws SQLException {
        if (filas != 1) {
            throw new SQLException("La operacion no modifico exactamente una persona");
        }
    }

    @FunctionalInterface
    private interface Configurador {
        void configurar(PreparedStatement sentencia) throws SQLException;
    }
}
