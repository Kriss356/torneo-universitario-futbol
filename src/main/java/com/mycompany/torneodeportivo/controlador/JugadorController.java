package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.modelo.Equipo;
import com.mycompany.torneodeportivo.modelo.EstadoJugador;
import com.mycompany.torneodeportivo.modelo.Jugador;
import com.mycompany.torneodeportivo.modelo.PosicionCampo;
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

public class JugadorController extends FormularioBase<Jugador> {

    @FXML private TableColumn<Jugador, Integer> colId, colEdad, colNumero;
    @FXML private TableColumn<Jugador, String> colNombre, colEquipo;
    @FXML private TableColumn<Jugador, PosicionCampo> colPosicion;
    @FXML private TableColumn<Jugador, EstadoJugador> colEstado;

    @FXML private TextField txtNombre, txtEdad, txtNumero;
    @FXML private ComboBox<PosicionCampo> cmbPosicion;
    @FXML private ComboBox<Equipo> cmbEquipo;
    @FXML private ComboBox<EstadoJugador> cmbEstado;
    @FXML private Label errNombre, errEdad, errNumero, errPosicion, errEquipo, errEstado;
    @FXML private ImageView imgFoto;

    /** Foto elegida y aún no guardada; se copia a la carpeta "imagenes" al presionar Guardar. */
    private File fotoElegida;

    @FXML
    private void initialize() {
        DatosServicio servicio = DatosServicio.getInstancia();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroCamiseta"));
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicionCampo"));
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("nombreEquipo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tabla.setItems(servicio.getJugadores());
        cmbEquipo.setItems(servicio.getEquipos());
        cmbEquipo.setConverter(Convertidores.equipos());
        cmbPosicion.setItems(FXCollections.observableArrayList(PosicionCampo.values()));
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoJugador.values()));

        gestor.agregarTexto(txtNombre, errNombre, Reglas.persona(txtNombre, 3),
                Reglas.CAR_PERSONA, 40, Reglas.MSG_PERSONA);
        gestor.agregarTexto(txtEdad, errEdad, Reglas.entero(txtEdad, 15, 45), Reglas.CAR_ENTERO, 2, Reglas.MSG_ENTERO);
        gestor.agregarTexto(txtNumero, errNumero, Reglas.entero(txtNumero, 1, 99), Reglas.CAR_ENTERO, 2, Reglas.MSG_ENTERO);
        gestor.agregar(cmbPosicion, errPosicion, cmbPosicion.valueProperty(), Reglas.seleccion(cmbPosicion.valueProperty()));
        gestor.agregar(cmbEquipo, errEquipo, cmbEquipo.valueProperty(), Reglas.seleccion(cmbEquipo.valueProperty()));
        gestor.agregar(cmbEstado, errEstado, cmbEstado.valueProperty(), Reglas.seleccion(cmbEstado.valueProperty()));

        iniciarFormulario();
    }

    /** Elegir foto: muestra la vista previa; el archivo se copia y su ruta se guarda en la BD al presionar Guardar. */
    @FXML
    private void seleccionarFoto() {
        File f = ImagenUtil.elegir(imgFoto.getScene().getWindow());
        if (f != null) {
            fotoElegida = f;
            imgFoto.setImage(new Image(f.toURI().toString(), 140, 140, true, true));
        }
    }

    /** Prefijo de los permisos de este módulo (tabla permiso). */
    @Override
    protected String modulo() {
        return "JUGADORES";
    }

    @Override
    protected void limpiarCampos() {
        txtNombre.clear();
        txtEdad.clear();
        txtNumero.clear();
        cmbPosicion.getSelectionModel().clearSelection();
        cmbEquipo.getSelectionModel().clearSelection();
        cmbEstado.getSelectionModel().clearSelection();
        imgFoto.setImage(null);
        fotoElegida = null;
    }

    @Override
    protected void cargarCampos(Jugador j) {
        txtNombre.setText(j.getNombre());
        txtEdad.setText(String.valueOf(j.getEdad()));
        txtNumero.setText(String.valueOf(j.getNumeroCamiseta()));
        cmbPosicion.setValue(j.getPosicionCampo());
        cmbEquipo.setValue(j.getEquipo());
        cmbEstado.setValue(j.getEstado());
        imgFoto.setImage(ImagenUtil.cargar(j.getRutaFoto()));
        fotoElegida = null;
    }

    @Override
    protected String describir(Jugador j) {
        return j.getNombre() + " (#" + j.getNumeroCamiseta() + ", " + j.getNombreEquipo() + ")";
    }

    @Override
    protected void guardarRegistro(Jugador editando) throws Exception {
        DatosServicio servicio = DatosServicio.getInstancia();
        String ruta = fotoElegida != null ? ImagenUtil.guardar(fotoElegida)
                : (editando == null ? null : editando.getRutaFoto());
        String nombre = txtNombre.getText().trim();
        int edad = Integer.parseInt(txtEdad.getText().trim());
        int numero = Integer.parseInt(txtNumero.getText().trim());
        if (editando == null) {
            servicio.agregarJugador(nombre, edad, numero, cmbPosicion.getValue(), cmbEquipo.getValue(), ruta, cmbEstado.getValue());
        } else {
            servicio.actualizarJugador(editando, nombre, edad, numero, cmbPosicion.getValue(), cmbEquipo.getValue(), ruta, cmbEstado.getValue());
        }
    }

    @Override
    protected void eliminarRegistro(Jugador j) {
        DatosServicio.getInstancia().eliminarJugador(j);
    }
}
