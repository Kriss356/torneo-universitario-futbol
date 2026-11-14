package com.mycompany.torneodeportivo;

/**
 * Clase principal del JAR ejecutable.
 * Con "java -jar" JavaFX no deja arrancar directo una clase que extiende Application
 * si JavaFX no está en el module-path; esta clase normal evita ese error y llama a App.
 *
 * @author Christian Hernandez Garcia
 */
public class Launcher {

    public static void main(String[] args) {
        App.main(args);
    }
}
