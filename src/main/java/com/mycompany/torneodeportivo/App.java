package com.mycompany.torneodeportivo;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(cargarFXML("login"), 1100, 700);
        stage.setTitle("Torneo Universitario de Fútbol");
        stage.setScene(scene);
        stage.show();
    }

    /** Cambia la pantalla completa: App.setRoot("login") carga vista/login.fxml */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(cargarFXML(fxml));
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
