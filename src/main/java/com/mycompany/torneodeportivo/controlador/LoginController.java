package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.App;
import com.mycompany.torneodeportivo.modelo.Administrador;
import com.mycompany.torneodeportivo.mysql.daos.PermisoDAO;
import com.mycompany.torneodeportivo.mysql.daos.SesionDAO;
import com.mycompany.torneodeportivo.mysql.modelos.EPermisos;
import com.mycompany.torneodeportivo.servicio.DatosServicio;
import com.mycompany.torneodeportivo.util.Alertas;
import com.mycompany.torneodeportivo.util.Sesion;
import java.io.IOException;
import java.util.Set;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Ventana de inicio de sesión (la primera de la aplicación). Valida el ID o correo y la
 * contraseña contra la base de datos con SesionDAO; solo entran las cuentas ACTIVAS.
 * No existe formulario de registro: las cuentas las crea el Administrador del torneo.
 *
 * @author Christian Hernandez Garcia
 */
public class LoginController {

    private static final String PATRON_ID = "\\d{1,9}";
    private static final String PATRON_CORREO = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private Label lblMensaje;

    private final SesionDAO sesionDAO = new SesionDAO();
    private final PermisoDAO permisoDAO = new PermisoDAO();

    @FXML
    private void initialize() {
        // Al escribir de nuevo se borra el mensaje de error
        txtUsuario.textProperty().addListener((o, a, n) -> lblMensaje.setText(""));
        txtContrasena.textProperty().addListener((o, a, n) -> lblMensaje.setText(""));
        Platform.runLater(txtUsuario::requestFocus);
    }

    @FXML
    private void iniciarSesion() {
        String idOCorreo = txtUsuario.getText() == null ? "" : txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText() == null ? "" : txtContrasena.getText();

        if (idOCorreo.isEmpty() || contrasena.isEmpty()) {
            lblMensaje.setText("Escribe tu ID o correo electrónico y tu contraseña.");
            return;
        }
        if (!idOCorreo.matches(PATRON_ID) && !idOCorreo.matches(PATRON_CORREO)) {
            lblMensaje.setText("Escribe un ID numérico (ej. 1) o un correo válido (ej. nombre@uni.mx).");
            return;
        }

        try {
            // 1. Credenciales y cuenta ACTIVA (tabla administrador)
            Administrador a = sesionDAO.validarCredenciales(idOCorreo, contrasena);
            // 2. Permisos del rol (tablas rol_permiso y permiso)
            Set<EPermisos> permisos = permisoDAO.permisosDe(a.getId());
            if (permisos.isEmpty()) {
                mostrarError("Tu rol no tiene permisos asignados.\nEjecuta bd/07_login_roles_permisos.sql en phpMyAdmin.");
                return;
            }
            // 3. Información del torneo
            DatosServicio.getInstancia().cargarDesdeBD();
            // 4. Bitácora de la sesión (tabla sesion)
            int idSesion = sesionDAO.registrarInicio(a.getId());
            Sesion.iniciar(a, idSesion, permisos);
            App.abrirVentanaPrincipal();
        } catch (IllegalArgumentException ex) {
            txtContrasena.clear();
            mostrarError(ex.getMessage());
        } catch (IOException ex) {
            Alertas.error("No se pudo abrir la ventana principal: " + ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setText(mensaje);
        Alertas.error(mensaje);
    }

    /** Cierra la aplicación completa, previa confirmación del usuario. */
    @FXML
    private void salir() {
        if (Alertas.confirmar("¿Deseas cerrar la aplicación?")) {
            Platform.exit();
        }
    }
}
