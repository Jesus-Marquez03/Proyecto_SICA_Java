package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.TipoPersona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.EmpresaRepository;
import com.proyecto.sica.domain.repository.EstadoVisitaRepository;
import com.proyecto.sica.domain.repository.PersonaRepository;
import com.proyecto.sica.domain.repository.UsuarioRepository;

/** Reglas compartidas por los cuatro flujos de acceso. */
public final class ValidadorVisita {
    private final PersonaRepository personaRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoVisitaRepository estadoRepository;

    public ValidadorVisita(PersonaRepository personaRepository,
            EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository,
            EstadoVisitaRepository estadoRepository) {
        this.personaRepository = personaRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoRepository = estadoRepository;
    }

    public Persona obtenerPersonaHabilitada(long personaId, TipoPersona tipoEsperado) {
        Persona persona = personaRepository.buscarPorId(personaId)
                .orElseThrow(() -> new ValidacionException("La persona no existe"));
        if (!persona.isActiva()) throw new ValidacionException("La persona esta inactiva");
        if (persona.getTipoPersona() != tipoEsperado) {
            throw new ValidacionException("El tipo de persona no corresponde al flujo seleccionado");
        }
        verificarAcceso(persona);
        return persona;
    }

    public void verificarAcceso(Persona persona) {
        String codigo = persona.getEstadoAcceso().getCodigo();
        if ("PROHIBICION_INGRESO".equals(codigo) || "BLOQUEADO".equals(codigo)) {
            throw new ValidacionException("La persona tiene prohibicion de ingreso");
        }
    }

    public Empresa obtenerEmpresaActiva(long empresaId) {
        Empresa empresa = empresaRepository.buscarPorId(empresaId)
                .orElseThrow(() -> new ValidacionException("La empresa no existe"));
        if (!empresa.isActiva()) throw new ValidacionException("La empresa esta inactiva");
        return empresa;
    }

    public Usuario obtenerAnfitrion(long usuarioId, long empresaId) {
        Usuario anfitrion = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new ValidacionException("El funcionario anfitrion no existe"));
        if (!anfitrion.isActivo() || anfitrion.getEmpresaId() == null
                || anfitrion.getEmpresaId() != empresaId
                || !anfitrion.getRol().isRequiereEmpresa()) {
            throw new ValidacionException("El anfitrion no es un funcionario activo de la empresa");
        }
        return anfitrion;
    }

    public EstadoVisita obtenerEstado(String codigo) {
        return estadoRepository.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ValidacionException(
                        "No se configuro el estado de visita " + codigo));
    }

    public void verificarActorEmpresa(Usuario actor, long empresaId) {
        if (actor.getEmpresaId() == null || actor.getEmpresaId() != empresaId) {
            throw new AccesoDenegadoException(
                    "Solo puede operar solicitudes de su propia empresa");
        }
    }

    public String textoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) throw new ValidacionException(mensaje);
        return valor.trim();
    }

    public String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
