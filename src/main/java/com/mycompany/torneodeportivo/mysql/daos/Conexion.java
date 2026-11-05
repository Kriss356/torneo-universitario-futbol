package com.mycompany.torneodeportivo.mysql.daos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Crea las conexiones a la base de datos torneo_futbol (XAMPP / MariaDB).
 * Si tu XAMPP tiene contraseña para root, cámbiala en CLAVE.
 *
 * @author Christian Hernandez Garcia
 */
public final class Conexion {

    private static final String URL = "jdbc:mariadb://localhost:3306/torneo_futbol";
    private static final String USUARIO = "root";
    private static final String CLAVE = "";

    private Conexion() {
    }

    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
