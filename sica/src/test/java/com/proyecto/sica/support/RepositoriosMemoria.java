package com.proyecto.sica.support;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.AuditoriaRepository;
import com.proyecto.sica.domain.repository.EmpresaRepository;
import com.proyecto.sica.domain.repository.RolRepository;
import com.proyecto.sica.domain.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adaptadores sencillos para probar la aplicacion sin MySQL ni mocks externos.
 */
public final class RepositoriosMemoria {

    private RepositoriosMemoria() {
    }

    public static final class AuditoriaMemoria implements AuditoriaRepository {
        private final List<EventoAuditoria> eventos = new ArrayList<>();

        @Override
        public void registrar(EventoAuditoria evento) {
            eventos.add(evento);
        }

        public List<EventoAuditoria> getEventos() {
            return List.copyOf(eventos);
        }
    }

    public static final class UsuariosMemoria implements UsuarioRepository {
        private final Map<Long, Usuario> usuarios = new LinkedHashMap<>();
        private final AuditoriaMemoria auditoria;
        private long siguienteId = 1;

        public UsuariosMemoria(AuditoriaMemoria auditoria) {
            this.auditoria = auditoria;
        }

        public void agregar(Usuario usuario) {
            usuarios.put(usuario.getId(), usuario);
            siguienteId = Math.max(siguienteId, usuario.getId() + 1);
        }

        @Override
        public Optional<Usuario> buscarPorCorreo(String correo) {
            return usuarios.values().stream()
                    .filter(usuario -> usuario.getCorreo().equalsIgnoreCase(correo))
                    .findFirst();
        }

        @Override
        public Optional<Usuario> buscarPorId(long id) {
            return Optional.ofNullable(usuarios.get(id));
        }

        @Override
        public List<Usuario> listar() {
            return List.copyOf(usuarios.values());
        }

        @Override
        public boolean existeCorreo(String correo, Long idExcluido) {
            return usuarios.values().stream().anyMatch(usuario ->
                    usuario.getCorreo().equalsIgnoreCase(correo)
                            && (idExcluido == null || !usuario.getId().equals(idExcluido)));
        }

        @Override
        public Usuario crearConAuditoria(Usuario usuario, EventoAuditoria evento) {
            long id = siguienteId++;
            Usuario creado = copiar(usuario, id, usuario.isActivo());
            usuarios.put(id, creado);
            auditoria.registrar(evento.conEntidadId(String.valueOf(id)));
            return creado;
        }

        @Override
        public void actualizarConAuditoria(Usuario usuario, EventoAuditoria evento) {
            usuarios.put(usuario.getId(), usuario);
            auditoria.registrar(evento);
        }

        @Override
        public void cambiarEstadoConAuditoria(long usuarioId, boolean activo,
                EventoAuditoria evento) {
            Usuario actual = usuarios.get(usuarioId);
            usuarios.put(usuarioId, copiar(actual, usuarioId, activo));
            auditoria.registrar(evento);
        }

        @Override
        public void registrarLoginExitoso(long usuarioId, EventoAuditoria evento) {
            auditoria.registrar(evento);
        }

        private Usuario copiar(Usuario usuario, long id, boolean activo) {
            return new Usuario(id, usuario.getRol(), usuario.getEmpresaId(),
                    usuario.getEmpresaNombre(), usuario.getNombreUsuario(),
                    usuario.getPasswordHash(), usuario.getNombres(), usuario.getApellidos(),
                    usuario.getCorreo(), activo, usuario.getUltimoAcceso());
        }
    }

    public static final class RolesMemoria implements RolRepository {
        private final Map<Integer, Rol> roles = new LinkedHashMap<>();

        public void agregar(Rol rol) {
            roles.put(rol.getId(), rol);
        }

        @Override
        public Optional<Rol> buscarPorId(int id) {
            return Optional.ofNullable(roles.get(id));
        }

        @Override
        public List<Rol> listar() {
            return List.copyOf(roles.values());
        }
    }

    public static final class EmpresasMemoria implements EmpresaRepository {
        private final Map<Long, String> empresas = new LinkedHashMap<>();

        public void agregar(long id, String nombre) {
            empresas.put(id, nombre);
        }

        @Override
        public boolean existeActiva(long id) {
            return empresas.containsKey(id);
        }

        @Override
        public Optional<String> buscarNombre(long id) {
            return Optional.ofNullable(empresas.get(id));
        }
    }
}
