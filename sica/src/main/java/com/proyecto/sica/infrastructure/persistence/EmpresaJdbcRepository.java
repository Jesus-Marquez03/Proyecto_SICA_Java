package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.repository.EmpresaRepository;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador JDBC de empresas con cambios auditados.
 */
public final class EmpresaJdbcRepository implements EmpresaRepository {

    private static final String COLUMNAS = "id, nit, nombre, direccion, telefono, correo, activa";
    private final ConnectionFactory connectionFactory;
    private final EjecutorTransaccionalJdbc transacciones;

    public EmpresaJdbcRepository(ConnectionFactory connectionFactory,
            EjecutorTransaccionalJdbc transacciones) {
        this.connectionFactory = connectionFactory;
        this.transacciones = transacciones;
    }

    @Override
    public boolean existeActiva(long id) {
        return buscarPorId(id).map(Empresa::isActiva).orElse(false);
    }

    @Override
    public Optional<String> buscarNombre(long id) {
        return buscarPorId(id).map(Empresa::getNombre);
    }

    @Override
    public Optional<Empresa> buscarPorId(long id) {
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        "SELECT " + COLUMNAS + " FROM empresas WHERE id = ?")) {
            sentencia.setLong(1, id);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar la empresa", excepcion);
        }
    }

    @Override
    public List<Empresa> listar() {
        return buscarLista("SELECT " + COLUMNAS + " FROM empresas ORDER BY nombre", null);
    }

    @Override
    public List<Empresa> buscarPorNombre(String nombre) {
        return buscarLista("SELECT " + COLUMNAS
                + " FROM empresas WHERE LOWER(nombre) LIKE ? ORDER BY nombre",
                sentencia -> sentencia.setString(1, "%" + nombre.toLowerCase() + "%"));
    }

    @Override
    public boolean existeNombre(String nombre, Long idExcluido) {
        return existeValor("nombre", nombre, idExcluido);
    }

    @Override
    public boolean existeNit(String nit, Long idExcluido) {
        return existeValor("nit", nit, idExcluido);
    }

    @Override
    public Empresa crearConAuditoria(Empresa empresa, EventoAuditoria evento) {
        return transacciones.ejecutar("No fue posible crear la empresa", conexion -> {
            String sql = """
                    INSERT INTO empresas (nit, nombre, direccion, telefono, correo, activa)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
            try (PreparedStatement sentencia = conexion.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {
                asignar(sentencia, empresa);
                sentencia.executeUpdate();
                try (ResultSet claves = sentencia.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new SQLException("MySQL no devolvio el ID de la empresa");
                    }
                    return copiarConId(empresa, claves.getLong(1));
                }
            }
        }, creada -> evento.conEntidadId(String.valueOf(creada.getId())));
    }

    @Override
    public void actualizarConAuditoria(Empresa empresa, EventoAuditoria evento) {
        transacciones.ejecutar("No fue posible actualizar la empresa", conexion -> {
            String sql = """
                    UPDATE empresas SET nit = ?, nombre = ?, direccion = ?,
                        telefono = ?, correo = ? WHERE id = ?
                    """;
            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                sentencia.setString(1, empresa.getNit());
                sentencia.setString(2, empresa.getNombre());
                sentencia.setString(3, empresa.getDireccion());
                sentencia.setString(4, empresa.getTelefono());
                sentencia.setString(5, empresa.getCorreo());
                sentencia.setLong(6, empresa.getId());
                if (sentencia.executeUpdate() != 1) {
                    throw new SQLException("La operacion no modifico exactamente una empresa");
                }
                return empresa;
            }
        }, resultado -> evento);
    }

    private boolean existeValor(String columna, String valor, Long idExcluido) {
        String sql = "SELECT COUNT(*) FROM empresas WHERE LOWER(" + columna + ") = LOWER(?)"
                + (idExcluido == null ? "" : " AND id <> ?");
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, valor);
            if (idExcluido != null) {
                sentencia.setLong(2, idExcluido);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                resultado.next();
                return resultado.getInt(1) > 0;
            }
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible validar la empresa", excepcion);
        }
    }

    private List<Empresa> buscarLista(String sql, Configurador configurador) {
        List<Empresa> empresas = new ArrayList<>();
        try (Connection conexion = connectionFactory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (configurador != null) {
                configurador.configurar(sentencia);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    empresas.add(mapear(resultado));
                }
            }
            return empresas;
        } catch (SQLException excepcion) {
            throw new RepositorioException("No fue posible consultar empresas", excepcion);
        }
    }

    private Empresa mapear(ResultSet resultado) throws SQLException {
        return new Empresa(resultado.getLong("id"), resultado.getString("nit"),
                resultado.getString("nombre"), resultado.getString("direccion"),
                resultado.getString("telefono"), resultado.getString("correo"),
                resultado.getBoolean("activa"));
    }

    private void asignar(PreparedStatement sentencia, Empresa empresa) throws SQLException {
        sentencia.setString(1, empresa.getNit());
        sentencia.setString(2, empresa.getNombre());
        sentencia.setString(3, empresa.getDireccion());
        sentencia.setString(4, empresa.getTelefono());
        sentencia.setString(5, empresa.getCorreo());
        sentencia.setBoolean(6, empresa.isActiva());
    }

    private Empresa copiarConId(Empresa empresa, long id) {
        return new Empresa(id, empresa.getNit(), empresa.getNombre(), empresa.getDireccion(),
                empresa.getTelefono(), empresa.getCorreo(), empresa.isActiva());
    }

    @FunctionalInterface
    private interface Configurador {
        void configurar(PreparedStatement sentencia) throws SQLException;
    }
}
