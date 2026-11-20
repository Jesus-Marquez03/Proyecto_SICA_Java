package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.repository.EstadoAccesoRepository;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC del catalogo de estados de acceso.
 */
public final class EstadoAccesoJdbcRepository implements EstadoAccesoRepository {

    private static final String COLUMNAS = "id, codigo, nombre, descripcion";
    private final ConnectionFactory connectionFactory;

    public EstadoAccesoJdbcRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<EstadoAcceso> buscarPorId(int id) {
        return buscarUno("SELECT " + COLUMNAS + " FROM persona_estados_acceso WHERE id = ?",
                sentencia -> sentencia.setInt(1, id));
    }

    @Override
    public Optional<EstadoAcceso> buscarPorCodigo(String codigo) {
        return buscarUno("SELECT " + COLUMNAS + " FROM persona_estados_acceso WHERE codigo = ?",
                sentencia -> sentencia.setString(1, codigo));
    }

    @Override
    public List<EstadoAcceso> listar() {
        List<EstadoAcceso> estados = new ArrayList<>();
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        "SELECT " + COLUMNAS + " FROM persona_estados_acceso ORDER BY id");
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                estados.add(mapear(resultado));
            }
            return estados;
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible listar estados de acceso", excepcion);
        }
    }

    private Optional<EstadoAcceso> buscarUno(String sql, Configurador configurador) {
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            configurador.configurar(sentencia);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar el estado de acceso", excepcion);
        }
    }

    private EstadoAcceso mapear(ResultSet resultado) throws SQLException {
        return new EstadoAcceso(resultado.getInt("id"), resultado.getString("codigo"),
                resultado.getString("nombre"), resultado.getString("descripcion"));
    }

    @FunctionalInterface
    private interface Configurador {
        void configurar(PreparedStatement sentencia) throws SQLException;
    }
}
