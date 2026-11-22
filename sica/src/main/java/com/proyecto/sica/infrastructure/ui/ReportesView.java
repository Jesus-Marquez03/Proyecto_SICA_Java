package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.application.usecase.ResultadoReportes;
import com.proyecto.sica.domain.entity.ConteoCategoria;
import com.proyecto.sica.domain.entity.DatoSerieFecha;
import com.proyecto.sica.domain.entity.PersonaDentroReporte;
import com.proyecto.sica.infrastructure.ui.components.EncabezadoModulo;
import com.proyecto.sica.infrastructure.ui.components.IconosSica;
import com.proyecto.sica.infrastructure.ui.components.TarjetaMetrica;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Pantalla de reportes con filtros, graficas y tablas verificables. */
public final class ReportesView {
    private final VBox root = new VBox(18);
    private final DatePicker desde = new DatePicker(LocalDate.now().withDayOfMonth(1));
    private final DatePicker hasta = new DatePicker(LocalDate.now());
    private final Button consultar = new Button("Generar reporte");
    private final Label estado = new Label();
    private final TarjetaMetrica ingresos = new TarjetaMetrica("Ingresos", TipoIcono.CONTROL_ACCESO);
    private final TarjetaMetrica salidas = new TarjetaMetrica("Salidas", TipoIcono.SALIR);
    private final TarjetaMetrica dentro = new TarjetaMetrica("Personas dentro", TipoIcono.PERSONAS);
    private final TarjetaMetrica incidentes = new TarjetaMetrica("Incidentes", TipoIcono.INCIDENTES);
    private final LineChart<String, Number> grafica = new LineChart<>(
            new CategoryAxis(), new NumberAxis());
    private final TableView<PersonaDentroReporte> tablaDentro = new TableView<>();
    private final TableView<ConteoCategoria> tablaEstados = new TableView<>();
    private final TableView<ConteoCategoria> tablaGravedad = new TableView<>();
    private final VBox trazabilidad = new VBox(9);

    public ReportesView() { construir(); }

    private void construir() {
        root.getStyleClass().add("contenido-modulo-final");
        root.setPadding(new Insets(24, 28, 30, 28));
        EncabezadoModulo titulo = new EncabezadoModulo("Reportes",
                "Indicadores operativos calculados directamente desde MySQL.", TipoIcono.REPORTES);
        Label desdeLabel = new Label("Desde");
        Label hastaLabel = new Label("Hasta");
        consultar.setGraphic(IconosSica.crear(TipoIcono.BUSCAR));
        consultar.getStyleClass().add("boton-principal");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox filtros = new HBox(9, desdeLabel, desde, hastaLabel, hasta, consultar, espacio, estado);
        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.getStyleClass().add("barra-filtros-final");

        HBox metricas = new HBox(14, ingresos, salidas, dentro, incidentes);
        for (TarjetaMetrica tarjeta : new TarjetaMetrica[]{ingresos, salidas, dentro, incidentes}) {
            tarjeta.setMinWidth(170);
            HBox.setHgrow(tarjeta, Priority.ALWAYS);
        }

        grafica.setAnimated(false);
        grafica.setLegendVisible(false);
        grafica.setMinHeight(310);
        VBox graficaCaja = tarjeta("Accesos por dia", grafica);

        configurarTablas();
        TabPane pestañas = new TabPane(
                tab("Personas dentro", tablaDentro),
                tab("Estados de visita", tablaEstados),
                tab("Incidentes por gravedad", tablaGravedad),
                tab("Trazabilidad", trazabilidad));
        pestañas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pestañas.setMinHeight(330);
        root.getChildren().addAll(titulo, filtros, metricas, graficaCaja, pestañas);
    }

    private void configurarTablas() {
        tablaDentro.getColumns().add(columnaPersona("Solicitud", PersonaDentroReporte::solicitud));
        tablaDentro.getColumns().add(columnaPersona("Persona", PersonaDentroReporte::persona));
        tablaDentro.getColumns().add(columnaPersona("Documento", PersonaDentroReporte::documento));
        tablaDentro.getColumns().add(columnaPersona("Empresa", PersonaDentroReporte::empresa));
        tablaDentro.getColumns().add(columnaPersona("Ingreso", p -> p.fechaIngreso() == null
                ? "--" : p.fechaIngreso().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        configurarCategorias(tablaEstados);
        configurarCategorias(tablaGravedad);
    }

    private TableColumn<PersonaDentroReporte, String> columnaPersona(String titulo,
            java.util.function.Function<PersonaDentroReporte, String> valor) {
        TableColumn<PersonaDentroReporte, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(valor.apply(dato.getValue())));
        return columna;
    }

    private void configurarCategorias(TableView<ConteoCategoria> tabla) {
        TableColumn<ConteoCategoria, String> categoria = new TableColumn<>("Categoria");
        categoria.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().categoria()));
        TableColumn<ConteoCategoria, Long> cantidad = new TableColumn<>("Cantidad");
        cantidad.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().cantidad()));
        tabla.getColumns().addAll(categoria, cantidad);
    }

    private Tab tab(String titulo, javafx.scene.Node contenido) {
        return new Tab(titulo, contenido);
    }

    private VBox tarjeta(String titulo, javafx.scene.Node contenido) {
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("titulo-tarjeta");
        VBox caja = new VBox(12, etiqueta, contenido);
        caja.getStyleClass().add("tarjeta-bento");
        return caja;
    }

    public void mostrar(ResultadoReportes datos) {
        ingresos.actualizar(datos.accesos().ingresos(), diferenciaDias(datos));
        salidas.actualizar(datos.accesos().salidas(), "Salidas en el periodo seleccionado");
        dentro.actualizar(datos.personasDentro().size(), "Presencia fisica actual");
        incidentes.actualizar(datos.incidentes().total(), "Incidentes del periodo");
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM");
        for (DatoSerieFecha punto : datos.accesos().ingresosPorDia()) {
            serie.getData().add(new XYChart.Data<>(punto.fecha().format(formato), punto.valor()));
        }
        grafica.setData(FXCollections.observableArrayList(serie));
        tablaDentro.setItems(FXCollections.observableArrayList(datos.personasDentro()));
        tablaEstados.setItems(FXCollections.observableArrayList(datos.visitas().estados()));
        tablaGravedad.setItems(FXCollections.observableArrayList(datos.incidentes().porGravedad()));
        trazabilidad.getChildren().setAll(
                fila("Accesos rechazados", datos.trazabilidad().accesosRechazados()),
                fila("Regularizaciones", datos.trazabilidad().regularizaciones()),
                fila("Pases temporales", datos.trazabilidad().pasesTemporales()),
                fila("Solicitudes pendientes", datos.trazabilidad().solicitudesPendientes()));
        boolean sinDatos = datos.accesos().ingresos() == 0 && datos.accesos().salidas() == 0
                && datos.personasDentro().isEmpty() && datos.incidentes().total() == 0
                && datos.visitas().estados().isEmpty();
        estado.setText(sinDatos ? "Sin datos para el periodo" : "Reporte actualizado");
        consultar.setDisable(false);
    }

    private String diferenciaDias(ResultadoReportes datos) {
        int cantidad = datos.accesos().ingresosPorDia().size();
        if (cantidad < 2) return "Entradas en el periodo seleccionado";
        long actual = datos.accesos().ingresosPorDia().get(cantidad - 1).valor();
        long anterior = datos.accesos().ingresosPorDia().get(cantidad - 2).valor();
        long diferencia = actual - anterior;
        return (diferencia >= 0 ? "+" : "") + diferencia + " frente al dia anterior";
    }

    private HBox fila(String texto, long valor) {
        Label etiqueta = new Label(texto);
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Label cantidad = new Label(String.valueOf(valor));
        cantidad.getStyleClass().add("valor-trazabilidad");
        HBox fila = new HBox(10, etiqueta, espacio, cantidad);
        fila.getStyleClass().add("fila-actividad");
        return fila;
    }

    public void cargando() { estado.setText("Consultando..."); consultar.setDisable(true); }
    public void error(String mensaje) { estado.setText(mensaje); consultar.setDisable(false); }
    public void alConsultar(Runnable accion) { consultar.setOnAction(e -> accion.run()); }
    public LocalDate getDesde() { return desde.getValue(); }
    public LocalDate getHasta() { return hasta.getValue(); }
    public Parent getRoot() { return root; }
}
