package com.proyecto.sica.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.usecase.SolicitudPersona;
import com.proyecto.sica.application.usecase.SolicitudVisita;
import com.proyecto.sica.config.ConfiguracionAplicacion;
import com.proyecto.sica.config.ConfiguracionBaseDatos;
import com.proyecto.sica.domain.entity.EstadoVisita;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.TipoPersona;
import com.proyecto.sica.domain.entity.TipoSolicitudVisita;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import com.proyecto.sica.infrastructure.persistence.AuditoriaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.EjecutorTransaccionalJdbc;
import com.proyecto.sica.infrastructure.persistence.EmpresaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.EstadoAccesoJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.EstadoVisitaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.PersonaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.RolJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.UsuarioJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.VisitaJdbcRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

/** Verifica los cuatro flujos, bloqueos y rollback sobre MySQL real. */
class ControlVisitasMysqlIT {

    @Test
    void verificaFlujosConcurrenciaAuditoriaYRollback() throws Exception {
        ConfiguracionAplicacion appGuarda = new ConfiguracionAplicacion();
        appGuarda.getInicializadorUsuariosDemo().ejecutar();
        appGuarda.getInicializadorVisitasDemo().ejecutar();
        Usuario guarda = appGuarda.getServicioAutenticacion().iniciarSesion(
                "guarda@sica.example", "SicaGuarda2026!".toCharArray());

        ConfiguracionAplicacion appFuncionario = new ConfiguracionAplicacion();
        Usuario funcionario = appFuncionario.getServicioAutenticacion().iniciarSesion(
                "funcionario@sica.example", "SicaFuncionario2026!".toCharArray());
        assertEquals(java.util.List.of(funcionario.getId()),
                appGuarda.getConsultarAnfitrionesUseCase()
                        .ejecutar(guarda, funcionario.getEmpresaId()).stream()
                        .map(Usuario::getId).toList());

        String documento = "IT" + System.currentTimeMillis();
        Persona invitada = appGuarda.getCrearPersonaUseCase().ejecutar(guarda,
                new SolicitudPersona(null, "CC", documento, "Invitada", "Integracion",
                        null, null, TipoPersona.VISITANTE, null));

        // Dos instancias: el guarda crea y el funcionario ve la solicitud sin reiniciar.
        Visita pendiente = appGuarda.getSolicitarVisitaNoAnunciadaUseCase().ejecutar(guarda,
                solicitud(invitada, funcionario));
        assertTrue(appFuncionario.getConsultarVisitasUseCase()
                .pendientesAsignadas(funcionario).stream()
                .anyMatch(visita -> visita.getId().equals(pendiente.getId())));
        appFuncionario.getResponderSolicitudVisitaUseCase().aprobar(funcionario, pendiente.getId());
        appGuarda.getRegistrarCheckInUseCase().ejecutar(guarda, pendiente.getId());
        assertEquals("FUERA", appGuarda.getRegistrarCheckOutUseCase()
                .ejecutar(guarda, pendiente.getId()).getEstado().getCodigo());

        // Aprobar y rechazar simultaneamente: solo una respuesta puede confirmar.
        Visita competencia = appGuarda.getSolicitarVisitaNoAnunciadaUseCase().ejecutar(
                guarda, solicitud(invitada, funcionario));
        int respuestasExitosas = ejecutarDos(() -> {
            appFuncionario.getResponderSolicitudVisitaUseCase()
                    .aprobar(funcionario, competencia.getId());
            return true;
        }, () -> {
            appFuncionario.getResponderSolicitudVisitaUseCase()
                    .rechazar(funcionario, competencia.getId());
            return true;
        });
        assertEquals(1, respuestasExitosas);

        // Dos check-in sobre la misma autorizacion: solo uno cambia el estado.
        Visita concurrente = appFuncionario.getPrerregistrarVisitaUseCase().ejecutar(
                funcionario, solicitud(invitada, funcionario));
        int ingresosExitosos = ejecutarDos(() -> {
            appGuarda.getRegistrarCheckInUseCase().ejecutar(guarda, concurrente.getId());
            return true;
        }, () -> {
            appGuarda.getRegistrarCheckInUseCase().ejecutar(guarda, concurrente.getId());
            return true;
        });
        assertEquals(1, ingresosExitosos);
        appGuarda.getRegistrarCheckOutUseCase().ejecutar(guarda, concurrente.getId());

        // Una visita abierta se cierra junto con el nuevo ingreso.
        Visita anterior = appFuncionario.getPrerregistrarVisitaUseCase().ejecutar(
                funcionario, solicitud(invitada, funcionario));
        appGuarda.getRegistrarCheckInUseCase().ejecutar(guarda, anterior.getId());
        Visita nueva = appFuncionario.getPrerregistrarVisitaUseCase().ejecutar(
                funcionario, solicitud(invitada, funcionario));
        var resultado = appGuarda.getRegistrarCheckInUseCase().ejecutar(guarda, nueva.getId());
        assertEquals(anterior.getId(), resultado.getVisitaRegularizadaId());
        assertEquals("CERRADA_POR_SISTEMA", appGuarda.getConsultarVisitasUseCase()
                .buscarPorId(guarda, anterior.getId()).getEstado().getCodigo());
        appGuarda.getRegistrarCheckOutUseCase().ejecutar(guarda, nueva.getId());

        // Pase temporal aprobado, usado una vez y persona prohibida rechazada.
        Persona trabajador = appGuarda.getBuscarPersonaPorDocumentoUseCase()
                .ejecutar(guarda, "CC", "10000001");
        Visita pase = appGuarda.getSolicitarPaseTemporalUseCase().ejecutar(
                guarda, solicitud(trabajador, funcionario));
        assertNotNull(pase.getCodigoPase());
        appFuncionario.getResponderSolicitudVisitaUseCase().aprobar(funcionario, pase.getId());
        appGuarda.getRegistrarCheckInUseCase().ejecutar(guarda, pase.getId());
        assertThrows(ValidacionException.class,
                () -> appGuarda.getRegistrarCheckInUseCase().ejecutar(guarda, pase.getId()));
        appGuarda.getRegistrarCheckOutUseCase().ejecutar(guarda, pase.getId());
        Persona prohibida = appGuarda.getBuscarPersonaPorDocumentoUseCase()
                .ejecutar(guarda, "CC", "10000002");
        assertThrows(ValidacionException.class,
                () -> appGuarda.getSolicitarPaseTemporalUseCase().ejecutar(
                        guarda, solicitud(prohibida, funcionario)));

        verificarRollbackPorFalloAuditoria(invitada, funcionario);
        assertTrue(contarAuditoriasVisitas() >= 8);
        appGuarda.getServicioAutenticacion().cerrarSesion();
        appFuncionario.getServicioAutenticacion().cerrarSesion();
    }

    private SolicitudVisita solicitud(Persona persona, Usuario funcionario) {
        return new SolicitudVisita(persona.getId(), funcionario.getEmpresaId(),
                funcionario.getId(), LocalDateTime.now().plusMinutes(10),
                "Prueba de integracion", "IT123", null);
    }

    private int ejecutarDos(Callable<Boolean> primera, Callable<Boolean> segunda)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> a = executor.submit(() -> intentar(primera));
            Future<Boolean> b = executor.submit(() -> intentar(segunda));
            return (a.get() ? 1 : 0) + (b.get() ? 1 : 0);
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean intentar(Callable<Boolean> operacion) {
        try {
            return operacion.call();
        } catch (Exception excepcion) {
            return false;
        }
    }

    private void verificarRollbackPorFalloAuditoria(Persona persona, Usuario funcionario) {
        ConnectionFactory factory = new ConnectionFactory(ConfiguracionBaseDatos.cargar());
        AuditoriaJdbcRepository auditoria = new AuditoriaJdbcRepository(factory);
        EjecutorTransaccionalJdbc transacciones = new EjecutorTransaccionalJdbc(factory, auditoria);
        RolJdbcRepository roles = new RolJdbcRepository(factory);
        EmpresaJdbcRepository empresas = new EmpresaJdbcRepository(factory, transacciones);
        EstadoAccesoJdbcRepository estadosAcceso = new EstadoAccesoJdbcRepository(factory);
        PersonaJdbcRepository personas = new PersonaJdbcRepository(factory, transacciones);
        UsuarioJdbcRepository usuarios = new UsuarioJdbcRepository(factory, roles, auditoria);
        EstadoVisitaJdbcRepository estadosVisita = new EstadoVisitaJdbcRepository(factory);
        VisitaJdbcRepository visitas = new VisitaJdbcRepository(
                factory, personas, usuarios, transacciones);
        EstadoVisita aprobada = estadosVisita.buscarPorCodigo("APROBADA").orElseThrow();
        String numero = "IT-ROLL-" + UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime ahora = LocalDateTime.now();
        Visita visita = new Visita(null, numero, persona, funcionario, aprobada,
                funcionario.getId(), funcionario.getId(), null,
                TipoSolicitudVisita.PRERREGISTRADA, "Debe revertirse", null,
                ahora, ahora.plusHours(1), ahora, null, null, null, null,
                null, null, null);
        EventoAuditoria invalida = new EventoAuditoria(999999999L, null,
                "IT_ROLLBACK", "visitas", null, "EXITOSO", "Debe fallar por FK");

        assertThrows(RepositorioException.class,
                () -> visitas.crearConAuditoria(visita, invalida));
        assertFalse(visitas.buscarPorNumero(numero).isPresent());
        assertTrue(empresas.existeActiva(funcionario.getEmpresaId()));
        assertNotNull(estadosAcceso.buscarPorCodigo("ACTIVO").orElseThrow());
    }

    private long contarAuditoriasVisitas() throws Exception {
        ConnectionFactory factory = new ConnectionFactory(ConfiguracionBaseDatos.cargar());
        try (Connection conexion = factory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        "SELECT COUNT(*) FROM bitacora_auditoria WHERE entidad='visitas'");
                ResultSet resultado = sentencia.executeQuery()) {
            resultado.next();
            return resultado.getLong(1);
        }
    }
}
