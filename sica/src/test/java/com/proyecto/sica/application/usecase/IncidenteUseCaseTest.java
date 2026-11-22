package com.proyecto.sica.application.usecase;

import static com.proyecto.sica.support.DatosPrueba.rolConPermisos;
import static com.proyecto.sica.support.DatosPrueba.rolSinPermisos;
import static com.proyecto.sica.support.DatosPrueba.usuario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.sica.application.service.AccesoDenegadoException;
import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorIncidente;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.EstadoIncidente;
import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.GravedadIncidente;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.Permiso;
import com.proyecto.sica.domain.entity.RegistroAuditoria;
import com.proyecto.sica.domain.entity.ResultadoCheckIn;
import com.proyecto.sica.domain.entity.TipoPersona;
import com.proyecto.sica.domain.entity.TipoSolicitudVisita;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.repository.ConsultaAuditoriaRepository;
import com.proyecto.sica.domain.repository.FiltroAuditoria;
import com.proyecto.sica.domain.repository.FiltroIncidentes;
import com.proyecto.sica.domain.repository.IncidenteRepository;
import com.proyecto.sica.domain.repository.VisitaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IncidenteUseCaseTest {
    private IncidentesMemoria repositorio;
    private VisitasMemoria visitas;
    private Usuario supervisor;
    private Visita visita;
    private CrearIncidenteUseCase crear;
    private ConsultarIncidentesUseCase consultar;
    private ConsultarIncidentePorIdUseCase consultarId;
    private ActualizarIncidenteUseCase actualizar;

    @BeforeEach
    void preparar() {
        supervisor = usuario(2, rolConPermisos(PermisosSistema.REGISTRAR_INCIDENTE,
                PermisosSistema.CONSULTAR_INCIDENTE, PermisosSistema.ACTUALIZAR_INCIDENTE,
                PermisosSistema.CONSULTAR_AUDITORIA), "supervisor@test", "hash", true);
        Empresa empresa = new Empresa(2L, "NIT", "Empresa", null, null, null, true);
        Persona persona = new Persona(10L, empresa,
                new EstadoAcceso(5, "ACTIVO", "Activo", null), "CC", "100",
                "Persona", "Prueba", null, null, TipoPersona.VISITANTE, true, null);
        visita = new Visita(20L, "VIS-20", persona, supervisor,
                new EstadoVisita(13, "FUERA", "Fuera", null), supervisor.getId(),
                supervisor.getId(), null, TipoSolicitudVisita.PRERREGISTRADA, "Prueba",
                null, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1), null, null, null, null, null);
        visitas = new VisitasMemoria(); visitas.agregar(visita);
        repositorio = new IncidentesMemoria();
        ServicioAutorizacion autorizacion = new ServicioAutorizacion();
        ValidadorIncidente validador = new ValidadorIncidente();
        crear = new CrearIncidenteUseCase(repositorio, visitas, autorizacion, validador);
        consultar = new ConsultarIncidentesUseCase(repositorio, autorizacion);
        consultarId = new ConsultarIncidentePorIdUseCase(repositorio, autorizacion, validador);
        actualizar = new ActualizarIncidenteUseCase(repositorio, autorizacion, validador);
    }

    @Test
    void creaYAuditaIncidenteValido() {
        Incidente creado = crear.ejecutar(supervisor, solicitud("Novedad"));
        assertEquals(visita.getId(), creado.getVisita().getId());
        assertEquals("CREACION_INCIDENTE", repositorio.eventos.get(0).getAccion());
    }

    @Test
    void rechazaDescripcionVaciaYVisitaInexistente() {
        assertThrows(ValidacionException.class, () -> crear.ejecutar(supervisor,
                new SolicitudIncidente(20, "NOVEDAD", GravedadIncidente.BAJA,
                        " ", null, LocalDateTime.now())));
        assertThrows(ValidacionException.class, () -> crear.ejecutar(supervisor,
                new SolicitudIncidente(999, "NOVEDAD", GravedadIncidente.BAJA,
                        "Descripcion", null, LocalDateTime.now())));
    }

    @Test
    void consultaPorIdVisitaPersonaFechaYResponsable() {
        Incidente creado = crear.ejecutar(supervisor, solicitud("Consulta"));
        assertEquals(creado.getId(), consultarId.ejecutar(supervisor, creado.getId()).getId());
        FiltroIncidentes filtro = new FiltroIncidentes(visita.getId(),
                visita.getVisitante().getId(), supervisor.getId(), LocalDate.now(),
                LocalDate.now(), "ABIERTO");
        assertEquals(1, consultar.ejecutar(supervisor, filtro).size());
    }

    @Test
    void rechazaActorSinPermiso() {
        Usuario actor = usuario(8, rolSinPermisos(), "sin@test", "hash", true);
        assertThrows(AccesoDenegadoException.class,
                () -> crear.ejecutar(actor, solicitud("No autorizado")));
        assertThrows(AccesoDenegadoException.class,
                () -> consultar.ejecutar(actor, FiltroIncidentes.todos()));
    }

    @Test
    void impideConsultarIncidenteDeEmpresaAjena() {
        Incidente creado = crear.ejecutar(supervisor, solicitud("Ambito"));
        Rol rolEmpresa = new Rol(4, "Funcionario", "Prueba", true, true,
                Set.of(new Permiso(1, PermisosSistema.CONSULTAR_INCIDENTE,
                        "Consultar", "Prueba")));
        Usuario funcionarioAjeno = new Usuario(50L, rolEmpresa, 99L, "Empresa ajena",
                "ajeno", "hash", "Funcionario", "Ajeno", "ajeno@test", true, null);
        assertThrows(AccesoDenegadoException.class,
                () -> consultarId.ejecutar(funcionarioAjeno, creado.getId()));
    }

    @Test
    void actualizaConJustificacionYConservaReferenciaOriginal() {
        Incidente creado = crear.ejecutar(supervisor, solicitud("Original"));
        Incidente cambiado = actualizar.ejecutar(supervisor, creado.getId(),
                new SolicitudSeguimientoIncidente(GravedadIncidente.ALTA,
                        EstadoIncidente.CERRADO, "Descripcion ampliada", "Se reviso",
                        "Informacion confirmada"));
        assertEquals("VIS-20", cambiado.getVisita().getNumeroSolicitud());
        assertEquals(EstadoIncidente.CERRADO, cambiado.getEstado());
        assertTrue(repositorio.eventos.get(1).getDescripcion().contains("Original"));
    }

    @Test
    void bitacoraEsSoloLecturaYExigePermiso() {
        ConsultaAuditoriaMemoria memoria = new ConsultaAuditoriaMemoria();
        ConsultarAuditoriaUseCase caso = new ConsultarAuditoriaUseCase(
                memoria, new ServicioAutorizacion());
        assertEquals(1, caso.ejecutar(supervisor, FiltroAuditoria.recientes()).size());
        assertFalse(java.util.Arrays.stream(ConsultaAuditoriaRepository.class.getMethods())
                .anyMatch(m -> m.getName().matches("actualizar|eliminar")));
        Usuario sinPermiso = usuario(9, rolSinPermisos(), "no@test", "hash", true);
        assertThrows(AccesoDenegadoException.class,
                () -> caso.ejecutar(sinPermiso, FiltroAuditoria.recientes()));
    }

    private SolicitudIncidente solicitud(String descripcion) {
        return new SolicitudIncidente(visita.getId(), "NOVEDAD",
                GravedadIncidente.MEDIA, descripcion, null, LocalDateTime.now());
    }

    private static final class IncidentesMemoria implements IncidenteRepository {
        private final Map<Long, Incidente> datos = new LinkedHashMap<>();
        private final List<EventoAuditoria> eventos = new ArrayList<>();
        private long secuencia = 1;
        @Override public Optional<Incidente> buscarPorId(long id) { return Optional.ofNullable(datos.get(id)); }
        @Override public List<Incidente> consultar(FiltroIncidentes f, Long empresa) {
            return datos.values().stream().filter(i -> f.visitaId() == null || f.visitaId().equals(i.getVisita().getId()))
                    .filter(i -> f.personaId() == null || f.personaId().equals(i.getPersona().getId()))
                    .filter(i -> f.reportadoPorId() == null || f.reportadoPorId().equals(i.getReportadoPor().getId()))
                    .filter(i -> f.desde() == null || !i.getFechaIncidente().toLocalDate().isBefore(f.desde()))
                    .filter(i -> f.hasta() == null || !i.getFechaIncidente().toLocalDate().isAfter(f.hasta()))
                    .filter(i -> f.estado() == null || f.estado().isBlank() || f.estado().equals(i.getEstado().name()))
                    .toList();
        }
        @Override public Incidente crearConAuditoria(Incidente i, EventoAuditoria e) {
            long id = secuencia++; Incidente c = copiar(i, id); datos.put(id, c);
            eventos.add(e.conEntidadId(String.valueOf(id))); return c;
        }
        @Override public Incidente actualizarConAuditoria(Incidente a, Incidente n, EventoAuditoria e) {
            datos.put(n.getId(), n); eventos.add(e); return n;
        }
        @Override public boolean existeTipoEnVisita(String tipo, long id) { return false; }
        private Incidente copiar(Incidente i, long id) { return new Incidente(id, i.getVisita(), i.getPersona(),
                i.getReportadoPor(), i.getTipo(), i.getGravedad(), i.getEstado(), i.getDescripcion(),
                i.getAccionesTomadas(), i.getFechaIncidente(), LocalDateTime.now(), i.getFechaCierre()); }
    }

    private static final class VisitasMemoria implements VisitaRepository {
        private final Map<Long, Visita> datos = new LinkedHashMap<>();
        void agregar(Visita v) { datos.put(v.getId(), v); }
        @Override public Optional<Visita> buscarPorId(long id) { return Optional.ofNullable(datos.get(id)); }
        @Override public Optional<Visita> buscarPorNumero(String n) { return Optional.empty(); }
        @Override public List<Visita> buscarPorDocumento(String t, String n) { return List.of(); }
        @Override public List<Visita> listarPendientesPorAnfitrion(long id) { return List.of(); }
        @Override public List<Visita> listarHistorial(String n, LocalDate d, LocalDate h, String e, Long a) { return List.of(); }
        @Override public Visita crearConAuditoria(Visita v, EventoAuditoria e) { throw new UnsupportedOperationException(); }
        @Override public Visita responderConAuditoria(long a,long b,int c,int d,EventoAuditoria e){throw new UnsupportedOperationException();}
        @Override public ResultadoCheckIn registrarIngresoConAuditoria(long a,long b,int c,int d,int e,String f,EventoAuditoria g,EventoAuditoria h){throw new UnsupportedOperationException();}
        @Override public Visita registrarSalidaConAuditoria(long a,long b,int c,int d,EventoAuditoria e){throw new UnsupportedOperationException();}
        @Override public Visita regularizarSalidaConAuditoria(long a,long b,int c,int d,String e,EventoAuditoria f){throw new UnsupportedOperationException();}
    }

    private static final class ConsultaAuditoriaMemoria implements ConsultaAuditoriaRepository {
        @Override public List<RegistroAuditoria> consultar(FiltroAuditoria filtro) {
            return List.of(new RegistroAuditoria(1, 2L, "Supervisor", null,
                    "CREACION_INCIDENTE", "incidentes", "1", "EXITOSO",
                    "Prueba", null, LocalDateTime.now()));
        }
    }
}
