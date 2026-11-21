package com.proyecto.sica.application.service;

import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.TipoSolicitudVisita;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.EstadoVisitaRepository;
import com.proyecto.sica.domain.repository.PersonaRepository;
import com.proyecto.sica.domain.repository.UsuarioRepository;
import com.proyecto.sica.domain.repository.VisitaRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Crea cuatro escenarios ficticios solo cuando se solicita por consola. */
public final class InicializadorVisitasDemo {
    private final VisitaRepository visitaRepository;
    private final EstadoVisitaRepository estadoRepository;
    private final PersonaRepository personaRepository;
    private final UsuarioRepository usuarioRepository;

    public InicializadorVisitasDemo(VisitaRepository visitaRepository,
            EstadoVisitaRepository estadoRepository, PersonaRepository personaRepository,
            UsuarioRepository usuarioRepository) {
        this.visitaRepository = visitaRepository;
        this.estadoRepository = estadoRepository;
        this.personaRepository = personaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<String> ejecutar() {
        Usuario guarda = usuario("guarda@sica.example");
        Usuario funcionario = usuario("funcionario@sica.example");
        Persona invitada = persona("CE", "20000001");
        Persona trabajador = persona("CC", "10000001");
        LocalDateTime ahora = LocalDateTime.now();
        List<Visita> demos = List.of(
                visita("DEMO-VIS-PRE", invitada, funcionario, estado("APROBADA"),
                        funcionario, funcionario.getId(), TipoSolicitudVisita.PRERREGISTRADA,
                        "Reunion academica prerregistrada", ahora.plusDays(1), null, null, null),
                visita("DEMO-VIS-NO", invitada, funcionario,
                        estado("PENDIENTE_APROBACION"), guarda, null,
                        TipoSolicitudVisita.NO_ANUNCIADA, "Visita no anunciada de demostracion",
                        ahora, null, null, null),
                visita("DEMO-PASE-TEMP", trabajador, funcionario,
                        estado("PENDIENTE_APROBACION_OLVIDO"), guarda, null,
                        TipoSolicitudVisita.PASE_TEMPORAL, "Carnet olvidado", ahora,
                        "PASE-DEMO-ACADEMICO", ahora.toLocalDate().atTime(LocalTime.MAX), null),
                visita("DEMO-SALIDA-ABIERTA", trabajador, funcionario, estado("DENTRO"),
                        guarda, funcionario.getId(), TipoSolicitudVisita.PRERREGISTRADA,
                        "Salida olvidada de demostracion", ahora.minusHours(5), null, null,
                        ahora.minusHours(4)));

        List<String> resultados = new ArrayList<>();
        for (Visita demo : demos) {
            if (visitaRepository.buscarPorNumero(demo.getNumeroSolicitud()).isPresent()) {
                resultados.add("Omitida visita demo (ya existe): " + demo.getNumeroSolicitud());
                continue;
            }
            EventoAuditoria evento = new EventoAuditoria(demo.getRegistradoPorUsuarioId(), null,
                    "INICIALIZAR_VISITA_DEMO", "visitas", null, "EXITOSO",
                    "Inicializacion controlada de " + demo.getNumeroSolicitud());
            visitaRepository.crearConAuditoria(demo, evento);
            resultados.add("Creada visita demo: " + demo.getNumeroSolicitud());
        }
        return resultados;
    }

    private Visita visita(String numero, Persona persona, Usuario anfitrion,
            EstadoVisita estado, Usuario registradoPor, Long respondidoPor,
            TipoSolicitudVisita tipo, String motivo, LocalDateTime programada,
            String codigoPase, LocalDateTime vencimiento, LocalDateTime ingreso) {
        LocalDateTime solicitud = programada.minusMinutes(30);
        return new Visita(null, numero, persona, anfitrion, estado, registradoPor.getId(),
                respondidoPor, null, tipo, motivo, null, solicitud, programada,
                respondidoPor == null ? null : solicitud.plusMinutes(5), ingreso,
                null, null, null, codigoPase, vencimiento, "Dato academico ficticio");
    }

    private Usuario usuario(String correo) {
        return usuarioRepository.buscarPorCorreo(correo)
                .orElseThrow(() -> new IllegalStateException(
                        "Inicialice primero el usuario demo " + correo));
    }

    private Persona persona(String tipo, String numero) {
        return personaRepository.buscarPorDocumento(tipo, numero)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe la persona demo " + numero));
    }

    private EstadoVisita estado(String codigo) {
        return estadoRepository.buscarPorCodigo(codigo)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el estado demo " + codigo));
    }
}
