package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.mysql.modelos.EPermisos;
import com.mycompany.torneodeportivo.servicio.Autorizacion;
import com.mycompany.torneodeportivo.util.Alertas;
import com.mycompany.torneodeportivo.util.GestorFormulario;
import com.mycompany.torneodeportivo.util.Sesion;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

/**
 * Comportamiento común de todos los formularios: estado de los botones, Nuevo, Validar, Guardar,
 * Editar, Eliminar y Limpiar. Cada controlador concreto registra sus campos y define cómo
 * limpiarlos, cargarlos, guardarlos y eliminarlos.
 *
 * Flujo: "Editar" carga el registro seleccionado en el formulario; después "Guardar"
 * guarda los cambios en la base de datos. Si no se está editando, "Guardar" crea un registro nuevo.
 *
 * Autorización: cada controlador indica su módulo (por ejemplo "CATEGORIAS") y, según los permisos
 * del rol (CREAR, EDITAR, ELIMINAR), los botones se habilitan o se bloquean. Si el rol solo puede
 * consultar, el formulario queda en "modo consulta". Además, cada acción se vuelve a validar en la
 * base de datos antes de ejecutarse (Autorizacion.exigir).
 *
 * @author Christian Hernandez Garcia
 */
public abstract class FormularioBase<T> {

    @FXML protected TableView<T> tabla;
    @FXML protected Button btnNuevo, btnValidar, btnGuardar, btnEditar, btnEliminar, btnLimpiar;

    /** Gestiona los campos validados; cada cambio vuelve a calcular el estado de los botones. */
    protected final GestorFormulario gestor = new GestorFormulario(this::actualizarBotones);

    /** Registro que se está editando; null cuando se captura uno nuevo. */
    protected T editando;

    /** Permisos del rol en este módulo (se calculan al iniciar el formulario). */
    protected boolean puedeCrear, puedeEditar, puedeEliminar;

    /** @return prefijo del módulo en los permisos: CATEGORIAS, EQUIPOS, JUGADORES, PARTIDOS o ADMINISTRADORES */
    protected abstract String modulo();

    /** Deja vacíos todos los campos del formulario. */
    protected abstract void limpiarCampos();

    /** Muestra en el formulario los datos del registro seleccionado. */
    protected abstract void cargarCampos(T registro);

    /** Texto corto que identifica a un registro en los mensajes. */
    protected abstract String describir(T registro);

    /**
     * Guarda en la base de datos lo capturado en el formulario.
     *
     * @param editando registro a modificar, o null si es un registro nuevo
     */
    protected abstract void guardarRegistro(T editando) throws Exception;

    /** Elimina el registro de la base de datos. */
    protected abstract void eliminarRegistro(T registro);

    protected final EPermisos permiso(String accion) {
        return EPermisos.de(modulo(), accion);
    }

    /** Debe llamarse al final del initialize() de cada controlador. */
    protected final void iniciarFormulario() {
        puedeCrear = Sesion.puede(permiso("CREAR"));
        puedeEditar = Sesion.puede(permiso("EDITAR"));
        puedeEliminar = Sesion.puede(permiso("ELIMINAR"));

        // Editar y Eliminar: solo con un registro seleccionado Y si el rol tiene el permiso
        btnEditar.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !puedeEditar || tabla.getSelectionModel().getSelectedItem() == null,
                tabla.getSelectionModel().selectedItemProperty()));
        btnEliminar.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !puedeEliminar || tabla.getSelectionModel().getSelectedItem() == null,
                tabla.getSelectionModel().selectedItemProperty()));
        // Doble clic en una fila = Editar (solo si tiene permiso)
        tabla.setOnMouseClicked(e -> {
            if (puedeEditar && e.getClickCount() == 2 && tabla.getSelectionModel().getSelectedItem() != null) editar();
        });
        restablecer();
        if (!puedeCrear && !puedeEditar) {
            activarModoConsulta();
        }
    }

    /**
     * El rol solo puede consultar: el formulario se bloquea, se muestra un aviso y al
     * seleccionar una fila se muestran sus datos (incluida la imagen) sin poder modificarlos.
     */
    private void activarModoConsulta() {
        Parent panel = buscarPanelFormulario();
        if (panel instanceof Pane) {
            Pane contenedor = (Pane) panel;
            for (Node n : contenedor.getChildren()) n.setDisable(true);
            Label aviso = new Label("🔒 Modo consulta: tu perfil (" + Sesion.nombreRol()
                    + ") solo puede ver la información de este módulo.");
            aviso.setWrapText(true);
            aviso.setMaxWidth(Double.MAX_VALUE);
            aviso.getStyleClass().add("aviso-permiso");
            contenedor.getChildren().add(0, aviso);
        }
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, antes, ahora) -> {
            if (ahora != null) gestor.silenciar(() -> cargarCampos(ahora));
        });
    }

    /** Sube desde el botón Guardar hasta el contenedor con estilo "panel-form". */
    private Parent buscarPanelFormulario() {
        Parent p = btnGuardar.getParent();
        while (p != null && !p.getStyleClass().contains("panel-form")) {
            p = p.getParent();
        }
        return p;
    }

    /** Nuevo, Validar y Limpiar disponibles; Guardar solo si todo es válido y el rol puede crear/editar. */
    protected void actualizarBotones() {
        if (btnGuardar == null) return;
        btnNuevo.setDisable(!puedeCrear);
        btnValidar.setDisable(!puedeCrear && !puedeEditar);
        btnLimpiar.setDisable(false);
        boolean autorizado = editando == null ? puedeCrear : puedeEditar;
        btnGuardar.setDisable(!autorizado || !gestor.esValido());
    }

    @FXML
    protected void nuevo() {
        restablecer();
        gestor.enfocarPrimero();
    }

    @FXML
    protected void limpiar() {
        restablecer();
    }

    @FXML
    protected void validar() {
        if (gestor.validarTodo()) {
            Alertas.informacion("Validación correcta",
                    "Todos los datos son correctos. Presiona \"" + btnGuardar.getText() + "\" para guardarlos.");
        } else {
            gestor.enfocarPrimerInvalido();
        }
        actualizarBotones();
    }

    @FXML
    protected void guardar() {
        if (!gestor.validarTodo()) {
            gestor.enfocarPrimerInvalido();
            actualizarBotones();
            return;
        }
        boolean esNuevo = editando == null;
        try {
            Autorizacion.exigir(permiso(esNuevo ? "CREAR" : "EDITAR"));
            guardarRegistro(editando);
            tabla.refresh();
            Alertas.info(esNuevo
                    ? "Registro guardado correctamente en la base de datos."
                    : "Cambios guardados correctamente en la base de datos.");
            restablecer();
        } catch (IllegalArgumentException ex) {
            Alertas.error(ex.getMessage());
        } catch (Exception ex) {
            Alertas.error("No se pudo guardar: " + ex.getMessage());
        }
    }

    @FXML
    protected void editar() {
        T seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null || !puedeEditar) return;
        editando = seleccionado;
        gestor.silenciar(() -> cargarCampos(seleccionado));
        gestor.validarTodo();
        btnGuardar.setText("Guardar cambios");
        actualizarBotones();
    }

    @FXML
    protected void eliminar() {
        T seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;
        try {
            Autorizacion.exigir(permiso("ELIMINAR"));
        } catch (IllegalArgumentException ex) {
            Alertas.error(ex.getMessage());
            return;
        }
        if (!Alertas.confirmar("¿Eliminar " + describir(seleccionado) + "?\n\nEsta acción no se puede deshacer.")) {
            return;
        }
        try {
            eliminarRegistro(seleccionado);
            tabla.refresh();
            Alertas.info("Registro eliminado correctamente de la base de datos.");
            restablecer();
        } catch (IllegalArgumentException ex) {
            Alertas.error(ex.getMessage());
        } catch (Exception ex) {
            Alertas.error("No se pudo eliminar: " + ex.getMessage());
        }
    }

    private void restablecer() {
        editando = null;
        btnGuardar.setText("Guardar");
        tabla.getSelectionModel().clearSelection();
        gestor.silenciar(this::limpiarCampos);
        actualizarBotones();
    }
}
