package com.mycompany.torneodeportivo.mysql.daos;

import com.mycompany.torneodeportivo.modelo.Equipo;
import com.mycompany.torneodeportivo.modelo.EstadoJugador;
import com.mycompany.torneodeportivo.modelo.Jugador;
import com.mycompany.torneodeportivo.modelo.PosicionCampo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos (CRUD) de la tabla "jugador".
 *
 * @author Christian Hernandez Garcia
 */
public class JugadorDAO {

    private static final String SQL_LISTAR =
            "SELECT id_jugador, nombre, edad, numero_camiseta, posicion_campo, id_equipo, ruta_foto, estado "
            + "FROM jugador ORDER BY id_jugador";
    private static final String SQL_INSERTAR =
            "INSERT INTO jugador (nombre, edad, numero_camiseta, posicion_campo, id_equipo, ruta_foto, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE jugador SET nombre=?, edad=?, numero_camiseta=?, posicion_campo=?, id_equipo=?, ruta_foto=?, estado=? "
            + "WHERE id_jugador=?";
    private static final String SQL_ELIMINAR = "DELETE FROM jugador WHERE id_jugador=?";

    /** @param equipos equipos ya cargados, por id */
    public List<Jugador> listar(Map<Integer, Equipo> equipos) {
        List<Jugador> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Jugador j = new Jugador(rs.getInt("id_jugador"), rs.getString("nombre"), rs.getInt("edad"),
                        rs.getInt("numero_camiseta"), PosicionCampo.valueOf(rs.getString("posicion_campo")),
                        equipos.get(rs.getInt("id_equipo")));
                j.setRutaFoto(rs.getString("ruta_foto"));
                j.setEstado(EstadoJugador.valueOf(rs.getString("estado")));
                lista.add(j);
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
        return lista;
    }

    public int insertar(Jugador j) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            llenar(ps, j);
            ps.executeUpdate();
            return Ids.generado(ps);
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    public void actualizar(Jugador j) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            llenar(ps, j);
            ps.setInt(8, j.getId());
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

    private void llenar(PreparedStatement ps, Jugador j) throws SQLException {
        ps.setString(1, j.getNombre());
        ps.setInt(2, j.getEdad());
        ps.setInt(3, j.getNumeroCamiseta());
        ps.setString(4, j.getPosicionCampo().name());
        ps.setInt(5, j.getEquipo().getId());
        ps.setString(6, Ids.vacioANulo(j.getRutaFoto()));
        ps.setString(7, j.getEstado().name());
    }
}
