package com.mycompany.torneodeportivo.mysql.daos;

import java.sql.SQLException;

/**
 * Traduce los errores de MariaDB a mensajes claros para el usuario.
 * Devuelve una IllegalArgumentException para que los controladores la muestren en un Alert.
 *
 * @author Christian Hernandez Garcia
 */
public final class ErroresBD {

    private ErroresBD() {
    }

    public static IllegalArgumentException traducir(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        String estado = e.getSQLState() == null ? "" : e.getSQLState();

        if (estado.startsWith("08") || msg.contains("Could not connect") || msg.contains("Connection refused")
                || msg.contains("Socket fail")) {
            return new IllegalArgumentException("No hay conexión con la base de datos.\n\n"
                    + "Revisa que XAMPP tenga encendido MySQL y que exista la base \"torneo_futbol\".");
        }
        switch (e.getErrorCode()) {
            case 1049:
                return new IllegalArgumentException("No existe la base de datos \"torneo_futbol\".\n"
                        + "Ejecuta los scripts de la carpeta bd en phpMyAdmin.");
            case 1146:
                return new IllegalArgumentException("Falta una tabla en la base de datos.\n"
                        + "Ejecuta en phpMyAdmin bd/06_preparar_base.sql y después bd/07_login_roles_permisos.sql."
                        + "\n\nDetalle: " + msg);
            case 1062:
                return new IllegalArgumentException(duplicado(msg));
            case 1451:
                return new IllegalArgumentException("No se puede eliminar: el registro está siendo usado por otros datos "
                        + "(por ejemplo, equipos, jugadores o partidos).");
            case 1452:
                return new IllegalArgumentException("El registro relacionado no existe en la base de datos.");
            case 4025:
                return new IllegalArgumentException("Los datos no cumplen una regla de la base de datos (CHECK).\n\nDetalle: " + msg);
            default:
                return new IllegalArgumentException("Error de base de datos: " + msg);
        }
    }

    private static String duplicado(String msg) {
        if (msg.contains("uq_categoria_nombre")) return "Ya existe una categoría con ese nombre.";
        if (msg.contains("uq_equipo_nombre")) return "Ya existe un equipo con ese nombre.";
        if (msg.contains("uq_jugador_camiseta")) return "Ese número de camiseta ya está ocupado en el equipo.";
        if (msg.contains("uq_admin_correo")) return "Ya existe un administrador con ese correo.";
        if (msg.contains("uq_admin_usuario")) return "Ya existe un administrador con ese usuario.";
        if (msg.contains("uq_resultado_partido")) return "Ese partido ya tiene resultado.";
        return "Ya existe un registro con esos datos (valor repetido).";
    }
}
