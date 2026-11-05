package com.mycompany.torneodeportivo.mysql.controladores;

import com.mycompany.torneodeportivo.mysql.daos.AdministradorDAO;
import com.mycompany.torneodeportivo.mysql.modelos.Administrador;
import com.mycompany.torneodeportivo.mysql.modelos.EEstados;
import com.mycompany.torneodeportivo.mysql.modelos.ERoles;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

/**
 * Controlador JavaFX con comunicación directa al DAO para la gestión de Administradores.
 *
 * @author Christian Hernandez Garcia
 */
public class AdministradorController implements Initializable {

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtTelefono;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtUsuario;
    @FXML
    private TextField txtClaveAcceso;
    @FXML
    private ComboBox<ERoles> cmbRol;
    @FXML
    private ComboBox<EEstados> cmbEstado;
    @FXML
    private TextField txtRutaImagen;
    @FXML
    private ImageView imgVistaPrevia;

    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Administrador> tblAdministradores;
    @FXML
    private TableColumn<Administrador, Integer> colId;
    @FXML
    private TableColumn<Administrador, String> colNombre;
    @FXML
    private TableColumn<Administrador, String> colEmail;
    @FXML
    private TableColumn<Administrador, String> colUsuario;
    @FXML
    private TableColumn<Administrador, ERoles> colRol;
    @FXML
    private TableColumn<Administrador, EEstados> colEstado;
    @FXML
    private TableColumn<Administrador, String> colRutaImagen;

    // Instanciación directa de la capa DAO
    private final AdministradorDAO administradorDAO = new AdministradorDAO();
    private final ObservableList<Administrador> listaObservable = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colRutaImagen.setCellValueFactory(new PropertyValueFactory<>("rutaImagen"));

        cmbRol.setItems(FXCollections.observableArrayList(ERoles.values()));
        cmbRol.setValue(ERoles.ADMIN_EQUIPOS);

        cmbEstado.setItems(FXCollections.observableArrayList(EEstados.values()));
        cmbEstado.setValue(EEstados.ACTIVO);

        tblAdministradores.setItems(listaObservable);

        tblAdministradores.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> mostrarDetalleAdministrador(newValue)
        );

        actualizarTabla();
    }

    @FXML
    private void btnSeleccionarImagenOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Foto de Perfil");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos de Imagen", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File archivoSeleccionado = fileChooser.showOpenDialog(txtId.getScene().getWindow());
        if (archivoSeleccionado != null) {
            txtRutaImagen.setText(archivoSeleccionado.getAbsolutePath());
            cargarImagenVistaPrevia(archivoSeleccionado.getAbsolutePath());
        }
    }

    @FXML
    private void btnGuardarOnAction(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String clave = txtClaveAcceso.getText().trim();
        ERoles rol = cmbRol.getValue();
        EEstados estado = cmbEstado.getValue();
        String rutaImagen = txtRutaImagen.getText().trim();

        if (nombre.isEmpty() || email.isEmpty() || usuario.isEmpty() || clave.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Requeridos", "Nombre, Email, Usuario y Contraseña son obligatorios.");
            return;
        }

        Administrador nuevoAdmin = new Administrador(0, nombre, telefono, email, direccion, usuario, clave, rutaImagen, rol, estado);

        // Registro directo mediante el DAO
        if (administradorDAO.guardar(nuevoAdmin)) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Administrador registrado correctamente en la base de datos.");
            actualizarTabla();
            limpiarCampos();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el registro. Verifique que usuario o email no estén duplicados.");
        }
    }

    @FXML
    private void btnActualizarOnAction(ActionEvent event) {
        if (txtId.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Seleccione un administrador de la tabla para actualizar.");
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String email = txtEmail.getText().trim();
            String direccion = txtDireccion.getText().trim();
            String usuario = txtUsuario.getText().trim();
            String clave = txtClaveAcceso.getText().trim();
            ERoles rol = cmbRol.getValue();
            EEstados estado = cmbEstado.getValue();
            String rutaImagen = txtRutaImagen.getText().trim();

            if (nombre.isEmpty() || email.isEmpty() || usuario.isEmpty() || clave.isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campos Requeridos", "Nombre, Email, Usuario y Contraseña son obligatorios.");
                return;
            }

            Administrador adminActualizar = new Administrador(id, nombre, telefono, email, direccion, usuario, clave, rutaImagen, rol, estado);

            // Actualización directa mediante el DAO
            if (administradorDAO.actualizar(adminActualizar)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Registro actualizado correctamente.");
                actualizarTabla();
                limpiarCampos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la información en la base de datos.");
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Identificador no válido.");
        }
    }

    @FXML
    private void btnEliminarOnAction(ActionEvent event) {
        if (txtId.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Seleccione un registro de la tabla para eliminar.");
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText().trim());

            // Eliminación directa mediante el DAO
            if (administradorDAO.eliminar(id)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Administrador eliminado correctamente.");
                actualizarTabla();
                limpiarCampos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el registro de la base de datos.");
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Identificador no válido.");
        }
    }

    @FXML
    private void btnLimpiarOnAction(ActionEvent event) {
        limpiarCampos();
    }

    private void mostrarDetalleAdministrador(Administrador admin) {
        if (admin != null) {
            txtId.setText(String.valueOf(admin.getId()));
            txtNombre.setText(admin.getNombre());
            txtTelefono.setText(admin.getTelefono());
            txtEmail.setText(admin.getEmail());
            txtDireccion.setText(admin.getDireccion());
            txtUsuario.setText(admin.getUsuario());
            txtClaveAcceso.setText(admin.getClaveAcceso());
            cmbRol.setValue(admin.getRol());
            cmbEstado.setValue(admin.getEstado());
            txtRutaImagen.setText(admin.getRutaImagen());
            cargarImagenVistaPrevia(admin.getRutaImagen());
        }
    }

    private void cargarImagenVistaPrevia(String ruta) {
        if (ruta != null && !ruta.trim().isEmpty()) {
            File archivo = new File(ruta);
            if (archivo.exists()) {
                Image imagen = new Image(archivo.toURI().toString());
                imgVistaPrevia.setImage(imagen);
                return;
            }
        }
        imgVistaPrevia.setImage(null);
    }

    private void actualizarTabla() {
        listaObservable.setAll(administradorDAO.listarTodos());
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        txtDireccion.setText("");
        txtUsuario.setText("");
        txtClaveAcceso.setText("");
        cmbRol.setValue(ERoles.ADMIN_EQUIPOS);
        cmbEstado.setValue(EEstados.ACTIVO);
        txtRutaImagen.setText("");
        imgVistaPrevia.setImage(null);
        tblAdministradores.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
