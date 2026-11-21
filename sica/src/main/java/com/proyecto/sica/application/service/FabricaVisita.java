package com.proyecto.sica.application.service;

import com.proyecto.sica.application.usecase.SolicitudVisita;
import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.TipoSolicitudVisita;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import java.time.LocalDateTime;

/** Construye visitas nuevas con identificadores y fechas consistentes. */
public final class FabricaVisita {
    private final GeneradorNumeroVisita generador;

    public FabricaVisita(GeneradorNumeroVisita generador) {
        this.generador = generador;
    }

    public Visita crear(Persona persona, Usuario anfitrion, EstadoVisita estado,
            Usuario actor, TipoSolicitudVisita tipo, SolicitudVisita solicitud,
            boolean respondida, String codigoPase, LocalDateTime vencimientoPase) {
        LocalDateTime ahora = LocalDateTime.now();
        return new Visita(null, generador.generarSolicitud(), persona, anfitrion, estado,
                actor.getId(), respondida ? actor.getId() : null, null, tipo,
                solicitud.getMotivo().trim(), opcional(solicitud.getPlacaVehiculo()), ahora,
                solicitud.getFechaProgramada(), respondida ? ahora : null, null, null,
                null, null, codigoPase, vencimientoPase,
                opcional(solicitud.getObservaciones()));
    }

    public String generarCodigoPase() {
        return generador.generarPase();
    }

    private String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
