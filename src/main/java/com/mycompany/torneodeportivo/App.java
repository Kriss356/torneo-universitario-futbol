package com.mycompany.torneodeportivo;

import com.mycompany.torneodeportivo.mysql.daos.SesionDAO;
import com.mycompany.torneodeportivo.util.Alertas;
import com.mycompany.torneodeportivo.util.Sesion;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Arranque de la aplicación. Maneja dos ventanas:
 * <ul>
 *   <li><b>Login</b>: es la primera que se ejecuta.</li>
 *   <li><b>Ventana principal</b> (menú): se abre al iniciar sesión y se cierra al cerrar sesión.</li>
 * </ul>
 *
 * @author Christian Hernandez Garcia
 */
public class App extends Application {

    private static final String TITULO = "Torneo Universitario de Fútbol";
    private static Stage ventanaLogin;
    private static Stage ventanaPrincipal;

    @Override
    public void start(Stage stage) throws IOException {
        ventanaLogin = stage;
        mostrarLogin();
    }

    /** Muestra la ventana de inicio de sesión (la primera de la aplicación). */
    public static void mostrarLogin() throws IOException {
        if (ventanaLogin == null) ventanaLogin = new Stage();
        ventanaLogin.setTitle(TITULO + " - Iniciar sesión");
        ventanaLogin.setScene(new Scene(cargarFXML("login")));
        ventanaLogin.setResizable(false);
        ventanaLogin.centerOnScreen();
        ventanaLogin.show();
    }

    /** Abre la ventana principal (menú) y cierra el login. Se llama después de validar las credenciales. */
    public static void abrirVentanaPrincipal() throws IOException {
        Stage principal = new Stage();
        principal.setTitle(TITULO + " - " + Sesion.getActual().getNombre() + " (" + Sesion.nombreRol() + ")");
        principal.setScene(new Scene(cargarFXML("menu"), 1200, 720));
        // Cerrar con la X también registra el cierre de sesión en la base de datos
        principal.setOnCloseRequest(evento -> {
            evento.consume();
            if (Alertas.confirmar("¿Deseas cerrar sesión y salir de la aplicación?")) {
                registrarCierreEnBD();
                Platform.exit();
            }
        });
        principal.show();
        ventanaPrincipal = principal;
        ventanaLogin.close();
    }

    /**
     * Cierra la sesión: la registra en la BD, limpia los datos de la sesión,
     * cierra la ventana principal y regresa al login.
     */
    public static void cerrarSesion() throws IOException {
        registrarCierreEnBD();
        mostrarLogin(); // primero se abre el login para que la aplicación no se quede sin ventanas
        if (ventanaPrincipal != null) {
            ventanaPrincipal.close();
            ventanaPrincipal = null;
        }
    }

    private static void registrarCierreEnBD() {
        try {
            if (Sesion.getIdSesion() > 0) {
                new SesionDAO().registrarCierre(Sesion.getIdSesion());
            }
        } catch (IllegalArgumentException ex) {
            // Si la BD no responde, la sesión se cierra igual en la aplicación
            System.err.println("No se pudo registrar el cierre de sesión: " + ex.getMessage());
        } finally {
            Sesion.cerrar();
        }
    }

    public static Parent cargarFXML(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("vista/" + fxml + ".fxml"));
        return loader.load();
    }

    /** Carga una vista del módulo MySQL: cargarFXMLMysql("administrador") -> mysql/vista/administrador.fxml */
    public static Parent cargarFXMLMysql(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("mysql/vista/" + fxml + ".fxml"));
        return loader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
