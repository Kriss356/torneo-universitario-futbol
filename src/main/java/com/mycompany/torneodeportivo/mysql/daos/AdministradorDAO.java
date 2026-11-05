package com.mycompany.torneodeportivo.mysql.daos;

import com.mycompany.torneodeportivo.mysql.modelos.Administrador;
import com.mycompany.torneodeportivo.mysql.modelos.EEstados;
import com.mycompany.torneodeportivo.mysql.modelos.ERoles;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos (CRUD) de la tabla "administradores".
 *
 * @author Christian Hernandez Garcia
 */
public class AdministradorDAO {

    private static final String SQL_INSERTAR =
            "INSERT INTO administradores (nombre, telefono, email, direccion, usuario, clave_acceso, ruta_imagen, rol, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE administradores SET nombre=?, telefono=?, email=?, direccion=?, usuario=?, clave_acceso=?, "
            + "ruta_imagen=?, rol=?, estado=? WHERE id=?";
    private static final String SQL_ELIMINAR = "DELETE FROM administradores WHERE id=?";
    private static final String SQL_LISTAR = "SELECT * FROM administradores ORDER BY id";

    /** Inserta un administrador. El id lo genera la base (AUTO_INCREMENT). */
    public boolean guardar(Administrador a) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_INSERTAR)) {
            llenar(ps, a);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar administrador: " + e.getMessage());
            return false;
        }
    }

    /** Actualiza el administrador con el id indicado. */
    public boolean actualizar(Administrador a) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            llenar(ps, a);
            ps.setInt(10, a.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar administrador: " + e.getMessage());
            return false;
        }
    }

    /** Elimina el administrador con ese id. */
    public boolean eliminar(int id) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar administrador: " + e.getMessage());
            return false;
        }
    }

    /** Devuelve todos los administradores. */
    public List<Administrador> listarTodos() {
        List<Administrador> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Administrador(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("email"),
                        rs.getString("direccion"),
                        rs.getString("usuario"),
                        rs.getString("clave_acceso"),
                        rs.getString("ruta_imagen"),
                        ERoles.valueOf(rs.getString("rol")),
                        EEstados.valueOf(rs.getString("estado"))));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar administradores: " + e.getMessage());
        }
        return lista;
    }

    private void llenar(PreparedStatement ps, Administrador a) throws SQLException {
        ps.setString(1, a.getNombre());
        ps.setString(2, vacioANulo(a.getTelefono()));
        ps.setString(3, a.getEmail());
        ps.setString(4, vacioANulo(a.getDireccion()));
        ps.setString(5, a.getUsuario());
        ps.setString(6, a.getClaveAcceso());
        ps.setString(7, vacioANulo(a.getRutaImagen()));
        ps.setString(8, a.getRol().name());
        ps.setString(9, a.getEstado().name());
    }

    private String vacioANulo(String texto) {
        return (texto == null || texto.trim().isEmpty()) ? null : texto.trim();
    }
}
