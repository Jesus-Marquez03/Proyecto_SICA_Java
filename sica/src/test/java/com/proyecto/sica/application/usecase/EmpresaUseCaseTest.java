package com.proyecto.sica.application.usecase;

import static com.proyecto.sica.support.DatosPrueba.rolConPermisos;
import static com.proyecto.sica.support.DatosPrueba.usuario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.application.service.ValidadorEmpresa;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.support.RepositoriosMemoria.AuditoriaMemoria;
import com.proyecto.sica.support.RepositoriosMemoria.EmpresasMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmpresaUseCaseTest {
    private AuditoriaMemoria auditoria;
    private EmpresasMemoria empresas;
    private CrearEmpresaUseCase crear;
    private ActualizarEmpresaUseCase actualizar;
    private Usuario actor;

    @BeforeEach
    void preparar() {
        auditoria = new AuditoriaMemoria();
        empresas = new EmpresasMemoria(auditoria);
        ServicioAutorizacion autorizacion = new ServicioAutorizacion();
        ValidadorEmpresa validador = new ValidadorEmpresa(empresas);
        crear = new CrearEmpresaUseCase(empresas, autorizacion, validador);
        actualizar = new ActualizarEmpresaUseCase(empresas, autorizacion, validador);
        actor = usuario(90, rolConPermisos("CREAR_EMPRESA", "ACTUALIZAR_EMPRESA"),
                "supervisor@sica.example", "hash", true);
    }

    @Test
    void creaEmpresaYRegistraAuditoria() {
        Empresa creada = crear.ejecutar(actor,
                new SolicitudEmpresa("900-100", "Empresa Prueba", null, null, null));

        assertEquals("Empresa Prueba", creada.getNombre());
        assertEquals("CREAR_EMPRESA", auditoria.getEventos().get(0).getAccion());
    }

    @Test
    void rechazaNombreVacio() {
        SolicitudEmpresa solicitud = new SolicitudEmpresa("900-101", " ", null, null, null);
        assertThrows(ValidacionException.class, () -> crear.ejecutar(actor, solicitud));
    }

    @Test
    void actualizaEmpresaExistente() {
        Empresa creada = crear.ejecutar(actor,
                new SolicitudEmpresa("900-102", "Nombre Inicial", null, null, null));
        Empresa editada = actualizar.ejecutar(actor, creada.getId(),
                new SolicitudEmpresa("900-102", "Nombre Editado", "Calle 1", null, null));

        assertEquals("Nombre Editado", editada.getNombre());
        assertEquals(2, auditoria.getEventos().size());
    }
}
