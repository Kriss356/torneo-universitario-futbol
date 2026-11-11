package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.App;
import com.mycompany.torneodeportivo.mysql.daos.PermisoDAO;
import com.mycompany.torneodeportivo.mysql.modelos.EPermisos;
import com.mycompany.torneodeportivo.servicio.Autorizacion;
import com.mycompany.torneodeportivo.util.Alertas;
import com.mycompany.torneodeportivo.util.Sesion;
import java.io.IOException;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * Ventana principal. Cada opción del menú se muestra solo si el rol tiene el permiso de
 * consulta (VER) del módulo; al abrir un módulo se vuelve a validar el permiso en la BD.
 *
 * @author Christian Hernandez Garcia
 */
public class MenuController {

    @FXML private BorderPane raiz;
    @FXML private Label lblUsuario;
    @FXML private Node btnCategorias, btnEquipos, btnJugadores, btnPartidos, btnPosiciones, btnAdministradores, btnAdminBD;

    @FXML
    private void initialize() {
        lblUsuario.setText(Sesion.getActual().getNombre() + " · " + Sesion.nombreRol());

        // Autorización: cada rol solo ve los módulos que puede consultar
        visible(btnCategorias, Sesion.puede(EPermisos.CATEGORIAS_VER));
        visible(btnEquipos, Sesion.puede(EPermisos.EQUIPOS_VER));
        visible(btnJugadores, Sesion.puede(EPermisos.JUGADORES_VER));
        visible(btnPartidos, Sesion.puede(EPermisos.PARTIDOS_VER));
        visible(btnPosiciones, Sesion.puede(EPermisos.POSICIONES_VER));
        visible(btnAdministradores, Sesion.puede(EPermisos.ADMINISTRADORES_VER));
        visible(btnAdminBD, Sesion.puede(EPermisos.ADMINISTRADORES_VER));

        // Pantalla inicial según el perfil
        if (Sesion.esEquipos()) irCategorias();
        else if (Sesion.esPartidos()) irPartidos();
        else irPosiciones();
    }

    @FXML private void irCategorias() { mostrar("categoria", EPermisos.CATEGORIAS_VER); }
    @FXML private void irEquipos() { mostrar("equipo", EPermisos.EQUIPOS_VER); }
    @FXML private void irJugadores() { mostrar("jugador", EPermisos.JUGADORES_VER); }
    @FXML private void irPartidos() { mostrar("partido", EPermisos.PARTIDOS_VER); }
    @FXML private void irPosiciones() { mostrar("posicion", EPermisos.POSICIONES_VER); }
    @FXML private void irAdministradores() { mostrar("administrador", EPermisos.ADMINISTRADORES_VER); }

    @FXML
    private void irAdministradoresBD() {
        try {
            Autorizacion.exigir(EPermisos.ADMINISTRADORES_VER);
            raiz.setCenter(App.cargarFXMLMysql("administrador"));
        } catch (IllegalArgumentException ex) {
            Alertas.error(ex.getMessage());
        } catch (IOException ex) {
            Alertas.error("No se pudo cargar la pantalla de administradores (BD): " + ex.getMessage());
        }
    }

    /** Muestra el perfil del usuario: rol, responsabilidades y permisos (leídos de la BD). */
    @FXML
    private void verPerfil() {
        try {
            PermisoDAO dao = new PermisoDAO();
            int id = Sesion.getActual().getId();
            List<String> permisos = dao.detallePermisos(id);
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(id).append("\n")
              .append("Nombre: ").append(Sesion.getActual().getNombre()).append("\n")
              .append("Correo: ").append(Sesion.getActual().getCorreo()).append("\n")
              .append("Rol: ").append(Sesion.nombreRol()).append("\n\n")
              .append("Responsabilidades:\n").append(dao.descripcionRol(id)).append("\n\n")
              .append("Permisos (").append(permisos.size()).append("):\n");
            for (String p : permisos) sb.append("  • ").append(p).append("\n");
            Alertas.informacion("Mi perfil y permisos", sb.toString());
        } catch (IllegalArgumentException ex) {
            Alertas.error(ex.getMessage());
        }
    }

    /** Cierra sesión: cierra la ventana principal y regresa al login. */
    @FXML
    private void cerrarSesion() {
        if (!Alertas.confirmar("¿Deseas cerrar la sesión de " + Sesion.getActual().getNombre() + "?")) {
            return;
        }
        try {
            App.cerrarSesion();
        } catch (IOException ex) {
            Alertas.error("No se pudo abrir el inicio de sesión: " + ex.getMessage());
        }
    }

    private void visible(Node nodo, boolean valor) {
        nodo.setVisible(valor);
        nodo.setManaged(valor);
    }

    private void mostrar(String vista, EPermisos permiso) {
        try {
            Autorizacion.exigir(permiso);
            raiz.setCenter(App.cargarFXML(vista));
        } catch (IllegalArgumentException ex) {
            Alertas.error(ex.getMessage());
        } catch (IOException ex) {
            Alertas.error("No se pudo cargar la pantalla \"" + vista + "\": " + ex.getMessage());
        }
    }
}
