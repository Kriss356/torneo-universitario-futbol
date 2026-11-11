package com.mycompany.torneodeportivo.mysql.daos;

import com.mycompany.torneodeportivo.modelo.Equipo;
import com.mycompany.torneodeportivo.modelo.EstadoPartido;
import com.mycompany.torneodeportivo.modelo.EstadoResultado;
import com.mycompany.torneodeportivo.modelo.Partido;
import com.mycompany.torneodeportivo.modelo.Resultado;
import com.mycompany.torneodeportivo.modelo.Torneo;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de las tablas "partido" (con su torneo y jornada) y "resultado" (relación 1 a 1).
 *
 * @author Christian Hernandez Garcia
 */
public class PartidoDAO {

    private static final String SQL_LISTAR =
            "SELECT p.id_partido, p.id_torneo, p.jornada, p.id_equipo_local, p.id_equipo_visitante, p.fecha, p.hora, p.lugar, p.estado, "
            + "r.id_resultado, r.goles_local, r.goles_visitante, r.estado AS estado_resultado "
            + "FROM partido p LEFT JOIN resultado r ON r.id_partido = p.id_partido ORDER BY p.id_partido";
    private static final String SQL_INSERTAR =
            "INSERT INTO partido (id_torneo, jornada, id_equipo_local, id_equipo_visitante, fecha, hora, lugar, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE partido SET id_torneo=?, jornada=?, id_equipo_local=?, id_equipo_visitante=?, fecha=?, hora=?, lugar=?, estado=? "
            + "WHERE id_partido=?";
    private static final String SQL_ELIMINAR = "DELETE FROM partido WHERE id_partido=?";

    /** Si el partido ya tenía resultado, se reemplaza (UNIQUE en id_partido). */
    private static final String SQL_GUARDAR_RESULTADO =
            "INSERT INTO resultado (id_partido, goles_local, goles_visitante, id_equipo_ganador, estado) VALUES (?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE goles_local=VALUES(goles_local), goles_visitante=VALUES(goles_visitante), "
            + "id_equipo_ganador=VALUES(id_equipo_ganador), estado=VALUES(estado)";
    private static final String SQL_ID_RESULTADO = "SELECT id_resultado FROM resultado WHERE id_partido=?";
    private static final String SQL_FINALIZAR = "UPDATE partido SET estado='FINALIZADO' WHERE id_partido=?";

    /**
     * @param torneos torneos ya cargados, por id
     * @param equipos equipos ya cargados, por id
     */
    public List<Partido> listar(Map<Integer, Torneo> torneos, Map<Integer, Equipo> equipos) {
        List<Partido> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Equipo local = equipos.get(rs.getInt("id_equipo_local"));
                Equipo visitante = equipos.get(rs.getInt("id_equipo_visitante"));
                Partido p = new Partido(rs.getInt("id_partido"), local, visitante,
                        rs.getDate("fecha").toLocalDate(), rs.getTime("hora").toLocalTime());
                p.setTorneo(torneos.get(rs.getInt("id_torneo")));
                p.setJornada(rs.getInt("jornada"));
                p.setLugar(rs.getString("lugar"));
                p.setEstado(EstadoPartido.valueOf(rs.getString("estado")));

                int idResultado = rs.getInt("id_resultado");
                if (!rs.wasNull()) {
                    Resultado r = new Resultado(idResultado, rs.getInt("goles_local"), rs.getInt("goles_visitante"));
                    r.determinarGanador(local, visitante);
                    r.setEstado(EstadoResultado.valueOf(rs.getString("estado_resultado")));
                    p.setResultado(r);
                }
                lista.add(p);
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
        return lista;
    }

    public int insertar(Partido p) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            llenar(ps, p);
            ps.executeUpdate();
            return Ids.generado(ps);
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    public void actualizar(Partido p) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            llenar(ps, p);
            ps.setInt(9, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    /** Elimina el partido; su resultado se borra solo (ON DELETE CASCADE). */
    public void eliminar(int id) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    /**
     * Guarda (o reemplaza) el resultado del partido y lo marca como FINALIZADO, en una sola transacción.
     *
     * @return id del resultado en la base
     */
    public int guardarResultado(int idPartido, Resultado r) {
        try (Connection con = Conexion.obtener()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(SQL_GUARDAR_RESULTADO);
                    PreparedStatement psId = con.prepareStatement(SQL_ID_RESULTADO);
                    PreparedStatement psFin = con.prepareStatement(SQL_FINALIZAR)) {
                ps.setInt(1, idPartido);
                ps.setInt(2, r.getGolesLocal());
                ps.setInt(3, r.getGolesVisitante());
                if (r.getEquipoGanador() == null) {
                    ps.setNull(4, Types.INTEGER); // empate
                } else {
                    ps.setInt(4, r.getEquipoGanador().getId());
                }
                ps.setString(5, r.getEstado().name());
                ps.executeUpdate();

                psFin.setInt(1, idPartido);
                psFin.executeUpdate();

                psId.setInt(1, idPartido);
                int id;
                try (ResultSet rs = psId.executeQuery()) {
                    rs.next();
                    id = rs.getInt(1);
                }
                con.commit();
                return id;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    private void llenar(PreparedStatement ps, Partido p) throws SQLException {
        ps.setInt(1, p.getTorneo().getId());
        ps.setInt(2, p.getJornada());
        ps.setInt(3, p.getEquipoLocal().getId());
        ps.setInt(4, p.getEquipoVisitante().getId());
        ps.setDate(5, Date.valueOf(p.getFecha()));
        ps.setTime(6, Time.valueOf(p.getHora()));
        ps.setString(7, p.getLugar() == null || p.getLugar().trim().isEmpty() ? "Por definir" : p.getLugar());
        ps.setString(8, p.getEstado().name());
    }
}
