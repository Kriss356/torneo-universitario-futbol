package com.mycompany.torneodeportivo.controlador;

import com.mycompany.torneodeportivo.App;
import com.mycompany.torneodeportivo.util.Alertas;
import com.mycompany.torneodeportivo.util.Sesion;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class MenuController {

    @FXML private BorderPane raiz;
    @FXML private Label lblUsuario;
    @FXML private Node btnCategorias, btnEquipos, btnJugadores, btnPartidos, btnPosiciones, btnAdministradores, btnAdminBD;

    @FXML
    private void initialize() {
        lblUsuario.setText(Sesion.getActual().getNombre() + " · " + Sesion.nombreRol());

        // Cada rol solo ve los módulos que le corresponden
        visible(btnCategorias, Sesion.esEquipos());
        visible(btnEquipos, Sesion.esEquipos());
        visible(btnJugadores, Sesion.esEquipos());
        visible(btnPartidos, Sesion.esPartidos() || Sesion.esTorneo());
        visible(btnAdministradores, Sesion.esTorneo());
        visible(btnAdminBD, Sesion.esTorneo());
        visible(btnPosiciones, true);

        if (Sesion.esEquipos()) mostrar("categoria");
        else if (Sesion.esPartidos()) mostrar("partido");
        else mostrar("posicion");
    }

    @FXML private void irCategorias() { mostrar("categoria"); }
    @FXML private void irEquipos() { mostrar("equipo"); }
    @FXML private void irJugadores() { mostrar("jugador"); }
    @FXML private void irPartidos() { mostrar("partido"); }
    @FXML private void irPosiciones() { mostrar("posicion"); }
    @FXML private void irAdministradores() { mostrar("administrador"); }

    @FXML
    private void irAdministradoresBD() {
        try {
            raiz.setCenter(App.cargarFXMLMysql("administrador"));
        } catch (IOException ex) {
            Alertas.error("No se pudo cargar la pantalla de administradores (BD): " + ex.getMessage());
        }
    }

    @FXML
    private void cerrarSesion() {
        Sesion.cerrar();
        try {
            App.setRoot("login");
        } catch (IOException ex) {
            Alertas.error("No se pudo cerrar la sesión: " + ex.getMessage());
        }
    }

    private void visible(Node nodo, boolean valor) {
        nodo.setVisible(valor);
        nodo.setManaged(valor);
    }

    private void mostrar(String vista) {
        try {
            raiz.setCenter(App.cargarFXML(vista));
        } catch (IOException ex) {
            Alertas.error("No se pudo cargar la pantalla \"" + vista + "\": " + ex.getMessage());
        }
    }
}
