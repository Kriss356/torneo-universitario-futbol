package com.mycompany.torneodeportivo.mysql.daos;

import com.mycompany.torneodeportivo.modelo.Administrador;
import com.mycompany.torneodeportivo.modelo.AdministradorEquipos;
import com.mycompany.torneodeportivo.modelo.AdministradorPartidos;
import com.mycompany.torneodeportivo.modelo.AdministradorTorneo;
import com.mycompany.torneodeportivo.modelo.EstadoAdministrador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla "administrador" (con su rol en la tabla "rol").
 * Son las cuentas con las que se inicia sesión. La contraseña se guarda cifrada con SHA-256.
 * Según el rol se crea la subclase correcta (herencia): AdministradorEquipos, AdministradorPartidos
 * o AdministradorTorneo.
 *
 * @author Christian Hernandez Garcia
 */
public class AdministradorSistemaDAO {

    public static final String ROL_EQUIPOS = "Administrador de equipos";
    public static final String ROL_PARTIDOS = "Administrador de partidos";
    public static final String ROL_TORNEO = "Administrador del torneo";

    private static final String SQL_LISTAR =
            "SELECT a.id_administrador, a.nombre, a.edad, a.correo, a.usuario, a.contrasena, a.estado, "
            + "a.seccion_asignada, a.turno_asignado, a.nivel_permiso, r.nombre_rol "
            + "FROM administrador a JOIN rol r ON r.id_rol = a.id_rol ORDER BY a.id_administrador";
    private static final String SQL_INSERTAR =
            "INSERT INTO administrador (nombre, edad, correo, usuario, contrasena, estado, "
            + "seccion_asignada, turno_asignado, nivel_permiso, id_rol) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT id_rol FROM rol WHERE nombre_rol = ?))";
    private static final String SQL_ACTUALIZAR =
            "UPDATE administrador SET nombre=?, edad=?, correo=?, usuario=?, contrasena=?, estado=?, "
            + "seccion_asignada=?, turno_asignado=?, nivel_permiso=?, "
            + "id_rol=(SELECT id_rol FROM rol WHERE nombre_rol = ?) WHERE id_administrador=?";
    private static final String SQL_ELIMINAR = "DELETE FROM administrador WHERE id_administrador=?";

    public List<Administrador> listar() {
        List<Administrador> lista = new ArrayList<>();
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(crearDesdeFila(rs));
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
        return lista;
    }

    public int insertar(Administrador a) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            llenar(ps, a);
            ps.executeUpdate();
            return Ids.generado(ps);
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    public void actualizar(Administrador a) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            llenar(ps, a);
            ps.setInt(11, a.getId());
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

    private void llenar(PreparedStatement ps, Administrador a) throws SQLException {
        ps.setString(1, a.getNombre());
        ps.setInt(2, a.getEdad());
        ps.setString(3, a.getCorreo());
        ps.setString(4, a.getUsuario());
        ps.setString(5, a.getContrasena()); // ya viene cifrada (SHA-256)
        ps.setString(6, a.getEstado().name());
        // Cada rol llena solo su campo propio; los otros dos quedan en NULL
        ps.setString(7, a instanceof AdministradorEquipos ? ((AdministradorEquipos) a).getSeccionAsignada() : null);
        ps.setString(8, a instanceof AdministradorPartidos ? ((AdministradorPartidos) a).getTurnoAsignado() : null);
        ps.setString(9, a instanceof AdministradorTorneo ? ((AdministradorTorneo) a).getNivelPermiso() : null);
        ps.setString(10, a.getRol());
    }

    /**
     * Convierte la fila actual de una consulta (administrador + nombre_rol) en la subclase correcta.
     * También la usa SesionDAO al iniciar sesión.
     *
     * @param rs resultado posicionado en una fila
     * @return AdministradorEquipos, AdministradorPartidos o AdministradorTorneo según el rol
     */
    static Administrador crearDesdeFila(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_administrador");
        String nombre = rs.getString("nombre");
        int edad = rs.getInt("edad");
        String correo = rs.getString("correo");
        String usuario = rs.getString("usuario");
        String clave = rs.getString("contrasena");
        String rol = rs.getString("nombre_rol");

        Administrador a;
        if (ROL_EQUIPOS.equalsIgnoreCase(rol)) {
            a = new AdministradorEquipos(id, nombre, edad, correo, usuario, clave,
                    valor(rs.getString("seccion_asignada"), "Equipos y jugadores"));
        } else if (ROL_PARTIDOS.equalsIgnoreCase(rol)) {
            a = new AdministradorPartidos(id, nombre, edad, correo, usuario, clave,
                    valor(rs.getString("turno_asignado"), "Matutino"));
        } else {
            a = new AdministradorTorneo(id, nombre, edad, correo, usuario, clave,
                    valor(rs.getString("nivel_permiso"), "Total"));
        }
        a.setEstado(EstadoAdministrador.valueOf(rs.getString("estado")));
        return a;
    }

    private static String valor(String texto, String porDefecto) {
        return (texto == null || texto.trim().isEmpty()) ? porDefecto : texto;
    }
}
