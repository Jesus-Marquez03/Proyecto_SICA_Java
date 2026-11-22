package com.proyecto.sica.infrastructure.persistence;

import com.proyecto.sica.domain.entity.ActividadReciente;
import com.proyecto.sica.domain.entity.ConteoCategoria;
import com.proyecto.sica.domain.entity.DatoSerieFecha;
import com.proyecto.sica.domain.entity.EstadisticasIncidentes;
import com.proyecto.sica.domain.entity.PersonaDentroReporte;
import com.proyecto.sica.domain.entity.ReporteAccesos;
import com.proyecto.sica.domain.entity.ResumenDashboard;
import com.proyecto.sica.domain.entity.ResumenEstadosVisita;
import com.proyecto.sica.domain.entity.ResumenTrazabilidad;
import com.proyecto.sica.domain.repository.ReporteRepository;
import com.proyecto.sica.domain.repository.RepositorioException;
import com.proyecto.sica.infrastructure.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Adaptador JDBC que delega conteos y agrupaciones en MySQL. */
public final class ReporteJdbcRepository implements ReporteRepository {
    private final ConnectionFactory connectionFactory;

    public ReporteJdbcRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public ReporteAccesos consultarAccesos(LocalDate desde, LocalDate hasta, Long empresaId) {
        String alcance = empresaId == null ? "" : " AND u.empresa_id=?";
        String totalesSql = """
                SELECT COALESCE(SUM(v.fecha_ingreso>=? AND v.fecha_ingreso<?),0) ingresos,
                       COALESCE(SUM(v.fecha_salida>=? AND v.fecha_salida<?),0) salidas
                FROM visitas v JOIN usuarios u ON u.id=v.anfitrion_usuario_id
                WHERE 1=1
                """ + alcance;
        String diarioSql = """
                SELECT DATE(v.fecha_ingreso) fecha,COUNT(*) cantidad
                FROM visitas v JOIN usuarios u ON u.id=v.anfitrion_usuario_id
                WHERE v.fecha_ingreso>=? AND v.fecha_ingreso<?
                """ + alcance + " GROUP BY DATE(v.fecha_ingreso) ORDER BY fecha";
        try (Connection conexion = connectionFactory.obtenerConexion()) {
            long ingresos;
            long salidas;
            try (PreparedStatement s = conexion.prepareStatement(totalesSql)) {
                int i = 1;
                s.setTimestamp(i++, inicio(desde)); s.setTimestamp(i++, fin(hasta));
                s.setTimestamp(i++, inicio(desde)); s.setTimestamp(i++, fin(hasta));
                if (empresaId != null) s.setLong(i, empresaId);
                try (ResultSet r = s.executeQuery()) {
                    r.next(); ingresos = r.getLong("ingresos"); salidas = r.getLong("salidas");
                }
            }
            List<DatoSerieFecha> serie = new ArrayList<>();
            try (PreparedStatement s = conexion.prepareStatement(diarioSql)) {
                int i = 1; s.setTimestamp(i++, inicio(desde)); s.setTimestamp(i++, fin(hasta));
                if (empresaId != null) s.setLong(i, empresaId);
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) serie.add(new DatoSerieFecha(
                            r.getDate("fecha").toLocalDate(), r.getLong("cantidad")));
                }
            }
            return new ReporteAccesos(ingresos, salidas, completarDias(desde, hasta, serie));
        } catch (SQLException e) {
            throw new RepositorioException("No fue posible consultar accesos", e);
        }
    }

    @Override
    public List<PersonaDentroReporte> consultarPersonasDentro(Long empresaId) {
        String sql = """
                SELECT v.id,v.numero_solicitud,p.nombres,p.apellidos,p.tipo_documento,
                       p.numero_documento,COALESCE(e.nombre,'Sin empresa') empresa,
                       p.tipo_persona,v.fecha_ingreso
                FROM visitas v
                JOIN visita_estados ev ON ev.id=v.estado_visita_id
                JOIN personas p ON p.id=v.visitante_persona_id
                JOIN usuarios u ON u.id=v.anfitrion_usuario_id
                LEFT JOIN empresas e ON e.id=p.empresa_id
                WHERE ev.codigo='DENTRO' AND v.fecha_ingreso IS NOT NULL
                """ + (empresaId == null ? "" : " AND u.empresa_id=?")
                + " ORDER BY v.fecha_ingreso";
        List<PersonaDentroReporte> resultado = new ArrayList<>();
        try (Connection c = connectionFactory.obtenerConexion();
                PreparedStatement s = c.prepareStatement(sql)) {
            if (empresaId != null) s.setLong(1, empresaId);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) resultado.add(new PersonaDentroReporte(r.getLong("id"),
                        r.getString("numero_solicitud"),
                        r.getString("nombres") + " " + r.getString("apellidos"),
                        r.getString("tipo_documento") + " " + r.getString("numero_documento"),
                        r.getString("empresa"), r.getString("tipo_persona"),
                        r.getTimestamp("fecha_ingreso").toLocalDateTime()));
            }
            return resultado;
        } catch (SQLException e) {
            throw new RepositorioException("No fue posible consultar personas dentro", e);
        }
    }

    @Override
    public ResumenEstadosVisita consultarEstadosVisita(LocalDate desde, LocalDate hasta,
            Long empresaId) {
        String sql = """
                SELECT ev.codigo,COUNT(*) cantidad FROM visitas v
                JOIN visita_estados ev ON ev.id=v.estado_visita_id
                JOIN usuarios u ON u.id=v.anfitrion_usuario_id
                WHERE v.fecha_solicitud>=? AND v.fecha_solicitud<?
                """ + (empresaId == null ? "" : " AND u.empresa_id=?")
                + " GROUP BY ev.codigo ORDER BY ev.codigo";
        return new ResumenEstadosVisita(consultarCategorias(sql, desde, hasta, empresaId));
    }

    @Override
    public EstadisticasIncidentes consultarIncidentes(LocalDate desde, LocalDate hasta,
            Long empresaId) {
        String alcance = empresaId == null ? "" : " AND p.empresa_id=?";
        String totales = """
                SELECT COUNT(*) total,
                    COALESCE(SUM(i.estado='ABIERTO'),0) abiertos,
                    COALESCE(SUM(i.estado='EN_REVISION'),0) revision,
                    COALESCE(SUM(i.estado='CERRADO'),0) cerrados
                FROM incidentes i JOIN personas p ON p.id=i.persona_id
                WHERE i.fecha_incidente>=? AND i.fecha_incidente<?
                """ + alcance;
        String gravedad = """
                SELECT i.gravedad codigo,COUNT(*) cantidad
                FROM incidentes i JOIN personas p ON p.id=i.persona_id
                WHERE i.fecha_incidente>=? AND i.fecha_incidente<?
                """ + alcance + " GROUP BY i.gravedad ORDER BY i.gravedad";
        String fechas = """
                SELECT DATE(i.fecha_incidente) fecha,COUNT(*) cantidad
                FROM incidentes i JOIN personas p ON p.id=i.persona_id
                WHERE i.fecha_incidente>=? AND i.fecha_incidente<?
                """ + alcance + " GROUP BY DATE(i.fecha_incidente) ORDER BY fecha";
        try (Connection c = connectionFactory.obtenerConexion()) {
            long total; long abiertos; long revision; long cerrados;
            try (PreparedStatement s = preparar(c, totales, desde, hasta, empresaId);
                    ResultSet r = s.executeQuery()) {
                r.next(); total = r.getLong("total"); abiertos = r.getLong("abiertos");
                revision = r.getLong("revision"); cerrados = r.getLong("cerrados");
            }
            List<ConteoCategoria> porGravedad = new ArrayList<>();
            try (PreparedStatement s = preparar(c, gravedad, desde, hasta, empresaId);
                    ResultSet r = s.executeQuery()) {
                while (r.next()) porGravedad.add(new ConteoCategoria(
                        r.getString("codigo"), r.getLong("cantidad")));
            }
            List<DatoSerieFecha> porFecha = new ArrayList<>();
            try (PreparedStatement s = preparar(c, fechas, desde, hasta, empresaId);
                    ResultSet r = s.executeQuery()) {
                while (r.next()) porFecha.add(new DatoSerieFecha(
                        r.getDate("fecha").toLocalDate(), r.getLong("cantidad")));
            }
            return new EstadisticasIncidentes(total, abiertos, revision, cerrados,
                    porGravedad, porFecha);
        } catch (SQLException e) {
            throw new RepositorioException("No fue posible consultar incidentes", e);
        }
    }

    @Override
    public ResumenTrazabilidad consultarTrazabilidad(LocalDate desde, LocalDate hasta,
            Long empresaId) {
        String alcance = empresaId == null ? "" : " AND u.empresa_id=?";
        String resumenSql = """
                SELECT COALESCE(SUM(ev.codigo='RECHAZADA'),0) rechazadas,
                    COALESCE(SUM(v.tipo_solicitud='PASE_TEMPORAL'),0) pases,
                    COALESCE(SUM(ev.codigo IN ('PENDIENTE_APROBACION','PENDIENTE_APROBACION_OLVIDO')),0) pendientes
                FROM visitas v JOIN visita_estados ev ON ev.id=v.estado_visita_id
                JOIN usuarios u ON u.id=v.anfitrion_usuario_id
                WHERE v.fecha_solicitud>=? AND v.fecha_solicitud<?
                """ + alcance;
        String regularizacionesSql = """
                SELECT COUNT(*) regularizaciones
                FROM visitas v JOIN usuarios u ON u.id=v.anfitrion_usuario_id
                WHERE v.fecha_cierre_sistema>=? AND v.fecha_cierre_sistema<?
                """ + alcance;
        try (Connection c = connectionFactory.obtenerConexion()) {
            long rechazadas;
            long pases;
            long pendientes;
            try (PreparedStatement s = preparar(c, resumenSql, desde, hasta, empresaId);
                    ResultSet r = s.executeQuery()) {
                r.next();
                rechazadas = r.getLong("rechazadas");
                pases = r.getLong("pases");
                pendientes = r.getLong("pendientes");
            }
            long regularizaciones;
            try (PreparedStatement s = preparar(
                    c, regularizacionesSql, desde, hasta, empresaId);
                    ResultSet r = s.executeQuery()) {
                r.next();
                regularizaciones = r.getLong("regularizaciones");
            }
            return new ResumenTrazabilidad(
                    rechazadas, regularizaciones, pases, pendientes);
        } catch (SQLException e) {
            throw new RepositorioException("No fue posible consultar trazabilidad", e);
        }
    }

    @Override
    public ResumenDashboard consultarDashboard(LocalDate hoy, Long empresaId,
            boolean incluirIncidentes) {
        ReporteAccesos semana = consultarAccesos(hoy.minusDays(6), hoy, empresaId);
        long dentro = consultarPersonasDentro(empresaId).size();
        ResumenEstadosVisita mes = consultarEstadosVisita(hoy.withDayOfMonth(1), hoy, empresaId);
        long pendientes = mes.cantidad("PENDIENTE_APROBACION")
                + mes.cantidad("PENDIENTE_APROBACION_OLVIDO");
        long incidentes = incluirIncidentes
                ? consultarIncidentes(hoy.withDayOfMonth(1), hoy, empresaId).abiertos() : 0;
        long finalizadas = mes.cantidad("FUERA") + mes.cantidad("CERRADA_POR_SISTEMA");
        long elegibles = finalizadas + mes.cantidad("APROBADA") + mes.cantidad("DENTRO")
                + mes.cantidad("EXPIRADA");
        double progreso = elegibles == 0 ? 0 : finalizadas * 100.0 / elegibles;
        return new ResumenDashboard(valorDia(semana.ingresosPorDia(), hoy), dentro,
                pendientes, incidentes, progreso, elegibles,
                semana.ingresosPorDia(), consultarActividad(empresaId));
    }

    private List<ActividadReciente> consultarActividad(Long empresaId) {
        String sql = """
                SELECT p.nombres,p.apellidos,v.numero_solicitud,ev.nombre estado,
                    COALESCE(v.fecha_salida,v.fecha_ingreso,v.fecha_respuesta,v.fecha_solicitud) fecha
                FROM visitas v JOIN personas p ON p.id=v.visitante_persona_id
                JOIN visita_estados ev ON ev.id=v.estado_visita_id
                JOIN usuarios u ON u.id=v.anfitrion_usuario_id
                WHERE 1=1
                """ + (empresaId == null ? "" : " AND u.empresa_id=?")
                + " ORDER BY fecha DESC LIMIT 6";
        List<ActividadReciente> datos = new ArrayList<>();
        try (Connection c = connectionFactory.obtenerConexion();
                PreparedStatement s = c.prepareStatement(sql)) {
            if (empresaId != null) s.setLong(1, empresaId);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) datos.add(new ActividadReciente(
                        r.getString("nombres") + " " + r.getString("apellidos"),
                        r.getString("numero_solicitud"), r.getString("estado"),
                        r.getTimestamp("fecha").toLocalDateTime()));
            }
            return datos;
        } catch (SQLException e) {
            throw new RepositorioException("No fue posible consultar actividad reciente", e);
        }
    }

    private List<ConteoCategoria> consultarCategorias(String sql, LocalDate desde,
            LocalDate hasta, Long empresaId) {
        List<ConteoCategoria> datos = new ArrayList<>();
        try (Connection c = connectionFactory.obtenerConexion();
                PreparedStatement s = preparar(c, sql, desde, hasta, empresaId);
                ResultSet r = s.executeQuery()) {
            while (r.next()) datos.add(new ConteoCategoria(
                    r.getString("codigo"), r.getLong("cantidad")));
            return datos;
        } catch (SQLException e) {
            throw new RepositorioException("No fue posible consultar estados", e);
        }
    }

    private PreparedStatement preparar(Connection c, String sql, LocalDate desde,
            LocalDate hasta, Long empresaId) throws SQLException {
        PreparedStatement s = c.prepareStatement(sql);
        int i = 1; s.setTimestamp(i++, inicio(desde)); s.setTimestamp(i++, fin(hasta));
        if (empresaId != null) s.setLong(i, empresaId);
        return s;
    }

    private List<DatoSerieFecha> completarDias(LocalDate desde, LocalDate hasta,
            List<DatoSerieFecha> existentes) {
        List<DatoSerieFecha> resultado = new ArrayList<>();
        for (LocalDate fecha = desde; !fecha.isAfter(hasta); fecha = fecha.plusDays(1)) {
            LocalDate dia = fecha;
            long valor = existentes.stream().filter(d -> d.fecha().equals(dia))
                    .mapToLong(DatoSerieFecha::valor).findFirst().orElse(0);
            resultado.add(new DatoSerieFecha(fecha, valor));
        }
        return resultado;
    }

    private long valorDia(List<DatoSerieFecha> datos, LocalDate fecha) {
        return datos.stream().filter(d -> d.fecha().equals(fecha))
                .mapToLong(DatoSerieFecha::valor).findFirst().orElse(0);
    }

    private Timestamp inicio(LocalDate fecha) { return Timestamp.valueOf(fecha.atStartOfDay()); }
    private Timestamp fin(LocalDate fecha) { return Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()); }
}
