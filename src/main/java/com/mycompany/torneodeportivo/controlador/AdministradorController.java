package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.modelo.Administrador;
import com.mycompany.torneodeportivo.modelo.EstadoAdministrador;
import com.mycompany.torneodeportivo.mysql.daos.AdministradorSistemaDAO;
import com.mycompany.torneodeportivo.servicio.DatosServicio;
import com.mycompany.torneodeportivo.util.Reglas;
import com.mycompany.torneodeportivo.util.Sesion;
import java.util.function.Supplier;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdministradorController extends FormularioBase<Administrador> {

    @FXML private TableColumn<Administrador, Integer> colId, colEdad;
    @FXML private TableColumn<Administrador, String> colNombre, colCorreo, colUsuario, colRol;
    @FXML private TableColumn<Administrador, EstadoAdministrador> colEstado;

    @FXML private TextField txtNombre, txtEdad, txtCorreo, txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private ComboBox<String> cmbRol;
    @FXML private ComboBox<EstadoAdministrador> cmbEstado;
    @FXML private Label errNombre, errEdad, errCorreo, errUsuario, errContrasena, errRol, errEstado;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEdad.setCellValueFactory(new PropertyValueFactory<>("edad"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tabla.setItems(DatosServicio.getInstancia().getAdministradores());
        cmbRol.setItems(FXCollections.observableArrayList(AdministradorSistemaDAO.ROL_EQUIPOS,
                AdministradorSistemaDAO.ROL_PARTIDOS, AdministradorSistemaDAO.ROL_TORNEO));
        cmbEstado.setItems(FXCollections.observableArrayList(EstadoAdministrador.values()));

        gestor.agregarTexto(txtNombre, errNombre, Reglas.persona(txtNombre, 3),
                Reglas.CAR_PERSONA, 40, Reglas.MSG_PERSONA);
        gestor.agregarTexto(txtEdad, errEdad, Reglas.entero(txtEdad, 18, 70), Reglas.CAR_ENTERO, 2, Reglas.MSG_ENTERO);
        gestor.agregarTexto(txtCorreo, errCorreo, Reglas.correo(txtCorreo), Reglas.CAR_CORREO, 60, Reglas.MSG_CORREO);
        gestor.agregarTexto(txtUsuario, errUsuario, Reglas.usuario(txtUsuario, 4),
                Reglas.CAR_USUARIO, 20, Reglas.MSG_USUARIO);
        // Al editar, la contraseña puede quedar vacía: así se conserva la actual (está cifrada en la BD)
        Supplier<String> reglaClave = Reglas.contrasena(txtContrasena, 4);
        gestor.agregarTexto(txtContrasena, errContrasena,
                () -> (editando != null && txtContrasena.getText().isEmpty()) ? null : reglaClave.get(),
                Reglas.CAR_CUALQUIERA, 20, Reglas.MSG_CUALQUIERA);
        gestor.agregar(cmbRol, errRol, cmbRol.valueProperty(), Reglas.seleccion(cmbRol.valueProperty()));
        gestor.agregar(cmbEstado, errEstado, cmbEstado.valueProperty(), Reglas.seleccion(cmbEstado.valueProperty()));

        iniciarFormulario();
    }

    /** Prefijo de los permisos de este módulo (tabla permiso). */
    @Override
    protected String modulo() {
        return "ADMINISTRADORES";
    }

    @Override
    protected void limpiarCampos() {
        txtNombre.clear();
        txtEdad.clear();
        txtCorreo.clear();
        txtUsuario.clear();
        txtContrasena.clear();
        txtContrasena.setPromptText("");
        cmbRol.getSelectionModel().clearSelection();
        cmbEstado.getSelectionModel().clearSelection();
    }

    @Override
    protected void cargarCampos(Administrador a) {
        txtNombre.setText(a.getNombre());
        txtEdad.setText(String.valueOf(a.getEdad()));
        txtCorreo.setText(a.getCorreo());
        txtUsuario.setText(a.getUsuario());
        txtContrasena.clear(); // la contraseña guardada está cifrada, no se muestra
        txtContrasena.setPromptText("Déjala vacía para conservar la actual");
        cmbRol.setValue(a.getRol());
        cmbEstado.setValue(a.getEstado());
    }

    @Override
    protected String describir(Administrador a) {
        return a.getNombre() + " (" + a.getUsuario() + ")";
    }

    @Override
    protected void guardarRegistro(Administrador editando) {
        DatosServicio servicio = DatosServicio.getInstancia();
        String nombre = txtNombre.getText().trim();
        int edad = Integer.parseInt(txtEdad.getText().trim());
        String correo = txtCorreo.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String clave = txtContrasena.getText();
        String rol = cmbRol.getValue();
        EstadoAdministrador estado = cmbEstado.getValue();
        if (editando == null) {
            servicio.agregarAdministrador(nombre, edad, correo, usuario, clave, rol, estado);
            return;
        }
        if (Sesion.esUsuarioActual(editando)) {
            if (!rol.equals(editando.getRol())) {
                throw new IllegalArgumentException("No puedes cambiar tu propio rol mientras tienes la sesión abierta.");
            }
            if (estado != EstadoAdministrador.ACTIVO) {
                throw new IllegalArgumentException("No puedes desactivar tu propia cuenta.");
            }
        }
        Administrador actualizado = servicio.actualizarAdministrador(editando, nombre, edad, correo, usuario, clave, rol, estado);
        if (Sesion.esUsuarioActual(editando)) {
            Sesion.setActual(actualizado);
        }
    }

    @Override
    protected void eliminarRegistro(Administrador a) {
        if (Sesion.esUsuarioActual(a)) {
            throw new IllegalArgumentException("No puedes eliminar la cuenta con la que iniciaste sesión.");
        }
        DatosServicio.getInstancia().eliminarAdministrador(a);
    }
}
