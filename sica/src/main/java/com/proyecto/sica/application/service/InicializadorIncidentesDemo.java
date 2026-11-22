package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.entity.EstadoIncidente;
import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.GravedadIncidente;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.TipoSolicitudVisita;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.EstadoVisitaRepository;
import com.proyecto.sica.domain.repository.IncidenteRepository;
import com.proyecto.sica.domain.repository.PersonaRepository;
import com.proyecto.sica.domain.repository.UsuarioRepository;
import com.proyecto.sica.domain.repository.VisitaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Crea tres incidentes ficticios sin duplicar registros existentes. */
public final class InicializadorIncidentesDemo {
    private final IncidenteRepository incidentes;
    private final VisitaRepository visitas;
    private final EstadoVisitaRepository estados;
    private final PersonaRepository personas;
    private final UsuarioRepository usuarios;

    public InicializadorIncidentesDemo(IncidenteRepository incidentes,
            VisitaRepository visitas, EstadoVisitaRepository estados,
            PersonaRepository personas, UsuarioRepository usuarios) {
        this.incidentes = incidentes;
        this.visitas = visitas;
        this.estados = estados;
        this.personas = personas;
        this.usuarios = usuarios;
    }

    public List<String> ejecutar() {
        Usuario supervisor = usuario("supervisor@sica.example");
        Usuario guarda = usuario("guarda@sica.example");
        Usuario funcionario = usuario("funcionario@sica.example");
        Visita restriccion = obtenerVisitaRestriccion(supervisor, funcionario);
        List<Incidente> demos = List.of(
                incidente("DEMO_NOVEDAD", visita("DEMO-VIS-PRE"), guarda,
                        GravedadIncidente.BAJA, EstadoIncidente.ABIERTO,
                        "Novedad ficticia durante la recepcion del invitado.", null),
                incidente("DEMO_SUPERVISION", visita("DEMO-SALIDA-ABIERTA"), supervisor,
                        GravedadIncidente.MEDIA, EstadoIncidente.EN_REVISION,
                        "Salida olvidada revisada por el supervisor de seguridad.",
                        "Se solicito validar el procedimiento de salida."),
                incidente("DEMO_RESTRICCION", restriccion, supervisor,
                        GravedadIncidente.ALTA, EstadoIncidente.CERRADO,
                        "Incidente ficticio previo a una prohibicion de ingreso.",
                        "La restriccion se aplico con el modulo de estados de acceso."));

        List<String> resultado = new ArrayList<>();
        for (Incidente demo : demos) {
            if (incidentes.existeTipoEnVisita(demo.getTipo(), demo.getVisita().getId())) {
                resultado.add("Omitido incidente demo (ya existe): " + demo.getTipo());
                continue;
            }
            EventoAuditoria evento = new EventoAuditoria(demo.getReportadoPor().getId(), null,
                    "INICIALIZAR_INCIDENTE_DEMO", "incidentes", null, "EXITOSO",
                    "Inicializacion controlada de " + demo.getTipo());
            incidentes.crearConAuditoria(demo, evento);
            resultado.add("Creado incidente demo: " + demo.getTipo());
        }
        return resultado;
    }

    private Visita obtenerVisitaRestriccion(Usuario supervisor, Usuario funcionario) {
        return visitas.buscarPorNumero("DEMO-INC-RESTRICCION").orElseGet(() -> {
            Persona persona = personas.buscarPorDocumento("CC", "10000002")
                    .orElseThrow(() -> new IllegalStateException("No existe Bruno demo"));
            EstadoVisita fuera = estados.buscarPorCodigo("FUERA").orElseThrow();
            LocalDateTime fecha = LocalDateTime.now().minusDays(10);
            Visita visita = new Visita(null, "DEMO-INC-RESTRICCION", persona, funcionario,
                    fuera, supervisor.getId(), supervisor.getId(), null,
                    TipoSolicitudVisita.PRERREGISTRADA, "Visita historica ficticia", null,
                    fecha.minusHours(1), fecha.minusHours(1), fecha.minusMinutes(50),
                    fecha, fecha.plusHours(1), null, null, null, null,
                    "Dato academico para incidente y restriccion");
            EventoAuditoria evento = new EventoAuditoria(supervisor.getId(), null,
                    "INICIALIZAR_VISITA_DEMO", "visitas", null, "EXITOSO",
                    "Visita historica para incidente demo");
            return visitas.crearConAuditoria(visita, evento);
        });
    }

    private Incidente incidente(String tipo, Visita visita, Usuario responsable,
            GravedadIncidente gravedad, EstadoIncidente estado, String descripcion,
            String acciones) {
        LocalDateTime fecha = LocalDateTime.now().minusDays(2);
        return new Incidente(null, visita, visita.getVisitante(), responsable, tipo,
                gravedad, estado, descripcion, acciones, fecha, null,
                estado == EstadoIncidente.CERRADO ? fecha.plusHours(3) : null);
    }

    private Visita visita(String numero) {
        return visitas.buscarPorNumero(numero)
                .orElseThrow(() -> new IllegalStateException("No existe visita demo " + numero));
    }

    private Usuario usuario(String correo) {
        return usuarios.buscarPorCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("No existe usuario demo " + correo));
    }
}
