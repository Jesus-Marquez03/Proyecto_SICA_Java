package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.TipoPersona;
import com.proyecto.sica.domain.repository.EmpresaRepository;
import com.proyecto.sica.domain.repository.EstadoAccesoRepository;
import com.proyecto.sica.domain.repository.PersonaRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Pattern;

/** Centraliza las reglas de trabajadores e invitados. */
public final class ValidadorPersona {
    private static final Pattern PATRON_CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final PersonaRepository personaRepository;
    private final EmpresaRepository empresaRepository;
    private final EstadoAccesoRepository estadoRepository;

    public ValidadorPersona(PersonaRepository personaRepository,
            EmpresaRepository empresaRepository, EstadoAccesoRepository estadoRepository) {
        this.personaRepository = personaRepository;
        this.empresaRepository = empresaRepository;
        this.estadoRepository = estadoRepository;
    }

    public void validarTextos(String tipoDocumento, String numeroDocumento,
            String nombres, String apellidos, String correo, String fotoUrl) {
        obligatorio(tipoDocumento, "El tipo de documento es obligatorio");
        obligatorio(numeroDocumento, "El numero de documento es obligatorio");
        obligatorio(nombres, "Los nombres son obligatorios");
        obligatorio(apellidos, "Los apellidos son obligatorios");
        if (correo != null && !correo.isBlank()
                && !PATRON_CORREO.matcher(correo.trim()).matches()) {
            throw new ValidacionException("El correo de la persona no tiene un formato valido");
        }
        validarFotoUrl(fotoUrl);
    }

    public void validarDocumentoUnico(String tipoDocumento,
            String numeroDocumento, Long idExcluido) {
        if (personaRepository.existeDocumento(normalizarTipoDocumento(tipoDocumento),
                numeroDocumento.trim(), idExcluido)) {
            throw new ValidacionException("El documento ya esta registrado");
        }
    }

    public Empresa obtenerEmpresaValida(Long empresaId, TipoPersona tipoPersona) {
        if (tipoPersona == TipoPersona.TRABAJADOR && empresaId == null) {
            throw new ValidacionException("Un trabajador debe pertenecer a una empresa");
        }
        if (empresaId == null) {
            return null;
        }
        Empresa empresa = empresaRepository.buscarPorId(empresaId)
                .orElseThrow(() -> new ValidacionException("La empresa indicada no existe"));
        if (!empresa.isActiva()) {
            throw new ValidacionException("La empresa indicada esta inactiva");
        }
        return empresa;
    }

    public EstadoAcceso obtenerEstado(int estadoId) {
        return estadoRepository.buscarPorId(estadoId)
                .orElseThrow(() -> new ValidacionException("El estado de acceso no existe"));
    }

    public EstadoAcceso obtenerEstadoInicial() {
        return estadoRepository.buscarPorCodigo("ACTIVO")
                .orElseThrow(() -> new ValidacionException(
                        "No se configuro el estado de acceso ACTIVO"));
    }

    public String normalizarTipoDocumento(String valor) {
        return valor.trim().toUpperCase(Locale.ROOT);
    }

    public String normalizarCorreo(String correo) {
        return correo == null || correo.isBlank()
                ? null : correo.trim().toLowerCase(Locale.ROOT);
    }

    private void validarFotoUrl(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.isBlank()) {
            return;
        }
        try {
            URI uri = new URI(fotoUrl.trim());
            if (uri.getScheme() == null
                    || !(uri.getScheme().equalsIgnoreCase("http")
                    || uri.getScheme().equalsIgnoreCase("https"))) {
                throw new ValidacionException("La URL de fotografia debe usar http o https");
            }
        } catch (URISyntaxException excepcion) {
            throw new ValidacionException("La URL de fotografia no es valida");
        }
    }

    private void obligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException(mensaje);
        }
    }
}
