package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.repository.EmpresaRepository;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Consulta solamente los datos de empresa requeridos por usuarios.
 */
public final class EmpresaJdbcRepository implements EmpresaRepository {

    private static final String BUSCAR = "SELECT nombre, activa FROM empresas WHERE id = ?";
    private final ConnectionFactory connectionFactory;

    public EmpresaJdbcRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public boolean existeActiva(long id) {
        return buscar(id).map(EmpresaMinima::activa).orElse(false);
    }

    @Override
    public Optional<String> buscarNombre(long id) {
        return buscar(id).map(EmpresaMinima::nombre);
    }

    private Optional<EmpresaMinima> buscar(long id) {
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(BUSCAR)) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return Optional.empty();
                }
                return Optional.of(new EmpresaMinima(
                        resultado.getString("nombre"), resultado.getBoolean("activa")));
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar la empresa", excepcion);
        }
    }

    private record EmpresaMinima(String nombre, boolean activa) {
    }
}
