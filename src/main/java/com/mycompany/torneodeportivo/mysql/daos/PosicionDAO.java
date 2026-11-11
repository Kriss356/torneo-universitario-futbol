package com.mycompany.torneodeportivo.mysql.daos;

import com.mycompany.torneodeportivo.modelo.Equipo;
import com.mycompany.torneodeportivo.modelo.EstadoPosicion;
import com.mycompany.torneodeportivo.modelo.Posicion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de la tabla "posicion" (estadísticas de cada equipo).
 * Los puntos y la diferencia de goles no se guardan: se calculan (igual que en la vista v_tabla_posiciones).
 *
 * @author Christian Hernandez Garcia
 */
public class PosicionDAO {

    private static final String SQL_LISTAR =
            "SELECT id_posicion, id_equipo, partidos_jugados, partidos_ganados, partidos_empatados, partidos_perdidos, "
            + "goles_a_favor, goles_en_contra, estado FROM posicion ORDER BY id_posicion";
    private static final String SQL_CREAR_FALTANTES =
            "INSERT INTO posicion (id_equipo) SELECT e.id_equipo FROM equipo e "
            + "WHERE NOT EXISTS (SELECT 1 FROM posicion p WHERE p.id_equipo = e.id_equipo)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE posicion SET partidos_jugados=?, partidos_ganados=?, partidos_empatados=?, partidos_perdidos=?, "
            + "goles_a_favor=?, goles_en_contra=? WHERE id_posicion=?";

    /** Crea la fila de posiciones de los equipos que no la tengan (por ejemplo, si se insertaron a mano). */
    public void crearFaltantes() {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_CREAR_FALTANTES)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    /** @param equipos equipos ya cargados, por id */
    public List<Posicion> listar(Map<Integer, Equipo> equipos) {
        List<Posicion> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Equipo e = equipos.get(rs.getInt("id_equipo"));
                if (e == null) continue;
                Posicion p = new Posicion(rs.getInt("id_posicion"), e);
                p.setPartidosJugados(rs.getInt("partidos_jugados"));
                p.setPartidosGanados(rs.getInt("partidos_ganados"));
                p.setPartidosEmpatados(rs.getInt("partidos_empatados"));
                p.setPartidosPerdidos(rs.getInt("partidos_perdidos"));
                p.setGolesAFavor(rs.getInt("goles_a_favor"));
                p.setGolesEnContra(rs.getInt("goles_en_contra"));
                p.setEstado(EstadoPosicion.valueOf(rs.getString("estado")));
                lista.add(p);
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
        return lista;
    }

    /** Guarda las estadísticas de todos los equipos en una sola transacción. */
    public void guardarTodas(Collection<Posicion> posiciones) {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
                for (Posicion p : posiciones) {
                    ps.setInt(1, p.getPartidosJugados());
                    ps.setInt(2, p.getPartidosGanados());
                    ps.setInt(3, p.getPartidosEmpatados());
                    ps.setInt(4, p.getPartidosPerdidos());
                    ps.setInt(5, p.getGolesAFavor());
                    ps.setInt(6, p.getGolesEnContra());
                    ps.setInt(7, p.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }
}
