package com.proyecto.sica.application.usecase;

import static com.proyecto.sica.support.DatosPrueba.rolConPermisos;
import static com.proyecto.sica.support.DatosPrueba.rolSinPermisos;
import static com.proyecto.sica.support.DatosPrueba.usuario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.proyecto.sica.application.service.AccesoDenegadoException;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorPersona;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.EstadoAcceso;
import com.proyecto.sica.domain.entity.Persona;
import com.proyecto.sica.domain.entity.TipoPersona;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.support.RepositoriosMemoria.AuditoriaMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.EmpresasMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.EstadosAccesoMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.PersonasMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PersonaUseCaseTest {
    private AuditoriaMemoria auditoria;
    private EmpresasMemoria empresas;
    private EstadosAccesoMemoria estados;
    private PersonasMemoria personas;
    private CrearPersonaUseCase crear;
    private ActualizarPersonaUseCase actualizar;
    private BuscarPersonaPorDocumentoUseCase buscar;
    private CambiarEstadoAccesoUseCase cambiarEstado;
    private Usuario actor;

    @BeforeEach
    void preparar() {
        auditoria = new AuditoriaMemoria();
        empresas = new EmpresasMemoria(auditoria);
        estados = new EstadosAccesoMemoria();
        estados.agregar(new EstadoAcceso(5, "ACTIVO", "Activo", null));
        estados.agregar(new EstadoAcceso(6, "PROHIBICION_INGRESO",
                "Con Prohibicion de Ingreso", null));
        personas = new PersonasMemoria(auditoria, estados);
        empresas.agregar(new Empresa(2L, "NIT-2", "Empresa Demo",
                null, null, null, true));
        ServicioAutorizacion autorizacion = new ServicioAutorizacion();
        ValidadorPersona validador = new ValidadorPersona(personas, empresas, estados);
        crear = new CrearPersonaUseCase(personas, autorizacion, validador);
        actualizar = new ActualizarPersonaUseCase(personas, autorizacion, validador);
        buscar = new BuscarPersonaPorDocumentoUseCase(personas, autorizacion);
        cambiarEstado = new CambiarEstadoAccesoUseCase(personas, estados, autorizacion);
        actor = usuario(90, rolConPermisos("CREAR_PERSONA", "CONSULTAR_PERSONA",
                "ACTUALIZAR_PERSONA", "CAMBIAR_ESTADO_ACCESO"),
                "supervisor@sica.example", "hash", true);
    }

    @Test
    void creaTrabajadorConEmpresa() {
        Persona creada = crear.ejecutar(actor, solicitud(2L, "100", TipoPersona.TRABAJADOR));
        assertEquals("Empresa Demo", creada.getEmpresa().getNombre());
        assertEquals("ACTIVO", creada.getEstadoAcceso().getCodigo());
    }

    @Test
    void creaInvitadoSinEmpresa() {
        Persona creada = crear.ejecutar(actor, solicitud(null, "101", TipoPersona.VISITANTE));
        assertNull(creada.getEmpresa());
    }

    @Test
    void rechazaDocumentoDuplicado() {
        crear.ejecutar(actor, solicitud(null, "102", TipoPersona.VISITANTE));
        assertThrows(ValidacionException.class,
                () -> crear.ejecutar(actor, solicitud(null, "102", TipoPersona.VISITANTE)));
    }

    @Test
    void buscaYActualizaPersona() {
        Persona creada = crear.ejecutar(actor, solicitud(null, "103", TipoPersona.VISITANTE));
        Persona encontrada = buscar.ejecutar(actor, "CC", "103");
        Persona editada = actualizar.ejecutar(actor, creada.getId(),
                new SolicitudPersona(null, "CC", "103", "Nombre Editado", "Apellido",
                        null, null, TipoPersona.VISITANTE, null));

        assertEquals(creada.getId(), encontrada.getId());
        assertEquals("Nombre Editado", editada.getNombres());
    }

    @Test
    void cambiaEstadoYEvitaCambioRepetido() {
        Persona creada = crear.ejecutar(actor, solicitud(null, "104", TipoPersona.VISITANTE));
        assertTrue(cambiarEstado.ejecutar(actor, creada.getId(), 6));
        assertEquals("PROHIBICION_INGRESO",
                personas.buscarPorId(creada.getId()).orElseThrow().getEstadoAcceso().getCodigo());
        int eventos = auditoria.getEventos().size();
        assertFalse(cambiarEstado.ejecutar(actor, creada.getId(), 6));
        assertEquals(eventos, auditoria.getEventos().size());
    }

    @Test
    void rechazaOperacionSinPermiso() {
        Usuario sinPermiso = usuario(91, rolSinPermisos(), "guarda@sica.example", "hash", true);
        assertThrows(AccesoDenegadoException.class,
                () -> crear.ejecutar(sinPermiso, solicitud(null, "105", TipoPersona.VISITANTE)));
    }

    private SolicitudPersona solicitud(Long empresaId, String documento, TipoPersona tipo) {
        return new SolicitudPersona(empresaId, "CC", documento, "Nombre", "Apellido",
                null, null, tipo, null);
    }
}
