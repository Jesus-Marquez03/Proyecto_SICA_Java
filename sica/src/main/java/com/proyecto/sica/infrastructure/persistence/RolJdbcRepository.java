package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.Permiso;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.domain.repository.RolRepository;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Consulta roles y permisos desde MySQL.
 */
public final class RolJdbcRepository implements RolRepository {

    private static final String BUSCAR_ROL = """
            SELECT id, nombre, descripcion, activo, requiere_empresa
            FROM roles WHERE id = ?
            """;
    private static final String LISTAR_ROLES = """
            SELECT id, nombre, descripcion, activo, requiere_empresa
            FROM roles ORDER BY nombre
            """;
    private static final String BUSCAR_PERMISOS = """
            SELECT p.id, p.codigo, p.nombre, p.descripcion
            FROM permisos p
            INNER JOIN rol_permisos rp ON rp.permiso_id = p.id
            WHERE rp.rol_id = ?
            ORDER BY p.codigo
            """;

    private final ConnectionFactory connectionFactory;

    public RolJdbcRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Rol> buscarPorId(int id) {
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(BUSCAR_ROL)) {
            sentencia.setInt(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearRol(resultado));
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar el rol", excepcion);
        }
    }

    @Override
    public List<Rol> listar() {
        List<Rol> roles = new ArrayList<>();
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(LISTAR_ROLES);
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                roles.add(mapearRol(resultado));
            }
            return roles;
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible listar los roles", excepcion);
        }
    }

    private Rol mapearRol(ResultSet resultado) throws SQLException {
        int id = resultado.getInt("id");
        return new Rol(id, resultado.getString("nombre"),
                resultado.getString("descripcion"), resultado.getBoolean("activo"),
                resultado.getBoolean("requiere_empresa"), buscarPermisos(id));
    }

    private Set<Permiso> buscarPermisos(int rolId) {
        Set<Permiso> permisos = new HashSet<>();
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(BUSCAR_PERMISOS)) {
            sentencia.setInt(1, rolId);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    permisos.add(new Permiso(resultado.getInt("id"),
                            resultado.getString("codigo"), resultado.getString("nombre"),
                            resultado.getString("descripcion")));
                }
            }
            return permisos;
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar los permisos del rol", excepcion);
        }
    }
}
