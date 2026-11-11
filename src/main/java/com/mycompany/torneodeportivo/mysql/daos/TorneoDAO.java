package com.mycompany.torneodeportivo.mysql.daos;

import com.mycompany.torneodeportivo.modelo.EstadoTorneo;
import com.mycompany.torneodeportivo.modelo.TipoTorneo;
import com.mycompany.torneodeportivo.modelo.Torneo;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla "torneo" (Apertura / Clausura con su periodo).
 *
 * @author Christian Hernandez Garcia
 */
public class TorneoDAO {

    private static final String SQL_LISTAR =
            "SELECT id_torneo, nombre_torneo, tipo, fecha_inicio, fecha_fin, estado FROM torneo ORDER BY fecha_inicio, id_torneo";
    private static final String SQL_INSERTAR =
            "INSERT INTO torneo (nombre_torneo, tipo, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?)";

    public List<Torneo> listar() {
        List<Torneo> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Torneo t = new Torneo(rs.getInt("id_torneo"), rs.getString("nombre_torneo"),
                        TipoTorneo.valueOf(rs.getString("tipo")),
                        rs.getDate("fecha_inicio").toLocalDate(), rs.getDate("fecha_fin").toLocalDate());
                t.setEstado(EstadoTorneo.valueOf(rs.getString("estado")));
                lista.add(t);
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
        return lista;
    }

    public int insertar(Torneo t) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getNombreTorneo());
            ps.setString(2, t.getTipo().name());
            ps.setDate(3, Date.valueOf(t.getFechaInicio()));
            ps.setDate(4, Date.valueOf(t.getFechaFin()));
            ps.setString(5, t.getEstado().name());
            ps.executeUpdate();
            return Ids.generado(ps);
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }
}
