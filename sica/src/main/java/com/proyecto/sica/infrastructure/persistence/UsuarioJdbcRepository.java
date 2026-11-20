package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.domain.repository.RolRepository;
import com.proyecto.sica.domain.repository.UsuarioRepository;
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
 * Adaptador JDBC de usuarios con transacciones para cambios auditados.
 */
public final class UsuarioJdbcRepository implements UsuarioRepository {

    private static final String COLUMNAS = """
            u.id, u.rol_id, u.empresa_id, e.nombre AS empresa_nombre,
            u.nombre_usuario, u.password_hash, u.nombres, u.apellidos,
            u.correo, u.activo, u.ultimo_acceso
            """;
    private static final String BUSCAR_POR_CORREO = "SELECT " + COLUMNAS + """
            FROM usuarios u LEFT JOIN empresas e ON e.id = u.empresa_id
            WHERE LOWER(u.correo) = ?
            """;
    private static final String BUSCAR_POR_ID = "SELECT " + COLUMNAS + """
            FROM usuarios u LEFT JOIN empresas e ON e.id = u.empresa_id
            WHERE u.id = ?
            """;
    private static final String LISTAR = "SELECT " + COLUMNAS + """
            FROM usuarios u LEFT JOIN empresas e ON e.id = u.empresa_id
            ORDER BY u.apellidos, u.nombres
            """;
    private static final String INSERTAR = """
            INSERT INTO usuarios
                (rol_id, empresa_id, nombre_usuario, password_hash,
                 nombres, apellidos, correo, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String ACTUALIZAR = """
            UPDATE usuarios SET rol_id = ?, empresa_id = ?, nombre_usuario = ?,
                nombres = ?, apellidos = ?, correo = ? WHERE id = ?
            """;

    private final ConnectionFactory connectionFactory;
    private final RolRepository rolRepository;
    private final AuditoriaJdbcRepository auditoriaRepository;

    public UsuarioJdbcRepository(ConnectionFactory connectionFactory,
            RolRepository rolRepository, AuditoriaJdbcRepository auditoriaRepository) {
        this.connectionFactory = connectionFactory;
        this.rolRepository = rolRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return buscarUno(BUSCAR_POR_CORREO, sentencia -> sentencia.setString(1, correo));
    }

    @Override
    public Optional<Usuario> buscarPorId(long id) {
        return buscarUno(BUSCAR_POR_ID, sentencia -> sentencia.setLong(1, id));
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(LISTAR);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                usuarios.add(mapear(resultado));
            }
            return usuarios;
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible listar los usuarios", excepcion);
        }
    }

    @Override
    public boolean existeCorreo(String correo, Long idExcluido) {
        String sql = idExcluido == null
                ? "SELECT COUNT(*) FROM usuarios WHERE LOWER(correo) = ?"
                : "SELECT COUNT(*) FROM usuarios WHERE LOWER(correo) = ? AND id <> ?";
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, correo);
            if (idExcluido != null) {
                sentencia.setLong(2, idExcluido);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible validar el correo", excepcion);
        }
    }

    @Override
    public Usuario crearConAuditoria(Usuario usuario, EventoAuditoria evento) {
        try (Connection conexion = connectionFactory.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
                asignarUsuario(sentencia, usuario);
                sentencia.executeUpdate();
                try (ResultSet claves = sentencia.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new SQLException("MySQL no devolvio el ID del usuario");
                    }
                    long id = claves.getLong(1);
                    auditoriaRepository.registrar(conexion, evento.conEntidadId(String.valueOf(id)));
                    conexion.commit();
                    return copiarConId(usuario, id);
                }
            } catch (SQLException excepcion) {
                rollback(conexion, excepcion);
                throw excepcion;
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible crear el usuario", excepcion);
        }
    }

    @Override
    public void actualizarConAuditoria(Usuario usuario, EventoAuditoria evento) {
        ejecutarTransaccion(evento, conexion -> {
            try (PreparedStatement sentencia = conexion.prepareStatement(ACTUALIZAR)) {
                sentencia.setInt(1, usuario.getRol().getId());
                asignarEmpresa(sentencia, 2, usuario.getEmpresaId());
                sentencia.setString(3, usuario.getNombreUsuario());
                sentencia.setString(4, usuario.getNombres());
                sentencia.setString(5, usuario.getApellidos());
                sentencia.setString(6, usuario.getCorreo());
                sentencia.setLong(7, usuario.getId());
                verificarFila(sentencia.executeUpdate());
            }
        }, "No fue posible actualizar el usuario");
    }

    @Override
    public void cambiarEstadoConAuditoria(long usuarioId, boolean activo, EventoAuditoria evento) {
        ejecutarTransaccion(evento, conexion -> {
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    "UPDATE usuarios SET activo = ? WHERE id = ?")) {
                sentencia.setBoolean(1, activo);
                sentencia.setLong(2, usuarioId);
                verificarFila(sentencia.executeUpdate());
            }
        }, "No fue posible cambiar el estado del usuario");
    }

    @Override
    public void registrarLoginExitoso(long usuarioId, EventoAuditoria evento) {
        ejecutarTransaccion(evento, conexion -> {
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    "UPDATE usuarios SET ultimo_acceso = CURRENT_TIMESTAMP WHERE id = ?")) {
                sentencia.setLong(1, usuarioId);
                verificarFila(sentencia.executeUpdate());
            }
        }, "No fue posible confirmar el inicio de sesion");
    }

    private Optional<Usuario> buscarUno(String sql, ConfiguradorSentencia configurador) {
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            configurador.configurar(sentencia);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar el usuario", excepcion);
        }
    }

    private Usuario mapear(ResultSet resultado) throws SQLException {
        int rolId = resultado.getInt("rol_id");
        Rol rol = rolRepository.buscarPorId(rolId)
                .orElseThrow(() -> new SQLException("El usuario tiene un rol inexistente"));
        long empresaValor = resultado.getLong("empresa_id");
        Long empresaId = resultado.wasNull() ? null : empresaValor;
        return new Usuario(resultado.getLong("id"), rol, empresaId,
                resultado.getString("empresa_nombre"), resultado.getString("nombre_usuario"),
                resultado.getString("password_hash"), resultado.getString("nombres"),
                resultado.getString("apellidos"), resultado.getString("correo"),
                resultado.getBoolean("activo"),
                resultado.getTimestamp("ultimo_acceso") == null ? null
                        : resultado.getTimestamp("ultimo_acceso").toLocalDateTime());
    }

    private void asignarUsuario(PreparedStatement sentencia, Usuario usuario) throws SQLException {
        sentencia.setInt(1, usuario.getRol().getId());
        asignarEmpresa(sentencia, 2, usuario.getEmpresaId());
        sentencia.setString(3, usuario.getNombreUsuario());
        sentencia.setString(4, usuario.getPasswordHash());
        sentencia.setString(5, usuario.getNombres());
        sentencia.setString(6, usuario.getApellidos());
        sentencia.setString(7, usuario.getCorreo());
        sentencia.setBoolean(8, usuario.isActivo());
    }

    private void asignarEmpresa(PreparedStatement sentencia, int indice, Long empresaId)
            throws SQLException {
        if (empresaId == null) {
            sentencia.setNull(indice, Types.BIGINT);
        } else {
            sentencia.setLong(indice, empresaId);
        }
    }

    private Usuario copiarConId(Usuario usuario, long id) {
        return new Usuario(id, usuario.getRol(), usuario.getEmpresaId(),
                usuario.getEmpresaNombre(), usuario.getNombreUsuario(), usuario.getPasswordHash(),
                usuario.getNombres(), usuario.getApellidos(), usuario.getCorreo(),
                usuario.isActivo(), usuario.getUltimoAcceso());
    }

    private void ejecutarTransaccion(EventoAuditoria evento, OperacionJdbc operacion,
            String mensajeError) {
        try (Connection conexion = connectionFactory.obtenerConexion()) {
            conexion.setAutoCommit(false);
            try {
                operacion.ejecutar(conexion);
                auditoriaRepository.registrar(conexion, evento);
                conexion.commit();
            } catch (SQLException excepcion) {
                rollback(conexion, excepcion);
                throw excepcion;
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException(mensajeError, excepcion);
        }
    }

    private void rollback(Connection conexion, SQLException original) {
        try {
            conexion.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    private void verificarFila(int filas) throws SQLException {
        if (filas != 1) {
            throw new SQLException("La operacion no modifico exactamente un usuario");
        }
    }

    @FunctionalInterface
    private interface ConfiguradorSentencia {
        void configurar(PreparedStatement sentencia) throws SQLException;
    }

    @FunctionalInterface
    private interface OperacionJdbc {
        void ejecutar(Connection conexion) throws SQLException;
    }
}
