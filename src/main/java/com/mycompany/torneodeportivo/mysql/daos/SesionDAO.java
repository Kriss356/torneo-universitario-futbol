package com.mycompany.torneodeportivo.mysql.daos;

import com.mycompany.torneodeportivo.modelo.Administrador;
import com.mycompany.torneodeportivo.modelo.EstadoAdministrador;
import com.mycompany.torneodeportivo.util.Cifrado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DAO del inicio y cierre de sesión.
 * <ul>
 *   <li>Valida las credenciales (ID o correo + contraseña) contra la tabla {@code administrador}.</li>
 *   <li>Solo deja entrar a cuentas con estado ACTIVO.</li>
 *   <li>Registra cada inicio y cierre de sesión en la tabla {@code sesion}.</li>
 * </ul>
 *
 * @author Christian Hernandez Garcia
 */
public class SesionDAO {

    /** Busca la cuenta por su ID (si el texto es numérico) o por su correo electrónico. */
    private static final String SQL_BUSCAR =
            "SELECT a.id_administrador, a.nombre, a.edad, a.correo, a.usuario, a.contrasena, a.estado, "
            + "a.seccion_asignada, a.turno_asignado, a.nivel_permiso, r.nombre_rol "
            + "FROM administrador a JOIN rol r ON r.id_rol = a.id_rol "
            + "WHERE a.id_administrador = ? OR LOWER(a.correo) = LOWER(?)";
    private static final String SQL_INICIO =
            "INSERT INTO sesion (id_administrador, fecha_inicio, estado) VALUES (?, NOW(), 'ABIERTA')";
    private static final String SQL_CIERRE =
            "UPDATE sesion SET fecha_cierre = NOW(), estado = 'CERRADA' WHERE id_sesion = ? AND estado = 'ABIERTA'";

    /** Mensaje genérico: no se revela si lo incorrecto fue el ID/correo o la contraseña. */
    public static final String MSG_CREDENCIALES = "ID / correo o contraseña incorrectos.";

    /**
     * Valida las credenciales contra la base de datos.
     *
     * @param idOCorreo  ID numérico del administrador o su correo electrónico
     * @param contrasena contraseña escrita por el usuario (se compara cifrada con SHA-256)
     * @return el administrador autenticado (la subclase según su rol)
     * @throws IllegalArgumentException si las credenciales no coinciden, la cuenta no está activa
     *                                  o no hay conexión con la base de datos
     */
    public Administrador validarCredenciales(String idOCorreo, String contrasena) {
        String texto = idOCorreo.trim();
        int id = texto.matches("\\d{1,9}") ? Integer.parseInt(texto) : -1;
        Administrador encontrado = null;
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, id);
            ps.setString(2, texto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    encontrado = AdministradorSistemaDAO.crearDesdeFila(rs);
                }
            }
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }

        if (encontrado == null || !encontrado.getContrasena().equals(Cifrado.sha256(contrasena))) {
            throw new IllegalArgumentException(MSG_CREDENCIALES);
        }
        if (encontrado.getEstado() != EstadoAdministrador.ACTIVO) {
            throw new IllegalArgumentException("La cuenta de " + encontrado.getNombre() + " está "
                    + encontrado.getEstado() + ".\nSolo las cuentas ACTIVAS pueden iniciar sesión. "
                    + "Comunícate con el Administrador del torneo.");
        }
        return encontrado;
    }

    /**
     * Registra el inicio de sesión en la bitácora.
     *
     * @param idAdministrador administrador que entró
     * @return id de la sesión creada (se usa para registrar el cierre)
     */
    public int registrarInicio(int idAdministrador) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_INICIO, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idAdministrador);
            ps.executeUpdate();
            return Ids.generado(ps);
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }

    /**
     * Registra el cierre de sesión (fecha de cierre y estado CERRADA).
     *
     * @param idSesion sesión que se cierra
     */
    public void registrarCierre(int idSesion) {
        try (Connection con = Conexion.obtener();
                PreparedStatement ps = con.prepareStatement(SQL_CIERRE)) {
            ps.setInt(1, idSesion);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw ErroresBD.traducir(e);
        }
    }
}
