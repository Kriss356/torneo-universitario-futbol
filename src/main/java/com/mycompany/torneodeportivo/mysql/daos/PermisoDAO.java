package com.mycompany.torneodeportivo.mysql.daos;

import com.mycompany.torneodeportivo.mysql.modelos.EPermisos;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * DAO de la validación de permisos. Lee las tablas {@code permiso} y {@code rol_permiso}
 * para saber qué acciones puede realizar el rol del administrador que inició sesión.
 *
 * @author Christian Hernandez Garcia
 */
public class PermisoDAO {

    private static final String SQL_PERMISOS =
            "SELECT p.clave FROM administrador a "
            + "JOIN rol_permiso rp ON rp.id_rol = a.id_rol "
            + "JOIN permiso p ON p.id_permiso = rp.id_permiso "
            + "WHERE a.id_administrador = ? AND a.estado = 'ACTIVO'";
    private static final String SQL_TIENE_PERMISO =
            "SELECT COUNT(*) FROM administrador a "
            + "JOIN rol_permiso rp ON rp.id_rol = a.id_rol "
            + "JOIN permiso p ON p.id_permiso = rp.id_permiso "
            + "WHERE a.id_administrador = ? AND a.estado = 'ACTIVO' AND p.clave = ?";
    private static final String SQL_DETALLE =
            "SELECT p.modulo, p.descripcion FROM administrador a "
            + "JOIN rol_permiso rp ON rp.id_rol = a.id_rol "
            + "JOIN permiso p ON p.id_permiso = rp.id_permiso "
            + "WHERE a.id_administrador = ? ORDER BY p.id_permiso";
    private static final String SQL_DESCRIPCION_ROL =
            "SELECT r.descripcion FROM administrador a JOIN rol r ON r.id_rol = a.id_rol "
            + "WHERE a.id_administrador = ?";

    /**
     * Permisos del rol de un administrador activo. Se carga una vez al iniciar sesión.
     *
     * @param idAdministrador administrador
     * @return conjunto de permisos (vacío si la cuenta no está activa)
     */
    public Set<EPermisos> permisosDe(int idAdministrador) {
        Set<EPermisos> permisos = EnumSet.noneOf(EPermisos.class);
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_PERMISOS)) {
            ps.setInt(1, idAdministrador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        permisos.add(EPermisos.valueOf(rs.getString(1)));
                    } catch (IllegalArgumentException claveDesconocida) {
                        // Un permiso de la BD que la aplicación no usa: se ignora
                    }
                }
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
        return permisos;
    }

    /**
     * Valida en la base de datos, justo antes de una acción, que el administrador siga activo
     * y que su rol tenga el permiso.
     *
     * @param idAdministrador administrador que intenta la acción
     * @param permiso         acción a validar
     * @return true si está autorizado
     */
    public boolean tienePermiso(int idAdministrador, EPermisos permiso) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_TIENE_PERMISO)) {
            ps.setInt(1, idAdministrador);
            ps.setString(2, permiso.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    /**
     * Lista legible de los permisos del rol, por ejemplo "Equipos: Registrar equipos".
     *
     * @param idAdministrador administrador
     * @return una línea por permiso
     */
    public List<String> detallePermisos(int idAdministrador) {
        List<String> lineas = new ArrayList<>();
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_DETALLE)) {
            ps.setInt(1, idAdministrador);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lineas.add(rs.getString("modulo") + ": " + rs.getString("descripcion"));
                }
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
        return lineas;
    }

    /**
     * Responsabilidades del rol (columna descripcion de la tabla rol).
     *
     * @param idAdministrador administrador
     * @return descripción o texto vacío
     */
    public String descripcionRol(int idAdministrador) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_DESCRIPCION_ROL)) {
            ps.setInt(1, idAdministrador);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getString(1) != null ? rs.getString(1) : "";
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }
}
