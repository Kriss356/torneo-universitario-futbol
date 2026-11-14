package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.modelo.Categoria;
import com.mycompany.torneodeportivo.modelo.Equipo;
import com.mycompany.torneodeportivo.modelo.EstadoEquipo;
import com.mycompany.torneodeportivo.servicio.DatosServicio;
import com.mycompany.torneodeportivo.util.Convertidores;
import com.mycompany.torneodeportivo.util.ImagenUtil;
import com.mycompany.torneodeportivo.util.Reglas;
import java.io.File;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class EquipoController extends FormularioBase<Equipo> {

    @FXML private TableColumn<Equipo, Integer> colId;
    @FXML private TableColumn<Equipo, String> colNombre, colUniversidad, colEntrenador, colCategoria;
    @FXML private TableColumn<Equipo, EstadoEquipo> colEstado;

    @FXML private TextField txtNombre, txtUniversidad, txtEntrenador;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<EstadoEquipo> cmbEstado;
    @FXML private Label errNombre, errUniversidad, errEntrenador, errCategoria, errEstado;
    @FXML private ImageView imgLogo;

    /** Imagen elegida y aún no guardada; se copia a Documentos/TorneoUniversitarioFutbol/imagenes al presionar Guardar. */
    private File logoElegido;

    @FXML
    private void initialize() {
        DatosServicio servicio = DatosServicio.getInstancia();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreEquipo"));
        colUniversidad.setCellValueFactory(new PropertyValueFactory<>("universidad"));
        colEntrenador.setCellValueFactory(new PropertyValueFactory<>("entrenador"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tabla.setItems(servicio.getEquipos());
        cmbCategoria.setItems(servicio.getCategorias());
        cmbCategoria.setConverter(Convertidores.categorias());
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoEquipo.values()));

        gestor.agregarTexto(txtNombre, errNombre, Reglas.textoGeneral(txtNombre, 3),
                Reglas.CAR_TEXTO, 40, Reglas.MSG_TEXTO);
        gestor.agregarTexto(txtUniversidad, errUniversidad, Reglas.textoGeneral(txtUniversidad, 3),
                Reglas.CAR_TEXTO, 60, Reglas.MSG_TEXTO);
        gestor.agregarTexto(txtEntrenador, errEntrenador, Reglas.persona(txtEntrenador, 3),
                Reglas.CAR_PERSONA, 40, Reglas.MSG_PERSONA);
        gestor.agregar(cmbCategoria, errCategoria, cmbCategoria.valueProperty(), Reglas.seleccion(cmbCategoria.valueProperty()));
        gestor.agregar(cmbEstado, errEstado, cmbEstado.valueProperty(), Reglas.seleccion(cmbEstado.valueProperty()));

        iniciarFormulario();
    }

    /** Elegir logo: muestra la vista previa; el archivo se copia y su ruta se guarda en la BD al presionar Guardar. */
    @FXML
    private void seleccionarLogo() {
        File f = ImagenUtil.elegir(imgLogo.getScene().getWindow());
        if (f != null) {
            logoElegido = f;
            imgLogo.setImage(new Image(f.toURI().toString(), 140, 140, true, true));
        }
    }

    /** Prefijo de los permisos de este módulo (tabla permiso). */
    @Override
    protected String modulo() {
        return "EQUIPOS";
    }

    @Override
    protected void limpiarCampos() {
        txtNombre.clear();
        txtUniversidad.clear();
        txtEntrenador.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbEstado.getSelectionModel().clearSelection();
        imgLogo.setImage(null);
        logoElegido = null;
    }

    @Override
    protected void cargarCampos(Equipo e) {
        txtNombre.setText(e.getNombreEquipo());
        txtUniversidad.setText(e.getUniversidad());
        txtEntrenador.setText(e.getEntrenador());
        cmbCategoria.setValue(e.getCategoria());
        cmbEstado.setValue(e.getEstado());
        imgLogo.setImage(ImagenUtil.cargar(e.getRutaLogo()));
        logoElegido = null;
    }

    @Override
    protected String describir(Equipo e) {
        return e.getNombreEquipo() + " (" + e.getUniversidad() + ")";
    }

    @Override
    protected void guardarRegistro(Equipo editando) throws Exception {
        DatosServicio servicio = DatosServicio.getInstancia();
        String ruta = logoElegido != null ? ImagenUtil.guardar(logoElegido)
                : (editando == null ? null : editando.getRutaLogo());
        String nombre = txtNombre.getText().trim();
        String universidad = txtUniversidad.getText().trim();
        String entrenador = txtEntrenador.getText().trim();
        if (editando == null) {
            servicio.agregarEquipo(nombre, universidad, entrenador, cmbCategoria.getValue(), ruta, cmbEstado.getValue());
        } else {
            servicio.actualizarEquipo(editando, nombre, universidad, entrenador, cmbCategoria.getValue(), ruta, cmbEstado.getValue());
        }
    }

    @Override
    protected void eliminarRegistro(Equipo e) {
        DatosServicio.getInstancia().eliminarEquipo(e);
    }
}
