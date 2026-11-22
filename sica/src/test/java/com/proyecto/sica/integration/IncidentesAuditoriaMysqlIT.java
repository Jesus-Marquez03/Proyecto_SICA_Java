package com.proyecto.sica.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.sica.application.service.AccesoDenegadoException;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.usecase.SolicitudIncidente;
import com.proyecto.sica.application.usecase.SolicitudPersona;
import com.proyecto.sica.application.usecase.SolicitudSeguimientoIncidente;
import com.proyecto.sica.application.usecase.SolicitudVisita;
import com.proyecto.sica.config.ConfiguracionAplicacion;
import com.proyecto.sica.config.ConfiguracionBaseDatos;
import com.proyecto.sica.domain.entity.EstadoIncidente;
import com.proyecto.sica.domain.entity.EventoAuditoria;
import com.proyecto.sica.domain.entity.GravedadIncidente;
import com.proyecto.sica.domain.entity.Incidente;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.TipoPersona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.entity.Visita;
import com.proyecto.sica.domain.repository.ConflictoPersistenciaException;
import com.proyecto.sica.domain.repository.FiltroAuditoria;
import com.proyecto.sica.domain.repository.FiltroIncidentes;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import com.proyecto.sica.infrastructure.persistence.AuditoriaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.EjecutorTransaccionalJdbc;
import com.proyecto.sica.infrastructure.persistence.IncidenteJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.RolJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.UsuarioJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.VisitaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.EmpresaJdbcRepository;
import com.proyecto.sica.infrastructure.persistence.PersonaJdbcRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

/** Integracion real de incidentes, auditoria, rollback y revision de accesos. */
class IncidentesAuditoriaMysqlIT {

    @Test
    void verificaIncidentesFiltrosAuditoriaRollbackYConcurrencia() throws Exception {
        ConfiguracionAplicacion app = new ConfiguracionAplicacion();
        app.getInicializadorUsuariosDemo().ejecutar();
        app.getInicializadorVisitasDemo().ejecutar();
        Usuario supervisor = app.getServicioAutenticacion().iniciarSesion(
                "supervisor@sica.example", "SicaSupervisor2026!".toCharArray());
        Usuario guarda = usuarioEnOtraApp("guarda@sica.example", "SicaGuarda2026!");
        Usuario funcionario = usuarioEnOtraApp(
                "funcionario@sica.example", "SicaFuncionario2026!");

        Persona persona = crearInvitada(app, supervisor);
        Visita visita = new ConfiguracionAplicacion().getPrerregistrarVisitaUseCase().ejecutar(
                funcionario, solicitudVisita(persona, funcionario));
        String tipo = "IT_INC_" + UUID.randomUUID().toString().substring(0, 8);
        Incidente incidente = app.getCrearIncidenteUseCase().ejecutar(supervisor,
                new SolicitudIncidente(visita.getId(), tipo, GravedadIncidente.MEDIA,
                        "Incidente ficticio de integracion", "Verificacion inicial",
                        LocalDateTime.now()));

        assertEquals(incidente.getId(), app.getConsultarIncidentePorIdUseCase()
                .ejecutar(supervisor, incidente.getId()).getId());
        FiltroIncidentes filtro = new FiltroIncidentes(visita.getId(), persona.getId(),
                supervisor.getId(), LocalDate.now(), LocalDate.now(), "ABIERTO");
        assertEquals(1, app.getConsultarIncidentesUseCase().ejecutar(supervisor, filtro).size());
        assertThrows(AccesoDenegadoException.class, () -> app.getActualizarIncidenteUseCase()
                .ejecutar(guarda, incidente.getId(), seguimiento()));

        Incidente cerrado = app.getActualizarIncidenteUseCase().ejecutar(supervisor,
                incidente.getId(), seguimiento());
        assertEquals(EstadoIncidente.CERRADO, cerrado.getEstado());
        List<?> eventos = app.getConsultarAuditoriaUseCase().ejecutar(supervisor,
                new FiltroAuditoria(supervisor.getId(), "ACTUALIZACION_INCIDENTE",
                        LocalDate.now(), LocalDate.now(), "incidentes",
                        String.valueOf(incidente.getId()), persona.getId(), visita.getId(), 50));
        assertFalse(eventos.isEmpty());

        int prohibicionId = app.getConsultarEstadosAccesoUseCase().ejecutar(supervisor).stream()
                .filter(e -> "PROHIBICION_INGRESO".equals(e.getCodigo())).findFirst().orElseThrow().getId();
        app.getCambiarEstadoAccesoUseCase().ejecutar(supervisor, persona.getId(), prohibicionId);
        assertEquals(incidente.getId(), app.getConsultarIncidentePorIdUseCase()
                .ejecutar(supervisor, incidente.getId()).getId());
        assertThrows(ValidacionException.class,
                () -> new ConfiguracionAplicacion().getRegistrarCheckInUseCase()
                        .ejecutar(guarda, visita.getId()));

        verificarRollback(incidente, supervisor);
        verificarDosVisitasMismaPersona(supervisor, guarda, funcionario, app);
        verificarPaseVencido(guarda, funcionario);
    }

    private Persona crearInvitada(ConfiguracionAplicacion app, Usuario supervisor) {
        String documento = "INC" + System.currentTimeMillis();
        return app.getCrearPersonaUseCase().ejecutar(supervisor,
                new SolicitudPersona(2L, "CC", documento, "Invitada", "Incidente IT",
                        null, null, TipoPersona.VISITANTE, null));
    }

    private SolicitudVisita solicitudVisita(Persona persona, Usuario funcionario) {
        return new SolicitudVisita(persona.getId(), funcionario.getEmpresaId(),
                funcionario.getId(), LocalDateTime.now().plusHours(1),
                "Visita para incidente", null, null);
    }

    private SolicitudSeguimientoIncidente seguimiento() {
        return new SolicitudSeguimientoIncidente(GravedadIncidente.ALTA,
                EstadoIncidente.CERRADO, "Descripcion ampliada y conservada",
                "Revision completada", "Resultado de la investigacion");
    }

    private Usuario usuarioEnOtraApp(String correo, String clave) {
        return new ConfiguracionAplicacion().getServicioAutenticacion()
                .iniciarSesion(correo, clave.toCharArray());
    }

    private void verificarRollback(Incidente base, Usuario supervisor) {
        ConnectionFactory factory = new ConnectionFactory(ConfiguracionBaseDatos.cargar());
        AuditoriaJdbcRepository auditoria = new AuditoriaJdbcRepository(factory);
        EjecutorTransaccionalJdbc transacciones = new EjecutorTransaccionalJdbc(factory, auditoria);
        RolJdbcRepository roles = new RolJdbcRepository(factory);
        EmpresaJdbcRepository empresas = new EmpresaJdbcRepository(factory, transacciones);
        PersonaJdbcRepository personas = new PersonaJdbcRepository(factory, transacciones);
        UsuarioJdbcRepository usuarios = new UsuarioJdbcRepository(factory, roles, auditoria);
        VisitaJdbcRepository visitas = new VisitaJdbcRepository(factory, personas, usuarios, transacciones);
        IncidenteJdbcRepository incidentes = new IncidenteJdbcRepository(factory, visitas, usuarios, transacciones);
        String tipo = "IT_ROLL_" + UUID.randomUUID().toString().substring(0, 8);
        Incidente invalido = new Incidente(null, base.getVisita(), base.getPersona(), supervisor,
                tipo, GravedadIncidente.BAJA, EstadoIncidente.ABIERTO, "Debe revertirse",
                null, LocalDateTime.now(), null, null);
        EventoAuditoria evento = new EventoAuditoria(999999999L, null,
                "IT_ROLLBACK_INCIDENTE", "incidentes", null, "EXITOSO", "Debe fallar");
        assertThrows(RepositorioException.class,
                () -> incidentes.crearConAuditoria(invalido, evento));
        assertFalse(incidentes.existeTipoEnVisita(tipo, base.getVisita().getId()));
        assertTrue(empresas.existeActiva(2L));
    }

    private void verificarDosVisitasMismaPersona(Usuario supervisor, Usuario guarda,
            Usuario funcionario, ConfiguracionAplicacion app) throws Exception {
        Persona persona = crearInvitada(app, supervisor);
        ConfiguracionAplicacion funcionApp = new ConfiguracionAplicacion();
        Visita primera = funcionApp.getPrerregistrarVisitaUseCase().ejecutar(
                funcionario, solicitudVisita(persona, funcionario));
        Visita segunda = funcionApp.getPrerregistrarVisitaUseCase().ejecutar(
                funcionario, solicitudVisita(persona, funcionario));
        ejecutarDos(() -> new ConfiguracionAplicacion().getRegistrarCheckInUseCase()
                .ejecutar(guarda, primera.getId()), () -> new ConfiguracionAplicacion()
                .getRegistrarCheckInUseCase().ejecutar(guarda, segunda.getId()));
        long dentro = app.getConsultarVisitasUseCase().buscarPorDocumento(
                guarda, persona.getTipoDocumento(), persona.getNumeroDocumento()).stream()
                .filter(v -> "DENTRO".equals(v.getEstado().getCodigo())).count();
        assertEquals(1, dentro);
    }

    private void verificarPaseVencido(Usuario guarda, Usuario funcionario) throws Exception {
        ConfiguracionAplicacion app = new ConfiguracionAplicacion();
        Persona trabajador = app.getBuscarPersonaPorDocumentoUseCase()
                .ejecutar(guarda, "CC", "10000001");
        Visita pase = app.getSolicitarPaseTemporalUseCase().ejecutar(
                guarda, solicitudVisita(trabajador, funcionario));
        new ConfiguracionAplicacion().getResponderSolicitudVisitaUseCase()
                .aprobar(funcionario, pase.getId());
        ConnectionFactory factory = new ConnectionFactory(ConfiguracionBaseDatos.cargar());
        try (Connection conexion = factory.obtenerConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        "UPDATE visitas SET fecha_vencimiento_pase=CURRENT_TIMESTAMP-INTERVAL 1 MINUTE WHERE id=?")) {
            sentencia.setLong(1, pase.getId()); sentencia.executeUpdate();
        }
        assertThrows(ConflictoPersistenciaException.class,
                () -> new ConfiguracionAplicacion().getRegistrarCheckInUseCase()
                        .ejecutar(guarda, pase.getId()));
    }

    private void ejecutarDos(Callable<?> a, Callable<?> b) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> primera = executor.submit(a); Future<?> segunda = executor.submit(b);
            primera.get(); segunda.get();
        } finally {
            executor.shutdownNow();
        }
    }
}
