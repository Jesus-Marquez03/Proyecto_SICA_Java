package com.proyecto.sica.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.proyecto.sica.application.service.AccesoDenegadoException;
import com.proyecto.sica.application.service.PermisosSistema;
import com.proyecto.sica.application.service.ServicioAutorizacion;
import com.proyecto.sica.application.service.ValidacionException;
import com.proyecto.sica.domain.entity.ActividadReciente;
import com.proyecto.sica.domain.entity.ConteoCategoria;
import com.proyecto.sica.domain.entity.DatoSerieFecha;
import com.proyecto.sica.domain.entity.EstadisticasIncidentes;
import com.proyecto.sica.domain.entity.Permiso;
import com.proyecto.sica.domain.entity.PersonaDentroReporte;
import com.proyecto.sica.domain.entity.ReporteAccesos;
import com.proyecto.sica.domain.entity.ResumenDashboard;
import com.proyecto.sica.domain.entity.ResumenEstadosVisita;
import com.proyecto.sica.domain.entity.ResumenTrazabilidad;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.domain.repository.ReporteRepository;
import com.proyecto.sica.support.DatosPrueba;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ReportesUseCaseTest {

    @Test
    void consultaTodosLosBloquesDelReporte() {
        RepositorioReporteMemoria repositorio = new RepositorioReporteMemoria();
        Usuario actor = DatosPrueba.usuario(1,
                DatosPrueba.rolConPermisos(PermisosSistema.GENERAR_REPORTES,
                        PermisosSistema.CONSULTAR_INCIDENTE),
                "super@sica.local", "hash", true);
        ConsultarReportesUseCase caso = new ConsultarReportesUseCase(
                repositorio, new ServicioAutorizacion());

        ResultadoReportes resultado = caso.ejecutar(
                actor, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 22));

        assertEquals(8, resultado.accesos().ingresos());
        assertEquals(1, resultado.personasDentro().size());
        assertEquals(3, resultado.visitas().cantidad("DENTRO"));
        assertEquals(2, resultado.incidentes().total());
        assertEquals(1, resultado.trazabilidad().regularizaciones());
    }

    @Test
    void aplicaElAlcanceDeEmpresaDelActor() {
        RepositorioReporteMemoria repositorio = new RepositorioReporteMemoria();
        Set<Permiso> permisos = Set.of(
                new Permiso(1, PermisosSistema.GENERAR_REPORTES, "Reportes", "Prueba"),
                new Permiso(2, PermisosSistema.CONSULTAR_INCIDENTE, "Incidentes", "Prueba"));
        Rol rolEmpresa = new Rol(4, "Funcionario", "Prueba", true, true, permisos);
        Usuario actor = new Usuario(8L, rolEmpresa, 25L, "Zona Demo", "funcionario",
                "hash", "Ana", "Demo", "ana@sica.local", true, null);

        new ConsultarReportesUseCase(repositorio, new ServicioAutorizacion()).ejecutar(
                actor, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 22));

        assertEquals(25L, repositorio.ultimaEmpresaId);
    }

    @Test
    void rechazaPermisoIncompletoYPeriodosInvalidos() {
        RepositorioReporteMemoria repositorio = new RepositorioReporteMemoria();
        ConsultarReportesUseCase caso = new ConsultarReportesUseCase(
                repositorio, new ServicioAutorizacion());
        Usuario sinIncidentes = DatosPrueba.usuario(2,
                DatosPrueba.rolConPermiso(PermisosSistema.GENERAR_REPORTES),
                "super@sica.local", "hash", true);
        Usuario autorizado = DatosPrueba.usuario(3,
                DatosPrueba.rolConPermisos(PermisosSistema.GENERAR_REPORTES,
                        PermisosSistema.CONSULTAR_INCIDENTE),
                "super2@sica.local", "hash", true);

        assertThrows(AccesoDenegadoException.class, () -> caso.ejecutar(sinIncidentes,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2)));
        assertThrows(ValidacionException.class, () -> caso.ejecutar(autorizado,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)));
        assertThrows(ValidacionException.class, () -> caso.ejecutar(autorizado,
                LocalDate.of(2023, 1, 1), LocalDate.of(2026, 1, 2)));
    }

    private static final class RepositorioReporteMemoria implements ReporteRepository {
        private Long ultimaEmpresaId;

        @Override
        public ReporteAccesos consultarAccesos(LocalDate desde, LocalDate hasta,
                Long empresaId) {
            ultimaEmpresaId = empresaId;
            return new ReporteAccesos(8, 7, List.of(new DatoSerieFecha(desde, 8)));
        }

        @Override
        public List<PersonaDentroReporte> consultarPersonasDentro(Long empresaId) {
            ultimaEmpresaId = empresaId;
            return List.of(new PersonaDentroReporte(
                    1, "VIS-1", "Persona Demo", "CC 1", "Acme", "INVITADO", null));
        }

        @Override
        public ResumenEstadosVisita consultarEstadosVisita(LocalDate desde,
                LocalDate hasta, Long empresaId) {
            ultimaEmpresaId = empresaId;
            return new ResumenEstadosVisita(List.of(new ConteoCategoria("DENTRO", 3)));
        }

        @Override
        public EstadisticasIncidentes consultarIncidentes(LocalDate desde,
                LocalDate hasta, Long empresaId) {
            ultimaEmpresaId = empresaId;
            return new EstadisticasIncidentes(2, 1, 1, 0, List.of(), List.of());
        }

        @Override
        public ResumenTrazabilidad consultarTrazabilidad(LocalDate desde,
                LocalDate hasta, Long empresaId) {
            ultimaEmpresaId = empresaId;
            return new ResumenTrazabilidad(1, 1, 2, 3);
        }

        @Override
        public ResumenDashboard consultarDashboard(LocalDate hoy, Long empresaId,
                boolean incluirIncidentes) {
            return new ResumenDashboard(0, 0, 0, 0, 0, 0,
                    List.of(), List.<ActividadReciente>of());
        }
    }
}
