package com.proyecto.sica.infrastructure.ui;

import com.proyecto.sica.application.usecase.SolicitudActualizarUsuario;
import com.proyecto.sica.application.usecase.SolicitudCrearUsuario;
import com.proyecto.sica.domain.entity.Empresa;
import com.proyecto.sica.domain.entity.Rol;
import com.proyecto.sica.domain.entity.Usuario;
import com.proyecto.sica.infrastructure.ui.components.EncabezadoModulo;
import com.proyecto.sica.infrastructure.ui.components.ComboBoxLegible;
import com.proyecto.sica.infrastructure.ui.components.TablaCopiable;
import com.proyecto.sica.infrastructure.ui.components.TipoIcono;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Administracion funcional de usuarios, roles y estado de acceso al sistema. */
public final class UsuariosView {
    private final VBox root = new VBox(18);
    private final TableView<Usuario> tabla = new TableView<>();
    private final TextField usuario = new TextField();
    private final TextField nombres = new TextField();
    private final TextField apellidos = new TextField();
    private final TextField correo = new TextField();
    private final PasswordField password = new PasswordField();
    private final ComboBox<Rol> rol = new ComboBox<>();
    private final ComboBox<Empresa> empresa = new ComboBox<>();
    private final TextArea permisos = new TextArea();
    private final Button crear = new Button("Crear usuario");
    private final Button actualizar = new Button("Guardar cambios");
    private final Button cambiarEstado = new Button("Cambiar estado");
    private final Label estado = new Label();

    public UsuariosView() { construir(); }

    private void construir() {
        root.getStyleClass().add("contenido-modulo-final");
        root.setPadding(new Insets(24, 28, 30, 28));
        root.getChildren().add(new EncabezadoModulo("Usuarios y roles",
                "Administra cuentas sin eliminar el historial de auditoria.", TipoIcono.USUARIOS));
        configurarTabla();
        VBox tablaCaja = tarjeta("Usuarios registrados", tabla);
        GridPane formulario = formulario();
        VBox formularioCaja = tarjeta("Datos del usuario", formulario);
        HBox acciones = new HBox(9, crear, actualizar, cambiarEstado, estado);
        acciones.setAlignment(Pos.CENTER_LEFT);
        formularioCaja.getChildren().add(acciones);
        HBox cuerpo = new HBox(14, tablaCaja, formularioCaja);
        HBox.setHgrow(tablaCaja, Priority.ALWAYS);
        HBox.setHgrow(formularioCaja, Priority.ALWAYS);
        tablaCaja.setMinWidth(520);
        formularioCaja.setMinWidth(380);
        root.getChildren().add(cuerpo);

        crear.getStyleClass().add("boton-principal");
        actualizar.getStyleClass().add("boton-neutro");
        cambiarEstado.getStyleClass().add("boton-neutro");
        permisos.getStyleClass().add("permisos-consulta");
        permisos.setEditable(false);
        permisos.setWrapText(true);
        permisos.setPrefRowCount(4);
        password.setPromptText("Solo es obligatoria al crear");
        ComboBoxLegible.configurar(rol, Rol::getNombre);
        ComboBoxLegible.configurar(empresa, Empresa::getNombre);
        tabla.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> mostrarSeleccion(seleccionado));
        rol.valueProperty().addListener((obs, anterior, seleccionado) -> mostrarPermisos(seleccionado));
    }

    private void configurarTabla() {
        tabla.getColumns().add(columna("Usuario", Usuario::getNombreUsuario));
        tabla.getColumns().add(columna("Nombre", Usuario::getNombreCompleto));
        tabla.getColumns().add(columna("Correo", Usuario::getCorreo));
        tabla.getColumns().add(columna("Rol", u -> u.getRol().getNombre()));
        tabla.getColumns().add(columna("Estado", u -> u.isActivo() ? "Activo" : "Inactivo"));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setMinHeight(520);
        TablaCopiable.habilitar(tabla);
    }

    private TableColumn<Usuario, String> columna(String titulo,
            java.util.function.Function<Usuario, String> valor) {
        TableColumn<Usuario, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(valor.apply(d.getValue())));
        return columna;
    }

    private GridPane formulario() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("formulario-administrativo");
        grid.setHgap(12);
        grid.setVgap(9);
        subtitulo(grid, 0, "Identificacion del usuario");
        agregar(grid, 1, "Nombre de usuario", usuario);
        subtitulo(grid, 2, "Informacion personal");
        agregar(grid, 3, "Nombres", nombres);
        agregar(grid, 4, "Apellidos", apellidos);
        agregar(grid, 5, "Correo", correo);
        subtitulo(grid, 6, "Datos de acceso");
        agregar(grid, 7, "Contrasena inicial", password);
        subtitulo(grid, 8, "Rol y empresa");
        agregar(grid, 9, "Rol", rol);
        agregar(grid, 10, "Empresa", empresa);
        subtitulo(grid, 11, "Permisos consultables");
        agregar(grid, 12, "Permisos del rol", permisos);
        return grid;
    }

    private void subtitulo(GridPane grid, int fila, String texto) {
        Label subtitulo = new Label(texto);
        subtitulo.getStyleClass().add("subtitulo-formulario");
        grid.add(subtitulo, 0, fila, 2, 1);
    }

    private void agregar(GridPane grid, int fila, String texto, javafx.scene.Node campo) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("etiqueta-campo");
        grid.add(etiqueta, 0, fila);
        grid.add(campo, 1, fila);
        GridPane.setHgrow(campo, Priority.ALWAYS);
        if (campo instanceof javafx.scene.control.Control control) {
            control.setMaxWidth(Double.MAX_VALUE);
            control.getStyleClass().add("campo-formulario");
        }
    }

    private VBox tarjeta(String titulo, javafx.scene.Node contenido) {
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("titulo-tarjeta");
        VBox caja = new VBox(12, etiqueta, contenido);
        caja.getStyleClass().addAll("tarjeta-bento", "tarjeta-administrativa");
        return caja;
    }

    private void mostrarSeleccion(Usuario seleccionado) {
        if (seleccionado == null) return;
        usuario.setText(seleccionado.getNombreUsuario());
        nombres.setText(seleccionado.getNombres());
        apellidos.setText(seleccionado.getApellidos());
        correo.setText(seleccionado.getCorreo());
        rol.getItems().stream().filter(r -> r.getId() == seleccionado.getRol().getId())
                .findFirst().ifPresent(rol::setValue);
        empresa.getItems().stream().filter(e -> e.getId().equals(seleccionado.getEmpresaId()))
                .findFirst().ifPresentOrElse(empresa::setValue, () -> empresa.setValue(null));
        password.clear();
        cambiarEstado.setText(seleccionado.isActivo() ? "Desactivar" : "Activar");
    }

    private void mostrarPermisos(Rol seleccionado) {
        permisos.setText(seleccionado == null ? "" : seleccionado.getPermisos().stream()
                .map(p -> p.getCodigo()).sorted().collect(Collectors.joining("\n")));
    }

    public void mostrarDatos(List<Usuario> usuarios, List<Rol> roles, List<Empresa> empresas) {
        tabla.setItems(FXCollections.observableArrayList(usuarios));
        rol.setItems(FXCollections.observableArrayList(roles));
        empresa.setItems(FXCollections.observableArrayList(empresas));
        estado.setText("Datos actualizados");
        habilitar(true);
    }

    public SolicitudCrearUsuario solicitudCrear() {
        Rol rolSeleccionado = rol.getValue();
        Empresa empresaSeleccionada = empresa.getValue();
        return new SolicitudCrearUsuario(usuario.getText(), password.getText().toCharArray(),
                nombres.getText(), apellidos.getText(), correo.getText(),
                rolSeleccionado == null ? 0 : rolSeleccionado.getId(),
                empresaSeleccionada == null ? null : empresaSeleccionada.getId());
    }

    public SolicitudActualizarUsuario solicitudActualizar() {
        Usuario seleccionado = seleccionado();
        Rol rolSeleccionado = rol.getValue();
        Empresa empresaSeleccionada = empresa.getValue();
        return new SolicitudActualizarUsuario(seleccionado == null ? 0 : seleccionado.getId(),
                usuario.getText(), nombres.getText(), apellidos.getText(), correo.getText(),
                rolSeleccionado == null ? 0 : rolSeleccionado.getId(),
                empresaSeleccionada == null ? null : empresaSeleccionada.getId());
    }

    public Usuario seleccionado() { return tabla.getSelectionModel().getSelectedItem(); }
    public void alCrear(Runnable accion) { crear.setOnAction(e -> accion.run()); }
    public void alActualizar(Runnable accion) { actualizar.setOnAction(e -> accion.run()); }
    public void alCambiarEstado(Runnable accion) { cambiarEstado.setOnAction(e -> accion.run()); }
    public void permitir(boolean puedeCrear, boolean puedeActualizar, boolean puedeEstado) {
        crear.setVisible(puedeCrear); crear.setManaged(puedeCrear);
        actualizar.setVisible(puedeActualizar); actualizar.setManaged(puedeActualizar);
        cambiarEstado.setVisible(puedeEstado); cambiarEstado.setManaged(puedeEstado);
    }
    public void cargando() { estado.setText("Consultando..."); habilitar(false); }
    public void error(String mensaje) { estado.setText(mensaje); habilitar(true); }
    private void habilitar(boolean valor) { crear.setDisable(!valor); actualizar.setDisable(!valor); cambiarEstado.setDisable(!valor); }
    public Parent getRoot() { return root; }
}
