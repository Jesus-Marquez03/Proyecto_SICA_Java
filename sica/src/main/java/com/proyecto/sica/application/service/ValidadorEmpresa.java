package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.repository.EmpresaRepository;
import java.util.Locale;
import java.util.regex.Pattern;

/** Centraliza las validaciones de entrada de una empresa. */
public final class ValidadorEmpresa {

    private static final Pattern PATRON_CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final EmpresaRepository empresaRepository;

    public ValidadorEmpresa(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public void validar(String nit, String nombre, String correo, Long idExcluido) {
        if (nit == null || nit.isBlank()) {
            throw new ValidacionException("El NIT de la empresa es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ValidacionException("El nombre de la empresa es obligatorio");
        }
        if (correo != null && !correo.isBlank()
                && !PATRON_CORREO.matcher(correo.trim()).matches()) {
            throw new ValidacionException("El correo de la empresa no tiene un formato valido");
        }
        if (empresaRepository.existeNit(normalizarNit(nit), idExcluido)) {
            throw new ValidacionException("El NIT ya esta registrado");
        }
        if (empresaRepository.existeNombre(nombre.trim(), idExcluido)) {
            throw new ValidacionException("El nombre de la empresa ya esta registrado");
        }
    }

    public String normalizarNit(String nit) {
        return nit.trim().toUpperCase(Locale.ROOT);
    }

    public String normalizarCorreo(String correo) {
        return correo == null || correo.isBlank()
                ? null : correo.trim().toLowerCase(Locale.ROOT);
    }
}
