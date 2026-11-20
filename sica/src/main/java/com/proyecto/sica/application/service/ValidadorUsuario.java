package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.repository.EmpresaRepository;
import com.proyecto.sica.domain.repository.RolRepository;
import com.proyecto.sica.domain.repository.UsuarioRepository;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Centraliza las reglas de entrada de la gestion de usuarios.
 */
public final class ValidadorUsuario {

    private static final Pattern PATRON_CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;

    public ValidadorUsuario(UsuarioRepository usuarioRepository,
            RolRepository rolRepository, EmpresaRepository empresaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.empresaRepository = empresaRepository;
    }

    public void validarTextos(String nombreUsuario, String nombres,
            String apellidos, String correo) {
        obligatorio(nombreUsuario, "El nombre de usuario es obligatorio");
        obligatorio(nombres, "Los nombres son obligatorios");
        obligatorio(apellidos, "Los apellidos son obligatorios");
        obligatorio(correo, "El correo es obligatorio");
        if (!PATRON_CORREO.matcher(correo.trim()).matches()) {
            throw new ValidacionException("El correo no tiene un formato valido");
        }
    }

    public void validarCorreoUnico(String correo, Long idExcluido) {
        if (usuarioRepository.existeCorreo(normalizarCorreo(correo), idExcluido)) {
            throw new ValidacionException("El correo ya esta registrado");
        }
    }

    public Rol obtenerRolValido(int rolId) {
        Rol rol = rolRepository.buscarPorId(rolId)
                .orElseThrow(() -> new ValidacionException("El rol indicado no existe"));
        if (!rol.isActivo()) {
            throw new ValidacionException("El rol indicado se encuentra inactivo");
        }
        return rol;
    }

    public String validarYObtenerEmpresa(Rol rol, Long empresaId) {
        if (rol.isRequiereEmpresa() && empresaId == null) {
            throw new ValidacionException("El rol seleccionado requiere una empresa");
        }
        if (empresaId == null) {
            return null;
        }
        if (!empresaRepository.existeActiva(empresaId)) {
            throw new ValidacionException("La empresa indicada no existe o esta inactiva");
        }
        return empresaRepository.buscarNombre(empresaId).orElse(null);
    }

    public String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private void obligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException(mensaje);
        }
    }
}
