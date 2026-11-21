package com.proyecto.sica.application.usecase;

import static com.proyecto.sica.support.DatosPrueba.rolConPermisos;
import static com.proyecto.sica.support.DatosPrueba.usuario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.sica.application.service.FabricaVisita;
import com.proyecto.sica.application.service.GeneradorNumeroVisita;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorVisita;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Permiso;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.ResultadoCheckIn;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.TipoPersona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.ConflictoPersistenciaException;
import com.proyecto.sica.domain.repository.EstadoVisitaRepository;
import com.proyecto.sica.domain.repository.VisitaRepository;
import com.proyecto.sica.support.RepositoriosMemoria.AuditoriaMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.EmpresasMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.EstadosAccesoMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.PersonasMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.UsuariosMemoria;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VisitaUseCaseTest {
    private AuditoriaMemoria auditoria;
    private VisitasMemoria visitas;
    private Usuario guarda;
    private Usuario funcionario;
    private Usuario funcionarioAjeno;
    private Persona invitada;
    private Persona trabajador;
    private PrerregistrarVisitaUseCase prerregistrar;
    private SolicitarVisitaNoAnunciadaUseCase solicitar;
    private SolicitarPaseTemporalUseCase solicitarPase;
    private ResponderSolicitudVisitaUseCase responder;
    private RegistrarCheckInUseCase checkIn;
    private RegistrarCheckOutUseCase checkOut;
    private RegularizarSalidaOlvidadaUseCase regularizar;

    @BeforeEach
    void preparar() {
        auditoria = new AuditoriaMemoria();
        EmpresasMemoria empresas = new EmpresasMemoria(auditoria);
        Empresa empresa = new Empresa(2L, "900-2", "Empresa Demo", null, null, null, true);
        Empresa empresaAjena = new Empresa(3L, "900-3", "Empresa Ajena", null, null, null, true);
        empresas.agregar(empresa);
        empresas.agregar(empresaAjena);

        EstadosAccesoMemoria estadosAcceso = new EstadosAccesoMemoria();
        EstadoAcceso activo = new EstadoAcceso(5, "ACTIVO", "Activo", null);
        EstadoAcceso prohibido = new EstadoAcceso(6, "PROHIBICION_INGRESO", "Prohibido", null);
        estadosAcceso.agregar(activo);
        estadosAcceso.agregar(prohibido);
        PersonasMemoria personas = new PersonasMemoria(auditoria, estadosAcceso);
        invitada = persona(10, empresa, activo, TipoPersona.VISITANTE, "100");
        trabajador = persona(11, empresa, activo, TipoPersona.TRABAJADOR, "101");
        personas.agregar(invitada);
        personas.agregar(trabajador);
        personas.agregar(persona(12, empresa, prohibido, TipoPersona.VISITANTE, "102"));

        UsuariosMemoria usuarios = new UsuariosMemoria(auditoria);
        funcionario = funcionario(40, empresa, "funcionario@sica.example");
        funcionarioAjeno = funcionario(41, empresaAjena, "ajeno@sica.example");
        guarda = usuario(30, rolConPermisos("SOLICITAR_INGRESO", "CONSULTAR_VISITA",
                "REGISTRAR_CHECK_IN", "REGISTRAR_CHECK_OUT",
                "REGULARIZAR_SALIDA_OLVIDADA", "CONSULTAR_HISTORIAL_VISITAS"),
                "guarda@sica.example", "hash", true);
        usuarios.agregar(funcionario);
        usuarios.agregar(funcionarioAjeno);
        usuarios.agregar(guarda);

        EstadosVisitaMemoria estadosVisita = new EstadosVisitaMemoria();
        estadosVisita.agregar(9, "APROBADA");
        estadosVisita.agregar(10, "PENDIENTE_APROBACION");
        estadosVisita.agregar(11, "PENDIENTE_APROBACION_OLVIDO");
        estadosVisita.agregar(3, "RECHAZADA");
        estadosVisita.agregar(12, "DENTRO");
        estadosVisita.agregar(13, "FUERA");
        estadosVisita.agregar(15, "CERRADA_POR_SISTEMA");
        visitas = new VisitasMemoria(estadosVisita, auditoria);

        ServicioAutorizacion autorizacion = new ServicioAutorizacion();
        ValidadorVisita validador = new ValidadorVisita(
                personas, empresas, usuarios, estadosVisita);
        FabricaVisita fabrica = new FabricaVisita(new GeneradorNumeroVisita());
        prerregistrar = new PrerregistrarVisitaUseCase(visitas, autorizacion, validador, fabrica);
        solicitar = new SolicitarVisitaNoAnunciadaUseCase(visitas, autorizacion, validador, fabrica);
        solicitarPase = new SolicitarPaseTemporalUseCase(visitas, autorizacion, validador, fabrica);
        responder = new ResponderSolicitudVisitaUseCase(visitas, autorizacion, validador);
        checkIn = new RegistrarCheckInUseCase(visitas, autorizacion, validador);
        checkOut = new RegistrarCheckOutUseCase(visitas, autorizacion, validador);
        regularizar = new RegularizarSalidaOlvidadaUseCase(visitas, autorizacion, validador);
    }

    @Test
    void creaPrerregistroAprobadoSinIngreso() {
        Visita visita = prerregistrar.ejecutar(funcionario, solicitud(invitada, funcionario));
        assertEquals("APROBADA", visita.getEstado().getCodigo());
        assertEquals(null, visita.getFechaIngreso());
    }

    @Test
    void creaApruebaYRechazaSolicitudesPendientes() {
        Visita aprobable = solicitar.ejecutar(guarda, solicitud(invitada, funcionario));
        assertEquals("PENDIENTE_APROBACION", aprobable.getEstado().getCodigo());
        assertEquals("APROBADA", responder.aprobar(funcionario, aprobable.getId())
                .getEstado().getCodigo());

        Visita rechazable = solicitar.ejecutar(guarda, solicitud(invitada, funcionario));
        assertEquals("RECHAZADA", responder.rechazar(funcionario, rechazable.getId())
                .getEstado().getCodigo());
    }

    @Test
    void impideAprobacionAjenaYCheckInPendiente() {
        Visita pendiente = solicitar.ejecutar(guarda, solicitud(invitada, funcionario));
        assertThrows(ValidacionException.class,
                () -> responder.aprobar(funcionarioAjeno, pendiente.getId()));
        assertThrows(ValidacionException.class, () -> checkIn.ejecutar(guarda, pendiente.getId()));
    }

    @Test
    void registraCheckInCheckOutYEvitaSegundaSalida() {
        Visita aprobada = prerregistrar.ejecutar(funcionario, solicitud(invitada, funcionario));
        assertEquals("DENTRO", checkIn.ejecutar(guarda, aprobada.getId())
                .getVisitaIngresada().getEstado().getCodigo());
        assertEquals("FUERA", checkOut.ejecutar(guarda, aprobada.getId()).getEstado().getCodigo());
        assertThrows(ValidacionException.class, () -> checkOut.ejecutar(guarda, aprobada.getId()));
    }

    @Test
    void paseTemporalEsDeUnSoloIngreso() {
        Visita pase = solicitarPase.ejecutar(guarda, solicitud(trabajador, funcionario));
        assertEquals("PENDIENTE_APROBACION_OLVIDO", pase.getEstado().getCodigo());
        assertNotNull(pase.getCodigoPase());
        responder.aprobar(funcionario, pase.getId());
        checkIn.ejecutar(guarda, pase.getId());
        assertThrows(ValidacionException.class, () -> checkIn.ejecutar(guarda, pase.getId()));
    }

    @Test
    void regularizaSalidaOlvidadaYPermiteNuevoIngreso() {
        Visita anterior = prerregistrar.ejecutar(funcionario, solicitud(invitada, funcionario));
        checkIn.ejecutar(guarda, anterior.getId());
        Visita nueva = prerregistrar.ejecutar(funcionario, solicitud(invitada, funcionario));

        ResultadoCheckIn resultado = checkIn.ejecutar(guarda, nueva.getId());

        assertTrue(resultado.huboRegularizacion());
        assertEquals(anterior.getId(), resultado.getVisitaRegularizadaId());
        assertEquals("CERRADA_POR_SISTEMA",
                visitas.buscarPorId(anterior.getId()).orElseThrow().getEstado().getCodigo());
        assertEquals("DENTRO", resultado.getVisitaIngresada().getEstado().getCodigo());
    }

    @Test
    void regularizaManualmenteYAudita() {
        Visita visita = prerregistrar.ejecutar(funcionario, solicitud(invitada, funcionario));
        checkIn.ejecutar(guarda, visita.getId());
        Visita cerrada = regularizar.ejecutar(guarda, visita.getId(), "Olvido confirmado");
        assertEquals("CERRADA_POR_SISTEMA", cerrada.getEstado().getCodigo());
        assertTrue(auditoria.getEventos().stream().anyMatch(
                evento -> evento.getAccion().equals("REGULARIZAR_SALIDA_OLVIDADA")));
    }

    @Test
    void deniegaPersonaConProhibicion() {
        SolicitudVisita solicitud = new SolicitudVisita(12, 2, funcionario.getId(),
                LocalDateTime.now(), "Visita", null, null);
        assertThrows(ValidacionException.class, () -> solicitar.ejecutar(guarda, solicitud));
    }

    @Test
    void dosCheckInConcurrentesNoCreanDosIngresos() throws Exception {
        Visita visita = prerregistrar.ejecutar(funcionario, solicitud(invitada, funcionario));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> operacion = () -> {
                try {
                    checkIn.ejecutar(guarda, visita.getId());
                    return true;
                } catch (RuntimeException excepcion) {
                    return false;
                }
            };
            Future<Boolean> primero = executor.submit(operacion);
            Future<Boolean> segundo = executor.submit(operacion);
            int exitos = (primero.get() ? 1 : 0) + (segundo.get() ? 1 : 0);
            assertEquals(1, exitos);
            assertEquals("DENTRO", visitas.buscarPorId(visita.getId())
                    .orElseThrow().getEstado().getCodigo());
        } finally {
            executor.shutdownNow();
        }
    }

    private SolicitudVisita solicitud(Persona persona, Usuario anfitrion) {
        return new SolicitudVisita(persona.getId(), anfitrion.getEmpresaId(), anfitrion.getId(),
                LocalDateTime.now().plusHours(1), "Motivo de prueba", "ABC123", null);
    }

    private Persona persona(long id, Empresa empresa, EstadoAcceso estado,
            TipoPersona tipo, String documento) {
        return new Persona(id, empresa, estado, "CC", documento, "Nombre", "Prueba",
                null, null, tipo, true, null);
    }

    private Usuario funcionario(long id, Empresa empresa, String correo) {
        int permisoId = 1;
        Set<Permiso> permisos = Set.of(
                new Permiso(permisoId++, "PRERREGISTRAR_VISITA", "Prueba", null),
                new Permiso(permisoId++, "CONSULTAR_VISITA", "Prueba", null),
                new Permiso(permisoId++, "APROBAR_SOLICITUD", "Prueba", null),
                new Permiso(permisoId, "RECHAZAR_SOLICITUD", "Prueba", null));
        Rol rol = new Rol(4, "Funcionario", null, true, true, permisos);
        return new Usuario(id, rol, empresa.getId(), empresa.getNombre(), "func" + id,
                "hash", "Funcionario", "Prueba", correo, true, null);
    }

    private static final class EstadosVisitaMemoria implements EstadoVisitaRepository {
        private final Map<Integer, EstadoVisita> estados = new LinkedHashMap<>();
        void agregar(int id, String codigo) {
            estados.put(id, new EstadoVisita(id, codigo, codigo, null));
        }
        @Override public Optional<EstadoVisita> buscarPorId(int id) {
            return Optional.ofNullable(estados.get(id));
        }
        @Override public Optional<EstadoVisita> buscarPorCodigo(String codigo) {
            return estados.values().stream().filter(e -> e.getCodigo().equals(codigo)).findFirst();
        }
        @Override public List<EstadoVisita> listar() { return List.copyOf(estados.values()); }
    }

    private static final class VisitasMemoria implements VisitaRepository {
        private final Map<Long, Visita> visitas = new LinkedHashMap<>();
        private final EstadosVisitaMemoria estados;
        private final AuditoriaMemoria auditoria;
        private long siguienteId = 1;

        VisitasMemoria(EstadosVisitaMemoria estados, AuditoriaMemoria auditoria) {
            this.estados = estados;
            this.auditoria = auditoria;
        }

        @Override public synchronized Optional<Visita> buscarPorId(long id) {
            return Optional.ofNullable(visitas.get(id));
        }
        @Override public synchronized Optional<Visita> buscarPorNumero(String numero) {
            return visitas.values().stream().filter(v -> v.getNumeroSolicitud().equals(numero)).findFirst();
        }
        @Override public synchronized List<Visita> buscarPorDocumento(String tipo, String numero) {
            return visitas.values().stream().filter(v ->
                    v.getVisitante().getTipoDocumento().equals(tipo)
                            && v.getVisitante().getNumeroDocumento().equals(numero)).toList();
        }
        @Override public synchronized List<Visita> listarPendientesPorAnfitrion(long id) {
            return visitas.values().stream().filter(v -> v.getAnfitrion().getId() == id
                    && v.getEstado().getCodigo().startsWith("PENDIENTE_APROBACION")).toList();
        }
        @Override public synchronized List<Visita> listarHistorial(String documento,
                LocalDate desde, LocalDate hasta, String estado) {
            return List.copyOf(visitas.values());
        }
        @Override public synchronized Visita crearConAuditoria(Visita visita, EventoAuditoria evento) {
            long id = siguienteId++;
            Visita creada = copiar(visita, id, visita.getEstado(), null, null, null, null);
            visitas.put(id, creada);
            auditoria.registrar(evento.conEntidadId(String.valueOf(id)));
            return creada;
        }
        @Override public synchronized Visita responderConAuditoria(long id, long usuarioId,
                int esperado, int nuevo, EventoAuditoria evento) {
            Visita actual = requerida(id);
            verificar(actual, esperado);
            if (actual.getAnfitrion().getId() != usuarioId) throw new ConflictoPersistenciaException("Ajeno");
            Visita editada = copiar(actual, id, estado(nuevo), LocalDateTime.now(), null, null, null);
            visitas.put(id, editada);
            auditoria.registrar(evento);
            return editada;
        }
        @Override public synchronized ResultadoCheckIn registrarIngresoConAuditoria(long id,
                long usuarioId, int aprobado, int dentro, int cerrada, String motivo,
                EventoAuditoria ingreso, EventoAuditoria regularizacion) {
            Visita actual = requerida(id);
            verificar(actual, aprobado);
            Long anteriorId = visitas.values().stream().filter(v -> !v.getId().equals(id)
                    && v.getVisitante().getId().equals(actual.getVisitante().getId())
                    && v.getEstado().getId() == dentro).map(Visita::getId).findFirst().orElse(null);
            if (anteriorId != null) {
                Visita anterior = requerida(anteriorId);
                visitas.put(anteriorId, copiar(anterior, anteriorId, estado(cerrada), null,
                        LocalDateTime.now(), LocalDateTime.now(), motivo));
                auditoria.registrar(regularizacion.conEntidadId(String.valueOf(anteriorId)));
            }
            Visita ingresada = copiar(actual, id, estado(dentro), null,
                    LocalDateTime.now(), null, null);
            visitas.put(id, ingresada);
            auditoria.registrar(ingreso);
            return new ResultadoCheckIn(ingresada, anteriorId);
        }
        @Override public synchronized Visita registrarSalidaConAuditoria(long id, long usuarioId,
                int dentro, int fuera, EventoAuditoria evento) {
            Visita actual = requerida(id);
            verificar(actual, dentro);
            Visita salida = copiar(actual, id, estado(fuera), null,
                    actual.getFechaIngreso(), LocalDateTime.now(), null);
            visitas.put(id, salida);
            auditoria.registrar(evento);
            return salida;
        }
        @Override public synchronized Visita regularizarSalidaConAuditoria(long id,
                long usuarioId, int dentro, int cerrada, String motivo, EventoAuditoria evento) {
            Visita actual = requerida(id);
            verificar(actual, dentro);
            Visita cierre = copiar(actual, id, estado(cerrada), null,
                    actual.getFechaIngreso(), LocalDateTime.now(), motivo);
            visitas.put(id, cierre);
            auditoria.registrar(evento);
            return cierre;
        }
        private Visita requerida(long id) {
            Visita visita = visitas.get(id);
            if (visita == null) throw new ConflictoPersistenciaException("No existe");
            return visita;
        }
        private void verificar(Visita visita, int esperado) {
            if (visita.getEstado().getId() != esperado) {
                throw new ConflictoPersistenciaException("Estado concurrente");
            }
        }
        private EstadoVisita estado(int id) { return estados.buscarPorId(id).orElseThrow(); }
        private Visita copiar(Visita v, long id, EstadoVisita estado,
                LocalDateTime respuesta, LocalDateTime ingreso, LocalDateTime salida,
                String motivoRegularizacion) {
            LocalDateTime cierre = "CERRADA_POR_SISTEMA".equals(estado.getCodigo())
                    ? LocalDateTime.now() : v.getFechaCierreSistema();
            return new Visita(id, v.getNumeroSolicitud(), v.getVisitante(), v.getAnfitrion(),
                    estado, v.getRegistradoPorUsuarioId(),
                    respuesta == null ? v.getRespondidoPorUsuarioId() : v.getAnfitrion().getId(),
                    cierre == null ? v.getRegularizadaPorUsuarioId() : Long.valueOf(30),
                    v.getTipoSolicitud(), v.getMotivo(), v.getPlacaVehiculo(),
                    v.getFechaSolicitud(), v.getFechaProgramada(),
                    respuesta == null ? v.getFechaRespuesta() : respuesta,
                    ingreso == null ? v.getFechaIngreso() : ingreso,
                    salida == null ? v.getFechaSalida() : salida, cierre,
                    motivoRegularizacion == null ? v.getMotivoRegularizacion() : motivoRegularizacion,
                    v.getCodigoPase(), v.getFechaVencimientoPase(), v.getObservaciones());
        }
    }
}
