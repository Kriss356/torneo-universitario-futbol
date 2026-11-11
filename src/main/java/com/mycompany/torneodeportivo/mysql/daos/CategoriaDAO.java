package com.mycompany.torneodeportivo.mysql.daos;

import com.mycompany.torneodeportivo.modelo.Categoria;
import com.mycompany.torneodeportivo.modelo.EstadoCategoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos (CRUD) de la tabla "categoria".
 *
 * @author Christian Hernandez Garcia
 */
public class CategoriaDAO {

    private static final String SQL_LISTAR =
            "SELECT id_categoria, nombre_categoria, edad_minima, edad_maxima, estado FROM categoria ORDER BY id_categoria";
    private static final String SQL_INSERTAR =
            "INSERT INTO categoria (nombre_categoria, edad_minima, edad_maxima, estado) VALUES (?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE categoria SET nombre_categoria=?, edad_minima=?, edad_maxima=?, estado=? WHERE id_categoria=?";
    private static final String SQL_ELIMINAR = "DELETE FROM categoria WHERE id_categoria=?";

    public List<Categoria> listar() {
        List<Categoria> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Categoria c = new Categoria(rs.getInt("id_categoria"), rs.getString("nombre_categoria"),
                        rs.getInt("edad_minima"), rs.getInt("edad_maxima"));
                c.setEstado(EstadoCategoria.valueOf(rs.getString("estado")));
                lista.add(c);
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
        return lista;
    }

    /** Inserta la categoría y devuelve el id generado por la base. */
    public int insertar(Categoria c) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            llenar(ps, c);
            ps.executeUpdate();
            return Ids.generado(ps);
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    public void actualizar(Categoria c) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            llenar(ps, c);
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    public void eliminar(int id) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    private void llenar(PreparedStatement ps, Categoria c) throws SQLException {
        ps.setString(1, c.getNombreCategoria());
        ps.setInt(2, c.getEdadMinima());
        ps.setInt(3, c.getEdadMaxima());
        ps.setString(4, c.getEstado().name());
    }
}
