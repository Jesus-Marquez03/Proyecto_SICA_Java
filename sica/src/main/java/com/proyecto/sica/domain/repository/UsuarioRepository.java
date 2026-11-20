package com.proyecto.sica.domain.repository;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia para usuarios.
 */
public interface UsuarioRepository {

    Optional<Usuario> buscarPorCorreo(String correo);

    Optional<Usuario> buscarPorId(long id);

    List<Usuario> listar();

    boolean existeCorreo(String correo, Long idExcluido);

    Usuario crearConAuditoria(Usuario usuario, EventoAuditoria evento);

    void actualizarConAuditoria(Usuario usuario, EventoAuditoria evento);

    void cambiarEstadoConAuditoria(long usuarioId, boolean activo, EventoAuditoria evento);

    void registrarLoginExitoso(long usuarioId, EventoAuditoria evento);
}
