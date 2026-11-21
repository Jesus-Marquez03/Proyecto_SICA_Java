package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.repository.EstadoVisitaRepository;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Consulta los estados configurados en visita_estados. */
public final class EstadoVisitaJdbcRepository implements EstadoVisitaRepository {
    private final ConnectionFactory connectionFactory;

    public EstadoVisitaJdbcRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<EstadoVisita> buscarPorId(int id) {
        return buscarUno("SELECT id,codigo,nombre,descripcion FROM visita_estados WHERE id=?",
                sentencia -> sentencia.setInt(1, id));
    }

    @Override
    public Optional<EstadoVisita> buscarPorCodigo(String codigo) {
        return buscarUno("SELECT id,codigo,nombre,descripcion FROM visita_estados WHERE codigo=?",
                sentencia -> sentencia.setString(1, codigo));
    }

    @Override
    public List<EstadoVisita> listar() {
        List<EstadoVisita> estados = new ArrayList<>();
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        "SELECT id,codigo,nombre,descripcion FROM visita_estados ORDER BY id");
                ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) estados.add(mapear(resultado));
            return estados;
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar estados de visita", excepcion);
        }
    }

    private Optional<EstadoVisita> buscarUno(String sql, Configurador configurador) {
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            configurador.configurar(sentencia);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar el estado de visita", excepcion);
        }
    }

    private EstadoVisita mapear(ResultSet resultado) throws SQLException {
        return new EstadoVisita(resultado.getInt("id"), resultado.getString("codigo"),
                resultado.getString("nombre"), resultado.getString("descripcion"));
    }

    @FunctionalInterface
    private interface Configurador {
        void configurar(PreparedStatement sentencia) throws SQLException;
    }
}
