package com.mycompany.torneodeportivo.mysql.daos;

import com.mycompany.torneodeportivo.modelo.Categoria;
import com.mycompany.torneodeportivo.modelo.Equipo;
import com.mycompany.torneodeportivo.modelo.EstadoEquipo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos (CRUD) de la tabla "equipo".
 * Al insertar un equipo también se crea su fila en la tabla "posicion" (relación 1 a 1),
 * ambas dentro de una transacción.
 *
 * @author Christian Hernandez Garcia
 */
public class EquipoDAO {

    private static final String SQL_LISTAR =
            "SELECT id_equipo, nombre_equipo, universidad, entrenador, id_categoria, ruta_logo, estado "
            + "FROM equipo ORDER BY id_equipo";
    private static final String SQL_INSERTAR =
            "INSERT INTO equipo (nombre_equipo, universidad, entrenador, id_categoria, ruta_logo, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_INSERTAR_POSICION = "INSERT INTO posicion (id_equipo) VALUES (?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE equipo SET nombre_equipo=?, universidad=?, entrenador=?, id_categoria=?, ruta_logo=?, estado=? "
            + "WHERE id_equipo=?";
    private static final String SQL_ELIMINAR = "DELETE FROM equipo WHERE id_equipo=?";

    /** @param categorias categorías ya cargadas, por id */
    public List<Equipo> listar(Map<Integer, Categoria> categorias) {
        List<Equipo> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Equipo e = new Equipo(rs.getInt("id_equipo"), rs.getString("nombre_equipo"),
                        rs.getString("universidad"), categorias.get(rs.getInt("id_categoria")));
                e.setEntrenador(rs.getString("entrenador"));
                e.setRutaLogo(rs.getString("ruta_logo"));
                e.setEstado(EstadoEquipo.valueOf(rs.getString("estado")));
                lista.add(e);
            }
        } catch (SQLException ex) {
            throw ErroresBD.traducir(ex);
        }
        return lista;
    }

    /**
     * Inserta el equipo y su fila de posiciones.
     *
     * @return arreglo {idEquipo, idPosicion}
     */
    public int[] insertar(Equipo e) {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement psPos = con.prepareStatement(SQL_INSERTAR_POSICION, Statement.RETURN_GENERATED_KEYS)) {
                llenar(ps, e);
                ps.executeUpdate();
                int idEquipo = Ids.generado(ps);
                psPos.setInt(1, idEquipo);
                psPos.executeUpdate();
                int idPosicion = Ids.generado(psPos);
                con.commit();
                return new int[]{idEquipo, idPosicion};
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw ErroresBD.traducir(ex);
        }
    }

    public void actualizar(Equipo e) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            llenar(ps, e);
            ps.setInt(7, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw ErroresBD.traducir(ex);
        }
    }

    /** Elimina el equipo; su fila de posiciones se borra sola (ON DELETE CASCADE). */
    public void eliminar(int id) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw ErroresBD.traducir(ex);
        }
    }

    private void llenar(PreparedStatement ps, Equipo e) throws SQLException {
        ps.setString(1, e.getNombreEquipo());
        ps.setString(2, e.getUniversidad());
        ps.setString(3, Ids.vacioANulo(e.getEntrenador()));
        ps.setInt(4, e.getCategoria().getId());
        ps.setString(5, Ids.vacioANulo(e.getRutaLogo()));
        ps.setString(6, e.getEstado().name());
    }
}
