package com.proyecto.sica.support;

import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.AuditoriaRepository;
import com.proyecto.sica.domain.repository.EmpresaRepository;
import com.proyecto.sica.domain.repository.EstadoAccesoRepository;
import com.proyecto.sica.domain.repository.PersonaRepository;
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
        private final Map<Long, Empresa> empresas = new LinkedHashMap<>();
        private final AuditoriaMemoria auditoria;
        private long siguienteId = 1;

        public EmpresasMemoria() {
            this(new AuditoriaMemoria());
        }

        public EmpresasMemoria(AuditoriaMemoria auditoria) {
            this.auditoria = auditoria;
        }

        public void agregar(long id, String nombre) {
            agregar(new Empresa(id, "NIT-" + id, nombre, null, null, null, true));
        }

        public void agregar(Empresa empresa) {
            empresas.put(empresa.getId(), empresa);
            siguienteId = Math.max(siguienteId, empresa.getId() + 1);
        }

        @Override
        public boolean existeActiva(long id) {
            Empresa empresa = empresas.get(id);
            return empresa != null && empresa.isActiva();
        }

        @Override
        public Optional<String> buscarNombre(long id) {
            return buscarPorId(id).map(Empresa::getNombre);
        }

        @Override
        public Optional<Empresa> buscarPorId(long id) {
            return Optional.ofNullable(empresas.get(id));
        }

        @Override
        public List<Empresa> listar() {
            return List.copyOf(empresas.values());
        }

        @Override
        public List<Empresa> buscarPorNombre(String nombre) {
            String filtro = nombre.toLowerCase();
            return empresas.values().stream()
                    .filter(empresa -> empresa.getNombre().toLowerCase().contains(filtro))
                    .toList();
        }

        @Override
        public boolean existeNombre(String nombre, Long idExcluido) {
            return empresas.values().stream().anyMatch(empresa ->
                    empresa.getNombre().equalsIgnoreCase(nombre)
                            && (idExcluido == null || !empresa.getId().equals(idExcluido)));
        }

        @Override
        public boolean existeNit(String nit, Long idExcluido) {
            return empresas.values().stream().anyMatch(empresa ->
                    empresa.getNit().equalsIgnoreCase(nit)
                            && (idExcluido == null || !empresa.getId().equals(idExcluido)));
        }

        @Override
        public Empresa crearConAuditoria(Empresa empresa, EventoAuditoria evento) {
            long id = siguienteId++;
            Empresa creada = copiar(empresa, id);
            empresas.put(id, creada);
            auditoria.registrar(evento.conEntidadId(String.valueOf(id)));
            return creada;
        }

        @Override
        public void actualizarConAuditoria(Empresa empresa, EventoAuditoria evento) {
            empresas.put(empresa.getId(), empresa);
            auditoria.registrar(evento);
        }

        private Empresa copiar(Empresa empresa, long id) {
            return new Empresa(id, empresa.getNit(), empresa.getNombre(),
                    empresa.getDireccion(), empresa.getTelefono(), empresa.getCorreo(),
                    empresa.isActiva());
        }
    }

    public static final class EstadosAccesoMemoria implements EstadoAccesoRepository {
        private final Map<Integer, EstadoAcceso> estados = new LinkedHashMap<>();

        public void agregar(EstadoAcceso estado) {
            estados.put(estado.getId(), estado);
        }

        @Override
        public Optional<EstadoAcceso> buscarPorId(int id) {
            return Optional.ofNullable(estados.get(id));
        }

        @Override
        public Optional<EstadoAcceso> buscarPorCodigo(String codigo) {
            return estados.values().stream()
                    .filter(estado -> estado.getCodigo().equalsIgnoreCase(codigo))
                    .findFirst();
        }

        @Override
        public List<EstadoAcceso> listar() {
            return List.copyOf(estados.values());
        }
    }

    public static final class PersonasMemoria implements PersonaRepository {
        private final Map<Long, Persona> personas = new LinkedHashMap<>();
        private final AuditoriaMemoria auditoria;
        private final EstadoAccesoRepository estados;
        private long siguienteId = 1;

        public PersonasMemoria(AuditoriaMemoria auditoria, EstadoAccesoRepository estados) {
            this.auditoria = auditoria;
            this.estados = estados;
        }

        public void agregar(Persona persona) {
            personas.put(persona.getId(), persona);
            siguienteId = Math.max(siguienteId, persona.getId() + 1);
        }

        @Override
        public Optional<Persona> buscarPorId(long id) {
            return Optional.ofNullable(personas.get(id));
        }

        @Override
        public Optional<Persona> buscarPorDocumento(String tipo, String numero) {
            return personas.values().stream().filter(persona ->
                    persona.getTipoDocumento().equalsIgnoreCase(tipo)
                            && persona.getNumeroDocumento().equalsIgnoreCase(numero))
                    .findFirst();
        }

        @Override
        public List<Persona> listar() {
            return List.copyOf(personas.values());
        }

        @Override
        public List<Persona> buscarPorEmpresa(long empresaId) {
            return personas.values().stream().filter(persona ->
                    persona.getEmpresa() != null
                            && persona.getEmpresa().getId() == empresaId).toList();
        }

        @Override
        public boolean existeDocumento(String tipo, String numero, Long idExcluido) {
            return personas.values().stream().anyMatch(persona ->
                    persona.getTipoDocumento().equalsIgnoreCase(tipo)
                            && persona.getNumeroDocumento().equalsIgnoreCase(numero)
                            && (idExcluido == null || !persona.getId().equals(idExcluido)));
        }

        @Override
        public Persona crearConAuditoria(Persona persona, EventoAuditoria evento) {
            long id = siguienteId++;
            Persona creada = copiar(persona, id, persona.getEstadoAcceso());
            personas.put(id, creada);
            auditoria.registrar(evento.conEntidadId(String.valueOf(id)));
            return creada;
        }

        @Override
        public void actualizarConAuditoria(Persona persona, EventoAuditoria evento) {
            personas.put(persona.getId(), persona);
            auditoria.registrar(evento);
        }

        @Override
        public void cambiarEstadoConAuditoria(long personaId, int estadoId,
                EventoAuditoria evento) {
            Persona actual = personas.get(personaId);
            EstadoAcceso estado = estados.buscarPorId(estadoId).orElseThrow();
            personas.put(personaId, copiar(actual, personaId, estado));
            auditoria.registrar(evento);
        }

        private Persona copiar(Persona persona, long id, EstadoAcceso estado) {
            return new Persona(id, persona.getEmpresa(), estado, persona.getTipoDocumento(),
                    persona.getNumeroDocumento(), persona.getNombres(), persona.getApellidos(),
                    persona.getTelefono(), persona.getCorreo(), persona.getTipoPersona(),
                    persona.isActiva(), persona.getFotoUrl());
        }
    }
}
