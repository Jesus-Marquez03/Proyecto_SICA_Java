package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.entity.EstadoIncidente;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.domain.entity.Usuario;
import java.time.LocalDateTime;

/** Validaciones compartidas por el registro y seguimiento de incidentes. */
public final class ValidadorIncidente {
    public String textoObligatorio(String valor, String mensaje, int maximo) {
        if (valor == null || valor.isBlank()) throw new ValidacionException(mensaje);
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new ValidacionException("El texto supera " + maximo + " caracteres");
        }
        return limpio;
    }

    public String textoOpcional(String valor, int maximo) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new ValidacionException("El texto supera " + maximo + " caracteres");
        }
        return limpio;
    }

    public LocalDateTime fechaValida(LocalDateTime fecha) {
        if (fecha == null) throw new ValidacionException("La fecha del incidente es obligatoria");
        if (fecha.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new ValidacionException("La fecha del incidente no puede estar en el futuro");
        }
        return fecha;
    }

    public void verificarAmbito(Usuario actor, Incidente incidente) {
        if (actor.getRol().isRequiereEmpresa()) {
            Long empresaPersona = incidente.getPersona().getEmpresa() == null
                    ? null : incidente.getPersona().getEmpresa().getId();
            if (actor.getEmpresaId() == null || !actor.getEmpresaId().equals(empresaPersona)) {
                throw new AccesoDenegadoException("El incidente pertenece a otra empresa");
            }
        }
    }

    public void verificarTransicion(EstadoIncidente anterior, EstadoIncidente nuevo) {
        if (anterior == EstadoIncidente.CERRADO && nuevo != EstadoIncidente.CERRADO) {
            throw new ValidacionException("Un incidente cerrado no puede reabrirse");
        }
    }
}
