package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.modelo.Categoria;
import com.mycompany.torneodeportivo.modelo.EstadoCategoria;
import com.mycompany.torneodeportivo.servicio.DatosServicio;
import com.mycompany.torneodeportivo.util.CampoValidado;
import com.mycompany.torneodeportivo.util.Reglas;
import java.util.function.Supplier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class CategoriaController extends FormularioBase<Categoria> {

    @FXML private TableColumn<Categoria, Integer> colId, colMin, colMax;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, EstadoCategoria> colEstado;

    @FXML private TextField txtNombre, txtEdadMin, txtEdadMax;
    @FXML private ComboBox<EstadoCategoria> cmbEstado;
    @FXML private Label errNombre, errEdadMin, errEdadMax, errEstado;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        colMin.setCellValueFactory(new PropertyValueFactory<>("edadMinima"));
        colMax.setCellValueFactory(new PropertyValueFactory<>("edadMaxima"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tabla.setItems(DatosServicio.getInstancia().getCategorias());
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoCategoria.values()));

        // Validaciones en tiempo real
        gestor.agregarTexto(txtNombre, errNombre, Reglas.textoGeneral(txtNombre, 2),
                Reglas.CAR_TEXTO, 30, Reglas.MSG_TEXTO);
        Supplier<String> reglaMin = Reglas.entero(txtEdadMin, 10, 60);
        gestor.agregarTexto(txtEdadMin, errEdadMin, reglaMin, Reglas.CAR_ENTERO, 2, Reglas.MSG_ENTERO);
        Supplier<String> reglaMax = Reglas.entero(txtEdadMax, 10, 60);
        CampoValidado max = gestor.agregarTexto(txtEdadMax, errEdadMax, () -> {
            String error = reglaMax.get();
            if (error != null) return error;
            if (reglaMin.get() == null && Integer.parseInt(txtEdadMax.getText().trim())
                    < Integer.parseInt(txtEdadMin.getText().trim())) {
                return "Debe ser mayor o igual que la edad mínima.";
            }
            return null;
        }, Reglas.CAR_ENTERO, 2, Reglas.MSG_ENTERO);
        gestor.revalidarCuando(max, txtEdadMin.textProperty()); // si cambia la mínima, se revisa la máxima
        gestor.agregar(cmbEstado, errEstado, cmbEstado.valueProperty(), Reglas.seleccion(cmbEstado.valueProperty()));

        iniciarFormulario();
    }

    /** Prefijo de los permisos de este módulo (tabla permiso). */
    @Override
    protected String modulo() {
        return "CATEGORIAS";
    }

    @Override
    protected void limpiarCampos() {
        txtNombre.clear();
        txtEdadMin.clear();
        txtEdadMax.clear();
        cmbEstado.getSelectionModel().clearSelection();
    }

    @Override
    protected void cargarCampos(Categoria c) {
        txtNombre.setText(c.getNombreCategoria());
        txtEdadMin.setText(String.valueOf(c.getEdadMinima()));
        txtEdadMax.setText(String.valueOf(c.getEdadMaxima()));
        cmbEstado.setValue(c.getEstado());
    }

    @Override
    protected String describir(Categoria c) {
        return c.getNombreCategoria() + " (" + c.getEdadMinima() + "-" + c.getEdadMaxima() + ")";
    }

    @Override
    protected void guardarRegistro(Categoria editando) {
        DatosServicio servicio = DatosServicio.getInstancia();
        String nombre = txtNombre.getText().trim();
        int min = Integer.parseInt(txtEdadMin.getText().trim());
        int max = Integer.parseInt(txtEdadMax.getText().trim());
        EstadoCategoria estado = cmbEstado.getValue();
        if (editando == null) {
            servicio.agregarCategoria(nombre, min, max, estado);
        } else {
            servicio.actualizarCategoria(editando, nombre, min, max, estado);
        }
    }

    @Override
    protected void eliminarRegistro(Categoria c) {
        DatosServicio.getInstancia().eliminarCategoria(c);
    }
}
