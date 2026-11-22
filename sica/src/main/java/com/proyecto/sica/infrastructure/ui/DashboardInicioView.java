package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.domain.entity.ActividadReciente;
import com.proyecto.sica.domain.entity.DatoSerieFecha;
import com.proyecto.sica.domain.entity.ResumenDashboard;
import com.proyecto.sica.infrastructure.ui.components.EncabezadoModulo;
import com.proyecto.sica.infrastructure.ui.components.IconosSica;
import com.proyecto.sica.infrastructure.ui.components.TarjetaMetrica;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import java.time.format.DateTimeFormatter;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Dashboard Bento que presenta indicadores calculados desde MySQL. */
public final class DashboardInicioView {
    private final VBox root = new VBox(20);
    private final TarjetaMetrica accesos = new TarjetaMetrica(
            "Accesos hoy", TipoIcono.CONTROL_ACCESO);
    private final TarjetaMetrica dentro = new TarjetaMetrica(
            "Personas dentro", TipoIcono.PERSONAS);
    private final TarjetaMetrica pendientes = new TarjetaMetrica(
            "Visitas pendientes", TipoIcono.VISITAS);
    private final TarjetaMetrica incidentes = new TarjetaMetrica(
            "Incidentes abiertos", TipoIcono.INCIDENTES);
    private final LineChart<String, Number> grafica;
    private final ProgressIndicator progreso = new ProgressIndicator(0);
    private final Label porcentaje = new Label("Sin datos");
    private final VBox actividad = new VBox(8);
    private final Label estado = new Label();
    private final Button actualizar = new Button("Actualizar");

    public DashboardInicioView() {
        CategoryAxis ejeX = new CategoryAxis();
        NumberAxis ejeY = new NumberAxis();
        ejeY.setForceZeroInRange(true);
        grafica = new LineChart<>(ejeX, ejeY);
        construir();
    }

    private void construir() {
        root.getStyleClass().add("contenido-modulo-final");
        root.setPadding(new Insets(24, 28, 30, 28));
        EncabezadoModulo titulo = new EncabezadoModulo("Resumen operativo",
                "Estado actual de los accesos, visitas e incidentes de Zona Acme.",
                TipoIcono.DASHBOARD);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        actualizar.setGraphic(IconosSica.crear(TipoIcono.ACTUALIZAR));
        actualizar.getStyleClass().add("boton-neutro");
        HBox tituloFila = new HBox(12, titulo, espacio, estado, actualizar);
        tituloFila.setAlignment(Pos.CENTER_LEFT);

        HBox metricas = new HBox(14, accesos, dentro, pendientes, incidentes);
        for (TarjetaMetrica tarjeta : new TarjetaMetrica[]{accesos, dentro, pendientes, incidentes}) {
            tarjeta.setMinWidth(175);
            HBox.setHgrow(tarjeta, Priority.ALWAYS);
        }
        Label resumenTitulo = new Label("Informacion general");
        resumenTitulo.getStyleClass().add("titulo-tarjeta");
        VBox resumenCaja = new VBox(12, resumenTitulo, metricas);
        resumenCaja.getStyleClass().addAll("tarjeta-bento", "tarjeta-resumen-negra");

        grafica.setLegendVisible(false);
        grafica.setAnimated(false);
        grafica.setCreateSymbols(true);
        grafica.setMinHeight(300);
        VBox graficaCaja = tarjeta("Accesos de los ultimos 7 dias", grafica);
        graficaCaja.getStyleClass().add("tarjeta-grafica");

        progreso.setPrefSize(145, 145);
        porcentaje.getStyleClass().add("valor-progreso");
        Label progresoDetalle = new Label("Visitas finalizadas del mes");
        progresoDetalle.getStyleClass().add("detalle-metrica");
        progresoDetalle.setWrapText(true);
        VBox progresoCaja = tarjeta("Progreso mensual", progreso, porcentaje, progresoDetalle);
        progresoCaja.setAlignment(Pos.TOP_CENTER);

        actividad.getStyleClass().add("lista-actividad");
        VBox actividadCaja = tarjeta("Actividad reciente", actividad);
        actividadCaja.getStyleClass().add("tarjeta-oscura");

        GridPane bento = new GridPane();
        bento.setHgap(14);
        bento.setVgap(14);
        GridPane.setHgrow(graficaCaja, Priority.ALWAYS);
        GridPane.setHgrow(progresoCaja, Priority.ALWAYS);
        GridPane.setHgrow(actividadCaja, Priority.ALWAYS);
        bento.add(graficaCaja, 0, 0, 2, 1);
        bento.add(progresoCaja, 2, 0);
        bento.add(actividadCaja, 0, 1, 3, 1);
        graficaCaja.setMinWidth(510);
        progresoCaja.setMinWidth(210);

        root.getChildren().addAll(tituloFila, resumenCaja, bento);
    }

    private VBox tarjeta(String titulo, javafx.scene.Node... contenido) {
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("titulo-tarjeta");
        VBox caja = new VBox(12, etiqueta);
        caja.getChildren().addAll(contenido);
        caja.getStyleClass().add("tarjeta-bento");
        return caja;
    }

    public void mostrar(ResumenDashboard datos) {
        accesos.actualizar(datos.accesosHoy(), "Entradas registradas desde las 00:00");
        dentro.actualizar(datos.personasDentro(), "Visitas con presencia fisica activa");
        pendientes.actualizar(datos.visitasPendientes(), "Solicitudes por responder este mes");
        incidentes.actualizar(datos.incidentesAbiertos(), "Casos abiertos este mes");
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM");
        for (DatoSerieFecha dato : datos.accesosUltimosSieteDias()) {
            serie.getData().add(new XYChart.Data<>(dato.fecha().format(formato), dato.valor()));
        }
        grafica.setData(FXCollections.observableArrayList(java.util.List.of(serie)));
        if (datos.tieneProgreso()) {
            progreso.setProgress(datos.progresoFinalizadas() / 100.0);
            porcentaje.setText(String.format("%.0f%% completado", datos.progresoFinalizadas()));
        } else {
            progreso.setProgress(0);
            porcentaje.setText("Sin datos del mes");
        }
        actividad.getChildren().clear();
        if (datos.actividadReciente().isEmpty()) {
            Label vacio = new Label("Aun no hay actividad para mostrar.");
            vacio.getStyleClass().add("estado-vacio");
            actividad.getChildren().add(vacio);
        } else {
            datos.actividadReciente().forEach(item -> actividad.getChildren().add(fila(item)));
        }
        estado.setText("Actualizado");
        actualizar.setDisable(false);
    }

    private HBox fila(ActividadReciente item) {
        Label persona = new Label(item.titulo());
        persona.getStyleClass().add("actividad-persona");
        Label detalle = new Label(item.detalle() + " · " + item.estado());
        detalle.getStyleClass().add("detalle-metrica");
        VBox textos = new VBox(2, persona, detalle);
        HBox.setHgrow(textos, Priority.ALWAYS);
        Label fecha = new Label(item.fecha().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
        fecha.getStyleClass().add("actividad-fecha");
        HBox fila = new HBox(10, textos, fecha);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("fila-actividad");
        return fila;
    }

    public void cargando() {
        estado.setText("Consultando MySQL...");
        actualizar.setDisable(true);
    }

    public void error(String mensaje) {
        estado.setText(mensaje);
        actualizar.setDisable(false);
    }

    public void alActualizar(Runnable accion) {
        actualizar.setOnAction(evento -> accion.run());
    }

    public Parent getRoot() { return root; }
}
