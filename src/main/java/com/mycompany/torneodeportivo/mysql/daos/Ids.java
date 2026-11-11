package com.mycompany.torneodeportivo.mysql.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utilidad para leer el id AUTO_INCREMENT que la base generó en un INSERT.
 *
 * @author Christian Hernandez Garcia
 */
final class Ids {

    private Ids() {
    }

    static int generado(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("La base de datos no devolvió el id generado.");
    }

    /** Convierte texto vacío en NULL para las columnas opcionales. */
    static String vacioANulo(String texto) {
        return (texto == null || texto.trim().isEmpty()) ? null : texto.trim();
    }
}
