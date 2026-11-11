package com.mycompany.torneodeportivo.util;

import com.mycompany.torneodeportivo.modelo.Categoria;
import com.mycompany.torneodeportivo.modelo.Equipo;
import com.mycompany.torneodeportivo.modelo.Torneo;
import javafx.util.StringConverter;

/**
 * Convertidores para mostrar objetos del modelo dentro de un ComboBox.
 * Como toString() de las clases modelo devuelve el estado completo del objeto,
 * la vista decide aquí qué texto mostrar, sin modificar el modelo.
 */
public class Convertidores {

    public static StringConverter<Equipo> equipos() {
        return new StringConverter<>() {
            @Override
            public String toString(Equipo e) { return e == null ? "" : e.getNombreEquipo(); }
            @Override
            public Equipo fromString(String s) { return null; }
        };
    }

    public static StringConverter<Torneo> torneos() {
        return new StringConverter<>() {
            @Override
            public String toString(Torneo t) { return t == null ? "" : t.getNombreCompleto(); }
            @Override
            public Torneo fromString(String s) { return null; }
        };
    }

    public static StringConverter<Categoria> categorias() {
        return new StringConverter<>() {
            @Override
            public String toString(Categoria c) {
                return c == null ? "" : c.getNombreCategoria() + " (" + c.getEdadMinima() + "-" + c.getEdadMaxima() + ")";
            }
            @Override
            public Categoria fromString(String s) { return null; }
        };
    }
}
