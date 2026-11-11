package com.mycompany.torneodeportivo.util;

import java.util.function.Supplier;
import java.util.regex.Pattern;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;

/**
 * Reglas de validación reutilizables. Cada regla devuelve un {@code Supplier<String>} que entrega
 * el mensaje de error actual, o {@code null} cuando el valor es válido.
 */
public class Reglas {

    // Caracteres permitidos al escribir (cada uno debe cumplir el patrón) y su mensaje de rechazo
    public static final String CAR_PERSONA = "[\\p{L} .'\\-]";
    public static final String MSG_PERSONA = "Carácter no permitido: solo letras, espacios, punto, apóstrofo y guion.";
    public static final String CAR_TEXTO = "[\\p{L}\\p{N} .,'&()#/\\-]";
    public static final String MSG_TEXTO = "Carácter no permitido: solo letras, números, espacios y . , - ' & ( ) # /";
    public static final String CAR_ENTERO = "[0-9]";
    public static final String MSG_ENTERO = "Solo se permiten dígitos (0-9).";
    /** Correo: se permite cualquier carácter excepto espacios (el formato se revisa con la regla CORREO). */
    public static final String CAR_CORREO = "\\S";
    public static final String MSG_CORREO = "No se permiten espacios en el correo electrónico.";
    public static final String CAR_HORA = "[0-9:]";
    public static final String MSG_HORA = "Solo se permiten dígitos y dos puntos (HH:mm).";
    public static final String CAR_USUARIO = "[A-Za-z0-9._]";
    public static final String MSG_USUARIO = "Solo letras sin acentos, números, punto y guion bajo.";
    public static final String CAR_CUALQUIERA = "\\S";
    public static final String MSG_CUALQUIERA = "No se permiten espacios.";

    private static final String OBLIGATORIO = "Este campo es obligatorio.";
    private static final Pattern PERSONA = Pattern.compile("\\p{L}[\\p{L} .'\\-]*");
    private static final Pattern TEXTO = Pattern.compile("[\\p{L}\\p{N}][\\p{L}\\p{N} .,'&()#/\\-]*");
    private static final Pattern CORREO = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
    private static final Pattern HORA = Pattern.compile("([01]\\d|2[0-3]):[0-5]\\d");
    private static final Pattern USUARIO = Pattern.compile("[A-Za-z0-9._]+");

    private Reglas() { }

    private static boolean vacio(String s) { return s == null || s.trim().isEmpty(); }

    /** Nombre de persona: letras, espacios, punto, apóstrofo y guion. */
    public static Supplier<String> persona(TextInputControl c, int min) {
        return () -> {
            String t = c.getText();
            if (vacio(t)) return OBLIGATORIO;
            t = t.trim();
            if (!PERSONA.matcher(t).matches()) return "Formato incorrecto: solo letras, espacios, punto, apóstrofo y guion.";
            if (t.length() < min) return "Escribe al menos " + min + " caracteres.";
            return null;
        };
    }

    /** Texto general: letras, números, espacios y algunos signos. */
    public static Supplier<String> textoGeneral(TextInputControl c, int min) {
        return () -> {
            String t = c.getText();
            if (vacio(t)) return OBLIGATORIO;
            t = t.trim();
            if (!TEXTO.matcher(t).matches()) return "Formato incorrecto: debe iniciar con letra o número.";
            if (t.length() < min) return "Escribe al menos " + min + " caracteres.";
            return null;
        };
    }

    /** Número entero dentro de un rango. */
    public static Supplier<String> entero(TextInputControl c, int min, int max) {
        return () -> {
            String t = c.getText();
            if (vacio(t)) return OBLIGATORIO;
            t = t.trim();
            if (!t.matches("\\d+")) return "Formato incorrecto: escribe un número entero.";
            int n;
            try {
                n = Integer.parseInt(t);
            } catch (NumberFormatException ex) {
                return "Número demasiado grande.";
            }
            if (n < min || n > max) return "Debe ser un número entero entre " + min + " y " + max + ".";
            return null;
        };
    }

    /** Correo electrónico con formato nombre@dominio.ext */
    public static Supplier<String> correo(TextInputControl c) {
        return () -> {
            String t = c.getText();
            if (vacio(t)) return OBLIGATORIO;
            if (!CORREO.matcher(t.trim()).matches()) return "Formato de correo no válido. Ejemplo: nombre@dominio.com";
            return null;
        };
    }

    /** Hora con formato HH:mm de 24 horas. */
    public static Supplier<String> hora(TextInputControl c) {
        return () -> {
            String t = c.getText();
            if (vacio(t)) return OBLIGATORIO;
            if (!HORA.matcher(t.trim()).matches()) return "Formato de hora no válido. Usa HH:mm, por ejemplo 16:30.";
            return null;
        };
    }

    /** Nombre de usuario: letras sin acentos, números, punto y guion bajo. */
    public static Supplier<String> usuario(TextInputControl c, int min) {
        return () -> {
            String t = c.getText();
            if (vacio(t)) return OBLIGATORIO;
            t = t.trim();
            if (!USUARIO.matcher(t).matches()) return "Formato incorrecto: solo letras, números, punto y guion bajo.";
            if (t.length() < min) return "Escribe al menos " + min + " caracteres.";
            return null;
        };
    }

    /** Contraseña con longitud mínima. */
    public static Supplier<String> contrasena(TextInputControl c, int min) {
        return () -> {
            String t = c.getText();
            if (vacio(t)) return OBLIGATORIO;
            if (t.length() < min) return "La contraseña debe tener al menos " + min + " caracteres.";
            return null;
        };
    }

    /** ComboBox: debe haber una opción elegida. */
    public static Supplier<String> seleccion(ObservableValue<?> valor) {
        return () -> valor.getValue() == null ? "Selecciona una opción." : null;
    }

    /** DatePicker: debe haber una fecha elegida. */
    public static Supplier<String> fecha(ObservableValue<?> valor) {
        return () -> valor.getValue() == null ? "Selecciona una fecha." : null;
    }
}
